package com.groupe.gestion_.de_.notes.security.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.groupe.gestion_.de_.notes.model.ClassSubject;
import com.groupe.gestion_.de_.notes.model.Enrollment;
import com.groupe.gestion_.de_.notes.model.Grade;
import com.groupe.gestion_.de_.notes.model.Student;
import com.groupe.gestion_.de_.notes.model.Teacher;
import com.groupe.gestion_.de_.notes.model.TeacherClass;
import com.groupe.gestion_.de_.notes.model.User;
import com.groupe.gestion_.de_.notes.repository.ClassRepository;
import com.groupe.gestion_.de_.notes.repository.ClassSubjectRepository;
import com.groupe.gestion_.de_.notes.repository.EnrollmentRepository;
import com.groupe.gestion_.de_.notes.repository.GradeRepository;
import com.groupe.gestion_.de_.notes.repository.StudentRepository;
import com.groupe.gestion_.de_.notes.repository.SubjectRepository;
import com.groupe.gestion_.de_.notes.repository.TeacherClassRepository;
import com.groupe.gestion_.de_.notes.repository.TeacherRepository;
import com.groupe.gestion_.de_.notes.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service("objectLevelSecurity")
@RequiredArgsConstructor
public class ObjectLevelSecurity {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;

    /**
     * Helper to get the authenticated user's ID.
     */
    public Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        String currentUsername = authentication.getName();
        Optional<User> currentUserOptional = userRepository.findByUsername(currentUsername);
        return currentUserOptional.map(User::getId).orElse(null);
    }

    /**
     * Helper to get the authenticated user's username
     */
    public String getCurrentAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    // ==================== STUDENT OWNERSHIP METHODS ====================

    /**
     * Checks if the authenticated user is the owner of the given studentIdNum
     */
    public boolean isStudentOwner(String studentIdNum) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) {
            return false;
        }

        Optional<Student> studentOptional = studentRepository.findByStudentIdNum(studentIdNum);
        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();
            return Objects.equals(authenticatedUserId, student.getId());
        }

        return false;
    }

    /**
     * Checks if the authenticated user is the owner of a specific grade
     */
    public boolean isStudentOwnerOfGrade(Long gradeId) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) return false;

        Optional<Grade> grade = gradeRepository.findById(gradeId);
        return grade.map(g -> Objects.equals(g.getStudent().getId(), authenticatedUserId)).orElse(false);
    }

    /**
     * Checks if the authenticated student is enrolled in a specific class
     */
    public boolean isStudentEnrolledInClass(Long classId) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) return false;

        Optional<Student> studentOptional = studentRepository.findById(authenticatedUserId);
        if (studentOptional.isEmpty()) return false;

        String studentIdNum = studentOptional.get().getStudentIdNum();
        List<Enrollment> enrollments = enrollmentRepository.findByStudentStudentIdNum(studentIdNum);
        
        return enrollments.stream()
                .anyMatch(e -> Objects.equals(e.getClassEntity().getId(), classId));
    }

    /**
     * Checks if the authenticated student owns a specific enrollment
     */
    public boolean isStudentOwnerOfEnrollment(Long enrollmentId) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) return false;

        Optional<Enrollment> enrollmentOptional = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOptional.isEmpty()) return false;

        return Objects.equals(authenticatedUserId, enrollmentOptional.get().getStudent().getId());
    }

    // ==================== TEACHER OWNERSHIP & ASSIGNMENT METHODS ====================

    /**
     * Checks if the authenticated user is the owner of the given teacherIdNum.
     * This method gets the authenticated user by username, looks up their Teacher record,
     * and compares their teacherIdNum with the one provided.
     */
    public boolean isTeacherOwner(String teacherIdNum) {
        String currentUsername = getCurrentAuthenticatedUsername();
        if (currentUsername == null) return false;

        // Get the authenticated user
        Optional<User> userOptional = userRepository.findByUsername(currentUsername);
        if (userOptional.isEmpty()) return false;

        User user = userOptional.get();
        
        // Check if this user is a Teacher
        Optional<Teacher> authenticatedTeacherOptional = teacherRepository.findById(user.getId());
        if (authenticatedTeacherOptional.isEmpty()) return false;

        Teacher authenticatedTeacher = authenticatedTeacherOptional.get();
        
        // Compare the authenticated teacher's teacherIdNum with the provided one
        return Objects.equals(authenticatedTeacher.getTeacherIdNum(), teacherIdNum);
    }

    /**
     * Checks if the authenticated teacher is assigned to a specific class
     */
    public boolean isTeacherAssignedToClass(Long classId) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) return false;

        Optional<Teacher> teacherOptional = teacherRepository.findById(authenticatedUserId);
        if (teacherOptional.isEmpty()) return false;

        String teacherIdNum = teacherOptional.get().getTeacherIdNum();
        List<TeacherClass> teacherClasses = teacherClassRepository.findByTeacher_TeacherIdNum(teacherIdNum);
        
        return teacherClasses.stream()
                .anyMatch(tc -> Objects.equals(tc.getClassEntity().getId(), classId));
    }

    /**
     * Checks if the authenticated teacher is assigned to a specific subject
     */
    public boolean isTeacherAssignedToSubject(String subjectCode) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) return false;

        Optional<Teacher> teacherOptional = teacherRepository.findById(authenticatedUserId);
        if (teacherOptional.isEmpty()) return false;

        String teacherIdNum = teacherOptional.get().getTeacherIdNum();
        
        List<TeacherClass> teacherClasses = teacherClassRepository.findByTeacher_TeacherIdNum(teacherIdNum);
        if (teacherClasses.isEmpty()) {
            return false;
        }

        Set<String> subjectsTaughtByTeacher = new HashSet<>();
        for (TeacherClass tc : teacherClasses) {
            List<ClassSubject> classSubjects = classSubjectRepository.findByClassEntity_Id(tc.getClassEntity().getId());
            subjectsTaughtByTeacher.addAll(classSubjects.stream()
                    .map(cs -> cs.getSubject().getSubjectCode())
                    .collect(Collectors.toSet()));
        }

        return subjectsTaughtByTeacher.contains(subjectCode);
    }

    /**
     * Checks if the authenticated teacher is assigned to a specific student
     */
    public boolean isTeacherAssignedToStudent(String studentIdNum) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) return false;

        Optional<Teacher> teacherOptional = teacherRepository.findById(authenticatedUserId);
        if (teacherOptional.isEmpty()) return false;

        String teacherIdNum = teacherOptional.get().getTeacherIdNum();

        Set<Long> teacherClassIds = teacherClassRepository.findByTeacher_TeacherIdNum(teacherIdNum).stream()
                .map(tc -> tc.getClassEntity().getId())
                .collect(Collectors.toSet());

        if (teacherClassIds.isEmpty()) {
            return false;
        }

        Set<Long> studentClassIds = enrollmentRepository.findByStudentStudentIdNum(studentIdNum).stream()
                .map(e -> e.getClassEntity().getId())
                .collect(Collectors.toSet());

        if (studentClassIds.isEmpty()) {
            return false;
        }

        return teacherClassIds.stream().anyMatch(studentClassIds::contains);
    }

    /**
     * Checks if the authenticated teacher is assigned to a specific grade
     */
    public boolean isTeacherAssignedToGrade(Long gradeId) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) return false;

        Optional<Grade> gradeOptional = gradeRepository.findById(gradeId);
        if (gradeOptional.isEmpty()) {
            return false;
        }
        Grade grade = gradeOptional.get();

        if (grade.getRecordedByTeacher() != null && Objects.equals(grade.getRecordedByTeacher().getId(), authenticatedUserId)) {
            return true;
        }

        if (isTeacherAssignedToSubject(grade.getSubject().getSubjectCode())) {
            return true;
        }

        if (isTeacherAssignedToStudent(grade.getStudent().getStudentIdNum())) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the authenticated teacher is assigned to a specific enrollment
     */
    public boolean isTeacherAssignedToEnrollment(Long enrollmentId) {
        Optional<Enrollment> enrollmentOptional = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOptional.isEmpty()) return false;

        Enrollment enrollment = enrollmentOptional.get();
        
        return isTeacherAssignedToClass(enrollment.getClassEntity().getId()) ||
               isTeacherAssignedToSubject(enrollment.getSubject().getSubjectCode());
    }

    /**
     * Checks if the authenticated teacher is assigned to both a student and subject
     */
    public boolean isTeacherAssignedToStudentAndSubject(String studentIdNum, String subjectCode) {
        return isTeacherAssignedToStudent(studentIdNum) && isTeacherAssignedToSubject(subjectCode);
    }
}