package com.groupe.gestion_.de_.notes.controllers;

import com.groupe.gestion_.de_.notes.services.ServiceInterface.ExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
@Tag(name = "Data Export Management", description = "API for exporting application data to various formats like Excel.")
@CrossOrigin(origins = "http://localhost:4200")
public class ExcelExportController {

    private final ExcelExportService excelExportService;

    @Operation(summary = "Export all grades to an Excel file", description = "Generates and downloads an Excel file containing all grade records.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file generated and downloaded successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only Admins can download this data")
    })
    @GetMapping("/grades/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportGradesToExcel() throws IOException {
        byte[] excelBytes = excelExportService.exportGradesToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String filename = "grades_report.xlsx";
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}
