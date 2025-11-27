# 🎓 Grades Management System - Angular Frontend

A modern, responsive grades management application built with Angular 17, Tailwind CSS, and PrimeNG.

## 📋 Project Overview

This application provides three distinct interfaces:
- **Student Interface**: View grades, calculate averages, download transcripts
- **Teacher Interface**: Record and manage grades, view assigned classes
- **Admin Interface**: Full system management, user administration, bulk operations

## 🚀 Features

### Student Features
- 📊 Personal dashboard with grade visualization
- 📈 Interactive charts (Chart.js integration)
- 📥 Download academic transcripts (PDF)
- 🔍 Filter results by semester
- 📱 Responsive design for mobile access

### Teacher Features
- ✏️ Grade entry with automatic validation
- ⚖️ Manage subject coefficients
- 📝 Add comments to student grades
- 👥 View assigned classes and students
- 📊 Class performance analytics

### Admin Features
- 📑 Bulk PDF transcript generation
- 📚 Subject catalog management
- 📊 Global success statistics
- 👥 User account administration
- 📦 Excel data exports
- 🔐 System-wide security management

## 🛠️ Tech Stack

- **Framework**: Angular 17
- **Styling**: Tailwind CSS
- **UI Components**: PrimeNG
- **State Management**: RxJS + Services
- **Charts**: Chart.js / ng2-charts
- **HTTP Client**: Angular HttpClient
- **Authentication**: JWT Tokens
- **Build Tool**: Angular CLI

## 📦 Prerequisites

- Node.js (v18 or higher)
- npm (v9 or higher)
- Angular CLI (v17)

## 🔧 Installation

```bash
# Install Angular CLI globally
npm install -g @angular/cli@17

# Navigate to project directory
cd grades-management-frontend

# Install dependencies
npm install

# Install additional dependencies
npm install primeng primeicons
npm install chart.js ng2-charts
npm install @angular/material @angular/cdk
```

## 🚀 Development Server

```bash
# Start development server
ng serve

# Application will be available at http://localhost:4200
```

## 🔐 Backend Integration

The frontend connects to the Spring Boot backend at `http://localhost:8080`

### API Endpoints
- Authentication: `/api/auth/signin`
- Users: `/api/admin/users/**`
- Grades: `/api/grades/**`
- Classes: `/api/classes/**`
- Subjects: `/api/subjects/**`
- Enrollments: `/api/enrollments/**`
- Exports: `/api/exports/**`

## 📂 Project Structure

```
src/
├── app/
│   ├── core/                    # Core functionality
│   │   ├── guards/             # Route guards
│   │   ├── interceptors/       # HTTP interceptors
│   │   ├── models/            # TypeScript interfaces
│   │   └── services/          # Core services
│   ├── features/               # Feature modules
│   │   ├── admin/             # Admin dashboard
│   │   ├── student/           # Student dashboard
│   │   ├── teacher/           # Teacher dashboard
│   │   └── auth/              # Authentication
│   ├── shared/                 # Shared components
│   │   ├── components/        # Reusable components
│   │   └── pipes/             # Custom pipes
│   └── app.component.ts
├── assets/                     # Static assets
└── environments/              # Environment configs
```

## 🎨 Design System

The application follows a modern design system inspired by contemporary UI patterns:
- **Color Palette**: Neutral tones with accent colors
- **Typography**: Clean, readable fonts
- **Spacing**: Consistent 8px grid system
- **Components**: Rounded corners, subtle shadows
- **Interactions**: Smooth transitions and hover effects

## 🔒 Security Features

- JWT token-based authentication
- Role-based access control (RBAC)
- HTTP interceptors for token management
- Route guards for protected pages
- Automatic token refresh
- Secure session management

## 📱 Responsive Design

- Mobile-first approach
- Breakpoints: sm (640px), md (768px), lg (1024px), xl (1280px)
- Touch-friendly UI elements
- Optimized for tablets and desktops

## 🧪 Testing

```bash
# Run unit tests
ng test

# Run e2e tests
ng e2e

# Generate code coverage
ng test --code-coverage
```

## 🏗️ Build

```bash
# Production build
ng build --configuration production

# Output will be in dist/ directory
```

## 📄 Environment Configuration

Create environment files for different stages:

```typescript
// src/environments/environment.ts (Development)
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};

// src/environments/environment.prod.ts (Production)
export const environment = {
  production: true,
  apiUrl: 'https://your-production-api.com/api'
};
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👥 Authors

Developed as part of the Grades Management System project.

## 🐛 Known Issues

- None currently reported

## 📮 Support

For support, please contact the development team or open an issue in the repository.
