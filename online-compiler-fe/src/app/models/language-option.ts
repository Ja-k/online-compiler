export interface LanguageOption {
  readonly id: string;
  readonly label: string;
  readonly monacoLanguage: string;
  readonly versions: readonly string[];
  readonly defaultVersion: string;
  readonly defaultCode: string;
  readonly fileExtension: string;
  /**
   * Starter code for specific versions that differs from `defaultCode`, e.g.
   * Java 21's preview "instance main method" (JEP 445) doesn't need the
   * classic `public class Main { public static void main(String[] args) }`
   * boilerplate. Falls back to `defaultCode` for any version not listed here.
   */
  readonly versionCodeOverrides?: Readonly<Record<string, string>>;
}

/** Starter code to show for a given language + version combination. */
export function defaultCodeFor(language: LanguageOption, version: string): string {
  return language.versionCodeOverrides?.[version] ?? language.defaultCode;
}

/**
 * Appends the language's file extension to `filename` if it doesn't already
 * have one, so a download always has a sensible name even for files saved
 * before an extension was expected (or without one at all).
 */
export function ensureFileExtension(filename: string, language: LanguageOption): string {
  const trimmed = filename.trim();
  return trimmed.includes('.') ? trimmed : `${trimmed}.${language.fileExtension}`;
}

/** Looks up a `LanguageOption` by its id, falling back to the first language if unknown. */
export function findLanguageOption(languageId: string): LanguageOption {
  return LANGUAGE_OPTIONS.find((option) => option.id === languageId) ?? LANGUAGE_OPTIONS[0];
}

export const LANGUAGE_OPTIONS: readonly LanguageOption[] = [
  {
    id: 'java',
    label: 'Java',
    monacoLanguage: 'java',
    versions: ['8', '11', '17', '21'],
    defaultVersion: '17',
    fileExtension: 'java',
    defaultCode: `import java.util.*;
import java.time.*;
    
public class Main {
  public static void main(String[] args) {
    System.out.println("Hello, World!");
  }
}`,
    versionCodeOverrides: {
      '21': `import java.util.*;
import java.time.*;

void main() {
    System.out.println("Hello, World!");
}`
    }
  },
  {
    id: 'c',
    label: 'C',
    monacoLanguage: 'c',
    versions: ['90', '99', '11', '17', '23'],
    defaultVersion: '17',
    fileExtension: 'c',
    defaultCode: `#include <stdio.h>

int main() {
    printf("Hello, World!\\n");
    return 0;
}
`
  },
  {
    id: 'cpp',
    label: 'C++',
    monacoLanguage: 'cpp',
    versions: ['11', '14', '17', '20', '23'],
    defaultVersion: '17',
    fileExtension: 'cpp',
    defaultCode: `#include <iostream>

int main() {
    std::cout << "Hello, World!" << std::endl;
    return 0;
}
`
  },
  {
    id: 'python',
    label: 'Python',
    monacoLanguage: 'python',
    versions: ['3.8', '3.9', '3.10', '3.11', '3.12', '3.13'],
    defaultVersion: '3.11',
    fileExtension: 'py',
    defaultCode: `print("Hello, World!")
`
  },
  {
    id: 'rust',
    label: 'Rust',
    monacoLanguage: 'rust',
    /** Rust editions, rather than compiler versions, are the meaningful "version" choice here. */
    versions: ['2015', '2018', '2021', '2024'],
    defaultVersion: '2021',
    fileExtension: 'rs',
    defaultCode: `fn main() {
    println!("Hello, World!");
}
`
  },
  {
    id: 'go',
    label: 'Go',
    monacoLanguage: 'go',
    versions: ['1.20', '1.21', '1.22', '1.23'],
    defaultVersion: '1.22',
    fileExtension: 'go',
    defaultCode: `package main

import "fmt"

func main() {
    fmt.Println("Hello, World!")
}
`
  },
  {
    id: 'csharp',
    label: 'C#',
    monacoLanguage: 'csharp',
    versions: ['6.0', '7.0', '8.0', '9.0'],
    defaultVersion: '8.0',
    fileExtension: 'cs',
    defaultCode: `Console.WriteLine("Hello, World!");
`
  }
];
