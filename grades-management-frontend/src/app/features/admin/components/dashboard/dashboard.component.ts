import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { UsersService } from '../../../../core/services/users.service';
import { SubjectsService } from '../../../../core/services/subjects.service';
import { GradesService } from '../../../../core/services/grades.service';
import { ClassesService } from '../../../../core/services/classes.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  adminName = '';
  stats = {
    totalUsers: 0,
    totalClasses: 0,
    totalSubjects: 0,
    totalGrades: 0
  };
  loading = false;

  constructor(
    private authService: AuthService,
    private usersService: UsersService,
    private subjectsService: SubjectsService,
    private gradesService: GradesService,
    private classesService: ClassesService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.adminName = `${user.username}`;
    }
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading = true;

    // Load users count
    this.usersService.getAllUsers().subscribe({
      next: (users) => {
        this.stats.totalUsers = users.length;
      },
      error: (error) => console.error('Error loading users:', error)
    });

    // Load subjects count
    this.subjectsService.getAllSubjects().subscribe({
      next: (subjects) => {
        this.stats.totalSubjects = subjects.length;
      },
      error: (error) => console.error('Error loading subjects:', error)
    });

    // Load grades count
    this.gradesService.getAllGrades().subscribe({
      next: (grades) => {
        this.stats.totalGrades = grades.length;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading grades:', error);
        this.loading = false;
      }
    });

    // Load classes count
    this.classesService.getAllClasses().subscribe({
      next: (classes) => {
        this.stats.totalClasses = classes.length;
      },
      error: (error) => console.error('Error loading classes:', error)
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
