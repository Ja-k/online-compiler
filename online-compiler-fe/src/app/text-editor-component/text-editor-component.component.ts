import { Component } from '@angular/core';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { FormsModule } from '@angular/forms'; 
@Component({
  selector: 'app-text-editor-component',
  imports: [MonacoEditorModule, FormsModule],
  templateUrl: './text-editor-component.component.html',
  styleUrl: './text-editor-component.component.scss',
  standalone: true
})
export class TextEditorComponentComponent {
  code: string = `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`;
  editorOptions = { theme: 'vs-dark', language: 'java' };
}
