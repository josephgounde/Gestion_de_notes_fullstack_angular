import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/models';

export const roleGuard = (allowedRoles: Role[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/auth/login']);
      return false;
    }

    const hasRequiredRole = allowedRoles.some(role => authService.hasRole(role));

    if (hasRequiredRole) {
      return true;
    }

    // Redirect to appropriate dashboard if user doesn't have required role
    authService.navigateByRole();
    return false;
  };
};
