# CP Audit Reports Service

Self-service audit reports.

### Prerequisites

- ☕️ **Java 21.0.8 or later**: Ensure Java is installed and available on your `PATH`.
- ⚙️ **Gradle**: [Install Gradle](https://gradle.org/install/). The project itself defines which Gradle version to use (gradle/wrapper/gradle-wrapper.properties).

You can verify installation with:
```bash
java -version
gradle -v
```

## Installation

### Build
```bash
gradle build
```

`build` will run all tests.

### Tests
- `gradle test` for running unit and integration tests
- `gradle api` for running api tests


### Environment Setup for Local Builds

This project uses a two-file approach for environment variable management with `.env` and `.envrc` files. 

**Quick Setup:**
1. Install `direnv`: `brew install direnv`
2. Add to shell: `echo 'eval "$(direnv hook zsh)"' >> ~/.zshrc`
3. Allow direnv: `direnv allow`
4. Create `.env` file with your local configuration

**Server Port:** The application uses port `8082` by default. Override with:
- Environment variable: `export SERVER_PORT=8080`
- Gradle property: `./gradlew test -Pserver.port=8080`
- System property: `./gradlew test -Dserver.port=8080`

📖 **For complete setup instructions, troubleshooting, and best practices, see the [Environment Variables Guide](docs/EnvironmentVariables.md).**

## Static code analysis

Install PMD

```bash
brew install pmd
```
```bash
pmd check \
    --dir src/main/java \
    --rulesets \
    .github/pmd-ruleset.xml \
    --format html \
    -r build/reports/pmd/pmd-report.html
```

Run PMD from Gradle

```
gradle pmdTest
```

### Contribute to This Repository

Contributions are welcome! Please see the [CONTRIBUTING.md](.github/CONTRIBUTING.md) file for guidelines.

See also: [JWTFilter documentation](docs/JWTFilter.md)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
