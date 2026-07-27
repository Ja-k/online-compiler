import { Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { SavedFilesService } from '../services/saved-files.service';
import { FoldersService } from '../services/folders.service';
import { EditorStateService } from '../services/editor-state.service';
import { SavedFile } from '../models/saved-file';
import { Folder } from '../models/folder';
import { ensureFileExtension, findLanguageOption } from '../models/language-option';
import { downloadTextFile } from '../utils/download-text-file';

@Component({
  selector: 'app-files-drawer',
  imports: [FormsModule],
  templateUrl: './files-drawer.component.html',
  styleUrl: './files-drawer.component.scss'
})
export class FilesDrawerComponent {
  /** null = browsing the root (folders + unfiled files). */
  readonly currentFolderId = signal<number | null>(null);

  readonly pendingDeleteFileId = signal<number | null>(null);
  readonly pendingDeleteFolderId = signal<number | null>(null);

  readonly isCreatingFolder = signal(false);
  newFolderName = '';

  readonly renamingFolderId = signal<number | null>(null);
  renameFolderInput = '';

  readonly errorMessage = signal<string | null>(null);
  readonly downloadingFileId = signal<number | null>(null);

  readonly currentFolder = computed(() => {
    const id = this.currentFolderId();
    if (id === null) {
      return null;
    }
    return this.foldersService.folders().find((folder) => folder.id === id) ?? null;
  });

  readonly visibleFolders = computed(() => {
    // Single-level folders: only shown at root.
    return this.currentFolderId() === null ? this.foldersService.folders() : [];
  });

  readonly visibleFiles = computed(() => {
    const folderId = this.currentFolderId();
    return this.savedFilesService.files().filter((file) =>
      folderId === null ? file.folderId === null : file.folderId === folderId
    );
  });

  readonly isEmpty = computed(() => {
    return this.visibleFolders().length === 0 && this.visibleFiles().length === 0;
  });

  constructor(
    protected readonly savedFilesService: SavedFilesService,
    protected readonly foldersService: FoldersService,
    private readonly editorState: EditorStateService,
    private readonly router: Router
  ) {}

  close(): void {
    this.resetTransientUi();
    this.currentFolderId.set(null);
    this.savedFilesService.closeDrawer();
  }

  goToRoot(): void {
    this.resetTransientUi();
    this.currentFolderId.set(null);
  }

  openFolder(folder: Folder): void {
    this.resetTransientUi();
    this.currentFolderId.set(folder.id);
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

  downloadFile(file: SavedFile, event: Event): void {
    event.stopPropagation();
    this.errorMessage.set(null);
    this.downloadingFileId.set(file.id);

    // List responses omit `code` to keep the payload light, so the full
    // file (with code) is always fetched fresh rather than trusting
    // whatever happens to already be in the local signal.
    this.savedFilesService.getFile(file.id).subscribe({
      next: (fullFile) => {
        const language = findLanguageOption(fullFile.language);
        const filename = ensureFileExtension(fullFile.filename, language);
        downloadTextFile(filename, fullFile.code ?? '');
        this.downloadingFileId.set(null);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.downloadingFileId.set(null);
      }
    });
  }

  startCreateFolder(): void {
    this.resetTransientUi();
    this.isCreatingFolder.set(true);
    this.newFolderName = '';
  }

  cancelCreateFolder(): void {
    this.isCreatingFolder.set(false);
    this.newFolderName = '';
  }

  confirmCreateFolder(): void {
    const name = this.newFolderName.trim();
    if (!name) {
      this.errorMessage.set('Please enter a folder name.');
      return;
    }

    this.errorMessage.set(null);
    this.foldersService.createFolder({ name }).subscribe({
      next: () => {
        this.isCreatingFolder.set(false);
        this.newFolderName = '';
      },
      error: (error: Error) => this.errorMessage.set(error.message)
    });
  }

  startRenameFolder(folder: Folder, event: Event): void {
    event.stopPropagation();
    this.resetTransientUi();
    this.renamingFolderId.set(folder.id);
    this.renameFolderInput = folder.name;
  }

  cancelRenameFolder(event: Event): void {
    event.stopPropagation();
    this.renamingFolderId.set(null);
    this.renameFolderInput = '';
  }

  confirmRenameFolder(folder: Folder, event: Event): void {
    event.stopPropagation();
    const name = this.renameFolderInput.trim();
    if (!name) {
      this.errorMessage.set('Please enter a folder name.');
      return;
    }

    this.errorMessage.set(null);
    this.foldersService.renameFolder(folder.id, { name }).subscribe({
      next: () => {
        this.renamingFolderId.set(null);
        this.renameFolderInput = '';
      },
      error: (error: Error) => this.errorMessage.set(error.message)
    });
  }

  requestDeleteFolder(folder: Folder, event: Event): void {
    event.stopPropagation();
    this.pendingDeleteFileId.set(null);
    this.pendingDeleteFolderId.set(folder.id);
  }

  cancelDeleteFolder(event: Event): void {
    event.stopPropagation();
    this.pendingDeleteFolderId.set(null);
  }

  confirmDeleteFolder(folder: Folder, event: Event): void {
    event.stopPropagation();
    this.errorMessage.set(null);
    this.foldersService.deleteFolder(folder.id).subscribe({
      next: () => {
        this.savedFilesService.removeFilesInFolder(folder.id);
        if (this.editorState.currentFolderId() === folder.id) {
          this.editorState.clearFileAssociation();
        }
        if (this.currentFolderId() === folder.id) {
          this.currentFolderId.set(null);
        }
        this.pendingDeleteFolderId.set(null);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.pendingDeleteFolderId.set(null);
      }
    });
  }

  requestDeleteFile(file: SavedFile, event: Event): void {
    event.stopPropagation();
    this.pendingDeleteFolderId.set(null);
    this.pendingDeleteFileId.set(file.id);
  }

  cancelDeleteFile(event: Event): void {
    event.stopPropagation();
    this.pendingDeleteFileId.set(null);
  }

  confirmDeleteFile(file: SavedFile, event: Event): void {
    event.stopPropagation();
    this.errorMessage.set(null);
    this.savedFilesService.deleteFile(file.id).subscribe({
      next: () => {
        if (this.editorState.currentFileId() === file.id) {
          this.editorState.clearFileAssociation();
        }
        this.pendingDeleteFileId.set(null);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.pendingDeleteFileId.set(null);
      }
    });
  }

  private resetTransientUi(): void {
    this.errorMessage.set(null);
    this.downloadingFileId.set(null);
    this.pendingDeleteFileId.set(null);
    this.pendingDeleteFolderId.set(null);
    this.isCreatingFolder.set(false);
    this.newFolderName = '';
    this.renamingFolderId.set(null);
    this.renameFolderInput = '';
  }
}
