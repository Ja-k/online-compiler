import { Component, ElementRef, HostListener, ViewChild, signal } from '@angular/core';

import { TextEditorComponentComponent } from '../text-editor-component/text-editor-component.component';
import { ExecuteOutputComponent } from '../execute-output-component/execute-output.component';

const MIN_EDITOR_WIDTH_PERCENT = 20;
const MAX_EDITOR_WIDTH_PERCENT = 80;
const DEFAULT_EDITOR_WIDTH_PERCENT = 50;

@Component({
  selector: 'app-home',
  imports: [TextEditorComponentComponent, ExecuteOutputComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  @ViewChild('appContainer') private readonly appContainerRef?: ElementRef<HTMLDivElement>;

  readonly editorWidthPercent = signal(DEFAULT_EDITOR_WIDTH_PERCENT);
  isDragging = false;

  onSeparatorPointerDown(event: PointerEvent): void {
    event.preventDefault();
    this.isDragging = true;
    try {
      (event.target as HTMLElement).setPointerCapture(event.pointerId);
    } catch {
      // Pointer capture is a nice-to-have for smoother dragging; if the
      // browser rejects it we still track the drag via the window listeners.
    }
    document.body.style.userSelect = 'none';
  }

  resetSplit(): void {
    this.editorWidthPercent.set(DEFAULT_EDITOR_WIDTH_PERCENT);
  }

  @HostListener('window:pointermove', ['$event'])
  onPointerMove(event: PointerEvent): void {
    if (!this.isDragging || !this.appContainerRef) {
      return;
    }

    const rect = this.appContainerRef.nativeElement.getBoundingClientRect();
    const percent = ((event.clientX - rect.left) / rect.width) * 100;
    this.editorWidthPercent.set(
      Math.min(MAX_EDITOR_WIDTH_PERCENT, Math.max(MIN_EDITOR_WIDTH_PERCENT, percent))
    );
  }

  @HostListener('window:pointerup')
  @HostListener('window:pointercancel')
  onPointerUp(): void {
    if (!this.isDragging) {
      return;
    }
    this.isDragging = false;
    document.body.style.userSelect = '';
  }
}
