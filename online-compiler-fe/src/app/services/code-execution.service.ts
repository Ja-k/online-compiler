import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { catchError, finalize, of } from 'rxjs';

import { environment } from '../../environments/environment';

export interface RunCodeRequest {
  language: string;
  version: string;
  code: string;
}

@Injectable({ providedIn: 'root' })
export class CodeExecutionService {
  private readonly runUrl = `${environment.apiBaseUrl}/v1/run`;

  readonly output = signal<string>('');
  readonly isRunning = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);

  constructor(private readonly http: HttpClient) {}

  runCode(request: RunCodeRequest): void {
    this.isRunning.set(true);
    this.hasError.set(false);
    this.output.set('');

    this.http
      .post(this.runUrl, request, { responseType: 'text' })
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.hasError.set(true);
          return of(this.extractErrorMessage(error));
        }),
        finalize(() => this.isRunning.set(false))
      )
      .subscribe((result) => this.output.set(result));
  }

  private extractErrorMessage(error: HttpErrorResponse): string {
    // Most /v1/run errors are plain text (see CodeExecutionController), but
    // the rate limiter returns a structured {code, message} JSON body like
    // the auth endpoints do. Since this request uses responseType: 'text',
    // Angular never parses the body for us even on error - it stays a raw
    // string - so a JSON body arrives here as literal '{"code":...}' text
    // that needs parsing ourselves.
    if (typeof error.error === 'string' && error.error.trim().length > 0) {
      const jsonMessage = this.tryExtractJsonMessage(error.error);
      return jsonMessage ?? error.error;
    }
    if (error.status === 0) {
      return 'Could not reach the compiler service. Please make sure the backend is running.';
    }
    return `Execution failed (HTTP ${error.status}). Please try again.`;
  }

  private tryExtractJsonMessage(rawBody: string): string | null {
    const trimmed = rawBody.trim();
    if (!trimmed.startsWith('{')) {
      return null;
    }
    try {
      const parsed = JSON.parse(trimmed) as { message?: string };
      return typeof parsed.message === 'string' && parsed.message.trim().length > 0 ? parsed.message : null;
    } catch {
      return null;
    }
  }
}
