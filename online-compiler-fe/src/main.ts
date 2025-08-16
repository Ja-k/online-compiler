import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { TextEditorComponentComponent } from './app/text-editor-component/text-editor-component.component';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
