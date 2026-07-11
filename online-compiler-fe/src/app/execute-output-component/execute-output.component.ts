import { Component } from '@angular/core';

import { CodeExecutionService } from '../services/code-execution.service';

@Component({
  selector: 'app-output-component',
  imports: [],
  templateUrl: './execute-output.component.html',
  styleUrl: './execute-output.component.scss',
  standalone: true
})
export class ExecuteOutputComponent {
  constructor(private readonly codeExecutionService: CodeExecutionService) {}

  get output(): string {
    return this.codeExecutionService.output();
  }

  get isRunning(): boolean {
    return this.codeExecutionService.isRunning();
  }

  get hasError(): boolean {
    return this.codeExecutionService.hasError();
  }

  get placeholder(): string {
    return this.isRunning ? 'Running your code…' : 'Run your code to see the output here.';
  }
}
