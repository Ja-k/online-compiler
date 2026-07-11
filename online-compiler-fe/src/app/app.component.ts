import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TextEditorComponentComponent } from './text-editor-component/text-editor-component.component';
import { ExecuteOutputComponent } from './execute-output-component/execute-output.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TextEditorComponentComponent, ExecuteOutputComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'online-compiler-fe';
}
