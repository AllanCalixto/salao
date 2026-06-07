import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
  ],
  template: `
    <mat-toolbar color="primary" class="app-toolbar">
      <button mat-icon-button (click)="toggleSidebar()" class="menu-button">
        <mat-icon>menu</mat-icon>
      </button>
      <span class="app-title">Salão de Beleza</span>
      <span class="spacer"></span>
      <button mat-icon-button [matMenuTriggerFor]="menu">
        <mat-icon>account_circle</mat-icon>
      </button>
      <mat-menu #menu="matMenu">
        <button mat-menu-item (click)="logout()">
          <mat-icon>exit_to_app</mat-icon>
          <span>Sair</span>
        </button>
      </mat-menu>
    </mat-toolbar>

    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav
        #sidenav
        [opened]="sidebarOpen()"
        mode="side"
        class="app-sidenav"
      >
        <mat-nav-list>
          <a
            mat-list-item
            routerLink="/dashboard"
            routerLinkActive="active-link"
            (click)="closeOnMobile()"
          >
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          <a
            mat-list-item
            routerLink="/clientes"
            routerLinkActive="active-link"
            (click)="closeOnMobile()"
          >
            <mat-icon matListItemIcon>people</mat-icon>
            <span matListItemTitle>Clientes</span>
          </a>
          <a
            mat-list-item
            routerLink="/profissionais"
            routerLinkActive="active-link"
            (click)="closeOnMobile()"
          >
            <mat-icon matListItemIcon>badge</mat-icon>
            <span matListItemTitle>Profissionais</span>
          </a>
          <a
            mat-list-item
            routerLink="/atendimentos"
            routerLinkActive="active-link"
            (click)="closeOnMobile()"
          >
            <mat-icon matListItemIcon>calendar_today</mat-icon>
            <span matListItemTitle>Atendimentos</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>

      <mat-sidenav-content class="app-content">
        <router-outlet></router-outlet>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [
    `
      .app-toolbar {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 1000;
      }

      .app-title {
        margin-left: 8px;
        font-weight: 500;
      }

      .spacer {
        flex: 1 1 auto;
      }

      .sidenav-container {
        position: fixed;
        top: 64px;
        left: 0;
        right: 0;
        bottom: 0;
      }

      .app-sidenav {
        width: 250px;
        background: #fafafa;
        border-right: 1px solid #e0e0e0;
      }

      .app-content {
        padding: 24px;
        background: #f5f5f5;
        overflow-y: auto;
      }

      .active-link {
        background: rgba(63, 81, 181, 0.1) !important;
        color: var(--primary-color) !important;
        border-left: 3px solid var(--primary-color);
      }

      .active-link mat-icon {
        color: var(--primary-color);
      }

      @media (max-width: 768px) {
        .app-sidenav {
          width: 200px;
        }

        .app-content {
          padding: 16px;
        }
      }
    `,
  ],
})
export class AdminLayoutComponent {
  private authService = inject(AuthService);
  sidebarOpen = signal(true);

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  closeOnMobile(): void {
    if (window.innerWidth <= 768) {
      this.sidebarOpen.set(false);
    }
  }

  logout(): void {
    this.authService.logout();
  }
}