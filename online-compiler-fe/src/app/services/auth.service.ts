import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthUser } from '../models/auth-user';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface MessageResponse {
  message: string;
}

interface ErrorResponseBody {
  code?: string;
  message?: string;
}

/** Thrown for failed auth API calls; `code` lets components branch on specific cases (e.g. EMAIL_NOT_VERIFIED). */
export class AuthApiError extends Error {
  constructor(message: string, readonly code?: string) {
    super(message);
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authUrl = `${environment.apiBaseUrl}/v1/auth`;

  private readonly _currentUser = signal<AuthUser | null>(null);
  /** True once the initial session check (on app startup) has resolved, either way. */
  private readonly _sessionChecked = signal(false);

  readonly currentUser = this._currentUser.asReadonly();
  readonly sessionChecked = this._sessionChecked.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null);

  constructor(private readonly http: HttpClient) {}

  /** Called once on app startup to restore auth state from the session cookie, if any. */
  restoreSession(): Observable<AuthUser | null> {
    return new Observable<AuthUser | null>((subscriber) => {
      this.http.get<AuthUser>(`${this.authUrl}/me`, { withCredentials: true }).subscribe({
        next: (user) => {
          this._currentUser.set(user);
          this._sessionChecked.set(true);
          subscriber.next(user);
          subscriber.complete();
        },
        error: () => {
          this._currentUser.set(null);
          this._sessionChecked.set(true);
          subscriber.next(null);
          subscriber.complete();
        }
      });
    });
  }

  /** Registration no longer logs the user in: they must verify their email first. */
  register(request: RegisterRequest): Observable<MessageResponse> {
    return this.http
      .post<MessageResponse>(`${this.authUrl}/register`, request, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.toAuthApiError(error))));
  }

  login(request: LoginRequest): Observable<AuthUser> {
    return this.http
      .post<AuthUser>(`${this.authUrl}/login`, request, { withCredentials: true })
      .pipe(
        tap((user) => this._currentUser.set(user)),
        catchError((error: HttpErrorResponse) => throwError(() => this.toAuthApiError(error)))
      );
  }

  verifyEmail(token: string): Observable<MessageResponse> {
    return this.http
      .post<MessageResponse>(`${this.authUrl}/verify-email`, { token }, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.toAuthApiError(error))));
  }

  resendVerification(usernameOrEmail: string): Observable<MessageResponse> {
    return this.http
      .post<MessageResponse>(`${this.authUrl}/resend-verification`, { usernameOrEmail }, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.toAuthApiError(error))));
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.authUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => this._currentUser.set(null)),
      catchError(() => {
        // Even if the server call fails, drop the client-side session state.
        this._currentUser.set(null);
        return throwError(() => new Error('Logout failed, but you have been signed out locally.'));
      })
    );
  }

  private toAuthApiError(error: HttpErrorResponse): AuthApiError {
    const body = error.error as ErrorResponseBody | string | null;

    if (body && typeof body === 'object' && typeof body.message === 'string' && body.message.trim().length > 0) {
      return new AuthApiError(body.message, body.code);
    }
    if (typeof body === 'string' && body.trim().length > 0) {
      return new AuthApiError(body);
    }
    if (error.status === 0) {
      return new AuthApiError('Could not reach the server. Please make sure the backend is running.');
    }
    return new AuthApiError(`Request failed (HTTP ${error.status}). Please try again.`);
  }
}
