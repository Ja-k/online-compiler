import { Component } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { FormsModule } from '@angular/forms';

import { CodeExecutionService } from '../services/code-execution.service';
import { LANGUAGE_OPTIONS, LanguageOption } from '../models/language-option';

@Component({
  selector: 'app-text-editor-component',
  imports: [MonacoEditorModule, FormsModule],
  templateUrl: './text-editor-component.component.html',
  styleUrl: './text-editor-component.component.scss',
  standalone: true
})
export class TextEditorComponentComponent {
  readonly languages: readonly LanguageOption[] = LANGUAGE_OPTIONS;

  selectedLanguage: LanguageOption = this.languages[0];
  selectedVersion: number = this.selectedLanguage.defaultVersion;
  code: string = this.selectedLanguage.defaultCode;

  editorOptions = { theme: 'vs-dark', language: this.selectedLanguage.monacoLanguage };

  constructor(private readonly codeExecutionService: CodeExecutionService) {}

  get isRunning(): boolean {
    return this.codeExecutionService.isRunning();
  }

  onLanguageChange(): void {
    this.selectedVersion = this.selectedLanguage.defaultVersion;
    this.code = this.selectedLanguage.defaultCode;
    this.editorOptions = { theme: 'vs-dark', language: this.selectedLanguage.monacoLanguage };
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
}
