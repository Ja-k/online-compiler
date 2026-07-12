/**
 * A custom Monaco theme tuned for this app's dark "tech vibe" palette
 * (see :root variables in styles.scss), with keywords made bold and given
 * a strong accent color so they stand out clearly in every supported
 * language (Java, C++, Python, Rust all tokenize as "keyword*" in Monaco's
 * built-in grammars, so a handful of rules covers all of them).
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
      { token: 'keyword', foreground: '22d3ee', fontStyle: 'bold' },
      { token: 'keyword.flow', foreground: '22d3ee', fontStyle: 'bold' },
      { token: 'storage', foreground: '22d3ee', fontStyle: 'bold' },
      { token: 'comment', foreground: '5b6b8c', fontStyle: 'italic' },
      { token: 'string', foreground: 'facc15' },
      { token: 'string.escape', foreground: 'fbbf24' },
      { token: 'number', foreground: 'c084fc' },
      { token: 'type', foreground: '7dd3fc' },
      { token: 'type.identifier', foreground: '7dd3fc' },
      { token: 'identifier', foreground: 'e6ecff' },
      { token: 'delimiter', foreground: '8b9bc4' },
      { token: 'operator', foreground: '8b9bc4' },
      { token: 'annotation', foreground: 'a78bfa' },
      { token: 'tag', foreground: '22d3ee' }
    ],
    colors: {
      'editor.background': '#131a2b',
      'editor.foreground': '#e6ecff',
      'editorLineNumber.foreground': '#4a5578',
      'editorLineNumber.activeForeground': '#22d3ee',
      'editor.selectionBackground': '#2a3550',
      'editorCursor.foreground': '#22d3ee',
      'editor.lineHighlightBackground': '#1a2338',
      'editorGutter.background': '#131a2b'
    }
  });
}
