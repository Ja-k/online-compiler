import { Component } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { FormsModule } from '@angular/forms';

import { CodeExecutionService } from '../services/code-execution.service';

@Component({
  selector: 'app-text-editor-component',
  imports: [MonacoEditorModule, FormsModule],
  templateUrl: './text-editor-component.component.html',
  styleUrl: './text-editor-component.component.scss',
  standalone: true
})
export class TextEditorComponentComponent {
  /** Only Java is supported for now; this will become selectable once more languages are added. */
  readonly language = 'java';

  /** Java versions 8 and above; extend this list as new LTS releases need support. */
  readonly javaVersions = [8, 11, 17, 21];
  selectedJavaVersion = 17;

  code: string = `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`;

  editorOptions = { theme: 'vs-dark', language: 'java' };

  constructor(private readonly codeExecutionService: CodeExecutionService) {}

  get isRunning(): boolean {
    return this.codeExecutionService.isRunning();
  }

  runCode(): void {
    if (this.isRunning || !this.code.trim()) {
      return;
    }

    this.codeExecutionService.runCode({
      language: this.language,
      version: this.selectedJavaVersion,
      code: this.code
    });
  }
}
