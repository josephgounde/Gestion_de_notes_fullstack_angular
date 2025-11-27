package com.groupe.gestion_.de_.notes.controllers;

import com.groupe.gestion_.de_.notes.dto.GradeRequest;
import com.groupe.gestion_.de_.notes.dto.GradeResponse;
import com.groupe.gestion_.de_.notes.services.ServiceInterface.GradesService;
import com.groupe.gestion_.de_.notes.security.Utils.ObjectLevelSecurity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.service.SecurityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Grade Management", description = "API for managing student grades")
@CrossOrigin(origins = "http://localhost:4200") // Adjust for your Angular app's URL
public class GradeController {

    private final GradesService gradeService;
    private final ObjectLevelSecurity objectLevelSecurity; // Used for object-level security checks

    /**
     * Creates a new grade record.
     * Accessible by TEACHERs and ADMINs.
     * TEACHERs can only record grades for their assigned subjects/classes.
     * ADMINs can record any grade.
     */
    @Operation(summary = "Create a new grade", description = "Allows Teachers and Admins to record new grades for students in subjects.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grade created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or business rule violation (e.g., grade value out of range)"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only Teachers or Admins can create grades"),
            @ApiResponse(responseCode = "404", description = "Student, Subject, or Teacher not found")
    })
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToSubject(#request.subjectCode))")
    public ResponseEntity<GradeResponse> addGrade(@Valid @RequestBody GradeRequest request) {
        GradeResponse response = gradeService.addGrade(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a grade by its ID.
     * ADMINs can view any grade.
     * TEACHERs can view grades for subjects they are assigned to.
     * STUDENTs can only view their own grades.
     */
    @Operation(summary = "Get a grade by ID", description = "Retrieve details of a specific grade.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grade found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges to view this grade"),
            @ApiResponse(responseCode = "404", description = "Grade not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToGrade(#id)) or " +
            "(hasRole('STUDENT') and @objectLevelSecurity.isStudentOwnerOfGrade(#id))")
    public ResponseEntity<GradeResponse> findGradeById(@PathVariable Long id) {
        return gradeService.findGradeById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NoSuchElementException("Grade not found with ID: " + id));
    }

    /**
     * Retrieves all grade records.
     * Accessible only by ADMINs.
     */
    @Operation(summary = "Get all grades", description = "Retrieve a list of all grade records in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of grades retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only Admins can view all grades")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GradeResponse>> getAllGrades() {
        List<GradeResponse> grades = gradeService.getAllGrades();
        return ResponseEntity.ok(grades);
    }

    /**
     * Retrieves all grades for a specific student.
     * ADMINs can view any student's grades.
     * TEACHERs can view grades for students in their assigned subjects/classes.
     * STUDENTs can only view their own grades.
     */
    @Operation(summary = "Get grades by student IdNum", description = "Retrieve all grades for a specific student.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grades retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges to view these grades"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/student/{studentIdNum}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToStudent(#studentIdNum)) or " +
            "(hasRole('STUDENT') and @objectLevelSecurity.isStudentOwner(#studentIdNum))")
    public ResponseEntity<List<GradeResponse>> findGradesByStudentIdNum(@PathVariable String studentIdNum) {
        List<GradeResponse> grades = gradeService.findGradesByStudentIdNum(studentIdNum);
        return ResponseEntity.ok(grades);
    }

    /**
     * Retrieves all grades for a specific subject.
     * Accessible by ADMINs and TEACHERs (if assigned to that subject).
     */
    @Operation(summary = "Get grades by subject Code", description = "Retrieve all grades for a specific subject.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grades retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only Admins or Teachers assigned to the subject can view these grades"),
            @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    @GetMapping("/subject/{subjectCode}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToSubject(#subjectCode))")
    public ResponseEntity<List<GradeResponse>> findGradesBySubjectCode(@PathVariable String subjectCode) {
        List<GradeResponse> grades = gradeService.findGradesBySubjectCode(subjectCode);
        return ResponseEntity.ok(grades);
    }

    /**
     * Retrieves grades for a specific student in a specific subject.
     * Access similar to `getGradesByStudentId`.
     */
    @Operation(summary = "Get grades by student and subject ID", description = "Retrieve all grades for a specific student in a specific subject.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grades retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "Student or Subject not found")
    })
    @GetMapping("/student/{studentIdNum}/subject/{subjectCode}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToStudentAndSubject(#studentIdNum, #subjectCode)) or " +
            "(hasRole('STUDENT') and @objectLevelSecurity.isStudentOwner(#studentIdNum))")
    public ResponseEntity<List<GradeResponse>> findGradesByStudentIdNumAndSubjectCode(@PathVariable String studentIdNum, @PathVariable String subjectCode) {
        List<GradeResponse> grades = gradeService.findGradesByStudentIdNumAndSubjectCode(studentIdNum, subjectCode);
        return ResponseEntity.ok(grades);
    }

    /**
     * Updates an existing grade record.
     * Accessible by TEACHERs (only for grades they recorded or subjects they teach) and ADMINs.
     */
    @Operation(summary = "Update an existing grade", description = "Allows Teachers and Admins to modify existing grade records.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grade updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges to update this grade"),
            @ApiResponse(responseCode = "404", description = "Grade, Student, Subject, or Teacher not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToGrade(#id))")
    public ResponseEntity<GradeResponse> updateGrade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        GradeResponse updatedGrade = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(updatedGrade);
    }

    /**
     * Deletes a grade record.
     * Accessible only by ADMINs.
     */
    @Operation(summary = "Delete a grade by ID", description = "Allows only Admins to delete a grade record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Grade deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only Admins can delete grades"),
            @ApiResponse(responseCode = "404", description = "Grade not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }

    // --- Average Calculation Endpoints ---

    /**
     * Calculates the average grade for a specific student in a specific subject.
     * ADMINs can view any. TEACHERs for their assigned students/subjects. STUDENTs for their own.
     */
    @Operation(summary = "Calculate student's average grade for a subject", description = "Retrieves the average grade of a student in a specific subject.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Average calculated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "Student or Subject not found")
    })
    @GetMapping("/averages/student/{studentIdNum}/subject/{subjectCode}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToStudentAndSubject(#studentIdNum, #subjectCode)) or " +
            "(hasRole('STUDENT') and @objectLevelSecurity.isStudentOwner(#studentIdNum))")
    public ResponseEntity<Double> calculateStudentAverageGradeForSubject(@PathVariable String studentIdNum, @PathVariable String subjectCode) {
        Double average = gradeService.calculateStudentAverageGradeForSubject(studentIdNum, subjectCode);
        return ResponseEntity.ok(average);
    }

    /**
     * Calculates the overall average grade for a specific student across all subjects.
     * ADMINs can view any. TEACHERs for their assigned students. STUDENTs for their own.
     */
    @Operation(summary = "Calculate student's overall average grade", description = "Retrieves the overall weighted average grade of a student across all their subjects.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overall average calculated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/averages/student/overall/{studentIdNum}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToStudent(#studentIdNum)) or " +
            "(hasRole('STUDENT') and @objectLevelSecurity.isStudentOwner(#studentIdNum))")
    public ResponseEntity<Double> calculateStudentOverallAverageGrade(@PathVariable String studentIdNum) {
        Double average = gradeService.calculateStudentOverallAverageGrade(studentIdNum);
        return ResponseEntity.ok(average);
    }

    /**
     * Calculates the average grade for a specific subject across all students.
     * Accessible by ADMINs and TEACHERs (if assigned to that subject).
     */
    @Operation(summary = "Calculate subject's average grade", description = "Retrieves the average grade for a specific subject across all students.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subject average calculated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only Admins or Teachers assigned to the subject can view this"),
            @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    @GetMapping("/averages/subject/{subjectCode}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and @objectLevelSecurity.isTeacherAssignedToSubject(#subjectIdNum))")
    public ResponseEntity<Double> calculateSubjectAverageGrade(@PathVariable String subjectCode) {
        Double average = gradeService.calculateSubjectAverageGrade(subjectCode);
        return ResponseEntity.ok(average);
    }
}
