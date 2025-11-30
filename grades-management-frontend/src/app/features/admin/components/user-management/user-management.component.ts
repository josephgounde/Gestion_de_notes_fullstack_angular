import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { UsersService } from '../../../../core/services/users.service';
import { UserResponse, Role, StudentRequest, TeacherRequest, StudentResponse, TeacherResponse } from '../../../../core/models/models';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  users: UserResponse[] = [];
  filteredUsers: UserResponse[] = [];
  loading = false;
  showAddModal = false;
  showEditModal = false;
  userForm!: FormGroup;
  selectedUser: UserResponse | null = null;
  selectedRole: Role = Role.STUDENT;
  searchTerm = '';
  filterRole: string = 'ALL';

  // Make Role enum available in template
  Role = Role;

  roles = [
    { value: Role.STUDENT, label: 'Student' },
    { value: Role.TEACHER, label: 'Teacher' },
    { value: Role.ADMIN, label: 'Admin' }
  ];

  constructor(
    private usersService: UsersService,
    private fb: FormBuilder
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  initForm(): void {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      role: [Role.STUDENT, Validators.required],
      studentIdNum: [''],
      teacherIdNum: ['']
    });
  }

  loadUsers(): void {
    this.loading = true;
    this.usersService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.filteredUsers = users;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.loading = false;
      }
    });
  }

  openAddModal(role: Role): void {
    this.selectedRole = role;
    this.userForm.reset({ role });
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.userForm.reset();
  }

  openEditModal(user: UserResponse): void {
    this.selectedUser = user;
    this.userForm.patchValue({
      username: user.username,
      email: user.email,
      firstName: user.firstname,
      lastName: user.lastname
    });
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedUser = null;
    this.userForm.reset();
  }

  onSubmitAdd(): void {
    if (this.userForm.invalid) return;

    const formValue = this.userForm.value;
    this.loading = true;

    if (formValue.role === Role.STUDENT) {
      const studentData: StudentRequest = {
        username: formValue.username,
        email: formValue.email,
        password: formValue.password,
        firstname: formValue.firstName,
        lastname: formValue.lastName,
        studentIdNum: formValue.studentIdNum || formValue.username
      };

      this.usersService.registerStudent(studentData).subscribe({
        next: () => {
          this.loadUsers();
          this.closeAddModal();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error creating student:', error);
          alert('Error creating student: ' + (error.error?.message || 'Unknown error'));
          this.loading = false;
        }
      });
    } else if (formValue.role === Role.TEACHER) {
      const teacherData: TeacherRequest = {
        username: formValue.username,
        email: formValue.email,
        password: formValue.password,
        firstname: formValue.firstName,
        lastname: formValue.lastName,
        teacherIdNum: formValue.teacherIdNum || formValue.username
      };

      this.usersService.registerTeacher(teacherData).subscribe({
        next: () => {
          this.loadUsers();
          this.closeAddModal();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error creating teacher:', error);
          alert('Error creating teacher: ' + (error.error?.message || 'Unknown error'));
          this.loading = false;
        }
      });
    }
  }

  onSubmitEdit(): void {
    if (this.userForm.invalid || !this.selectedUser) return;

    const formValue = this.userForm.value;
    const updateData = {
      username: formValue.username,
      email: formValue.email,
      firstname: formValue.firstName,
      lastname: formValue.lastName,
      password: formValue.password || undefined
    };

    this.loading = true;
    this.usersService.updateUser(this.selectedUser.id, updateData).subscribe({
      next: () => {
        this.loadUsers();
        this.closeEditModal();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error updating user:', error);
        alert('Error updating user: ' + (error.error?.message || 'Unknown error'));
        this.loading = false;
      }
    });
  }

  deleteUser(user: UserResponse): void {
    if (!confirm(`Are you sure you want to delete user "${user.username}"?`)) {
      return;
    }

    this.loading = true;
    this.usersService.deleteUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error deleting user:', error);
        alert('Error deleting user: ' + (error.error?.message || 'Unknown error'));
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.filterUsers();
  }

  onFilterRole(): void {
    this.filterUsers();
  }

  filterUsers(): void {
    this.filteredUsers = this.users.filter(user => {
      const matchesSearch = !this.searchTerm || 
        user.username.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.firstname?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.lastname?.toLowerCase().includes(this.searchTerm.toLowerCase());

      const matchesRole = this.filterRole === 'ALL' || 
        user.role === this.filterRole;

      return matchesSearch && matchesRole;
    });
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case Role.ADMIN: return 'bg-red-100 text-red-800';
      case Role.TEACHER: return 'bg-blue-100 text-blue-800';
      case Role.STUDENT: return 'bg-green-100 text-green-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getRoleLabel(role: string): string {
    return role || 'Unknown';
  }

  getUserIdNumber(user: UserResponse): string {
    // Type guard to check if user is a student
    if (this.isStudent(user)) {
      return (user as StudentResponse).studentIdNum || '-';
    }
    // Type guard to check if user is a teacher
    if (this.isTeacher(user)) {
      return (user as TeacherResponse).teacherIdNum || '-';
    }
    return '-';
  }

  private isStudent(user: UserResponse): boolean {
    return user.role === Role.STUDENT;
  }

  private isTeacher(user: UserResponse): boolean {
    return user.role === Role.TEACHER;
  }
}