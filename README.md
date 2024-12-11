# Scala AI Documentation (ScalaAIDoc)

### Overview

**ScalaAIDoc** is a comprehensive tool designed to enhance the documentation experience for Scala projects by utilizing artificial intelligence. Powered by OpenAI's language models, this library automates the generation of detailed ScalaDocs and produces a high-quality `README.md` for your project. Leveraging functional programming principles through Cats Effect and FS2, ScalaAIDoc ensures scalable, efficient, and asynchronous processing of large projects.

The tool is interactive, allowing users to enhance ScalaDocs for individual files, entire projects, and even generate descriptive `README.md` files, all through the power of Generative AI. 

---

### Features

1. **Generate ScalaDocs for Individual Files**:
   - Automatically enhances a given `.scala` file with detailed and accurate ScalaDocs while preserving the code structure.
   
2. **Generate ScalaDocs for Entire Project**:
   - Automatically processes all `.scala` files in a specified directory, enhancing their documentation using AI.

3. **Generate High-Quality `README.md`**:
   - Analyzes the content and structure of Scala files in the project to generate a comprehensive and informative `README.md` file.

4. **Integration with OpenAI API**:
   - Uses OpenAI models for precise and context-aware documentation generation.

5. **Highly Concurrent and Streaming-Based Design**:
   - Exploits FS2 and Cats Effect libraries for efficient file processing, utilizing concurrency and streaming-safe paradigms.

---

### Architecture

The key components of the project:

1. **ScalaDocGenerator**:
   - The core module responsible for generating ScalaDocs and the `README.md`. It handles the interaction with OpenAI APIs for documentation tasks and ensures proper file management.

2. **FileProcessor**:
   - A modular utility to read the content of individual Scala files or traverse directories recursively to process all `.scala` files. This component ensures efficient and concurrent file handling.

3. **Main Application (`Main`)**:
   - The entry point for the user to interact with the application. This provides a simple menu-driven CLI to choose between generating ScalaDocs for files, the entire project, or a summarized `README.md`.

4. **Opaque Types and Abstractions**:
   - The project adopts an opaque type (`FileContent`) and strong type safety for file content to improve readability and reduce accidental misuse of raw strings.

---

### Installation and Quick Start

To use ScalaAIDoc in your project, you need to have [Scala](https://www.scala-lang.org/) installed, along with [sbt](https://www.scala-sbt.org/). This project is designed for Scala 3 and requires minimal setup.

#### How to Run

1. **Clone the Repository**:
   ```
   git clone <repository-url>
   cd scalaaidoc
   ```

2. **Set Up Environment Variables**:
   - You will need an OpenAI API key. Set the key as an environment variable:
     ```bash
     export OPENAI_API_KEY="your-api-key-here"
     ```

3. **Run the Application**:
   - Use `sbt` to run the program:
     ```bash
     sbt run
     ```

4. **Follow the CLI Menu**:
   - After running the program, follow the interactive CLI to choose between the following options:
     - `1` to generate ScalaDocs for your project's Scala files.
     - `2` to generate a `README.md` summarizing the project.
     - `3` to exit.

---

### Code Walkthrough

#### 1. **ScalaDocGenerator**
The `ScalaDocGenerator` trait defines the primary operations:
- `generateScalaDoc`: Enhances a specific Scala file with ScalaDoc annotations.
- `generateReadMe`: Creates a `README.md` summarizing a project’s purpose, features, and structure.
- `generateAllScalaDoc`: Processes all `.scala` files in a given directory to generate ScalaDocs.

A concrete implementation provided in the `ScalaDocGenerator` object interacts with the OpenAI API to process source files and output enriched documentation.

#### 2. **FileProcessor**
The `FileProcessor` trait provides utilities for reading files:
- `readScalaFile`: Reads the content of a single `.scala` file and outputs it as a `FileContent` type.
- `readAllScalaFiles`: Traverses all subdirectories to find `.scala` files and processes them concurrently using FS2.

#### 3. **Menu-Based CLI**
The `Main` object serves as an interactive entry point for the application. Users select actions like generating ScalaDocs or creating a project `README.md` through a simple menu interface.

- **Menu Options**:
  - Prompt the user to choose between options like generating ScalaDocs or `README.md`.
  - Process the selected task and display helpful feedback.

---

### Functional Programming (FP) Principles

ScalaAIDoc is built with a functional programming mindset using libraries like Cats Effect and FS2:
- **Pure Functional Design**: All core operations use effect types, avoiding side effects and enabling safe concurrency.
- **Streaming API**: Processes large files and directories without loading all data into memory.
- **Type Safety**: Strongly typed abstractions like `FileContent` and functional error handling enhance robustness.

---

### Dependencies

ScalaAIDoc relies on the following dependencies:
- **Cats Effect**: For asynchronous computations and resource safety.
- **FS2**: For stream-based file processing.
- **Typelevel Cats**: For functional programming utilities.
- **OpenAI API**: Integrated for generating documentation through its AI service.
- **Log4Cats**: For structured and type-safe logging.

To add the dependencies to your project, include the following in your `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.5.1",
  "co.fs2" %% "fs2-core" % "3.10.0",
  "co.fs2" %% "fs2-io" % "3.10.0",
  "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
  "io.circe" %% "circe-core" % "0.15.0",
  "org.typelevel" %% "cats-core" % "2.9.0",
  "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.8"
)
```

---

### Sample Use Case

1. **Generate ScalaDocs for the Entire Project**:
   - The user selects option `1` in the CLI.
   - All `.scala` files in the project directory are processed concurrently.
   - Enhanced ScalaDocs are written directly into the respective files.

2. **Generate `README.md`**:
   - The user selects option `2` in the CLI.
   - A comprehensive summary of all Scala files is created and saved as `README.md`.

---

### Future Enhancements

- **Improved ReadMe Summarization**: Incorporate metadata from build files (`build.sbt`) and configurations to enhance the README generation further.
- **IDE Integration**: Provide plugins for IntelliJ or VSCode to seamlessly integrate ScalaAIDoc into developer workflows.
- **Custom Documentation Styles**: Allow users to pick between different ScalaDoc templates.

---

### Contributing

We welcome contributions to ScalaAIDoc! Follow these steps to get started:
1. Fork the repository.
2. Make necessary changes and add tests.
3. Open a pull request with a clear description of your changes.
4. Ensure your code adheres to the functional programming principles used in the project.

---

### License

ScalaAIDoc is licensed under the [MIT License](LICENSE). Contributions are welcome under the same license.

Enjoy documenting your Scala projects effortlessly with **ScalaAIDoc**! 🚀