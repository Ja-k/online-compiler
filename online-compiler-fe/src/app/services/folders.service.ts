import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { Folder } from '../models/folder';

export interface FolderRequest {
  name: string;
}

@Injectable({ providedIn: 'root' })
export class FoldersService {
  private readonly foldersUrl = `${environment.apiBaseUrl}/v1/folders`;

  private readonly _folders = signal<Folder[]>([]);
  private readonly _isLoading = signal(false);

  readonly folders = this._folders.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();

  constructor(private readonly http: HttpClient) {}

  clear(): void {
    this._folders.set([]);
  }

  refresh(): void {
    this._isLoading.set(true);
    this.http
      .get<Folder[]>(this.foldersUrl, { withCredentials: true })
      .pipe(catchError(() => throwError(() => new Error('Could not load your folders.'))))
      .subscribe({
        next: (folders) => {
          this._folders.set(folders);
          this._isLoading.set(false);
        },
        error: () => this._isLoading.set(false)
      });
  }

  createFolder(request: FolderRequest): Observable<Folder> {
    return this.http.post<Folder>(this.foldersUrl, request, { withCredentials: true }).pipe(
      tap((folder) => {
        const next = [...this._folders(), folder].sort((a, b) =>
          a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })
        );
        this._folders.set(next);
      }),
      catchError((error: HttpErrorResponse) => throwError(() => new Error(this.extractErrorMessage(error))))
    );
  }

  renameFolder(id: number, request: FolderRequest): Observable<Folder> {
    return this.http.put<Folder>(`${this.foldersUrl}/${id}`, request, { withCredentials: true }).pipe(
      tap((folder) => {
        const next = this._folders()
          .map((f) => (f.id === id ? folder : f))
          .sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }));
        this._folders.set(next);
      }),
      catchError((error: HttpErrorResponse) => throwError(() => new Error(this.extractErrorMessage(error))))
    );
  }

  deleteFolder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.foldersUrl}/${id}`, { withCredentials: true }).pipe(
      tap(() => this._folders.set(this._folders().filter((f) => f.id !== id))),
      catchError((error: HttpErrorResponse) => throwError(() => new Error(this.extractErrorMessage(error))))
    );
  }

  private extractErrorMessage(error: HttpErrorResponse): string {
    if (typeof error.error === 'string' && error.error.trim().length > 0) {
      return error.error;
    }
    if (error.status === 0) {
      return 'Could not reach the server. Please make sure the backend is running.';
    }
    return `Request failed (HTTP ${error.status}). Please try again.`;
  }
}
