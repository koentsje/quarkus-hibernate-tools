package io.quarkiverse.hibernate.cli;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.quarkus.bootstrap.BootstrapException;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;
import io.quarkus.bootstrap.resolver.maven.workspace.ModelUtils;
import io.quarkus.bootstrap.utils.BuildToolHelper;

public class ConfigUtil {

    public static void readConfig() {
        System.out.println("Reading the configuration");
        try (CuratedApplication curatedApplication = createCuratedApplication(projectRoot())) {
            QuarkusClassLoader quarkusClassLoader = curatedApplication.createDeploymentClassLoader();
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(quarkusClassLoader);
                Object configProviderResolver = quarkusClassLoader
                        .loadClass("io.smallrye.config.SmallRyeConfigProviderResolver")
                        .getDeclaredConstructor()
                        .newInstance();
                Object config = configProviderResolver
                        .getClass()
                        .getDeclaredMethod("getConfig", ClassLoader.class)
                        .invoke(configProviderResolver, quarkusClassLoader);
                Object configValue = config
                        .getClass()
                        .getDeclaredMethod("getConfigValue", String.class)
                        .invoke(config, "quarkus.datasource.jdbc.url");
                Object value = configValue
                        .getClass()
                        .getDeclaredMethod("getValue")
                        .invoke(configValue);
                System.out.println("JDBC URL: " + value);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new RuntimeException("Could not obtain the configuration", e);
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }

    }

    private static Path projectRoot() {
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    }

    private static QuarkusBootstrap.Builder createQuarkusBootstrapBuilder() {
        Path projectRootPath = projectRoot();
        BuildToolHelper.BuildTool buildTool = BuildToolHelper.findBuildTool(projectRootPath);
        if (buildTool == BuildToolHelper.BuildTool.MAVEN) {
            return createMavenBuilder(projectRootPath);
        } else if (buildTool == BuildToolHelper.BuildTool.GRADLE) {
            return createGradleBuilder(projectRootPath);
        } else {
            throw new RuntimeException("Unable to create QuarkusBootstrap.Builder; Unknown BuildTool");
        }
    }

    private static QuarkusBootstrap.Builder createMavenBuilder(Path projectRootPath) {
        try {
            Path classesDir = projectRootPath.resolve(getMavenClassesPath(projectRootPath));
            BootstrapMavenContext mvnCtx = new BootstrapMavenContext(BootstrapMavenContext
                    .config()
                    .setCurrentProject(projectRootPath.toString()));
            MavenArtifactResolver mvnResolver = new MavenArtifactResolver(mvnCtx);
            return createInitialBuilder(projectRootPath)
                    .setTargetDirectory(classesDir.getParent())
                    .setApplicationRoot(classesDir)
                    .setProjectRoot(projectRootPath)
                    .setMavenArtifactResolver(mvnResolver);
        } catch (BootstrapMavenException e) {
            throw new RuntimeException("Exception while bootstrapping", e);
        }
    }

    private static Path getMavenClassesPath(Path projectRootPath) {
        try {
            String classesFolder = ModelUtils.readModel(projectRootPath.resolve("pom.xml"))
                    .getBuild()
                    .getOutputDirectory();
            classesFolder = classesFolder == null ? "target/classes" : classesFolder;
            Path result = projectRootPath.resolve(classesFolder);
            // create folders if they don't exist;
            if (!result.toFile().exists())
                if (!result.toFile().mkdirs()) {
                    throw new RuntimeException("The Maven target folder could not be created");
                }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Could not determine Maven target folder", e);
        }
    }

    private static QuarkusBootstrap.Builder createGradleBuilder(Path projectRootPath) {
        throw new RuntimeException("not yet implemented");
    }

    private static QuarkusBootstrap.Builder createInitialBuilder(Path projectRootPath) {
        return QuarkusBootstrap.builder()
                .setBaseClassLoader(RevengCommand.class.getClassLoader())
                .setIsolateDeployment(true)
                .setMode(QuarkusBootstrap.Mode.DEV);
    }

    private static CuratedApplication createCuratedApplication(Path projectRootPath) {
        try {
            return createQuarkusBootstrapBuilder().build().bootstrap();
        } catch (BootstrapException e) {
            throw new RuntimeException("Problem while bootstrapping Quarkus", e);
        }
    }

}
