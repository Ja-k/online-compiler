export interface LanguageOption {
  /** Sent to the backend to select which compiler/runner to use. */
  readonly id: string;
  readonly label: string;
  /** Monaco's language id, used for syntax highlighting. */
  readonly monacoLanguage: string;
  /** Selectable language/standard versions, e.g. Java 8-21, C++ standards, or Python releases. */
  readonly versions: readonly string[];
  readonly defaultVersion: string;
  readonly defaultCode: string;
}

export const LANGUAGE_OPTIONS: readonly LanguageOption[] = [
  {
    id: 'java',
    label: 'Java',
    monacoLanguage: 'java',
    versions: ['8', '11', '17', '21'],
    defaultVersion: '17',
    defaultCode: `
    import java.util.*;
    import java.time.*;
    
    public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`
  },
  {
    id: 'cpp',
    label: 'C++',
    monacoLanguage: 'cpp',
    versions: ['11', '14', '17', '20', '23'],
    defaultVersion: '17',
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
    defaultCode: `package main

import "fmt"

func main() {
    fmt.Println("Hello, World!")
}
`
  }
];
