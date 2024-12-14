# Scala AI Documentation Tool

This project is a Scala-based application that utilizes AI to generate enhanced ScalaDocs and project documentation for Scala source files. The tool reads Scala source code, processes it via the OpenAI API, and outputs detailed, high-quality documentation. It also offers utilities to generate a structured `README.md` summarizing the entire project.

---

## Features
1. **Generate ScalaDocs:**
   - Enriches individual or all Scala source files in a project with detailed ScalaDocs, comments, and explanations in a meaningful, AI-generated format.
 
2. **Generate README.md:**
   - Summarizes the project in a professional README file, analyzing the provided Scala files and extracting key features, usage, and high-level details.

3. **Reliable File Processing:**
   - Supports concurrent processing of Scala files using FS2 and Cats Effect for efficient handling of large projects.

4. **Error Logging:**
   - Provides meaningful error logs for any issues encountered during processing using Typelevel `log4cats`.

5. **Full CLI Support:**
   - Offers an interactive command-line interface for input and task selection.

---

## Installation
Clone this repository on your local machine:

```bash
git clone https://github.com/khanr1/scala-ai-doc.git
cd scala-ai-doc
```

Ensure you have the following dependencies installed on your system:
- **Scala 3.x**
- **sbt** (Scala Build Tool)
- **Java 8 or higher**

Install necessary libraries via SBT:
```bash
sbt update
```

---

## Usage

### Running the Application

To run the application, execute:
```bash
sbt run
```

The application will present a CLI menu to select the following tasks:
1. **Generate ScalaDocs**  
    - Enter the path to the directory where the Scala source files are located. The application will process the files and generate enriched ScalaDocs.
  
2. **Generate README.md**  
    - Point the application to the root directory of your Scala project. The tool will analyze the project's code and create a detailed `README.md`.

3. **Exit**  
    - Terminates the application.

---

## Project Structure

### 1. **Main Application (`Main`)**
   The entry point for the application.  
   - Displays a text-based menu.
   - Handles user interaction for generating documentation and selecting directories.

Highlights:
- **Menu-driven interface:** The application navigates through options with options to generate ScalaDocs, generate a `README`, or exit the application.
- **Customizable Paths:** Accepts input paths for target directories and files.
  
### 2. **Core Logic**

#### a. **`ScalaDocGenerator`:**
   A central component of this project, it defines the logic for:
   - Generating ScalaDoc for a single file.
   - Generating ScalaDoc for all files in a directory.
   - Creating a project-wide summary in `README.md`.

   **Key Features:**
   - AI-based documentation generation using the OpenAI API.
   - Concurrency control to streamline processing of multiple files with FS2.
   - Adaptive handling of errors and warnings during file updates.

#### b. **`FileProcessor`:**
   A utility for reading, validating, and processing Scala files.
   - Reads `.scala` files using streaming to optimize memory usage.
   - Filters and validates paths for true `.scala` files to avoid errors.
   - Works recursively through a directory tree to find and process all Scala files.

**Error Handling:**  
   Custom exceptions such as `InvalidPathError` and `DirectoryError` are raised for invalid file paths or unexpected directory structures. These enhance debugging and ensure safer file processing.

#### c. **Configuration:**
   - The `OpenAIConfig` handles API key management required for external API integrations.
   - You must configure an OpenAI API key in the form of environment variables or application configuration.

---

## Input and Output Details

1. **Input:**
   - A directory path containing `.scala` files or the root directory of your Scala project.
   - User input via a CLI interface for menu options and paths.

2. **Output:**
   - Updated `.scala` files enriched with detailed and standardized ScalaDocs.
   - A `README.md` file placed in the root directory of the project.

---

## Code Highlights

### Main Application (`Main`)
The `Main` object provides the application backbone:
```scala
def menuLoop: IO[Unit] = {
  for {
    _ <- showMenu
    choice <- getChoice
    _ <- choice match {
      case 1 => for { path <- getPath; _ <- generateScalaDocs(path) *> menuLoop } yield ()
      case 2 => for { path <- getPath; _ <- generateReadME(path) *> menuLoop } yield ()
      case 3 => IO.println("Goodbye!")
      case _ => IO.println("Invalid input. Try again.") *> menuLoop
    }
  } yield ()
}
```
Features:
- **Dynamic Path Selection:** Prompts the user to enter a directory or file path.
- **Activity Loop:** Repeats until the user selects the "exit" option.

### `ScalaDocGenerator`
The `ScalaDocGenerator` trait defines:
```scala
def generateScalaDoc(path: Path, content: FileContent): fs2.Stream[F, Nothing]
def generateReadMe(path: Path): fs2.Stream[F, Nothing]
def generateAllScalaDoc(path: Path): fs2.Stream[F, Nothing]
```
The `make` factory method creates an implementation that uses OpenAI for generating documentation:
- Enriches code comments via an AI model.
- Writes enhanced documentation back to the original source files.

### `FileProcessor`
The `FileProcessor` tracks and reads all `.scala` files within directories:
```scala
def readAllScalaFiles(path: Path): fs2.Stream[F, (Path, FileContent)] = ...
```
It uses FS2 to handle files in parallel, ensuring efficient processing while managing memory constraints.

---

## AI Integration
The project leverages OpenAI APIs via the `scala-openai` library to achieve the following:
1. Analyze and interpret Scala source code.
2. Generate ScalaDocs with enriched comments and meaningful documentation.
3. Compose a structured and informative project README.

The interaction flow involves generating a natural language prompt based on the input Scala code, submitting it to OpenAI, and receiving the processed response as Scala documentation or summarized project metadata.

---

## Requirements
1. **Scala 3.x**: Leverages modern features like opaque types and extension methods.
2. **FS2**: Functional streaming for efficient file processing.
3. **Cats Effect**: Provides effectful and concurrent abstractions.
4. **log4cats**: Logging utilities (SLF4J Logger).
5. **OpenAI API**: Cloud-based NLP services to generate documentation and summaries.

---

## Contributing
1. Clone the repository.
2. Submit PRs for improvements or new feature suggestions.
3. Ensure proper scaladoc-style comments for your contributions.

---

## Acknowledgments
1. **Typelevel Libraries**: Cats Effect, FS2.
2. **OpenAI**: NLP-based documentation generation.
3. **Contributors**: Special thanks to Raphael Khan for creating this tool.