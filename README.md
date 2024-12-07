# Scala AI Documentation Generator

## Project Description

**Scala AI Documentation Generator** is a library designed to enhance the documentation process for Scala projects using AI. It leverages OpenAI's APIs to generate detailed and enriched ScalaDoc and README files for Scala codebases. The solution aims to improve code readability, maintainability, and ease of understanding for developers by providing comprehensive documentation, all done programmatically in an automated fashion.

This library is particularly useful in scenarios where large codebases need clear and coherent documentation, saving manual effort and time.

---

## Key Features

1. **AI-Generated ScalaDoc**:
   - Automatically enhances Scala source files with enriched ScalaDoc.
   - Adds inline comments for improved code clarity without modifying the code itself.
   - Ensures all syntax details like brackets and parentheses remain untouched.

2. **README Generation**:
   - Automatically summarizes the entire Scala project into a detailed `README.md` file.
   - Extracts project structure, feature descriptions, dependencies, and usage guidelines from the codebase.

3. **Streaming and Error Handling**:
   - Uses FS2 streams for efficient and scalable handling of files and interactions with OpenAI.
   - Incorporates fine-grained logging for tracking progress, making it easy to debug and monitor process outputs.
   - Graceful error handling ensures smooth processing and provides meaningful error messages upon failure.

4. **File and Directory Support**:
   - Reads individual Scala files or processes entire directories of Scala files.
   - Automatically detects `.scala` files and ensures proper processing of valid Scala files only.

5. **Configurable and Extensible**:
   - Written in a modular style using typeclasses for easy integration with other functional libraries.
   - OpenAI authentication allows customized API keys for secure interaction.

---

## Dependencies

This project is built with the following tools and libraries:

1. **Scala 3**:
   - Leverages Scala's latest language capabilities for expressive and functional programming.

2. **Cats Effect**:
   - Provides concurrency primitives like `Async`, `Concurrent` for effectful computations.

3. **FS2**:
   - Enables functional stream processing for efficient handling of large files and I/O.

4. **OpenAI Service**:
   - Integrates the OpenAI API for interacting with ChatGPT to generate ScalaDocs and README files.

5. **Log4cats**:
   - Provides structured and type-safe logging for fine-grained tracking.

6. **Circe**:
   - Used for encoding and decoding JSON data when working with file content.

### Additional Build and Configuration Information:
- Requires an API key for OpenAI services, passed as a string parameter when creating the `ScalaDocGenerator` instance.
- Handles file operations with sanity checks and ensures directory traversal for recursive use cases.

---

## How To Run

### Prerequisites:
- **Scala 3.x**: The project uses Scala 3 and requires a compatible environment for compilation and execution.
- **sbt**: Ensure `sbt` is installed on your machine for dependency management and building the project.
- **OpenAI API Key**: You need a valid API key to interact with the OpenAI service.

---

### Generate ScalaDoc:
1. Instantiate the `ScalaDocGenerator` with necessary typeclasses and OpenAI API key:
   ```scala
   import cats.effect.IO
   import org.typelevel.log4cats.slf4j.Slf4jLogger
   import fs2.io.file.Files
   
   implicit val logger = Slf4jLogger.getLogger[IO]
   implicit val files = Files[IO]
   val apiKey = "your-openai-api-key"
   
   val generator = ScalaDocGenerator.make[IO](apiKey)
   ```

2. Invoke the `generateScalaDoc` method with the path to your Scala file:
   ```scala
   import fs2.io.file.Path
   
   val sourcePath = Path("path/to/your/scala/file.scala")
   generator.generateScalaDoc(sourcePath).compile.drain.unsafeRunSync()
   ```

3. Check the updated Scala file for newly added ScalaDoc.

---

### Generate README:
1. Use the same `ScalaDocGenerator` instance.
   
2. Invoke the `generateReadMe` method with the directory path where your Scala project resides:
   ```scala
   val projectDirPath = Path("path/to/your/project/root")
   generator.generateReadMe(projectDirPath).compile.drain.unsafeRunSync()
   ```

3. Look for the newly generated `README.md` file in the project root directory.

---

### Typical Usage Example:
- Place your project or Scala files in a directory (e.g., `src/main/scala`).
- Point the `ScalaDocGenerator` to individual files or the project directory to generate enriched docs and README with minimal manual intervention.

---

## Notable Configurations

- **Temporary File Handling**:
  Temporary files are written with `.tmp` extensions while processing is ongoing. On successful completion, they replace the target files.
  
- **Error Logging**:
  Any errors during processing (e.g., invalid file paths or API failures) are logged and will not interrupt the overall process.

- **Custom Streaming Approach**:
  All reading and writing operations are done using data streams, making this approach efficient for large files or directories with many `.scala` files.

---

## Conclusion

The **Scala AI Documentation Generator** is a powerful and efficient way to automate the documentation process for Scala projects, ensuring better code comprehension and reducing manual developer effort. With its seamless integration of FS2, Cats Effect, and OpenAI, the solution is highly scalable and robust, catering to both small and large-scale projects.