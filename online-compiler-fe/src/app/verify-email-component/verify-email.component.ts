import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthApiError, AuthService } from '../services/auth.service';

type VerificationStatus = 'verifying' | 'success' | 'error';

@Component({
  selector: 'app-verify-email',
  imports: [RouterLink],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.scss'
})
export class VerifyEmailComponent implements OnInit {
  readonly status = signal<VerificationStatus>('verifying');
  readonly message = signal<string>('Verifying your email…');

  constructor(private readonly route: ActivatedRoute, private readonly authService: AuthService) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.status.set('error');
      this.message.set('This verification link is missing its token.');
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: (response) => {
        this.status.set('success');
        this.message.set(response.message);
      },
      error: (error: AuthApiError) => {
        this.status.set('error');
        this.message.set(error.message);
      }
    });
  }
}
