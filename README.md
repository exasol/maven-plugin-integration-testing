# Maven Plugin Integration Testing

Java library for writing integration tests for maven plugins.

## Usage

```java
import java.io.IOException;

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

Note that this is only an example. To make it work with your plugin you need to change `MY_PLUGIN_JAR` and the calls of `verifier`.

For a working example check [`MavenIntegrationTestEnvironmentIT.java`](src/test/java/com/exasol/mavenpluginintegrationtesting/MavenIntegrationTestEnvironmentIT.java).

## Additional Information

* [Changelog](doc/changes/changelog.md)
* [Dependencies](dependencies.md)