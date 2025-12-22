import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { SubjectsService } from '../../../../core/services/subjects.service';
import { SubjectResponse, SubjectRequest } from '../../../../core/models/models';

@Component({
  selector: 'app-subject-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './subject-management.component.html',
  styleUrls: ['./subject-management.component.scss']
})
export class SubjectManagementComponent implements OnInit {
  subjects: SubjectResponse[] = [];
  filteredSubjects: SubjectResponse[] = [];
  loading = false;
  showAddModal = false;
  showEditModal = false;
  subjectForm!: FormGroup;
  selectedSubject: SubjectResponse | null = null;
  searchTerm = '';
  
  // Error handling properties
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private subjectsService: SubjectsService,
    private fb: FormBuilder
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadSubjects();
  }

  initForm(): void {
    this.subjectForm = this.fb.group({
      subjectCode: ['', [Validators.required, Validators.pattern(/^[A-Z0-9]+$/)]],
      name: ['', Validators.required],
      coefficient: [1, [Validators.required, Validators.min(0.5), Validators.max(10)]],
      description: ['', Validators.required]
    });
  }

  loadSubjects(): void {
    this.loading = true;
    this.clearMessages();
    
    this.subjectsService.getAllSubjects().subscribe({
      next: (subjects) => {
        this.subjects = subjects;
        this.filteredSubjects = subjects;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading subjects:', error);
        this.errorMessage = 'Failed to load subjects: ' + (error.message || 'Unknown error');
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredSubjects = this.subjects.filter(subject =>
      subject.name.toLowerCase().includes(term) ||
      subject.subjectCode.toLowerCase().includes(term)
    );
  }

  openAddModal(): void {
    this.showAddModal = true;
    this.subjectForm.reset({
      coefficient: 1
    });
    this.clearMessages();
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.subjectForm.reset();
    this.clearMessages();
  }

  openEditModal(subject: SubjectResponse): void {
    this.selectedSubject = subject;
    this.showEditModal = true;
    this.subjectForm.patchValue({
      subjectCode: subject.subjectCode,
      name: subject.name,
      coefficient: subject.coefficient,
      description: subject.description
    });
    this.clearMessages();
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedSubject = null;
    this.subjectForm.reset();
    this.clearMessages();
  }

  onSubmitAdd(): void {
    if (this.subjectForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      return;
    }

    const formValue = this.subjectForm.value;
    const subjectData: SubjectRequest = {
      subjectCode: formValue.subjectCode,
      name: formValue.name,
      description: formValue.description,
      coefficient: formValue.coefficient
    };

    this.loading = true;
    this.clearMessages();
    
    this.subjectsService.createSubject(subjectData).subscribe({
      next: () => {
        this.successMessage = 'Subject created successfully!';
        this.loadSubjects();
        
        // Close modal after showing success message briefly
        setTimeout(() => {
          this.closeAddModal();
        }, 1500);
      },
      error: (error) => {
        console.error('Error creating subject:', error);
        this.errorMessage = this.extractErrorMessage(error);
        this.loading = false;
      }
    });
  }

  onSubmitEdit(): void {
    if (this.subjectForm.invalid || !this.selectedSubject) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      return;
    }

    const formValue = this.subjectForm.value;
    const subjectData: SubjectRequest = {
      subjectCode: formValue.subjectCode,
      name: formValue.name,
      description: formValue.description,
      coefficient: formValue.coefficient
    };

    this.loading = true;
    this.clearMessages();
    
    this.subjectsService.updateSubject(this.selectedSubject.subjectCode, subjectData).subscribe({
      next: () => {
        this.successMessage = 'Subject updated successfully!';
        this.loadSubjects();
        
        // Close modal after showing success message briefly
        setTimeout(() => {
          this.closeEditModal();
        }, 1500);
      },
      error: (error) => {
        console.error('Error updating subject:', error);
        this.errorMessage = this.extractErrorMessage(error);
        this.loading = false;
      }
    });
  }

  deleteSubject(id: number): void {
    if (!confirm('Are you sure you want to delete this subject? This action cannot be undone.')) {
      return;
    }

    this.loading = true;
    this.clearMessages();
    
    this.subjectsService.deleteSubject(id).subscribe({
      next: () => {
        this.successMessage = 'Subject deleted successfully!';
        this.loadSubjects();
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          this.clearMessages();
        }, 3000);
      },
      error: (error) => {
        console.error('Error deleting subject:', error);
        this.errorMessage = this.extractErrorMessage(error);
        this.loading = false;
      }
    });
  }

  /**
   * Extract a user-friendly error message from the error object
   */
  private extractErrorMessage(error: any): string {
    // Check for backend validation messages
    if (error.message) {
      return error.message;
    }
    
    // Check for HTTP error response
    if (error.error?.message) {
      return error.error.message;
    }
    
    // Check for validation errors
    if (error.error?.errors) {
      const errors = error.error.errors;
      if (Array.isArray(errors)) {
        return errors.join(', ');
      }
      return JSON.stringify(errors);
    }
    
    // Default error message
    return 'An unexpected error occurred. Please try again.';
  }

  /**
   * Clear all error and success messages
   */
  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  /**
   * Get form control error messages
   */
  getFieldError(fieldName: string): string {
    const control = this.subjectForm.get(fieldName);
    
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return `${this.getFieldLabel(fieldName)} is required.`;
    }
    
    if (control.errors['pattern']) {
      return `${this.getFieldLabel(fieldName)} must contain only uppercase letters and numbers.`;
    }
    
    if (control.errors['min']) {
      return `${this.getFieldLabel(fieldName)} must be at least ${control.errors['min'].min}.`;
    }
    
    if (control.errors['max']) {
      return `${this.getFieldLabel(fieldName)} must be at most ${control.errors['max'].max}.`;
    }

    return 'Invalid value.';
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      subjectCode: 'Subject code',
      name: 'Subject name',
      coefficient: 'Coefficient',
      description: 'Description'
    };
    return labels[fieldName] || fieldName;
  }
}