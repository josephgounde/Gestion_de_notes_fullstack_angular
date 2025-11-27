import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  GradeRequest,
  GradeResponse,
  GradeUpdateRequest
} from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class GradesService {
  private apiUrl = `${environment.apiUrl}/grades`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new grade
   */
  addGrade(grade: GradeRequest): Observable<GradeResponse> {
    return this.http.post<GradeResponse>(`${this.apiUrl}/add`, grade);
  }

  /**
   * Get grade by ID
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
   * Get grades by student ID number
   */
  getGradesByStudentIdNum(studentIdNum: string): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(`${this.apiUrl}/student/${studentIdNum}`);
  }

  /**
   * Get grades by subject code
   */
  getGradesBySubjectCode(subjectCode: string): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(`${this.apiUrl}/subject/${subjectCode}`);
  }

  /**
   * Get grades by student ID number and subject code
   */
  getGradesByStudentAndSubject(studentIdNum: string, subjectCode: string): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(
      `${this.apiUrl}/student/${studentIdNum}/subject/${subjectCode}`
    );
  }

  /**
   * Update grade
   */
  updateGrade(id: number, grade: GradeRequest): Observable<GradeResponse> {
    return this.http.put<GradeResponse>(`${this.apiUrl}/${id}`, grade);
  }

  /**
   * Delete grade (Admin only)
   */
  deleteGrade(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Calculate student average grade for a subject
   */
  calculateStudentAverageForSubject(studentIdNum: string, subjectCode: string): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/averages/student/${studentIdNum}/subject/${subjectCode}`
    );
  }

  /**
   * Calculate student overall average grade
   */
  calculateStudentOverallAverage(studentIdNum: string): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/averages/student/overall/${studentIdNum}`
    );
  }

  /**
   * Calculate subject average grade across all students
   */
  calculateSubjectAverage(subjectCode: string): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/averages/subject/${subjectCode}`
    );
  }
}
