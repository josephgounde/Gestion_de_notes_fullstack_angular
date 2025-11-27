package com.groupe.gestion_.de_.notes.services.ServiceImplementation;

import com.groupe.gestion_.de_.notes.exceptions.ResourceNotFoundException;
import com.groupe.gestion_.de_.notes.model.Student;
import com.groupe.gestion_.de_.notes.dto.EnrollmentResponse;
import com.groupe.gestion_.de_.notes.dto.GradeResponse;
import com.groupe.gestion_.de_.notes.dto.SubjectResponse;
import com.groupe.gestion_.de_.notes.repository.StudentRepository;
import com.groupe.gestion_.de_.notes.services.ServiceInterface.EnrollmentService;
import com.groupe.gestion_.de_.notes.services.ServiceInterface.GradesService;
import com.groupe.gestion_.de_.notes.services.ServiceInterface.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.pdfbox.pdmodel.font.PDType1Font.*;

@Service
@RequiredArgsConstructor
public class TranscriptServiceImpl implements TranscriptService {

    private final StudentRepository studentRepository;
    private final EnrollmentService enrollmentService;
    private final GradesService gradesService;

    @Override
    public byte[] generateTranscriptForStudent(String studentIdNum) throws IOException {
        // 1. Fetch all necessary data
        Student student = studentRepository.findByStudentIdNum(studentIdNum)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentIdNum));
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByStudentIdNum(studentIdNum);

        // 2. Create the PDF document
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Simplified PDF Generation Logic

                // A. Header
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Academic Transcript");
                contentStream.endText();

                // B. Student Information
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Student Name: " + student.getFirstname() + " " + student.getLastname());
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Student studentIdNum: " + student.getStudentIdNum());
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Date of Issue: " + LocalDate.now());
                contentStream.endText();

                // C. Grades Table
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(50, 650);
                contentStream.showText("Enrollment and Grades");
                contentStream.endText();

                float yPosition = 620;
                contentStream.setFont(PDType1Font.HELVETICA, 10);

                // Table Header
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("Subject Name");
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText("Grades");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("Subject Average");
                contentStream.endText();
                yPosition -= 20;

                // Table Rows
                for (EnrollmentResponse enrollment : enrollments) {
                    SubjectResponse subject = enrollment.getSubject();
                    List<GradeResponse> grades = gradesService.findGradesByStudentIdNumAndSubjectCode(student.getStudentIdNum(), subject.getSubjectCode());
                    Double subjectAverage = gradesService.calculateStudentAverageGradeForSubject(student.getStudentIdNum(), subject.getSubjectCode());

                    // Grades as a comma-separated string
                    String gradesString = grades.stream()
                            .map(g -> String.valueOf(g.getValue()))
                            .collect(Collectors.joining(", "));

                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(subject.getName());
                    contentStream.newLineAtOffset(200, 0);
                    contentStream.showText(gradesString);
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(String.format("%.2f", subjectAverage));
                    contentStream.endText();

                    yPosition -= 20;
                }

                // D. Overall Average
                Double overallAverage = gradesService.calculateStudentOverallAverageGrade(studentIdNum);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, yPosition - 30);
                contentStream.showText("Overall Weighted Average: " + String.format("%.2f", overallAverage));
                contentStream.endText();

                // E. Footer/Signature
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("This is an official document of the institution.");
                contentStream.endText();
            }

            document.save(out);
            return out.toByteArray();
        }
    }
}
