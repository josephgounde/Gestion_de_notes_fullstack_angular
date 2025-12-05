import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../core/services/auth.service';
import { GradesService } from '../../../../core/services/grades.service';
import { GradeResponse } from '../../../../core/models/models';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  studentName = '';
  grades: GradeResponse[] = [];
  loading = false;
  overallAverage = 0;

  constructor(
    private authService: AuthService,
    private gradesService: GradesService
  ) {
    console.log('🏗️ Dashboard constructor called');
  }

  ngOnInit(): void {
    console.log('🚀 Dashboard ngOnInit called');
    const user = this.authService.getCurrentUser();
    console.log('👤 Current user:', user);
    if (user) {
      this.studentName = `${user.username}`;
      console.log('🔑 Token available:', !!this.authService.getToken(), this.authService.getToken()?.substring(0, 30) + '...');
      
      // Small delay to ensure everything is initialized
      setTimeout(() => {
        console.log('⏰ Starting to load grades after component initialization');
        this.loadStudentGrades();
      }, 100);
    }
  }

  loadStudentGrades(): void {
    const user = this.authService.getCurrentUser();
    if (!user) {
      console.error('❌ No user available for loading grades');
      return;
    }

    console.log('📊 Loading grades for user:', user.username);
    
    // Use studentIdNum if available, otherwise fall back to username
    // The backend expects studentIdNum (like "STU001"), not username (like "student1")
    const studentId = user.studentIdNum || user.username;
    console.log('📊 Student ID to use:', studentId);
    
    // Verify token one more time before making requests
    const token = this.authService.getToken();
    console.log('📊 Token check before request:', !!token);
    
    this.loading = true;
    
    // Load grades by student ID
    this.gradesService.getGradesByStudentIdNum(studentId).subscribe({
      next: (grades) => {
        console.log('✅ Grades loaded successfully:', grades);
        this.grades = grades;
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Error loading grades:', error);
        this.loading = false;
      }
    });

    // Calculate overall average
    this.gradesService.calculateStudentOverallAverage(studentId).subscribe({
      next: (average) => {
        console.log('✅ Average calculated successfully:', average);
        this.overallAverage = average;
      },
      error: (error) => {
        console.error('❌ Error calculating average:', error);
      }
    });
  }

  getGradeColor(value: number): string {
    if (value >= 80) return 'text-green-600';
    if (value >= 60) return 'text-yellow-600';
    return 'text-red-600';
  }

  logout(): void {
    this.authService.logout();
  }
}