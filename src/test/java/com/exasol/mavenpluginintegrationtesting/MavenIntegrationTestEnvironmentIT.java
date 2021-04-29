package com.exasol.mavenpluginintegrationtesting;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.exasol.errorreporting.ExaError;

class MavenIntegrationTestEnvironmentIT {
    private static final Path dummyPlugin = Path.of("src", "test", "resources", "dummy-plugin");
    private static File dummyPluginJar;
    private static File dummyPluginPom;
    private static MavenIntegrationTestEnvironment testEnvironment;

    @BeforeAll
    static void beforeAll() {
        dummyPluginJar = dummyPlugin.resolve(Path.of("target", "dummy-plugin-0.1.0.jar")).toFile();
        if (!dummyPluginJar.exists()) {
            throw new IllegalStateException(ExaError.messageBuilder("E-MPIT-7")
                    .message("Could not find dummy project jar {{jar}}.", dummyPluginJar)
                    .mitigation("Run mvn verify to build it.").toString());
        }
        dummyPluginPom = dummyPlugin.resolve("pom.xml").toFile();
        testEnvironment = new MavenIntegrationTestEnvironment();
    }

    @Test
    void testInstallAndRunPlugin(@TempDir final Path tempDir) throws IOException, VerificationException {
        testEnvironment.installPlugin(dummyPluginJar, dummyPluginPom);
        writePomFile(tempDir);
        final Verifier verifier = testEnvironment.getVerifier(tempDir);
        verifier.executeGoal("com.example:dummy-plugin:hello");
        verifier.verifyTextInLog("Hello World!");
    }

    private void writePomFile(final Path tempDir) throws IOException {
        Files.copy(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream("project-using-dummy-plugin/pom.xml")),
                tempDir.resolve("pom.xml"), StandardCopyOption.REPLACE_EXISTING);
    }
}