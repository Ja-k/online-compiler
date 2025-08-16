import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TextEditorComponentComponent } from './text-editor-component/text-editor-component.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TextEditorComponentComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'online-compiler-fe';
}
