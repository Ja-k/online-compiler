import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthApiError, AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username = '';
  password = '';

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly needsVerification = signal(false);

  readonly isResending = signal(false);
  readonly resendMessage = signal<string | null>(null);

  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  onSubmit(): void {
    if (!this.username.trim() || !this.password) {
      this.errorMessage.set('Please enter your username and password.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.needsVerification.set(false);
    this.resendMessage.set(null);

    this.authService.login({ username: this.username.trim(), password: this.password }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigate(['/']);
      },
      error: (error: AuthApiError) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(error.message);
        this.needsVerification.set(error.code === 'EMAIL_NOT_VERIFIED');
      }
    });
  }

  onResendClick(): void {
    const usernameOrEmail = this.username.trim();
    if (!usernameOrEmail || this.isResending()) {
      return;
    }

    this.isResending.set(true);
    this.resendMessage.set(null);

    this.authService.resendVerification(usernameOrEmail).subscribe({
      next: (response) => {
        this.isResending.set(false);
        this.resendMessage.set(response.message);
      },
      error: (error: AuthApiError) => {
        this.isResending.set(false);
        this.resendMessage.set(error.message);
      }
    });
  }
}
