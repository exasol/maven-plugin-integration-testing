package com.exasol.mavenpluginintegrationtesting;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Logger;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.utils.io.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.exasol.errorreporting.ExaError;

/**
 * This class sets up an isolated local maven repository for testing maven plugins without installing them.
 */
public class MavenIntegrationTestEnvironment {
    public static final String COVERAGE_KEY = "test.coverage";
    public static final String DEBUG_KEY = "test.debug";
    private static final Logger LOGGER = Logger.getLogger(MavenIntegrationTestEnvironment.class.getName());
    static Path mavenRepo;
    private static int jacocoAgentCounter = 0;

    /**
     * Create a new instance of {@link MavenIntegrationTestEnvironment}.
     */
    public MavenIntegrationTestEnvironment() {
        createTemporaryLocalRepositoryIfNotExist();
        printDebuggerWarning();
    }

    private static void createTemporaryLocalRepositoryIfNotExist() {
        mavenRepo = Path.of(System.getProperty("java.io.tmpdir"))
                .resolve("project-keeper-integration-test-maven-repository");
        if (!mavenRepo.toFile().exists()) {
            mavenRepo.toFile().mkdir();
        }
    }

    private static synchronized int getNextJacocoAgentNumber() {
        final int thisNumber = jacocoAgentCounter;
        jacocoAgentCounter++;
        return thisNumber;
    }

    /**
     * Get a configured {@link Verifier}.
     *
     * @param projectDirectory test projects root directory
     * @return {@link Verifier} for running maven
     */
    public Verifier getVerifier(final Path projectDirectory) {
        final Verifier verifier = buildVerifier(projectDirectory);
        verifier.setLocalRepo(mavenRepo.toAbsolutePath().toString());
        if (isDebuggingEnabled()) {
            verifier.setDebug(true);
            verifier.setDebugJvm(true);
        }
        if (isCoverageEnabled()) {
            addJacocoAgent(verifier);
        }
        return verifier;
    }

    public Path getLocalMavenRepository() {
        return mavenRepo;
    }

    /**
     * Install a given plugin in the test maven repository.
     *
     * @param pluginJar path to the plugins jar
     * @param pluginPom path to the plugins pom file
     */
    public void installPlugin(final File pluginJar, final File pluginPom) {
        assertPluginJarExists(pluginJar);
        assertPluginPomExists(pluginPom);
        final Model projectModel = getProjectModel(pluginPom);
        uninstallPlugin(projectModel);
        installPlugin(pluginJar, pluginPom, projectModel);
    }

    private Verifier buildVerifier(final Path projectDirectory) {
        try {
            return new Verifier(projectDirectory.toFile().getAbsolutePath());
        } catch (final VerificationException exception) {
            throw new IllegalStateException(
                    ExaError.messageBuilder("E-MPIT-1").message("Failed to create maven verifier.").toString(),
                    exception);
        }
    }

    private void printDebuggerWarning() {
        if (isDebuggingEnabled()) {
            LOGGER.warning("Debugging is enabled. Please connect to localhost:8000 using the debugger of your IDE. "
                    + "The tests will wait until the debugger is connected.");
        }
    }

    private boolean isDebuggingEnabled() {
        return System.getProperty(DEBUG_KEY, "false").equals("true");
    }

    private boolean isCoverageEnabled() {
        return System.getProperty(COVERAGE_KEY, "false").equals("true");
    }

    private void addJacocoAgent(final Verifier verifier) {
        final var agentPath = Path.of("target", "jacoco-agent", "org.jacoco.agent-runtime.jar").toAbsolutePath();
        if (!agentPath.toFile().exists()) {
            throw new IllegalStateException(ExaError.messageBuilder("E-MPIT-8").message(
                    "Could not find jacoco agent at {{path}}. The agent is exported by the maven-dependency-plugin during build.",
                    agentPath).mitigation("Run `mvn package`.").toString());
        }
        final var reportPath = Path.of("target", "jacoco-mvn-" + getNextJacocoAgentNumber() + ".exec").toAbsolutePath();
        final String jacocoAgentParameter = "-javaagent:" + agentPath + "=output=file,destfile=" + reportPath;
        verifier.setEnvironmentVariable("MAVEN_OPTS", jacocoAgentParameter);
    }

    private void installPlugin(final File pluginJar, final File pluginPom, final Model projectModel) {

        final Path folderInRepo = getPluginsInLocalRepo(projectModel).resolve(projectModel.getVersion());
        try {
            folderInRepo.toFile().mkdirs();
            final String fileName = projectModel.getArtifactId() + "-" + projectModel.getVersion();
            Files.copy(pluginJar.toPath(), folderInRepo.resolve(fileName + ".jar"),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(pluginPom.toPath(), folderInRepo.resolve(fileName + ".pom"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exception) {
            throw new IllegalStateException(
                    ExaError.messageBuilder("E-MPIT-2")
                            .message("Failed to copy plugin files to local test maven repository.").toString(),
                    exception);
        }
    }

    private void uninstallPlugin(final Model projectModel) {
        try {
            FileUtils.deleteDirectory(getPluginsInLocalRepo(projectModel).toFile());
        } catch (final IOException exception) {
            throw new IllegalStateException(ExaError.messageBuilder("E-MPIT-3")
                    .message("Failed to remove plugin from local maven repository.", exception).toString());
        }
    }

    private Path getPluginsInLocalRepo(final Model projectModel) {
        return mavenRepo.resolve(projectModel.getGroupId().replace(".", "/")).resolve(projectModel.getArtifactId());
    }

    private Model getProjectModel(final File pluginPom) {
        try {
            return new MavenXpp3Reader().read(new FileReader(pluginPom));
        } catch (final XmlPullParserException | IOException exception) {
            throw new IllegalStateException(ExaError.messageBuilder("E-MPIT-4")
                    .message("Failed to plugins pom file {{pom}} for extracting details for installation.", pluginPom)
                    .toString(), exception);
        }
    }

    private void assertPluginJarExists(final File pluginJar) {
        if (!pluginJar.exists()) {
            throw new IllegalArgumentException(
                    ExaError.messageBuilder("E-MPIT-5").message("Could not find plugins jar {{jar}}.", pluginJar)
                            .mitigation("Make sure that you built the jar using mvn package.").toString());
        }
    }

    private void assertPluginPomExists(final File pluginPom) {
        if (!pluginPom.exists()) {
            throw new IllegalArgumentException(
                    ExaError.messageBuilder("E-MPIT-6").message("Could not find plugins pom {{pom}}.", pluginPom)
                            .mitigation("Make sure that you specified the correct location.").toString());
        }
    }
}
