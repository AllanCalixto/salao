import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, throwError, catchError } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly TOKEN_KEY = 'access_token';
  private readonly EXPIRES_KEY = 'expires_at';

  private readonly _isAuthenticated = signal<boolean>(this.hasToken());

  readonly isAuthenticated = computed(() => this._isAuthenticated());

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/login', credentials).pipe(
      tap((response) => this.handleLoginSuccess(response)),
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.EXPIRES_KEY);
    this._isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isTokenExpired(): boolean {
    const expiresAt = localStorage.getItem(this.EXPIRES_KEY);
    if (!expiresAt) return true;
    return Date.now() > parseInt(expiresAt, 10);
  }

  private hasToken(): boolean {
    const token = this.getToken();
    if (!token) return false;
    if (this.isTokenExpired()) {
      this.logout();
      return false;
    }
    return true;
  }

  private handleLoginSuccess(response: LoginResponse): void {
    const expiresAt = Date.now() + response.expiresIn * 1000;
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.EXPIRES_KEY, expiresAt.toString());
    this._isAuthenticated.set(true);
  }
}