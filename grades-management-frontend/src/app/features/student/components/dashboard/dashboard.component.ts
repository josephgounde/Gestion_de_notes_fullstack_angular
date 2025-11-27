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
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.studentName = `${user.username}`;
      this.loadStudentGrades();
    }
  }

  loadStudentGrades(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.loading = true;
    
    // Load grades by student username/ID
    this.gradesService.getGradesByStudentIdNum(user.username).subscribe({
      next: (grades) => {
        this.grades = grades;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading grades:', error);
        this.loading = false;
      }
    });

    // Calculate overall average
    this.gradesService.calculateStudentOverallAverage(user.username).subscribe({
      next: (average) => {
        this.overallAverage = average;
      },
      error: (error) => {
        console.error('Error calculating average:', error);
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
