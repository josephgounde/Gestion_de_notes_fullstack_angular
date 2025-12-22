import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EnrollmentRequest, EnrollmentResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class EnrollmentsService {
  private apiUrl = `${environment.apiUrl}/enrollments`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new enrollment (Admin only)
   * Enrolls a student in a specific class and subject
   */
  createEnrollment(enrollmentData: EnrollmentRequest): Observable<EnrollmentResponse> {
    return this.http.post<EnrollmentResponse>(this.apiUrl, enrollmentData);
  }

  /**
   * Get all enrollments (Admin and Teacher)
   */
  getAllEnrollments(): Observable<EnrollmentResponse[]> {
    return this.http.get<EnrollmentResponse[]>(this.apiUrl);
  }

  /**
   * Get a specific enrollment by ID
   */
  getEnrollmentById(id: number): Observable<EnrollmentResponse> {
    return this.http.get<EnrollmentResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get all enrollments for a specific student
   * @param studentIdNum - The student's ID number (e.g., "STU001")
   */
  getEnrollmentsByStudent(studentIdNum: string): Observable<EnrollmentResponse[]> {
    return this.http.get<EnrollmentResponse[]>(`${this.apiUrl}/student/${studentIdNum}`);
  }

  /**
   * Delete an enrollment (Admin only)
   */
  deleteEnrollment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}