/**
 * Custom Monaco themes tuned for this app's dark and light palettes
 * (see data-theme variables in styles.scss). Accents are deliberately muted —
 * lower saturation than a typical "neon tech" theme — so keywords and
 * other tokens stay readable for long sessions without causing eye fatigue.
 *
 * Java, C, C++, Python, Rust, Go, and C# all tokenize as "keyword*" (and the
 * other standard token classes) in Monaco's built-in grammars, so a handful
 * of rules covers all of them.
 */
export const TECH_VIBE_DARK_THEME = 'tech-vibe-dark';
export const TECH_VIBE_LIGHT_THEME = 'tech-vibe-light';

/** @deprecated Prefer TECH_VIBE_DARK_THEME; kept so existing imports keep working. */
export const TECH_VIBE_THEME_NAME = TECH_VIBE_DARK_THEME;

export function monacoThemeForMode(mode: 'dark' | 'light'): string {
  return mode === 'light' ? TECH_VIBE_LIGHT_THEME : TECH_VIBE_DARK_THEME;
}

export function defineTechVibeMonacoTheme(): void {
  const monaco = (window as unknown as { monaco?: any }).monaco;
  if (!monaco) {
    return;
  }

  monaco.editor.defineTheme(TECH_VIBE_DARK_THEME, {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '6a9eb8' },
      { token: 'keyword.flow', foreground: '6a9eb8' },
      { token: 'storage', foreground: '6a9eb8' },
      { token: 'comment', foreground: '5a6578', fontStyle: 'italic' },
      { token: 'string', foreground: 'b8a06a' },
      { token: 'string.escape', foreground: 'c4b07a' },
      { token: 'number', foreground: '9a8ab8' },
      { token: 'type', foreground: '7a9ab0' },
      { token: 'type.identifier', foreground: '7a9ab0' },
      { token: 'identifier', foreground: 'c8d0e0' },
      { token: 'delimiter', foreground: '6e7a90' },
      { token: 'operator', foreground: '6e7a90' },
      { token: 'annotation', foreground: '8a7aa8' },
      { token: 'tag', foreground: '6a9eb8' }
    ],
    colors: {
      'editor.background': '#131a2b',
      'editor.foreground': '#c8d0e0',
      'editorLineNumber.foreground': '#4a5568',
      'editorLineNumber.activeForeground': '#7a8a9e',
      'editor.selectionBackground': '#2a3550',
      'editorCursor.foreground': '#8a9ab0',
      'editor.lineHighlightBackground': '#1a2338',
      'editorGutter.background': '#131a2b'
    }
  });

  monaco.editor.defineTheme(TECH_VIBE_LIGHT_THEME, {
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '0e6e88' },
      { token: 'keyword.flow', foreground: '0e6e88' },
      { token: 'storage', foreground: '0e6e88' },
      { token: 'comment', foreground: '7a8699', fontStyle: 'italic' },
      { token: 'string', foreground: '8a6a20' },
      { token: 'string.escape', foreground: '9a7a30' },
      { token: 'number', foreground: '6b4f8a' },
      { token: 'type', foreground: '2a6a8a' },
      { token: 'type.identifier', foreground: '2a6a8a' },
      { token: 'identifier', foreground: '1a2333' },
      { token: 'delimiter', foreground: '6a7a90' },
      { token: 'operator', foreground: '6a7a90' },
      { token: 'annotation', foreground: '6b4f8a' },
      { token: 'tag', foreground: '0e6e88' }
    ],
    colors: {
      'editor.background': '#ffffff',
      'editor.foreground': '#1a2333',
      'editorLineNumber.foreground': '#8a97a8',
      'editorLineNumber.activeForeground': '#4a5a6e',
      'editor.selectionBackground': '#c8e6ef',
      'editorCursor.foreground': '#0e8fa8',
      'editor.lineHighlightBackground': '#f4f7fb',
      'editorGutter.background': '#ffffff'
    }
  });
}
