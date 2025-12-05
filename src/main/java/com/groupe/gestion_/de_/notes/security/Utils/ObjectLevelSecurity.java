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
import com.groupe.gestion_.de_.notes.model.Grade;
import com.groupe.gestion_.de_.notes.model.Student;
import com.groupe.gestion_.de_.notes.model.TeacherClass;
import com.groupe.gestion_.de_.notes.model.User;
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
public class
ObjectLevelSecurity {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository; // Still needed for subject existence
    private final TeacherClassRepository teacherClassRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Helper to get the authenticated user's ID.
     * Assumes your UserDetails implementation contains the ID or can be retrieved from username.
     */
    public Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Ensure user is authenticated and not anonymous
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null; // No valid authenticated user
        }

        String currentUsername = authentication.getName(); // This gives you the username

        // Use the UserRepository to find the User entity by username
        // UserRepository should have: Optional<User> findByUsername(String username);
        Optional<User> currentUserOptional = userRepository.findByUsername(currentUsername);

        // If a User is found, return their ID; otherwise, return null.
        return currentUserOptional.map(User::getId).orElse(null);
    }

    /**
     * Checks if the authenticated user (a student) is the owner of the given student ID.
     * Applicable for student accessing their own data.
     *
     * @param studentIdNum The ID of the student to check.
     * @return true if the current authenticated user is the student with the given ID.
     */
    public boolean isStudentOwner(String studentIdNum) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) {
            return false; // No authenticated user or ID not found
        }

        // 1. Find the student by their studentIdNum
        Optional<Student> studentOptional = studentRepository.findByStudentIdNum(studentIdNum);

        // 2. Check if the student exists AND if their associated user ID matches the authenticated user's ID
        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();
            //the actual way to get the user ID associated with the student,
            //because Student extends User, it might just be `student.getId()`:(the User ID).
            return Objects.equals(authenticatedUserId, student.getId()); // Assuming Student.getId() returns the base User ID
        }

        return false; // Student not found
    }

    /**
     * Checks if the authenticated user (a student) is the owner of a specific grade.
     *
     * @param gradeId The ID of the grade to check.
     * @return true if the current authenticated user owns the grade.
     */
    public boolean isStudentOwnerOfGrade(Long gradeId) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        if (authenticatedUserId == null) return false;

        Optional<Grade> grade = gradeRepository.findById(gradeId);
        return grade.map(g -> Objects.equals(g.getStudent().getId(), authenticatedUserId)).orElse(false);
    }

    /**
     * Checks if the authenticated user (a teacher) is assigned to a specific subject.
     * A teacher is assigned to a subject if they teach any class that offers this subject.
     *
     * @param subjectCode The ID of the subject to check.
     * @return true if the current authenticated teacher is assigned to this subject.
     */
    public boolean isTeacherAssignedToSubject(String subjectCode) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId(); // This is the teacher's ID
        if (authenticatedUserId == null) return false;

        // 1. Get all classes the teacher is assigned to
        List<TeacherClass> teacherClasses = teacherClassRepository.findByTeacher_TeacherIdNum(String.valueOf(authenticatedUserId));
        if (teacherClasses.isEmpty()) {
            return false;
        }

        // 2. Get all subjects offered in those classes
        Set<Long> subjectsTaughtByTeacher = new HashSet<>();
        for (TeacherClass tc : teacherClasses) {
            List<ClassSubject> classSubjects = classSubjectRepository.findByClassEntity_Id(tc.getClassEntity().getId());
            subjectsTaughtByTeacher.addAll(classSubjects.stream()
                    .map(cs -> cs.getSubject().getId())
                    .collect(Collectors.toSet()));
        }

        // 3. Check if the target subjectId is among the subjects the teacher teaches
        return subjectsTaughtByTeacher.contains(subjectCode);
    }

    /**
     * Checks if the authenticated user (a teacher) is assigned to a specific student.
     * A teacher is assigned to a student if they teach any class that the student is enrolled in.
     *
     * @param studentIdNum The ID of the student to check.
     * @return true if the current authenticated teacher is assigned to this student.
     */
    public boolean isTeacherAssignedToStudent(String studentIdNum) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId(); // This is the teacher's ID
        if (authenticatedUserId == null) return false;

        // 1. Get all classes the teacher is assigned to
        Set<Long> teacherClassIds = teacherClassRepository.findByTeacher_TeacherIdNum(String.valueOf(authenticatedUserId)).stream()
                .map(tc -> tc.getClassEntity().getId())
                .collect(Collectors.toSet());

        if (teacherClassIds.isEmpty()) {
            return false;
        }

        // 2. Get all classes the student is enrolled in
        Set<Long> studentClassIds = enrollmentRepository.findByStudentStudentIdNum(studentIdNum).stream()
                .map(e -> e.getClassEntity().getId())
                .collect(Collectors.toSet());

        if (studentClassIds.isEmpty()) {
            return false;
        }

        // 3. Check for any common classes between teacher's assignments and student's enrollments
        // If there's an overlap, the teacher is effectively assigned to the student.
        return teacherClassIds.stream().anyMatch(studentClassIds::contains);
    }

    /**
     * Checks if the authenticated user (a teacher) is assigned to a specific grade.
     * A teacher is assigned to a grade if:
     * 1. They recorded the grade, OR
     * 2. They teach the subject associated with that grade, OR
     * 3. They are assigned to the student associated with that grade.
     *
     * @param gradeId The ID of the grade to check.
     * @return true if the current authenticated teacher is assigned to this grade.
     */
    public boolean isTeacherAssignedToGrade(Long gradeId) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId(); // This is the teacher's ID
        if (authenticatedUserId == null) return false;

        Optional<Grade> gradeOptional = gradeRepository.findById(gradeId);
        if (gradeOptional.isEmpty()) {
            return false; // Grade not found
        }
        Grade grade = gradeOptional.get();

        // Check 1: Did the teacher record this grade?
        if (grade.getRecordedByTeacher() != null && Objects.equals(grade.getRecordedByTeacher().getId(), authenticatedUserId)) {
            return true;
        }

        // Check 2: Does the teacher teach the subject of this grade?
        if (isTeacherAssignedToSubject(String.valueOf(grade.getSubject().getId()))) {
            return true;
        }

        // Check 3: Is the teacher assigned to the student of this grade?
        if (isTeacherAssignedToStudent(String.valueOf(grade.getStudent().getId()))) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the authenticated user (a teacher) is assigned to a specific student AND subject.
     *
     * @param studentIdNum The ID of the student.
     * @param subjectCode The ID of the subject.
     * @return true if the current authenticated teacher is assigned to both the student and the subject.
     */
    public boolean isTeacherAssignedToStudentAndSubject(String studentIdNum, String subjectCode) {
        // This is a combination of the two previous checks
        return isTeacherAssignedToStudent(studentIdNum) && isTeacherAssignedToSubject(subjectCode);
    }

}
