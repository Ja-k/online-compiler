export interface LanguageOption {
  /** Sent to the backend to select which compiler/runner to use. */
  readonly id: string;
  readonly label: string;
  /** Monaco's language id, used for syntax highlighting. */
  readonly monacoLanguage: string;
  /** Selectable language/standard versions, e.g. Java 8-21 or C++ standards. */
  readonly versions: readonly number[];
  readonly defaultVersion: number;
  readonly defaultCode: string;
}

export const LANGUAGE_OPTIONS: readonly LanguageOption[] = [
  {
    id: 'java',
    label: 'Java',
    monacoLanguage: 'java',
    versions: [8, 11, 17, 21],
    defaultVersion: 17,
    defaultCode: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`
  },
  {
    id: 'cpp',
    label: 'C++',
    monacoLanguage: 'cpp',
    versions: [11, 14, 17, 20, 23],
    defaultVersion: 17,
    defaultCode: `#include <iostream>

int main() {
    std::cout << "Hello, World!" << std::endl;
    return 0;
}
`
  }
];
