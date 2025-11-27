import { Routes } from '@angular/router';

export const STUDENT_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];

// TODO: Create dashboard component:
// ng generate component features/student/components/dashboard --standalone
