
# ScalaAI-Doc: AI-Powered Scala Documentation Generator

ScalaAI-Doc is a robust and intelligent tool for processing and enriching Scala source files with detailed and meaningful documentation. It leverages generative AI capabilities to create professional-grade ScalaDoc for your projects. 

## Features

- **File Processing:** Reads and processes individual or multiple Scala files from a directory.
- **AI-Powered Documentation:** Enriches source files with high-quality ScalaDoc and inline comments.
- **Resource Safety:** Ensures concurrent, safe operations using FS2 and Cats Effect.
- **Error Handling:** Handles invalid paths and other common errors gracefully.
- **Seamless Integration:** Processes files and integrates enriched documentation back into the original or temporary files.

## Modules

### 1. `FileProcessor`
This module provides functionality to:
- Read the content of a single Scala file.
- Recursively read all `.scala` files in a directory and its subdirectories.

**Key Traits and Methods:**
- `readScalaFile(path: Path): Stream[F, FileContent]`: Reads content from a specific `.scala` file.
- `readAllScalaFiles(path: Path): Stream[F, (Path, FileContent)]`: Reads all Scala files in a directory and subdirectories.

### 2. `ScalaDocGenerator`
This module communicates with OpenAI's API to generate enriched ScalaDoc for a given Scala source file.

**Key Traits and Methods:**
- `generateScalaDoc(path: Path): Stream[F, Nothing]`: Processes a file and generates ScalaDoc using an AI-powered service.

### 3. `Main`
The entry point of the application. It initializes the required components, sets up logging, and orchestrates the file processing and ScalaDoc generation.

## How It Works

1. **File Reading:**
   The `FileProcessor` reads the content of Scala source files, ensuring only `.scala` files are processed.
   
2. **Documentation Generation:**
   Using OpenAI's generative AI, `ScalaDocGenerator` creates enriched documentation based on the content of the files.

3. **Output Integration:**
   The processed documentation is written to temporary files and replaces the original files upon successful completion.

## Setup and Installation

### Prerequisites
- **Scala 3**
- **Cats Effect, FS2, Circe**
- **OpenAI API Key**
- **SLF4J for Logging**

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/scalaai-doc.git
   ```
2. Navigate to the project directory:
   ```bash
   cd scalaai-doc
   ```
3. Configure the OpenAI API key in the appropriate configuration file.

4. Build the project using your preferred build tool (e.g., SBT).

### Running the Application
Run the application from the command line:
```bash
sbt run
```

## Code Example

```scala
import io.github.khanr1.scalaaidoc.core.{FileProcessor, ScalaDocGenerator}
import cats.effect.{IO, IOApp}
import fs2.io.file.Path

object Main extends IOApp.Simple:
  val run: IO[Unit] = {
    val inputPath = Path("src/main/scala")
    val fileProcessor = FileProcessor.make[IO]()
    fileProcessor
      .readAllScalaFiles(inputPath)
      .flatMap { case (path, _) =>
        val scalaDocGenerator = ScalaDocGenerator.make[IO]("your-api-key")
        scalaDocGenerator.generateScalaDoc(path)
      }
      .compile
      .drain
  }
```

## Contributing
Contributions are welcome! Feel free to submit a pull request or open an issue for feature requests, bugs, or improvements.


