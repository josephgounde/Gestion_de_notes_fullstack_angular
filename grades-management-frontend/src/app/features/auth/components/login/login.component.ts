import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { LoginRequest } from '../../../../core/models/models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  error = '';
  returnUrl = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Check if already logged in
    if (this.authService.isAuthenticated()) {
      this.authService.navigateByRole();
      return;
    }

    // Initialize form
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });

    // Get return URL from query params or default to dashboard
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '';
  }

  get f() {
    return this.loginForm.controls;
  }

/*  onSubmit(): void {
    // Reset error
    this.error = '';

    // Validate form
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    // Prepare login request
    const credentials: LoginRequest = {
      username: this.loginForm.value.username,
      password: this.loginForm.value.password
    };

    this.loading = true;

    // Call auth service
    this.authService.login(credentials).subscribe({
      next: (response) => {
        this.loading = false;
        
        // Navigate based on return URL or user role
        if (this.returnUrl) {
          this.router.navigateByUrl(this.returnUrl);
        } else {
          this.authService.navigateByRole();
        }
      },
      error: (error) => {
        this.loading = false;
        this.error = error.message || 'Invalid username or password';
        console.error('Login error:', error);
      }
    });
  }*/

  onSubmit(): void {
  this.error = '';

  if (this.loginForm.invalid) {
    Object.keys(this.loginForm.controls).forEach(key => {
      this.loginForm.get(key)?.markAsTouched();
    });
    return;
  }

  const credentials: LoginRequest = {
    username: this.loginForm.value.username,
    password: this.loginForm.value.password
  };

  this.loading = true;

  this.authService.login(credentials).subscribe({
    next: (response) => {
      console.log('✅ Login successful, response:', response);
      console.log('✅ Token received:', response.token ? response.token.substring(0, 20) + '...' : 'NO TOKEN');
      
      // Check if token is stored
      const storedToken = this.authService.getToken();
      console.log('✅ Token stored in localStorage:', storedToken ? storedToken.substring(0, 20) + '...' : 'NOT STORED');
      
      this.loading = false;
      
      // Add a small delay to ensure token is saved before navigation
      setTimeout(() => {
        if (this.returnUrl) {
          this.router.navigateByUrl(this.returnUrl);
        } else {
          this.authService.navigateByRole();
        }
      }, 100);
    },
    error: (error) => {
      this.loading = false;
      this.error = error.message || 'Invalid username or password';
      console.error('❌ Login error:', error);
    }
  });
}  

  // Helper method to show field errors
  showError(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  getErrorMessage(field: string): string {
    const control = this.loginForm.get(field);
    if (control?.hasError('required')) {
      return `${field.charAt(0).toUpperCase() + field.slice(1)} is required`;
    }
    if (control?.hasError('minlength')) {
      return `${field.charAt(0).toUpperCase() + field.slice(1)} must be at least 6 characters`;
    }
    return '';
  }
}
