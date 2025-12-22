package com.groupe.gestion_.de_.notes.services.ServiceImplementation;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.groupe.gestion_.de_.notes.dto.GradeRequest;
import com.groupe.gestion_.de_.notes.dto.GradeResponse;
import com.groupe.gestion_.de_.notes.dto.StudentResponse;
import com.groupe.gestion_.de_.notes.dto.SubjectResponse;
import com.groupe.gestion_.de_.notes.dto.TeacherResponse;
import com.groupe.gestion_.de_.notes.exceptions.BadRequestException;
import com.groupe.gestion_.de_.notes.exceptions.ResourceNotFoundException;
import com.groupe.gestion_.de_.notes.model.Grade;
import com.groupe.gestion_.de_.notes.model.Student;
import com.groupe.gestion_.de_.notes.model.Subject;
import com.groupe.gestion_.de_.notes.model.Teacher;
import com.groupe.gestion_.de_.notes.repository.GradeRepository;
import com.groupe.gestion_.de_.notes.repository.StudentRepository;
import com.groupe.gestion_.de_.notes.repository.SubjectRepository;
import com.groupe.gestion_.de_.notes.repository.TeacherRepository;
import com.groupe.gestion_.de_.notes.services.ServiceInterface.GradesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradesServiceImpl implements GradesService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository; // For recordedByTeacher

    /**
     * Creates a new grade record.
     * Validates if the associated student, subject, and teacher (if provided) exist.
     *
     * @param request The DTO containing grade details.
     * @return The created GradeResponse DTO.
     * @throws ResourceNotFoundException if student, subject, or teacher not found.
     * @throws BadRequestException if the grade value is invalid.
     */
    @Override
    @Transactional
    public GradeResponse addGrade(GradeRequest request) {
        // Validate grade value range (e.g., 0-20)
        if (request.getValue() < 0 || request.getValue() > 20) { // Example range, adjust as needed
            throw new BadRequestException("Grade value must be between 0 and 20.");
        }

        // Fetch and validate associated entities
        Student student = studentRepository.findByStudentIdNum(String.valueOf(request.getStudentIdNum()))
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with IDNum: " + request.getStudentIdNum()));
        Subject subject = subjectRepository.findBySubjectCode(String.valueOf(request.getSubjectCode()))
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + request.getSubjectCode()));

        Teacher recordedByTeacher = null;
        if (request.getRecordedBy() != null) {
            recordedByTeacher = teacherRepository.findByTeacherIdNum(request.getRecordedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + request.getRecordedBy()));
        }

        // Build the Grade entity
        Grade grade = Grade.builder()
                .value(request.getValue())
                .date(request.getDate())
                .comment(request.getComment())
                .student(student)
                .subject(subject)
                .recordedByTeacher(recordedByTeacher)
                .build();

        Grade savedGrade = gradeRepository.save(grade);
        return mapGradeToResponse(savedGrade);
    }


    /**
     * Retrieves a grade by its ID.
     *
     * @param id The ID of the grade.
     * @return An Optional containing the GradeResponse DTO if found, empty otherwise.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<GradeResponse> findGradeById(Long id) {
        return gradeRepository.findById(id)
                .map(this::mapGradeToResponse);
    }

    /**
     * Retrieves all grade records in the system.
     *
     * @return A list of GradeResponse DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getAllGrades() {
        return gradeRepository.findAll().stream()
                .map(this::mapGradeToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GradeResponse> getGradesByTeacher(String teacherIdNum) {
        // Find the teacher by teacherIdNum
        Teacher teacher = teacherRepository.findByTeacherIdNum(teacherIdNum)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found with ID: " + teacherIdNum));
        
        // Get all grades recorded by this teacher
        List<Grade> grades = gradeRepository.findByRecordedByTeacher_TeacherIdNum(teacherIdNum);
        
        // Map to response DTOs
        return grades.stream()
                .map(this::mapGradeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all grades for a specific student.
     *
     * @param studentIdNum The ID of the student.
     * @return A list of GradeResponse DTOs for the student.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> findGradesByStudentIdNum(String studentIdNum) {
        // Optional: Validate student existence before fetching grades
        if (studentRepository.findByStudentIdNum(studentIdNum).isEmpty()) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentIdNum);
        }
        return gradeRepository.findByStudentStudentIdNum(studentIdNum).stream()
                .map(this::mapGradeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all grades for a specific subject.
     *
     * @param subjectCode The ID of the subject.
     * @return A list of GradeResponse DTOs for the subject.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> findGradesBySubjectCode(String subjectCode) {
        // Optional: Validate subject existence
        if (!subjectRepository.findBySubjectCode(subjectCode).isEmpty()) {
            throw new ResourceNotFoundException("Subject not found with ID: " + subjectCode);
        }
        return gradeRepository.findBySubjectSubjectCode(subjectCode).stream()
                .map(this::mapGradeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all grades for a specific student in a specific subject.
     *
     * @param studentIdNum The ID of the student.
     * @param subjectCode The ID of the subject.
     * @return A list of GradeResponse DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> findGradesByStudentIdNumAndSubjectCode(String studentIdNum, String subjectCode) {
        // Validate existence of both student and subject
        if (!studentRepository.findByStudentIdNum(studentIdNum).isEmpty()) {
            throw new ResourceNotFoundException("Student not found with IDNum: " + studentIdNum);
        }
        if (!subjectRepository.findBySubjectCode(subjectCode).isEmpty()) {
            throw new ResourceNotFoundException("Subject not found with ID: " + subjectCode);
        }
        return gradeRepository.findByStudent_StudentIdNumAndSubject_SubjectCode(studentIdNum, subjectCode).stream()
                .map(this::mapGradeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing grade record.
     *
     * @param id The ID of the grade to update.
     * @param request The DTO containing updated grade details.
     * @return The updated GradeResponse DTO.
     * @throws ResourceNotFoundException if the grade, student, subject, or teacher (if provided) is not found.
     * @throws BadRequestException if the grade value is invalid.
     */
    @Override
    @Transactional
    public GradeResponse updateGrade(Long id, GradeRequest request) {
        Grade existingGrade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with ID: " + id));

        // Validate grade value range
        if (request.getValue() < 0 || request.getValue() > 20) {
            throw new BadRequestException("Grade value must be between 0 and 20.");
        }

        // Update basic fields
        existingGrade.setValue(request.getValue());
        existingGrade.setDate(request.getDate());
        existingGrade.setComment(request.getComment());

        // Update relationships if IDs are provided in the request
        if (request.getStudentIdNum() != null) {
            Student student = studentRepository.findByStudentIdNum(request.getStudentIdNum())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with IDNum: " + request.getStudentIdNum()));
            existingGrade.setStudent(student);
        }
        if (request.getSubjectCode() != null) {
            Subject subject = subjectRepository.findBySubjectCode(request.getSubjectCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + request.getSubjectCode()));
            existingGrade.setSubject(subject);
        }
        if (request.getRecordedBy() != null) {
            Teacher teacher = teacherRepository.findByTeacherIdNum(request.getRecordedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + request.getRecordedBy()));
            existingGrade.setRecordedByTeacher(teacher);
        } else if (existingGrade.getRecordedByTeacher() != null) {
            // If recordedByTeacherId is null in request but existed, remove it
            existingGrade.setRecordedByTeacher(null);
        }

        Grade updatedGrade = gradeRepository.save(existingGrade);
        return mapGradeToResponse(updatedGrade);
    }

    /**
     * Deletes a grade record by its ID.
     *
     * @param id The ID of the grade to delete.
     * @throws ResourceNotFoundException if the grade is not found.
     */
    @Override
    @Transactional
    public void deleteGrade(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Grade not found with ID: " + id);
        }
        gradeRepository.deleteById(id);
    }

    /**
     * Calculates the average grade for a specific student in a specific subject.
     *
     * @param studentIdNum The ID of the student.
     * @param subjectCode The ID of the subject.
     * @return The average grade as a Double, or null if no grades are found.
     * @throws ResourceNotFoundException if student or subject not found.
     */
    @Override
    @Transactional(readOnly = true)
    public Double calculateStudentAverageGradeForSubject(String studentIdNum, String subjectCode) {
        List<Grade> grades = getGradesForCalculation(studentIdNum, subjectCode);
        return grades.stream()
                .mapToDouble(Grade::getValue)
                .average()
                .orElse(0.0); // Return 0.0 if no grades found, or OptionalDouble could be used
    }

    /**
     * Calculates the overall average grade for a specific student across all subjects.
     *
     * @param studentIdNum The ID of the student.
     * @return The overall average grade as a Double, or null if no grades are found.
     * @throws ResourceNotFoundException if student not found.
     */
    @Override
    @Transactional(readOnly = true)
    public Double calculateStudentOverallAverageGrade(String studentIdNum) {
        // Validate student existence
        if (studentRepository.findByStudentIdNum(studentIdNum).isEmpty()) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentIdNum);
        }

        List<Grade> grades = gradeRepository.findByStudentStudentIdNum(studentIdNum);
        if (grades.isEmpty()) {
            return 0.0;
        }

        // Calculate weighted average if subjects have coefficients, otherwise simple average
        double totalWeightedSum = 0.0;
        double totalCoefficient = 0.0;
        for (Grade grade : grades) {
            // Assuming Subject is eagerly fetched or retrieved
            // If lazy, you might need grade.getSubject().getCoefficient() which triggers a fetch
            // Or use a custom query in repository to fetch grades with subject coefficients
            Subject subject = subjectRepository.findById(grade.getSubject().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found for grade ID: " + grade.getId()));
            totalWeightedSum += grade.getValue() * subject.getCoefficient();
            totalCoefficient += subject.getCoefficient();
        }

        return totalCoefficient == 0 ? 0.0 : totalWeightedSum / totalCoefficient;
    }


    /**
     * Calculates the average grade for a specific subject across all students who have grades for it.
     *
     * @param subjectCode The ID of the subject.
     * @return The average grade as a Double, or null if no grades are found.
     * @throws ResourceNotFoundException if subject not found.
     */
    @Override
    @Transactional(readOnly = true)
    public Double calculateSubjectAverageGrade(String subjectCode) {
        // Validate subject existence
        if (subjectRepository.findBySubjectCode(subjectCode).isEmpty()) {
            throw new ResourceNotFoundException("Subject not found with ID: " + subjectCode);
        }

        List<Grade> grades = gradeRepository.findBySubjectSubjectCode(subjectCode);
        return grades.stream()
                .mapToDouble(Grade::getValue)
                .average()
                .orElse(0.0); // Return 0.0 if no grades found
    }



    /**
     * Helper method to fetch grades for average calculation.
     * Throws ResourceNotFoundException if student or subject does not exist.
     */
    private List<Grade> getGradesForCalculation(String studentIdNum, String subjectCode) {
        // Validate student and subject existence
        studentRepository.findByStudentIdNum(studentIdNum)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentIdNum));
        subjectRepository.findBySubjectCode(subjectCode)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectCode));

        return gradeRepository.findByStudent_StudentIdNumAndSubject_SubjectCode(studentIdNum,subjectCode);
    }



    // --- Helper Methods ---

    /**
     * Maps a Grade entity to a GradeResponse DTO.
     * This includes mapping related Student, Subject, and Teacher entities to their respective DTOs.
     *
     * @param grade The Grade entity to map.
     * @return The GradeResponse DTO.
     */
    private GradeResponse mapGradeToResponse(Grade grade) {
        StudentResponse studentResponse = null;
        if (grade.getStudent() != null) {
            studentResponse = StudentResponse.builder()
                    .id(grade.getStudent().getId())
                    .username(grade.getStudent().getUsername())
                    .firstname(grade.getStudent().getFirstname())
                    .lastname(grade.getStudent().getLastname())
                    .email(grade.getStudent().getEmail())
                    .role(grade.getStudent().getRole())
                    .studentIdNum(grade.getStudent().getStudentIdNum())
                    .build();
        }

        SubjectResponse subjectResponse = null;
        if (grade.getSubject() != null) {
            subjectResponse = SubjectResponse.builder()
                    .id(grade.getSubject().getId())
                    .subjectCode(grade.getSubject().getSubjectCode())
                    .name(grade.getSubject().getName())
                    .coefficient(grade.getSubject().getCoefficient())
                    .description(grade.getSubject().getDescription())
                    .build();
        }

        TeacherResponse recordedByTeacherResponse = null;
        if (grade.getRecordedByTeacher() != null) {
            recordedByTeacherResponse = TeacherResponse.builder()
                    .id(grade.getRecordedByTeacher().getId())
                    .username(grade.getRecordedByTeacher().getUsername())
                    .firstname(grade.getRecordedByTeacher().getFirstname())
                    .lastname(grade.getRecordedByTeacher().getLastname())
                    .email(grade.getRecordedByTeacher().getEmail())
                    .role(grade.getRecordedByTeacher().getRole())
                    .teacherIdNum(grade.getRecordedByTeacher().getTeacherIdNum())
                    .build();
        }

        return GradeResponse.builder()
                .id(grade.getId())
                .value(grade.getValue())
                .date(grade.getDate())
                .comment(grade.getComment())
                .studentIdNum(studentResponse.getStudentIdNum())
                .subjectCode(subjectResponse.getSubjectCode())
                .recordedBy(recordedByTeacherResponse.getTeacherIdNum())
                .build();
    }
}
