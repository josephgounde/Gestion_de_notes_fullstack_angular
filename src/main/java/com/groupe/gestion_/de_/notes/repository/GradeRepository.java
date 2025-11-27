package com.groupe.gestion_.de_.notes.repository;

import com.groupe.gestion_.de_.notes.model.Grade;
import com.groupe.gestion_.de_.notes.model.Student;
import com.groupe.gestion_.de_.notes.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentStudentIdNum(String studentIdNum);
    List<Grade> findBySubjectSubjectCode(String subjectCode);
    List<Grade> findByStudent_StudentIdNumAndSubject_SubjectCode(String studentIdNum, String subjectCode);
    Optional<Grade> findByStudentAndSubjectAndDate(Student student, Subject subject, LocalDate date);
    List<Grade> findByRecordedByTeacher_TeacherIdNum(String teacherIdNum);
}