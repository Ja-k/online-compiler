/**
 * A custom Monaco theme tuned for this app's dark palette
 * (see :root variables in styles.scss). Accents are deliberately muted —
 * lower saturation than a typical "neon tech" theme — so keywords and
 * other tokens stay readable for long sessions without causing eye fatigue.
 *
 * Java, C, C++, Python, Rust, and Go all tokenize as "keyword*" (and the
 * other standard token classes) in Monaco's built-in grammars, so a handful
 * of rules covers all of them.
 */
export const TECH_VIBE_THEME_NAME = 'tech-vibe-dark';

export function defineTechVibeMonacoTheme(): void {
  const monaco = (window as unknown as { monaco?: any }).monaco;
  if (!monaco) {
    return;
  }

  monaco.editor.defineTheme(TECH_VIBE_THEME_NAME, {
    base: 'vs-dark',
    inherit: true,
    rules: [
      // Soft teal — readable but not neon
      { token: 'keyword', foreground: '6a9eb8' },
      { token: 'keyword.flow', foreground: '6a9eb8' },
      { token: 'storage', foreground: '6a9eb8' },
      // Dimmed comments so they recede into the background
      { token: 'comment', foreground: '5a6578', fontStyle: 'italic' },
      // Warm sand instead of bright yellow
      { token: 'string', foreground: 'b8a06a' },
      { token: 'string.escape', foreground: 'c4b07a' },
      // Soft lavender for numbers
      { token: 'number', foreground: '9a8ab8' },
      // Gentle sky blue for types
      { token: 'type', foreground: '7a9ab0' },
      { token: 'type.identifier', foreground: '7a9ab0' },
      // Slightly warmer, lower-contrast default text
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
}
