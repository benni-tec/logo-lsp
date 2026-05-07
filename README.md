# :turtle: LOGO Language Server

This project implements a basic language server for the LOGO programming language.
It currently has the following features:
- Text Documents
  - Synchronization: Full
  - Syntax Highlighting
  - Go to Definition/Decleration/Implementation/References: Variables and functions
  - Diagnostics: Undefined/Undeclared variables and functions, as well as mismatched arguments
- Workspace: Since LOGO is a single-file language, there is no workspace support.

## :rocket: Getting Started
The project is written in Kotlin (requires at least JVM 17) and uses Gradle for building.
The language server uses stdin/stdout for communication with the client.

To run the server, you can simply start the main class task using the Gradle wrapper:
- Windows
    ```shell
    ./gradlew.bat --quiet ':logo_lsp:run'
    ```
- MacOS/Linux
    ```shell
    ./gradlew --quiet ':logo_lsp:run'
    ```

### JetBrains IDEs
You can integrate the server into IntelliJ IDEA using the [lsp4ij](https://plugins.jetbrains.com/plugin/23257-lsp4ij) plugin.

Set up a Language Server in the "Language Server" panel and add the following command:
- Windows
    ```shell
    cmd.exe /c "cd /d ""$PROJECT_DIR$"" && call gradlew.bat --quiet :logo_lsp:build :logo_lsp:installDist && call ./logo_lsp/build/install/logo_lsp/bin/logo_lsp.bat "
    ```
- MacOS/Linux
    ```shell
    bash -c "cd $PROJECT_DIR$ ./gradlew --quiet :logo_lsp:build :logo_lsp:installDist && ./logo_lsp/build/install/logo_lsp/bin/logo_lsp"
    ```
  
To assign the language server to the LOGO file type add a mapping in the "File name patterns" tab:

| File name patterns | Language Id |
|--------------------|-------------|
| *.logo             | logo        |

You can now start the language server and use the language features in your IDE.

## :construction: Architecture
The server is implemented using the [lsp4j](https://github.com/eclipse/lsp4j) and [antlr-kotlin](https://github.com/Strumenta/antlr-kotlin) library.
The latter generates the lexer, parser and visitor from the grammar provided by the [grammars-v4](https://github.com/antlr/grammars-v4) project.

As mentioned above, LOGO is a single-file language. 
Therefore, only the text document service is implemented using the following internal services:
- [Analyzer](./logo_lsp/src/main/kotlin/de/benni_tec/logo_lsp/services/Analyzer.kt)
  - Lexes and parses the text document to find syntax errors
  - Uses a visitor to find all declared variables and functions in the document as well as validate their definitions and usages.
  - Provides mappings for all declared variables and functions, used to find definitions, declarations, implementations and references.
- [Highlighter](./logo_lsp/src/main/kotlin/de/benni_tec/logo_lsp/services/Highlighter.kt)
  - Generates semantic tokens based on the tokens from the Lexer, e.g. for keywords
  - Uses a visitor to generate semantic tokens for variables and functions

## License
Copyright 2026 [benni-tec](mailto:me@benni-tec.de)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
