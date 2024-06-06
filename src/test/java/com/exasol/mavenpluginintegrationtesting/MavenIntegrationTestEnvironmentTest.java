package com.exasol.mavenpluginintegrationtesting;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.exasol.mavenpluginintegrationtesting.MavenIntegrationTestEnvironment.COVERAGE_KEY;
import static com.exasol.mavenpluginintegrationtesting.MavenIntegrationTestEnvironment.DEBUG_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class MavenIntegrationTestEnvironmentTest {
    private static final Path dummyPlugin = Path.of("src", "test", "resources", "dummy-plugin");
    private static File emptyJarFile;
    private static File dummyPluginPom;
    private static MavenIntegrationTestEnvironment testEnvironment;

    @BeforeAll
    static void beforeAll() throws IOException {
        emptyJarFile = Files.createTempFile("emptyJarFile", "jar").toFile();
        dummyPluginPom = dummyPlugin.resolve("pom.xml").toFile();
        testEnvironment = new MavenIntegrationTestEnvironment();
    }

    @AfterAll
    static void afterAll() throws IOException {
        MavenIntegrationTestEnvironment.deleteDirectory(emptyJarFile.toPath());
    }

    @BeforeEach
    void beforeEach() {
        System.clearProperty(COVERAGE_KEY);
        System.clearProperty(DEBUG_KEY);
    }

    @Test
    void testMissingPom(@TempDir final Path tempDir) {
        final File nonExistingPom = tempDir.resolve("nonExistingPom.xml").toFile();
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> testEnvironment.installPlugin(emptyJarFile, nonExistingPom));
        assertThat(exception.getMessage(), startsWith("E-MPIT-6: Could not find plugins pom "));
    }

    @Test
    void testInvalidPom(@TempDir final Path tempDir) throws IOException {
        final File invalidPom = tempDir.resolve("invalidPom.xml").toFile();
        Files.writeString(invalidPom.toPath(), "invalid content");
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> testEnvironment.installPlugin(emptyJarFile, invalidPom));
        assertThat(exception.getMessage(), startsWith("E-MPIT-4: Failed to plugins pom file "));
    }

    @Test
    void testMissingJar(@TempDir final Path tempDir) {
        final File nonExistingJar = tempDir.resolve("nonExistingJar.jar").toFile();
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> testEnvironment.installPlugin(nonExistingJar, dummyPluginPom));
        assertThat(exception.getMessage(), startsWith("E-MPIT-5: Could not find plugins jar"));
    }

    @Test
    void testInstallPlugin() throws IOException {
        final Path pluginInLocalRepo = testEnvironment.getLocalMavenRepository()
                .resolve(Path.of("com", "example", "dummy-plugin"));
        MavenIntegrationTestEnvironment.deleteDirectory(pluginInLocalRepo);
        testEnvironment.installPlugin(emptyJarFile, dummyPluginPom);
        assertPluginIsInstalled(pluginInLocalRepo);
    }

    @Test
    void testInstallPluginWithoutJar() throws IOException {
        final Path pluginInLocalRepo = testEnvironment.getLocalMavenRepository()
                .resolve(Path.of("com", "example", "dummy-plugin"));
        MavenIntegrationTestEnvironment.deleteDirectory(pluginInLocalRepo);
        testEnvironment.installWithoutJar(dummyPluginPom);
        final Path pluginVersionInRepo = pluginInLocalRepo.resolve("0.1.0");
        assertThat(Files.readString(pluginVersionInRepo.resolve("dummy-plugin-0.1.0.pom")),
                equalTo(Files.readString(dummyPluginPom.toPath())));
    }

    @Test
    void testCoverageEnabled(@TempDir final Path tempDir) {
        System.setProperty(COVERAGE_KEY, "true");
        final Map<String, String> mavenEnvironmentVars = testEnvironment.getVerifier(tempDir).getEnvironmentVariables();
        assertThat(mavenEnvironmentVars.get("MAVEN_OPTS"), matchesPattern(
                "\\Q-javaagent:\\E.*\\Qtarget/jacoco-agent/org.jacoco.agent-runtime.jar=output=file,destfile=\\E.*\\Qtarget/jacoco-mvn-0.exec\\E"));
    }

    @Test
    void testCoverageDisabled(@TempDir final Path tempDir) {
        final Map<String, String> mavenEnvironmentVars = testEnvironment.getVerifier(tempDir).getEnvironmentVariables();
        assertFalse(mavenEnvironmentVars.containsKey("MAVEN_OPTS"));
    }

    @Test
    void testDebugEnabled(@TempDir final Path tempDir) {
        System.setProperty(DEBUG_KEY, "true");
        assertTrue(testEnvironment.getVerifier(tempDir).isDebugJvm());
    }

    @Test
    void testDebugDisabled(@TempDir final Path tempDir) {
        assertFalse(testEnvironment.getVerifier(tempDir).isDebugJvm());
    }

    @Test
    void testReInstallPlugin(@TempDir final Path tempDir) throws IOException {
        final Path brokenJar = tempDir.resolve("broken.jar");
        Files.writeString(brokenJar, "some string content");
        testEnvironment.installPlugin(brokenJar.toFile(), dummyPluginPom);
        testEnvironment.installPlugin(emptyJarFile, dummyPluginPom);
        final Path pluginInLocalRepo = testEnvironment.getLocalMavenRepository()
                .resolve(Path.of("com", "example", "dummy-plugin"));
        assertPluginIsInstalled(pluginInLocalRepo);
    }

    private void assertPluginIsInstalled(final Path pluginInLocalRepo) {
        final Path pluginVersionInRepo = pluginInLocalRepo.resolve("0.1.0");
        assertAll(//
                () -> assertThat(Files.readAllBytes(pluginVersionInRepo.resolve("dummy-plugin-0.1.0.jar")),
                        equalTo(Files.readAllBytes(emptyJarFile.toPath()))),
                () -> assertThat(Files.readString(pluginVersionInRepo.resolve("dummy-plugin-0.1.0.pom")),
                        equalTo(Files.readString(dummyPluginPom.toPath())))//
        );
    }
}
