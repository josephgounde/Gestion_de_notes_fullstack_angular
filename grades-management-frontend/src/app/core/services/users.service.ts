import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  UserRequest,
  UserResponse,
  StudentRequest,
  StudentResponse,
  TeacherRequest,
  TeacherResponse
} from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private apiUrl = `${environment.apiUrl}/admin/users`;

  constructor(private http: HttpClient) {}

  /**
   * Register a new student (Admin only)
   */
  registerStudent(student: StudentRequest): Observable<StudentResponse> {
    return this.http.post<StudentResponse>(`${this.apiUrl}/students`, student);
  }

  /**
   * Register a new teacher (Admin only)
   */
  registerTeacher(teacher: TeacherRequest): Observable<TeacherResponse> {
    return this.http.post<TeacherResponse>(`${this.apiUrl}/teachers`, teacher);
  }

  /**
   * Get user by ID
   */
  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get user by username
   */
  getUserByUsername(username: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/username/${username}`);
  }

  /**
   * Get user by email
   */
  getUserByEmail(email: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/email/${email}`);
  }

  /**
   * Get all users (Admin only)
   */
  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/users`);
  }

  /**
   * Update user information (Admin only)
   */
  updateUser(id: number, user: UserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/update/${id}`, user);
  }

  /**
   * Delete user (Admin only)
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}
