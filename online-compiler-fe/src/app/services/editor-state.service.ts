import { Injectable, signal } from '@angular/core';

import { defaultCodeFor, LANGUAGE_OPTIONS, LanguageOption } from '../models/language-option';
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
  readonly code = signal<string>(defaultCodeFor(this.languages[0], this.languages[0].defaultVersion));

  readonly currentFileId = signal<number | null>(null);
  readonly currentFileName = signal<string | null>(null);
  readonly currentFolderId = signal<number | null>(null);

  setLanguage(language: LanguageOption): void {
    this.selectedLanguage.set(language);
    this.selectedVersion.set(language.defaultVersion);
    this.code.set(defaultCodeFor(language, language.defaultVersion));
    // Switching languages starts a fresh, unsaved buffer.
    this.currentFileId.set(null);
    this.currentFileName.set(null);
    this.currentFolderId.set(null);
  }

  /**
   * Changes the selected version. If the editor still holds the unmodified
   * starter code for the current version (i.e. the user hasn't typed
   * anything of their own), it's swapped for that version's starter code so
   * e.g. picking Java 21 shows the simplified instance-main template instead
   * of leaving the Java 17-style boilerplate in place.
   */
  setVersion(version: string): void {
    const language = this.selectedLanguage();
    const previousDefault = defaultCodeFor(language, this.selectedVersion());
    const isUnmodified = this.code() === previousDefault;

    this.selectedVersion.set(version);
    if (isUnmodified) {
      this.code.set(defaultCodeFor(language, version));
    }
  }

  loadFile(file: SavedFile): void {
    const language = this.languages.find((option) => option.id === file.language) ?? this.languages[0];
    this.selectedLanguage.set(language);
    this.selectedVersion.set(file.version);
    this.code.set(file.code ?? '');
    this.currentFileId.set(file.id);
    this.currentFileName.set(file.filename);
    this.currentFolderId.set(file.folderId);
  }

  markSavedAs(id: number, filename: string, folderId: number | null): void {
    this.currentFileId.set(id);
    this.currentFileName.set(filename);
    this.currentFolderId.set(folderId);
  }

  clearFileAssociation(): void {
    this.currentFileId.set(null);
    this.currentFileName.set(null);
    this.currentFolderId.set(null);
  }

  /**
   * Closes the currently open saved file and resets the editor to a fresh
   * buffer for the selected language (default version + default starter code).
   */
  closeOpenFile(): void {
    const language = this.selectedLanguage();
    this.selectedVersion.set(language.defaultVersion);
    this.code.set(defaultCodeFor(language, language.defaultVersion));
    this.clearFileAssociation();
  }
}
