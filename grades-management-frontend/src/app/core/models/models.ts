// Role enum
export enum Role {
  ADMIN = 'ADMIN',
  TEACHER = 'TEACHER',
  STUDENT = 'STUDENT'
}

// Transcript Status enum
export enum TranscriptStatus {
  GENERATED = 'GENERATED',
  ARCHIVED = 'ARCHIVED',
  ERROR = 'ERROR'
}

// Base User interfaces
export interface UserRequest {
  username: string;
  password: string;
  firstname: string;
  lastname: string;
  email: string;
}

export interface UserResponse {
  id: number;
  username: string;
  firstname: string;
  lastname: string;
  email: string;
  role: Role;
}

// Student interfaces
export interface StudentRequest extends UserRequest {
  studentIdNum: string;
}

export interface StudentResponse extends UserResponse {
  studentIdNum: string;
}

// Teacher interfaces
export interface TeacherRequest extends UserRequest {
  teacherIdNum: string;
}

export interface TeacherResponse extends UserResponse {
  teacherIdNum: string;
}

// Admin interface
export interface AdminResponse extends UserResponse {
  // No additional fields
}

// Class interfaces
export interface ClassRequest {
  academicYear: string;
  name: string;
}

export interface ClassResponse {
  id: number;
  academicYear: string;
  name: string;
}

// Subject interfaces
export interface SubjectRequest {
  subjectCode: string;
  name: string;
  coefficient: number;
  description: string;
}

export interface SubjectResponse {
  id: number;
  subjectCode: string;
  name: string;
  coefficient: number;
  description: string;
}

// Grade interfaces
export interface GradeRequest {
  value: number;
  date: string; // ISO date string
  comment?: string;
  studentIdNum: string;
  subjectCode: string;
  recordedBy?: string;
}

export interface GradeResponse {
  id: number;
  value: number;
  date: string;
  comment?: string;
  studentIdNum: string;
  firstname: string;
  lastname: string;
  subjectCode: string;
  subjectName: string;
  recordedBy?: string;
}

export interface GradeUpdateRequest {
  value?: number;
  comment?: string;
}

// Enrollment interfaces
export interface EnrollmentRequest {
  studentIdNum: string;
  classId: number;
  subjectCode: string;
}

export interface EnrollmentResponse {
  id: number;
  enrollmentDate?: string;
  semester?: string;
  academicYear?: string;
  student: StudentResponse;
  subject: SubjectResponse;
  classEntity: ClassResponse;
}

// Teacher-Class Assignment interfaces
export interface TeacherClassRequest {
  teacherIdNum: string;
  classId: number;
}

export interface TeacherClassResponse {
  id: number;
  teacher: TeacherResponse;
  classEntity: ClassResponse;
}

// Class-Subject interfaces
export interface ClassSubjectRequest {
  classId: number;
  subjectId: number;
}

export interface ClassSubjectResponse {
  id: number;
  classEntity: ClassResponse;
  subject: SubjectResponse;
}

// Authentication interfaces
export interface LoginRequest {
  username: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

// Login Record interfaces
export interface LoginRecordResponse {
  id: number;
  logIn: string;
  logOut?: string;
  user: UserResponse;
  ipAddress?: string;
}

// Transcript interfaces
export interface TranscriptResponse {
  id: number;
  generationDate: string;
  status: TranscriptStatus;
  filepath: string;
  student: StudentResponse;
}

// Email interface
export interface EmailRequest {
  to: string;
  subject: string;
  body: string;
}

// Dashboard Statistics interfaces
export interface StudentStatistics {
  overallAverage: number;
  subjectAverages: { [subjectCode: string]: number };
  totalGrades: number;
  totalSubjects: number;
}

export interface TeacherStatistics {
  totalClasses: number;
  totalStudents: number;
  totalGradesRecorded: number;
  averageClassPerformance: number;
}

export interface AdminStatistics {
  totalStudents: number;
  totalTeachers: number;
  totalClasses: number;
  totalSubjects: number;
  totalGrades: number;
  averageSystemPerformance: number;
}

// Chart data interface
export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string | string[];
    borderWidth?: number;
  }[];
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: number;
}

// Error response
export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
  timestamp: string;
  path: string;
}
