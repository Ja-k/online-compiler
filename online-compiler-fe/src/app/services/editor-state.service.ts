import { Injectable, signal } from '@angular/core';

import { LANGUAGE_OPTIONS, LanguageOption } from '../models/language-option';
import { SavedFile } from '../models/saved-file';

/**
 * Single source of truth for "what's currently in the editor": the selected
 * language/version/code, and which saved file (if any) it corresponds to.
 * Shared between the editor toolbar, the save dialog, and the files drawer.
 */
@Injectable({ providedIn: 'root' })
export class EditorStateService {
  readonly languages: readonly LanguageOption[] = LANGUAGE_OPTIONS;

  readonly selectedLanguage = signal<LanguageOption>(this.languages[0]);
  readonly selectedVersion = signal<string>(this.languages[0].defaultVersion);
  readonly code = signal<string>(this.languages[0].defaultCode);

  readonly currentFileId = signal<number | null>(null);
  readonly currentFileName = signal<string | null>(null);

  setLanguage(language: LanguageOption): void {
    this.selectedLanguage.set(language);
    this.selectedVersion.set(language.defaultVersion);
    this.code.set(language.defaultCode);
    // Switching languages starts a fresh, unsaved buffer.
    this.currentFileId.set(null);
    this.currentFileName.set(null);
  }

  loadFile(file: SavedFile): void {
    const language = this.languages.find((option) => option.id === file.language) ?? this.languages[0];
    this.selectedLanguage.set(language);
    this.selectedVersion.set(file.version);
    this.code.set(file.code ?? '');
    this.currentFileId.set(file.id);
    this.currentFileName.set(file.filename);
  }

  markSavedAs(id: number, filename: string): void {
    this.currentFileId.set(id);
    this.currentFileName.set(filename);
  }

  clearFileAssociation(): void {
    this.currentFileId.set(null);
    this.currentFileName.set(null);
  }
}
