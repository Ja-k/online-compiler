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
    if (typeof error.error === 'string' && error.error.trim().length > 0) {
      return error.error;
    }
    if (error.status === 0) {
      return 'Could not reach the compiler service. Please make sure the backend is running.';
    }
    return `Execution failed (HTTP ${error.status}). Please try again.`;
  }
}
