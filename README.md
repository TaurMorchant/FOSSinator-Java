# FOSSinator-Java

**FOSSinator** is a tool for automatic replacement of dependencies and imports in Java repositories.

---

## How to Use

1. Clone the repository
2. If needed, edit the configuration files in the `src/main/resources` folder
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run either the `transform` or `validate` commands
    ```bash
    java -jar fossinator-java-1.0.0.jar transform -v -c -d=<your dir>   
    ```

---

## `validate` Command

The `validate` command analyzes the project's dependency tree to check for banned libraries.  
If such libraries are found, it means the repository is not yet ready for release.

As a result, a file named `dependency-tree-checked.txt` will be created in the project's root directory. It will contain the full output of the `mvn dependency:tree` command.  
Banned dependencies will be marked with the label `DEPRECATED`.

**Flags:**
- `-d` or `--dir`: Specifies the directory to process. If omitted, the current directory will be used.
- `-v` or `--verbose`: Enables additional logging.
- `-b` or `--branch`: Appends the branch name to the output file's name.  
  *Note: This does not affect which branch is checked out, only the name of the generated file!*

---

## `transform` Command

The `transform` command automatically replaces dependencies and import statements in your repository based on the provided configuration.

**Flags:**
- `-d` or `--dir`: Specifies the directory to process. If omitted, the current directory will be used.
- `-v` or `--verbose`: Enables additional logging.
- `-c` or `--check`: Runs `validate` both before and after the `transform` command.

---

## Features

- Replace or add dependencies in all `pom.xml` files
- Replace properties in `pom.xml`
- Replace package names in import statements
- Replace full class names in import statements
- Update `lombok.config` files

---

## Configuration Structure

FOSSinator is managed by three configuration files:

- `dependenciesBlackList.txt`: Defines the list of banned dependencies for the `validate` command.
- `classesIndex.txt`: Specifies a list of class names to be replaced based on pattern rules (commonly for package renaming).
- `config.yaml`: The main configuration file that defines all transformation rules.

### `config.yaml` Structure

- `imports-to-replace`: Explicit class name replacements in import statements.
- `imports-to-replace-by-pattern`: Pattern-based class name replacements in import statements. If a class listed in `classesIndex.txt` matches a pattern, it will be replaced.
- `dependencies-to-replace`: Rules for renaming, replacing, or changing the version of dependencies in `pom.xml`.
- `dependencies-to-add`: Rules for adding dependencies when specific other dependencies are present.
