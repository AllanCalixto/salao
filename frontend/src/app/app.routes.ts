import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'agendar',
    loadComponent: () =>
      import('./features/agendamento-publico/agendamento-publico.component').then(
        (m) => m.AgendamentoPublicoComponent
      ),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./layouts/auth/auth-layout.component').then((m) => m.AuthLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/auth/login/login.component').then((m) => m.LoginComponent),
      },
    ],
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layouts/admin/admin-layout.component').then((m) => m.AdminLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'clientes',
        loadComponent: () =>
          import('./features/clientes/cliente-list/cliente-list.component').then(
            (m) => m.ClienteListComponent
          ),
      },
      {
        path: 'clientes/novo',
        loadComponent: () =>
          import('./features/clientes/cliente-form/cliente-form.component').then(
            (m) => m.ClienteFormComponent
          ),
      },
      {
        path: 'clientes/:id',
        loadComponent: () =>
          import('./features/clientes/cliente-form/cliente-form.component').then(
            (m) => m.ClienteFormComponent
          ),
      },
      {
        path: 'profissionais',
        loadComponent: () =>
          import('./features/profissionais/profissional-list/profissional-list.component').then(
            (m) => m.ProfissionalListComponent
          ),
      },
      {
        path: 'profissionais/novo',
        loadComponent: () =>
          import('./features/profissionais/profissional-form/profissional-form.component').then(
            (m) => m.ProfissionalFormComponent
          ),
      },
      {
        path: 'profissionais/:id',
        loadComponent: () =>
          import('./features/profissionais/profissional-form/profissional-form.component').then(
            (m) => m.ProfissionalFormComponent
          ),
      },
      {
        path: 'atendimentos',
        loadComponent: () =>
          import('./features/atendimentos/atendimento-list/atendimento-list.component').then(
            (m) => m.AtendimentoListComponent
          ),
      },
      {
        path: 'atendimentos/novo',
        loadComponent: () =>
          import('./features/atendimentos/atendimento-form/atendimento-form.component').then(
            (m) => m.AtendimentoFormComponent
          ),
      },
      {
        path: 'atendimentos/:id',
        loadComponent: () =>
          import('./features/atendimentos/atendimento-form/atendimento-form.component').then(
            (m) => m.AtendimentoFormComponent
          ),
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'agendar',
  },
];