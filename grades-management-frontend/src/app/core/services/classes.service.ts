import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ClassRequest, ClassResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ClassesService {
  private apiUrl = `${environment.apiUrl}/classes`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new class (Admin only)
   */
  createClass(classData: ClassRequest): Observable<ClassResponse> {
    return this.http.post<ClassResponse>(this.apiUrl, classData);
  }

  /**
   * Get class by ID
   */
  getClassById(id: number): Observable<ClassResponse> {
    return this.http.get<ClassResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get all classes (Admin only)
   */
  getAllClasses(): Observable<ClassResponse[]> {
    return this.http.get<ClassResponse[]>(this.apiUrl);
  }

  /**
   * Get classes by teacher ID number
   */
  getClassesByTeacherIdNum(teacherIdNum: string): Observable<ClassResponse[]> {
    return this.http.get<ClassResponse[]>(`${this.apiUrl}/teacher/${teacherIdNum}`);
  }

  /**
   * Get classes by student ID number
   */
  getClassesByStudentIdNum(studentIdNum: string): Observable<ClassResponse[]> {
    return this.http.get<ClassResponse[]>(`${this.apiUrl}/student/${studentIdNum}`);
  }

  /**
   * Update a class (Admin only)
   */
  updateClass(id: number, classData: ClassRequest): Observable<ClassResponse> {
    return this.http.put<ClassResponse>(`${this.apiUrl}/${id}`, classData);
  }

  /**
   * Delete a class (Admin only)
   */
  deleteClass(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
