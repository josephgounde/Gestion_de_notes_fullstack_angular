import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { GradeRequest, GradeResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class GradesService {
  private apiUrl = `${environment.apiUrl}/grades`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new grade (Teacher and Admin)
   * Teachers can only record grades for subjects they're assigned to
   */
  addGrade(gradeData: GradeRequest): Observable<GradeResponse> {
    return this.http.post<GradeResponse>(`${this.apiUrl}/add`, gradeData);
  }

  /**
   * Get a specific grade by ID
   */
  getGradeById(id: number): Observable<GradeResponse> {
    return this.http.get<GradeResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get all grades (Admin only)
   */
  getAllGrades(): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(this.apiUrl);
  }

  /**
   * Get all grades recorded by a specific teacher
   * @param teacherIdNum - The teacher's ID number (e.g., "TCH001")
   */
  getGradesByTeacher(teacherIdNum: string): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(`${this.apiUrl}/teacher/${teacherIdNum}`);
  }

  /**
   * Get all grades for a specific student
   * @param studentIdNum - The student's ID number (e.g., "STU001")
   */
  getGradesByStudent(studentIdNum: string): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(`${this.apiUrl}/student/${studentIdNum}`);
  }

  /**
   * Get all grades for a specific subject
   * @param subjectCode - The subject code (e.g., "MATH101")
   */
  getGradesBySubject(subjectCode: string): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(`${this.apiUrl}/subject/${subjectCode}`);
  }

  /**
   * Get grades for a specific student in a specific subject
   * @param studentIdNum - The student's ID number
   * @param subjectCode - The subject code
   */
  getGradesByStudentAndSubject(studentIdNum: string, subjectCode: string): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(`${this.apiUrl}/student/${studentIdNum}/subject/${subjectCode}`);
  }

  /**
   * Update an existing grade (Teacher and Admin)
   */
  updateGrade(id: number, gradeData: GradeRequest): Observable<GradeResponse> {
    return this.http.put<GradeResponse>(`${this.apiUrl}/${id}`, gradeData);
  }

  /**
   * Delete a grade (Admin only)
   */
  deleteGrade(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Calculate average grade for a student in a specific subject
   * @param studentIdNum - The student's ID number
   * @param subjectCode - The subject code
   */
  calculateStudentAverageForSubject(studentIdNum: string, subjectCode: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/averages/student/${studentIdNum}/subject/${subjectCode}`);
  }

  /**
   * Calculate overall weighted average for a student across all subjects
   * @param studentIdNum - The student's ID number
   */
  calculateStudentOverallAverage(studentIdNum: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/averages/student/overall/${studentIdNum}`);
  }

  /**
   * Calculate average grade for a subject across all students
   * @param subjectCode - The subject code
   */
  calculateSubjectAverage(subjectCode: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/averages/subject/${subjectCode}`);
  }
}