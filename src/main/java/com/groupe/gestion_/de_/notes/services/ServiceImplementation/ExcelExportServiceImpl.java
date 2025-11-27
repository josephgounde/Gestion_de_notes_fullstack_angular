package com.groupe.gestion_.de_.notes.services.ServiceImplementation;

import com.groupe.gestion_.de_.notes.dto.GradeResponse;
import com.groupe.gestion_.de_.notes.services.ServiceInterface.ExcelExportService;
import com.groupe.gestion_.de_.notes.services.ServiceInterface.GradesService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private final GradesService gradesService;

    @Override
    public byte[] exportGradesToExcel() throws IOException {
        // 1. Fetch all grades from the Grade Service
        List<GradeResponse> grades = gradesService.getAllGrades();

        // 2. Create the Excel Workbook
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CreationHelper createHelper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("Grades Report");

            // 3. Create a header row with styling
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "StudentIdNum", "Student Name", "SubjectCode", "Subject Name", "Grade Value", "Recorded By Teacher ID"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // 4. Populate the data rows
            int rowIdx = 1;
            for (GradeResponse grade : grades) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(grade.getId());
                row.createCell(1).setCellValue(grade.getStudentIdNum());
                row.createCell(2).setCellValue(grade.getFirstname() + " " + grade.getLastname());
                row.createCell(3).setCellValue(grade.getSubjectCode());
                row.createCell(4).setCellValue(grade.getSubjectName());
                row.createCell(5).setCellValue(grade.getValue());
                row.createCell(6).setCellValue(grade.getRecordedBy());
            }

            // 5. Auto-size columns for readability
            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
            }

            // 6. Write the workbook to an output stream
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
