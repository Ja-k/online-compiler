import { Component, effect, signal } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { CodeExecutionService } from '../services/code-execution.service';
import { EditorStateService } from '../services/editor-state.service';
import { AuthService } from '../services/auth.service';
import { SavedFilesService } from '../services/saved-files.service';
import { LanguageOption } from '../models/language-option';
import { TECH_VIBE_THEME_NAME } from '../monaco-theme';

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
  readonly saveError = signal<string | null>(null);
  readonly isSaving = signal(false);

  constructor(
    private readonly codeExecutionService: CodeExecutionService,
    protected readonly editorState: EditorStateService,
    protected readonly authService: AuthService,
    private readonly savedFilesService: SavedFilesService,
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
    this.editorState.selectedVersion.set(version);
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
      this.router.navigate(['/login']);
      return;
    }
    this.saveFilenameInput = this.editorState.currentFileName() ?? '';
    this.saveError.set(null);
    this.isSaveDialogOpen.set(true);
  }

  closeSaveDialog(): void {
    this.isSaveDialogOpen.set(false);
  }

  confirmSave(): void {
    const filename = this.saveFilenameInput.trim();
    if (!filename) {
      this.saveError.set('Please enter a file name.');
      return;
    }

    this.isSaving.set(true);
    this.saveError.set(null);

    const request = {
      filename,
      language: this.selectedLanguage.id,
      version: this.selectedVersion,
      code: this.code
    };

    const currentId = this.editorState.currentFileId();
    const isUpdatingSameFile = currentId !== null && filename === this.editorState.currentFileName();

    const save$ = isUpdatingSameFile
      ? this.savedFilesService.updateFile(currentId, request)
      : this.savedFilesService.createFile(request);

    save$.subscribe({
      next: (file) => {
        this.editorState.markSavedAs(file.id, file.filename);
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
