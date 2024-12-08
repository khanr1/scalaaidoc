# scalaai-doc

`scalaai-doc` is a Scala-based application designed to automatically generate highly detailed and enriched ScalaDocs and README files for your Scala projects. This application leverages AI capabilities (via OpenAI API) to analyze, document, and describe your source code. It simplifies code documentation, ensuring better clarity, maintainability, and shareability across teams and projects.

---

## Features

### 1. **Enhanced Scala Documentation**
   - Automatically generate rich and detailed ScalaDocs for individual files or entire project directories.
   - Tailor-made ScalaDocs are added without modifying the actual code logic, enhancing only the documentation.

### 2. **README Generation**
   - Summarizes your entire project by analyzing all Scala source files in a directory. 
   - Produces a high-quality `README.md` file that contains key features, descriptions, and usage instructions for the project.

### 3. **AI-Powered Insights**
   - Uses the OpenAI API to ensure that generated documentation is of top-notch quality and human-readable in nature.
   - Incorporates prompt engineering for effective communication with the AI.

### 4. **Stream-Based I/O for Efficiency**
   - Built using FS2 and Cats Effect to ensure efficient and concurrent file processing.
   - Handles both small and large projects effectively without exhausting system resources.

### 5. **Configurable and Extendable**
   - Easily configure your OpenAI API keys and customize the input paths to process your projects.
   - Extendable architecture allows you to integrate it into your own tooling or CI workflows.

---

## Installation

### Prerequisites
- Scala (version 3.x preferred)
- SBT (Scala Build Tool)
- OpenAI API key

### Setup

1. Clone the repository or include it as a dependency in your project.

   ```bash
   git clone https://github.com/khanr1/scalaai-doc.git
   cd scalaai-doc
   ```

2. Set up your OpenAI API credentials in a supported configuration source (e.g., environment variables, config file).

3. Build the project using SBT:

   ```bash
   sbt compile
   ```

4. Execute the application:

   ```bash
   sbt run
   ```

---

## Usage

Once the application is running, you will be presented with an interactive menu:

```text
 Choose an Option: 
 1. Generate ScalaDoc
 2. Generate ReadMe.Md
 3. exit
```

### Features Accessible via Console

1. **Generate ScalaDoc for Your Project**
   - Select option `1` and the application will:
     - Traverse the project directory.
     - Analyze each Scala file and enhance its ScalaDoc with detailed AI-generated documentation.

2. **Generate a README File for Your Project**
   - Select option `2` to generate a `README.md` file that describes your project.

3. **Exit the Application**
   - Select option `3` to exit the interactive session.

### Example Directory Structure
Considering the following directory structure:

```
src/
 └── main/
     └── scala/
         └── io/
             └── github/
                 └── khanr1/
                     └── scalaaidoc/
                         ├── core/
                         │   ├── ScalaDocGenerator.scala
                         │   └── FileProcessor.scala
                         └── Main.scala
```

The application will:
- Enhance documentation for files like `Main.scala`, `ScalaDocGenerator.scala`, and `FileProcessor.scala`.
- Generate a `README.md` file summarizing the purpose and functionality of the entire project.

---

## Key Components

### `ScalaDocGenerator`
The core abstraction responsible for generating ScalaDocs and README files. This trait offers three main methods:
1. `generateScalaDoc` - Enriches documentation for a single Scala file.
2. `generateAllScalaDoc` - Processes all Scala files in a directory.
3. `generateReadMe` - Produces a summarized README based on project files.

### `FileProcessor`
A utility trait that performs file I/O tasks, such as reading Scala files or traversing directories. It ensures efficient and streaming-safe file operations:
- `readScalaFile` - Reads content for a single `.scala` file.
- `readAllScalaFiles` - Recursively processes and gathers content for all `.scala` files in a given directory.

### `Main`
The entry point for the application:
- Handles menu-driven workflows to trigger documentation generation and project summarization.
- Incorporates error handling, proper logging, and prompts the user for appropriate tasks.

---

## Implementation Overview

The application is built on these underlying technologies:

1. **Cats Effect & FS2**
   - Ensures robust functional programming patterns for concurrency, resource safety, and error handling.
   - Streaming capabilities for efficient file reading and processing.

2. **Log4Cats**
   - Provides an abstraction for logging across the application, backed by SLF4J.

3. **OpenAI Integration**
   - Uses `ChatCompletion` endpoint to interact with OpenAI's GPT models for generating documentation based on code analysis.

---

## Example Workflows

### **Scenario 1: Generating ScalaDoc for a Single File**
The `ScalaDocGenerator.scala` file contains the following code snippet:

```scala
// Before Enhancement
def generateScalaDoc(path: Path): fs2.Stream[F, Nothing] = {
  // Reads a file and processes its content for documentation.
}
```

After enhancement by the application:

```scala
// After Enhancement
/** Implements ScalaDoc generation for a single file.
  *
  * Reads the Scala source file, enhances its documentation using AI (via OpenAI's API), and
  * updates the file.
  *
  * @param path
  *   The file path pointing to the Scala source file.
  */
def generateScalaDoc(path: Path): fs2.Stream[F, Nothing] = {
  // Reads a file and processes its content for documentation.
}
```

### **Scenario 2: Generating a Project-Wide README**
For a project with multiple files, the application generates a structured `README.md` file summarizing:
- Key features
- Core components
- Example usage scenarios

---

## Logging and Error Handling
- Uses `Log4Cats` (`Slf4jLogger`) for logging at various stages like file read/write, error handling, and runtime diagnostics.
- Custom error types in `FileProcessorError` classify potential runtime issues:
  - `InvalidPathError` - Raised when a non-`.scala` file is processed.
  - `DirectoryError` - Raised for invalid directory paths.

---

## Future Improvements
- Multi-language support for non-English descriptions.
- CI/CD integration to automate documentation generation in pipelines.
- Enhanced prompt engineering to fine-tune results from the AI.

---

## Contributing
Contributions are welcome via pull requests. Ensure to include tests and follow established coding conventions.

---

## License
This project is licensed under the MIT License. See the LICENSE file for more details.