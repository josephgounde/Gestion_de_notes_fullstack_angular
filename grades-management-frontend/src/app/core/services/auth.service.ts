import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginRequest, JwtResponse, Role } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';
  
  private currentUserSubject = new BehaviorSubject<JwtResponse | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Authenticate user and store JWT token
   */
  login(credentials: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${environment.apiUrl}/auth/signin`, credentials)
      .pipe(
        tap(response => {
          if (response && response.token) {
            this.storeToken(response.token);
            this.storeUser(response);
            this.currentUserSubject.next(response);
          }
        })
      );
  }

  /**
   * Logout user and clear session
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    // Check if token is expired
    try {
      const payload = this.decodeToken(token);
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  /**
   * Get current user
   */
  getCurrentUser(): JwtResponse | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: Role): boolean {
    const user = this.getCurrentUser();
    if (!user || !user.roles) return false;
    return user.roles.includes(`ROLE_${role}`);
  }

  /**
   * Check if user is admin
   */
  isAdmin(): boolean {
    return this.hasRole(Role.ADMIN);
  }

  /**
   * Check if user is teacher
   */
  isTeacher(): boolean {
    return this.hasRole(Role.TEACHER);
  }

  /**
   * Check if user is student
   */
  isStudent(): boolean {
    return this.hasRole(Role.STUDENT);
  }

  /**
   * Get authentication token
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Store authentication token
   */
  private storeToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Store user information
   */
  private storeUser(user: JwtResponse): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Get user from storage
   */
  private getUserFromStorage(): JwtResponse | null {
    const userStr = localStorage.getItem(this.USER_KEY);
    if (!userStr) return null;
    
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }

  /**
   * Decode JWT token
   */
  private decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch (error) {
      throw new Error('Invalid token');
    }
  }

  /**
   * Navigate to dashboard based on user role
   */
  navigateByRole(): void {
    if (this.isAdmin()) {
      this.router.navigate(['/admin/dashboard']);
    } else if (this.isTeacher()) {
      this.router.navigate(['/teacher/dashboard']);
    } else if (this.isStudent()) {
      this.router.navigate(['/student/dashboard']);
    } else {
      this.router.navigate(['/auth/login']);
    }
  }
}
