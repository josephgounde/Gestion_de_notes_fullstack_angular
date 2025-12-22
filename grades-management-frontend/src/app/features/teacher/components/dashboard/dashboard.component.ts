import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { ClassesService } from '../../../../core/services/classes.service';
import { EnrollmentsService } from '../../../../core/services/enrollments.service';
import { GradesService } from '../../../../core/services/grades.service';
import { 
  TeacherStatistics, 
  QuickAction, 
  RecentActivity,
  ClassResponse,
  EnrollmentResponse,
  GradeResponse
} from '../../../../core/models/models';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  teacherName = '';
  teacherIdNum = '';
  
  // Dashboard statistics
  stats: TeacherStatistics = {
    totalClasses: 0,
    totalStudents: 0,
    totalGradesRecorded: 0,
    pendingGrades: 0,
    averageClassPerformance: 0
  };

  // Quick action cards
  quickActions: QuickAction[] = [
    {
      title: 'Record Grades',
      description: 'Add or update student grades',
      icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2',
      route: '/teacher/grades/record',
      color: 'bg-blue-500'
    },
    {
      title: 'My Classes',
      description: 'View and manage your assigned classes',
      icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z',
      route: '/teacher/classes',
      color: 'bg-green-500'
    },
    {
      title: 'Student Performance',
      description: 'View student grades and statistics',
      icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
      route: '/teacher/students',
      color: 'bg-purple-500'
    },
    {
      title: 'Grade Reports',
      description: 'Generate and export grade reports',
      icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
      route: '/teacher/reports',
      color: 'bg-orange-500'
    }
  ];

  // Recent activity
  recentActivities: RecentActivity[] = [];

  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private classesService: ClassesService,
    private enrollmentsService: EnrollmentsService,
    private gradesService: GradesService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.teacherName = user.username;
      this.teacherIdNum = user.teacherIdNum || '';
      
      if (this.teacherIdNum) {
        this.loadDashboardData();
      } else {
        this.errorMessage = 'Teacher ID not found. Please contact administrator.';
      }
    }
  }

  /**
   * Load dashboard statistics and data from backend APIs
   */
  loadDashboardData(): void {
    this.loading = true;
    this.errorMessage = '';
    
    // Fetch all required data in parallel
    forkJoin({
      classes: this.classesService.getClassesByTeacherIdNum(this.teacherIdNum),
      enrollments: this.enrollmentsService.getAllEnrollments(),
      /*grades: this.gradesService.getAllGrades()*/
      grades: this.gradesService.getGradesByTeacher(this.teacherIdNum)
    }).subscribe({
      next: (data) => {
        this.processData(data.classes, data.enrollments, data.grades);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.errorMessage = 'Failed to load dashboard data. ' + (error.message || 'Unknown error');
        this.loading = false;
      }
    });
  }

  /**
   * Process fetched data and calculate statistics
   */
  private processData(
  classes: ClassResponse[], 
  allEnrollments: EnrollmentResponse[], 
  teacherGrades: GradeResponse[] // Already filtered by backend!
  ): void {
  // Get class IDs for this teacher
  const teacherClassIds = classes.map(c => c.id);
  
  // Filter enrollments for teacher's classes
  const teacherEnrollments = allEnrollments.filter(e => 
    teacherClassIds.includes(e.classEntity.id)
  );
  
  // Get unique students in teacher's classes
  const uniqueStudentIds = new Set(
    teacherEnrollments.map(e => e.student.studentIdNum)
  );

  // Calculate statistics (grades already filtered by backend)
  this.stats = {
    totalClasses: classes.length,
    totalStudents: uniqueStudentIds.size,
    totalGradesRecorded: teacherGrades.length, // No filtering needed!
    pendingGrades: 0,
    averageClassPerformance: teacherGrades.length > 0
      ? parseFloat((
          teacherGrades.reduce((sum, g) => sum + g.value, 0) / teacherGrades.length
        ).toFixed(2))
      : 0
  };

  // Generate recent activities
  this.generateRecentActivities(teacherGrades, classes);
}

  /**
   * Generate recent activity entries from grade data
   */
  private generateRecentActivities(grades: GradeResponse[], classes: ClassResponse[]): void {
    // Get the 5 most recent grades
    const recentGrades = grades
      .sort((a, b) => {
        if (!a.date || !b.date) return 0;
        return new Date(b.date).getTime() - new Date(a.date ).getTime();
      })
      .slice(0, 5);

    this.recentActivities = recentGrades.map(grade => ({
      type: 'grade' as const,
      message: `Recorded grade ${grade.value}/20 for ${grade.firstname} ${grade.lastname} in ${grade.subjectName}`,
      timestamp: grade.date ? new Date(grade.date) : new Date()
    }));

    // Add a class viewing activity if no grades
    if (this.recentActivities.length === 0 && classes.length > 0) {
      this.recentActivities.push({
        type: 'class',
        message: `Viewing ${classes[0].name}`,
        timestamp: new Date()
      });
    }
  }

  /**
   * Navigate to specific action
   */
  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  /**
   * Get formatted time ago string
   */
  getTimeAgo(date: Date): string {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  }

  /**
   * Get icon for activity type
   */
  getActivityIcon(type: string): string {
    const icons: { [key: string]: string } = {
      grade: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2',
      class: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z',
      report: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
      student: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z'
    };
    return icons[type] || icons['grade'];
  }

  /**
   * Logout user
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  /**
   * Refresh dashboard data
   */
  refresh(): void {
    this.loadDashboardData();
  }
}