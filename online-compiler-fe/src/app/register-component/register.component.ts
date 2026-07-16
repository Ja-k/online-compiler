import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthApiError, AuthService } from '../services/auth.service';

const MIN_PASSWORD_LENGTH = 8;

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  username = '';
  email = '';
  password = '';
  confirmPassword = '';

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly registeredEmail = signal<string | null>(null);

  readonly isResending = signal(false);
  readonly resendMessage = signal<string | null>(null);

  constructor(private readonly authService: AuthService) {}

  onSubmit(): void {
    const username = this.username.trim();
    const email = this.email.trim();

    if (!username || !email || !this.password) {
      this.errorMessage.set('Please fill in all fields.');
      return;
    }
    if (this.password.length < MIN_PASSWORD_LENGTH) {
      this.errorMessage.set(`Password must be at least ${MIN_PASSWORD_LENGTH} characters.`);
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage.set('Passwords do not match.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService.register({ username, email, password: this.password }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.registeredEmail.set(email);
      },
      error: (error: AuthApiError) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(error.message);
      }
    });
  }

  onResendClick(): void {
    const email = this.registeredEmail();
    if (!email || this.isResending()) {
      return;
    }

    this.isResending.set(true);
    this.resendMessage.set(null);

    this.authService.resendVerification(email).subscribe({
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
