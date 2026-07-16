import { Component, signal } from '@angular/core';
import { Router } from '@angular/router';

import { SavedFilesService } from '../services/saved-files.service';
import { EditorStateService } from '../services/editor-state.service';
import { SavedFile } from '../models/saved-file';

@Component({
  selector: 'app-files-drawer',
  imports: [],
  templateUrl: './files-drawer.component.html',
  styleUrl: './files-drawer.component.scss'
})
export class FilesDrawerComponent {
  readonly pendingDeleteId = signal<number | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor(
    protected readonly savedFilesService: SavedFilesService,
    private readonly editorState: EditorStateService,
    private readonly router: Router
  ) {}

  close(): void {
    this.savedFilesService.closeDrawer();
  }

  openFile(file: SavedFile): void {
    this.errorMessage.set(null);
    this.savedFilesService.getFile(file.id).subscribe({
      next: (fullFile) => {
        this.editorState.loadFile(fullFile);
        this.close();
        this.router.navigate(['/']);
      },
      error: (error: Error) => this.errorMessage.set(error.message)
    });
  }

  requestDelete(file: SavedFile, event: Event): void {
    event.stopPropagation();
    this.pendingDeleteId.set(file.id);
  }

  cancelDelete(event: Event): void {
    event.stopPropagation();
    this.pendingDeleteId.set(null);
  }

  confirmDelete(file: SavedFile, event: Event): void {
    event.stopPropagation();
    this.errorMessage.set(null);
    this.savedFilesService.deleteFile(file.id).subscribe({
      next: () => {
        if (this.editorState.currentFileId() === file.id) {
          this.editorState.clearFileAssociation();
        }
        this.pendingDeleteId.set(null);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.pendingDeleteId.set(null);
      }
    });
  }
}
