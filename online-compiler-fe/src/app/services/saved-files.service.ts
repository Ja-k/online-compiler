import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { SavedFile } from '../models/saved-file';

export interface SaveFileRequest {
  filename: string;
  language: string;
  version: string;
  code: string;
  folderId: number | null;
}

@Injectable({ providedIn: 'root' })
export class SavedFilesService {
  private readonly filesUrl = `${environment.apiBaseUrl}/v1/files`;

  private readonly _files = signal<SavedFile[]>([]);
  private readonly _isLoading = signal(false);
  private readonly _isDrawerOpen = signal(false);

  readonly files = this._files.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly isDrawerOpen = this._isDrawerOpen.asReadonly();

  constructor(private readonly http: HttpClient) {}

  openDrawer(): void {
    this._isDrawerOpen.set(true);
    this.refresh();
  }

  closeDrawer(): void {
    this._isDrawerOpen.set(false);
  }

  clear(): void {
    this._files.set([]);
  }

  refresh(): void {
    this._isLoading.set(true);
    this.http
      .get<SavedFile[]>(this.filesUrl, { withCredentials: true })
      .pipe(catchError(() => throwError(() => new Error('Could not load your saved files.'))))
      .subscribe({
        next: (files) => {
          this._files.set(files);
          this._isLoading.set(false);
        },
        error: () => this._isLoading.set(false)
      });
  }

  /** Removes every local file that belonged to a deleted folder. */
  removeFilesInFolder(folderId: number): void {
    this._files.set(this._files().filter((f) => f.folderId !== folderId));
  }

  getFile(id: number): Observable<SavedFile> {
    return this.http.get<SavedFile>(`${this.filesUrl}/${id}`, { withCredentials: true }).pipe(
      catchError((error: HttpErrorResponse) => throwError(() => new Error(this.extractErrorMessage(error))))
    );
  }

  createFile(request: SaveFileRequest): Observable<SavedFile> {
    return this.http.post<SavedFile>(this.filesUrl, request, { withCredentials: true }).pipe(
      tap((file) => this._files.set([file, ...this._files()])),
      catchError((error: HttpErrorResponse) => throwError(() => new Error(this.extractErrorMessage(error))))
    );
  }

  updateFile(id: number, request: SaveFileRequest): Observable<SavedFile> {
    return this.http.put<SavedFile>(`${this.filesUrl}/${id}`, request, { withCredentials: true }).pipe(
      tap((file) => this._files.set(this._files().map((f) => (f.id === id ? file : f)))),
      catchError((error: HttpErrorResponse) => throwError(() => new Error(this.extractErrorMessage(error))))
    );
  }

  deleteFile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.filesUrl}/${id}`, { withCredentials: true }).pipe(
      tap(() => this._files.set(this._files().filter((f) => f.id !== id))),
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
