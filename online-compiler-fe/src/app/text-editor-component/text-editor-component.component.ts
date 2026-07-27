import { Component, effect, signal } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { CodeExecutionService } from '../services/code-execution.service';
import { EditorStateService } from '../services/editor-state.service';
import { AuthApiError, AuthService } from '../services/auth.service';
import { SavedFilesService } from '../services/saved-files.service';
import { FoldersService } from '../services/folders.service';
import { ensureFileExtension, LanguageOption } from '../models/language-option';
import { TECH_VIBE_THEME_NAME } from '../monaco-theme';
import { downloadTextFile } from '../utils/download-text-file';

@Component({
  selector: 'app-text-editor-component',
  imports: [MonacoEditorModule, FormsModule],
  templateUrl: './text-editor-component.component.html',
  styleUrl: './text-editor-component.component.scss',
  standalone: true
})
export class TextEditorComponentComponent {
  // automaticLayout makes Monaco re-measure its container via an internal
  // ResizeObserver, so it stays correctly sized when the split-pane separator
  // is dragged (a container resize isn't a window resize, which is all
  // ngx-monaco-editor-v2 listens for on its own).
  editorOptions: { theme: string; language: string; automaticLayout: boolean };

  readonly isSaveDialogOpen = signal(false);
  saveFilenameInput = '';
  /** null = save at root; otherwise the selected folder id. Bound as string for the select. */
  saveFolderIdValue: string = '';
  readonly saveError = signal<string | null>(null);
  readonly isSaving = signal(false);

  readonly isLoginDialogOpen = signal(false);
  loginUsername = '';
  loginPassword = '';
  readonly isLoggingIn = signal(false);
  readonly loginError = signal<string | null>(null);
  readonly needsVerification = signal(false);
  readonly isResending = signal(false);
  readonly resendMessage = signal<string | null>(null);

  constructor(
    private readonly codeExecutionService: CodeExecutionService,
    protected readonly editorState: EditorStateService,
    protected readonly authService: AuthService,
    private readonly savedFilesService: SavedFilesService,
    protected readonly foldersService: FoldersService,
    private readonly router: Router
  ) {
    this.editorOptions = {
      theme: TECH_VIBE_THEME_NAME,
      language: this.editorState.selectedLanguage().monacoLanguage,
      automaticLayout: true
    };

    // Keeps Monaco's tokenizer in sync with the selected language no matter
    // where the change comes from (the dropdown, or loading a saved file via
    // the drawer, which updates editorState directly without going through
    // the dropdown's setter below).
    effect(() => {
      const language = this.editorState.selectedLanguage().monacoLanguage;
      this.editorOptions = { ...this.editorOptions, language };
    });
  }

  get languages(): readonly LanguageOption[] {
    return this.editorState.languages;
  }

  get isRunning(): boolean {
    return this.codeExecutionService.isRunning();
  }

  get selectedLanguage(): LanguageOption {
    return this.editorState.selectedLanguage();
  }

  set selectedLanguage(language: LanguageOption) {
    // editorOptions.language is kept in sync by the effect() in the constructor.
    this.editorState.setLanguage(language);
  }

  get selectedVersion(): string {
    return this.editorState.selectedVersion();
  }

  set selectedVersion(version: string) {
    this.editorState.setVersion(version);
  }

  get code(): string {
    return this.editorState.code();
  }

  set code(value: string) {
    this.editorState.code.set(value);
  }

  get currentFileName(): string | null {
    return this.editorState.currentFileName();
  }

  closeOpenFile(): void {
    this.editorState.closeOpenFile();
  }

  /**
   * Downloads whatever is currently in the editor, entirely client-side, so
   * this works even for unsaved code or while logged out. Uses the open
   * file's name if there is one, otherwise a generic "Main.<ext>" name.
   */
  onDownloadClick(): void {
    const rawFilename = this.currentFileName ?? `Main.${this.selectedLanguage.fileExtension}`;
    const filename = ensureFileExtension(rawFilename, this.selectedLanguage);
    downloadTextFile(filename, this.code);
  }

  runCode(): void {
    if (this.isRunning || !this.code.trim()) {
      return;
    }

    this.codeExecutionService.runCode({
      language: this.selectedLanguage.id,
      version: this.selectedVersion,
      code: this.code
    });
  }

  onSaveClick(): void {
    if (!this.authService.isAuthenticated()) {
      this.openLoginDialog();
      return;
    }
    this.openSaveDialog();
  }

  openSaveDialog(): void {
    this.saveFilenameInput = this.editorState.currentFileName() ?? '';
    const folderId = this.editorState.currentFolderId();
    this.saveFolderIdValue = folderId === null ? '' : String(folderId);
    this.saveError.set(null);
    this.foldersService.refresh();
    this.isSaveDialogOpen.set(true);
  }

  closeSaveDialog(): void {
    this.isSaveDialogOpen.set(false);
  }

  openLoginDialog(): void {
    this.loginUsername = '';
    this.loginPassword = '';
    this.loginError.set(null);
    this.needsVerification.set(false);
    this.resendMessage.set(null);
    this.isLoginDialogOpen.set(true);
  }

  closeLoginDialog(): void {
    this.isLoginDialogOpen.set(false);
  }

  onLoginSubmit(): void {
    if (!this.loginUsername.trim() || !this.loginPassword) {
      this.loginError.set('Please enter your username and password.');
      return;
    }

    this.isLoggingIn.set(true);
    this.loginError.set(null);
    this.needsVerification.set(false);
    this.resendMessage.set(null);

    this.authService.login({ username: this.loginUsername.trim(), password: this.loginPassword }).subscribe({
      next: () => {
        this.isLoggingIn.set(false);
        this.isLoginDialogOpen.set(false);
        // Continue the save flow the user started before being asked to log in.
        this.openSaveDialog();
      },
      error: (error: AuthApiError) => {
        this.isLoggingIn.set(false);
        this.loginError.set(error.message);
        this.needsVerification.set(error.code === 'EMAIL_NOT_VERIFIED');
      }
    });
  }

  onResendVerificationClick(): void {
    const usernameOrEmail = this.loginUsername.trim();
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

  goToRegister(): void {
    this.closeLoginDialog();
    this.router.navigate(['/register']);
  }

  confirmSave(): void {
    const filename = this.saveFilenameInput.trim();
    if (!filename) {
      this.saveError.set('Please enter a file name.');
      return;
    }

    this.isSaving.set(true);
    this.saveError.set(null);

    const folderId = this.saveFolderIdValue === '' ? null : Number(this.saveFolderIdValue);
    const request = {
      filename,
      language: this.selectedLanguage.id,
      version: this.selectedVersion,
      code: this.code,
      folderId
    };

    const currentId = this.editorState.currentFileId();
    const isUpdatingSameFile = currentId !== null && filename === this.editorState.currentFileName();

    const save$ = isUpdatingSameFile
      ? this.savedFilesService.updateFile(currentId, request)
      : this.savedFilesService.createFile(request);

    save$.subscribe({
      next: (file) => {
        this.editorState.markSavedAs(file.id, file.filename, file.folderId);
        this.isSaving.set(false);
        this.isSaveDialogOpen.set(false);
      },
      error: (error: Error) => {
        this.isSaving.set(false);
        this.saveError.set(error.message);
      }
    });
  }
}
