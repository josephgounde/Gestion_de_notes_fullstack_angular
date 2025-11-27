import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SubjectRequest, SubjectResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class SubjectsService {
  private apiUrl = `${environment.apiUrl}/subjects`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new subject (Admin only)
   */
  createSubject(subject: SubjectRequest): Observable<SubjectResponse> {
    return this.http.post<SubjectResponse>(this.apiUrl, subject);
  }

  /**
   * Get all subjects
   */
  getAllSubjects(): Observable<SubjectResponse[]> {
    return this.http.get<SubjectResponse[]>(this.apiUrl);
  }

  /**
   * Get subject by code
   */
  getSubjectByCode(subjectCode: string): Observable<SubjectResponse> {
    return this.http.get<SubjectResponse>(`${this.apiUrl}/${subjectCode}`);
  }

  /**
   * Get subject by name
   */
  getSubjectByName(name: string): Observable<SubjectResponse> {
    return this.http.get<SubjectResponse>(`${this.apiUrl}/name/${name}`);
  }

  /**
   * Update a subject (Admin only)
   */
  updateSubject(subjectCode: string, subject: SubjectRequest): Observable<SubjectResponse> {
    return this.http.put<SubjectResponse>(`${this.apiUrl}/${subjectCode}`, subject);
  }

  /**
   * Delete a subject (Admin only)
   */
  deleteSubject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
