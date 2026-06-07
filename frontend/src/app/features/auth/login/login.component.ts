import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="login-content">
      <div class="login-header">
        <mat-icon class="login-icon">spa</mat-icon>
        <h1>Salão de Beleza</h1>
        <p class="subtitle">Faça login para continuar</p>
      </div>

      <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="login-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Usuário</mat-label>
          <input matInput formControlName="login" placeholder="Digite seu usuário" autocomplete="username" />
          <mat-icon matPrefix>person</mat-icon>
          @if (loginForm.get('login')?.hasError('required') && loginForm.get('login')?.touched) {
            <mat-error>Usuário é obrigatório</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Senha</mat-label>
          <input matInput [type]="hidePassword ? 'password' : 'text'" formControlName="senha" placeholder="Digite sua senha" autocomplete="current-password" />
          <mat-icon matPrefix>lock</mat-icon>
          <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
            <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          @if (loginForm.get('senha')?.hasError('required') && loginForm.get('senha')?.touched) {
            <mat-error>Senha é obrigatória</mat-error>
          }
        </mat-form-field>

        @if (errorMessage) {
          <div class="error-message">
            <mat-icon>error</mat-icon>
            <span>{{ errorMessage }}</span>
          </div>
        }

        <button mat-raised-button color="primary" type="submit" class="full-width" [disabled]="loading">
          @if (loading) {
            <mat-spinner diameter="20" class="button-spinner"></mat-spinner>
          } @else {
            <span>Entrar</span>
          }
        </button>
      </form>
    </div>
  `,
  styles: [
    `
      .login-content {
        text-align: center;
      }

      .login-header {
        margin-bottom: 32px;
      }

      .login-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: var(--primary-color);
        margin-bottom: 8px;
      }

      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 500;
        color: #333;
      }

      .subtitle {
        color: #666;
        margin-top: 8px;
      }

      .login-form {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      .error-message {
        display: flex;
        align-items: center;
        gap: 8px;
        background: #fce4ec;
        color: #c62828;
        padding: 12px 16px;
        border-radius: 8px;
        font-size: 14px;

        mat-icon {
          font-size: 20px;
          width: 20px;
          height: 20px;
        }
      }

      .button-spinner {
        display: inline-block;
        margin: 0 auto;
      }

      button[type='submit'] {
        height: 48px;
        font-size: 16px;
      }
    `,
  ],
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  hidePassword = true;
  loading = false;
  errorMessage = '';

  loginForm = this.fb.nonNullable.group({
    login: ['', Validators.required],
    senha: ['', Validators.required],
  });

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 400 || err.status === 401) {
          this.errorMessage = 'Usuário ou senha inválidos';
        } else {
          this.errorMessage = 'Erro ao conectar ao servidor. Tente novamente.';
        }
      },
    });
  }
}