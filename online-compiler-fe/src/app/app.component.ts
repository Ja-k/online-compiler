import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

import { AuthService } from './services/auth.service';
import { FoldersService } from './services/folders.service';
import { SavedFilesService } from './services/saved-files.service';
import { ThemeService } from './services/theme.service';
import { FilesDrawerComponent } from './files-drawer-component/files-drawer.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, FilesDrawerComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'online-compiler-fe';

  constructor(
    protected readonly authService: AuthService,
    protected readonly savedFilesService: SavedFilesService,
    protected readonly themeService: ThemeService,
    private readonly foldersService: FoldersService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.authService.restoreSession().subscribe();
  }

  onThemeToggle(): void {
    this.themeService.toggle();
  }

  onHamburgerClick(): void {
    this.savedFilesService.openDrawer();
    this.foldersService.refresh();
  }

  onLogoutClick(): void {
    this.authService.logout().subscribe({
      complete: () => {
        this.savedFilesService.clear();
        this.foldersService.clear();
        this.router.navigate(['/']);
      },
      error: () => {
        this.savedFilesService.clear();
        this.foldersService.clear();
        this.router.navigate(['/']);
      }
    });
  }
}
