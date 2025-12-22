import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = '';
      let shouldLogout = false;

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 401:
            // Check if this is actually a token expiration or just an authorization issue
            const isLoginRequest = req.url.includes('/auth/signin');
            const isTokenExpired = error.error?.message?.toLowerCase().includes('expired') ||
                                   error.error?.message?.toLowerCase().includes('invalid token') ||
                                   error.headers.get('Token-Expired') === 'true';
            
            if (isLoginRequest) {
              // Login failure - show error but don't logout (user isn't logged in)
              errorMessage = error.error?.message || 'Invalid username or password.';
            } else if (isTokenExpired) {
              // Token actually expired - logout and redirect
              shouldLogout = true;
              errorMessage = 'Your session has expired. Please login again.';
            } else {
              // Other 401 errors (validation, permissions) - just show the error
              errorMessage = error.error?.message || 'You are not authorized to perform this action.';
            }
            break;

          case 403:
            // Forbidden - insufficient permissions
            errorMessage = error.error?.message || 'You do not have permission to access this resource.';
            break;

          case 400:
            // Bad request - usually validation errors
            errorMessage = error.error?.message || 'Invalid request. Please check your input.';
            break;

          case 404:
            errorMessage = error.error?.message || 'Resource not found.';
            break;

          case 500:
            errorMessage = error.error?.message || 'Internal server error. Please try again later.';
            break;

          default:
            errorMessage = error.error?.message || `Error Code: ${error.status}\nMessage: ${error.message}`;
        }
      }

      // Only logout if it's truly a token expiration issue
      if (shouldLogout) {
        console.error('🔐 Session expired, logging out...');
        authService.logout();
        router.navigate(['/login']);
      } else {
        console.error('❌ HTTP Error:', errorMessage);
      }

      return throwError(() => new Error(errorMessage));
    })
  );
};