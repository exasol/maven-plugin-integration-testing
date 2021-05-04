# Maven Plugin Integration Testing

Java library for writing integration tests for maven plugins.

## Features

* Run tests with isolated local repository (the plugin won't be installed in your default local maven repository)
* Extract code coverage
* Debug

## Usage

The following code snippet gives an example on how the `MavenIntegrationTestEnvironment` is typically used.

Note that this is only an example. To make it work with your plugin you need to change `MY_PLUGIN_JAR` and the calls of `verifier`.

```java

private class MyPluginTest {
    private static final File MY_PLUGIN_JAR = new File("target/dummy-plugin-0.1.0.jar");
    private static final File MY_PLUGIN_POM = new File("pom.xml");
    private static MavenIntegrationTestEnvironment testEnvironment;

    @BeforeAll
    static void beforeAll() {
        testEnvironment = new MavenIntegrationTestEnvironment();
        testEnvironment.installPlugin(MY_PLUGIN_JAR, MY_PLUGIN_POM);
    }

    @Test
    void testInstallAndRunPlugin(@TempDir final Path tempProjectDir) throws IOException, VerificationException {
        writePomFile(tempProjectDir);
        final Verifier verifier = testEnvironment.getVerifier(tempDir);
        verifier.executeGoal("com.example:my-plugin:hello");
        verifier.verifyTextInLog("Hello World!");
    }

    /**
     * Write an example pom.xml to the test-project.
     * <p>
     *     The pom file should use your plugin.
     * </p>
     * @param tempProjectDir temporary test project folder to write the pom file to
     * @throws IOException if write fails
     */
    private void writePomFile(final Path tempProjectDir) throws IOException {
        Files.copy(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream("test-project/pom.xml")),
                tempProjectDir.resolve("pom.xml"), StandardCopyOption.REPLACE_EXISTING);
    }
}
```

For a working example check [`MavenIntegrationTestEnvironmentIT.java`](src/test/java/com/exasol/mavenpluginintegrationtesting/MavenIntegrationTestEnvironmentIT.java).

### Options

You can configure the verifier using system properties at runtime. By that you don't need to change your code for example to enable debugging. You simply add a command line parameter to the JVM options of the run command in your IDE. You can also define different run profiles in your IDE (e.g. run with debugging enabled).

#### Debugging

To enable debugging append `-Dtest.debug=true` to your JVM options. This library will then configure the `Validator` to start maven with debugging enabled. This will cause maven to wait on an incoming debugger connection on port 8000. After you have started the tests, you'll be able to connect to localhost:8000 using the remote-debugger of your IDE.

Note: The tests will wait for the debugger. So if you don't connect, the tests will not run.

#### Coverage:

You can enable the extraction of code coverage information by adding `-Dtest.coverage=true` to your JVM call. This library will then add a jacoco-agent from `target/jacoco-agent/org.jacoco.agent-runtime.jar` to the JVMs of the maven processes started by the `Verifier`.

For this to work we need to place the jacoco agent there. We can do so using `maven-dependency-plugin`. Check out this repository's [`pom.xml`](./pom.xml) for an example. If you use [project-keeper][project-keeper] simply add the `udf-coverage` module.

This agent will then write the coverage information to `target/jacoco-mvn-NUMBER-.exec` files. In order to create a report or send it to sonar you have to merge these executions into one. You can do so using the `jacoco-maven-plugin`. If you are using [project-keeper][project-keeper] simply add the `udf-coverage` module.

## Additional Information

* [Changelog](doc/changes/changelog.md)
* [Dependencies](dependencies.md)

[project-keeper]: https://github.com/exasol/project-keeper-maven-plugin/
