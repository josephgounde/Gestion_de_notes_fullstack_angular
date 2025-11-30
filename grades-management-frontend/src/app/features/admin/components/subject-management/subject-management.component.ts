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
      description: [''],
      coefficient: [1, [Validators.required, Validators.min(0.5), Validators.max(10)]]
    });
  }

  loadSubjects(): void {
    this.loading = true;
    this.subjectsService.getAllSubjects().subscribe({
      next: (subjects) => {
        this.subjects = subjects;
        this.filteredSubjects = subjects;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading subjects:', error);
        this.loading = false;
      }
    });
  }

  openAddModal(): void {
    this.subjectForm.reset({ coefficient: 1, description: '' });
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.subjectForm.reset();
  }

  openEditModal(subject: SubjectResponse): void {
    this.selectedSubject = subject;
    this.subjectForm.patchValue({
      name: subject.name,
      description: subject.description || '',
      coefficient: subject.coefficient
    });
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedSubject = null;
    this.subjectForm.reset();
  }

  onSubmitAdd(): void {
    if (this.subjectForm.invalid) return;

    const formValue = this.subjectForm.value;
    const subjectData: SubjectRequest = {
      subjectCode: formValue.subjectCode.toUpperCase(),
      name: formValue.name,
      description: formValue.description || '',
      coefficient: formValue.coefficient
    };

    this.loading = true;
    this.subjectsService.createSubject(subjectData).subscribe({
      next: () => {
        this.loadSubjects();
        this.closeAddModal();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error creating subject:', error);
        alert('Error creating subject: ' + (error.error?.message || 'Unknown error'));
        this.loading = false;
      }
    });
  }

  onSubmitEdit(): void {
    if (this.subjectForm.invalid || !this.selectedSubject) return;

    const formValue = this.subjectForm.value;
    const updateData: SubjectRequest = {
      subjectCode: this.selectedSubject.subjectCode,
      name: formValue.name,
      description: formValue.description || '',
      coefficient: formValue.coefficient
    };

    this.loading = true;
    this.subjectsService.updateSubject(this.selectedSubject.subjectCode, updateData).subscribe({
      next: () => {
        this.loadSubjects();
        this.closeEditModal();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error updating subject:', error);
        alert('Error updating subject: ' + (error.error?.message || 'Unknown error'));
        this.loading = false;
      }
    });
  }

  deleteSubject(subject: SubjectResponse): void {
    if (!confirm(`Are you sure you want to delete subject "${subject.name}"?`)) {
      return;
    }

    this.loading = true;
    this.subjectsService.deleteSubject(subject.id).subscribe({
      next: () => {
        this.loadSubjects();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error deleting subject:', error);
        alert('Error deleting subject: ' + (error.error?.message || 'Unknown error'));
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.filteredSubjects = this.subjects.filter(subject =>
      !this.searchTerm ||
      subject.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      subject.subjectCode.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  getCoefficientColor(coefficient: number): string {
    if (coefficient >= 3) return 'text-red-600';
    if (coefficient >= 2) return 'text-orange-600';
    return 'text-green-600';
  }
}