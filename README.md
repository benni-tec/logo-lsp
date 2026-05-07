# :turtle: LOGO Language Server

A Language Server Protocol (LSP) implementation for the LOGO programming language.

This project provides a language server for LOGO, built with Kotlin and based on the [lsp4j](https://github.com/eclipse/lsp4j) framework. It uses [antlr-kotlin](https://github.com/Strumenta/antlr-kotlin) for parsing of the LOGO grammar.

## :sparkles: Features

The server currently supports the following LSP features:

- **Text Document Synchronization**: Full synchronization
- **Syntax Highlighting**
- **Diagnostics**:
  - Syntax errors (Lexer & Parser).
  - Undefined/Undeclared variables and functions.
  - Procedure argument count mismatches.
- **Navigation**:
  - Go to **Definition** / **Declaration** / **Implementation**.
  - Find **References**.
  - Supports both variables and procedures.

> [!NOTE]
> Since LOGO is typically a single-file language, workspace-wide features are currently not implemented.

## :rocket: Getting Started

The language server requires **at least JVM 17** and communicates via **stdin/stdout**. You can run it directly using Gradle:

**Windows:**
```powershell
./gradlew.bat --quiet ':logo_lsp:run'
```

**MacOS / Linux:**
```bash
./gradlew --quiet ':logo_lsp:run'
```

### JetBrains IDEs (IntelliJ IDEA, etc.)
You can integrate this server into JetBrains IDEs using the [lsp4ij](https://plugins.jetbrains.com/plugin/23257-lsp4ij) plugin.

Set up the server in the "Language Servers" panel, using the following command:

**Windows:**
```shell
cmd.exe /c "cd /d ""$PROJECT_DIR$"" && call gradlew.bat --quiet :logo_lsp:build :logo_lsp:installDist && call ./logo_lsp/build/install/logo_lsp/bin/logo_lsp.bat"
```

**MacOS / Linux:**
```bash
bash -c "cd $PROJECT_DIR$ && ./gradlew --quiet :logo_lsp:build :logo_lsp:installDist && ./logo_lsp/build/install/logo_lsp/bin/logo_lsp"
```

Map the server to the LOGO file type in the **File name patterns** tab:

| File name patterns | Language Id |
|--------------------|-------------|
| `*.logo`           | `logo`      |

> [!NOTE]
> The plugin does not use the java version configured for the project in IntelliJ.
> You might need to set the JAVA_HOME environment variable to the JDK used by IntelliJ.

## :building_construction: Architecture
The server is implemented using the [lsp4j](https://github.com/eclipse/lsp4j) and [antlr-kotlin](https://github.com/Strumenta/antlr-kotlin) library.
The latter generates the lexer, parser, and visitor from the ANTLR grammar provided by the [grammars-v4](https://github.com/antlr/grammars-v4) project.

The project is split into two main modules:

- **[logo_antlr](./logo_antlr)**
    - Contains the [LOGO grammar](./logo_antlr/antlr/logo.g4) from the [grammars-v4](https://github.com/antlr/grammars-v4)) project
    - It uses the `antlr-kotlin` Gradle plugin to generate the lexer, parser, and base visitors
- **[logo_lsp](./logo_lsp)**

   As mentioned above, LOGO is a single-file language.
   Therefore, only the text document service is implemented using the following internal services:
   - **[Analyzer](./logo_lsp/src/main/kotlin/de/benni_tec/logo_lsp/services/Analyzer.kt)**
     - Gathers syntax errors from the lexer and parser
     - Uses a visitor to build mappings for variables and procedures as well as validating definitions and usages
   - **[Highlighter](./logo_lsp/src/main/kotlin/de/benni_tec/logo_lsp/services/Highlighter.kt)**
     - Maps ANTLR lexer tokens, e.g. keywords, to the fitting semantic tokens
     - Uses a visitor to highlight more constructs which include more than one token, e.g. procedure calls and variables

## :balance_scale: License
Copyright 2026 [benni-tec](mailto:me@benni-tec.de)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES, OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT, OR OTHERWISE, ARISING FROM, OUT OF, OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
