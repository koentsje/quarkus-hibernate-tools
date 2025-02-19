package io.quarkiverse.hibernate.cli;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
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

    public static HibernateToolsConfig readConfiguration() {
        System.out.println("Reading the configuration");
        try (CuratedApplication curatedApplication = createCuratedApplication(projectRoot())) {
            QuarkusClassLoader quarkusClassLoader = curatedApplication.createDeploymentClassLoader();
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(quarkusClassLoader);
                return new HibernateToolsConfigImpl(resolveConfig());
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }
    }

    private static Object resolveConfig() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Object configProviderResolver = classLoader
                    .loadClass("io.smallrye.config.SmallRyeConfigProviderResolver")
                    .getDeclaredConstructor()
                    .newInstance();
            return configProviderResolver
                    .getClass()
                    .getDeclaredMethod("getConfig", ClassLoader.class)
                    .invoke(configProviderResolver, classLoader);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException("Could not obtain the configuration", e);
        }

    }

    private static String getConfigValue(String name, Object config) {
        try {
            Object configValue = config
                    .getClass()
                    .getDeclaredMethod("getConfigValue", String.class)
                    .invoke(config, name);
            return (String) configValue
                    .getClass()
                    .getDeclaredMethod("getValue")
                    .invoke(configValue);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException("Could not obtain the configuration", e);
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

    private static class HibernateToolsConfigImpl implements HibernateToolsConfig {

        private static final String DATASOURCE_JDBC_URL = "quarkus.datasource.jdbc.url";
        private static final String DATASOURCE_USERNAME = "quarkus.datasource.username";
        private static final String DATASOURCE_PASSWORD = "quarkus.datasource.password";

        private final Jdbc jdbc = new JdbcImpl();
        private String url = "";
        private String username = "";
        private String password = null;

        HibernateToolsConfigImpl(Object config) {
            this.url = getConfigValue(DATASOURCE_JDBC_URL, config);
            this.username = getConfigValue(DATASOURCE_USERNAME, config);
            this.password = getConfigValue(DATASOURCE_PASSWORD, config);
        }

        @Override
        public Jdbc jdbc() {
            return this.jdbc;
        }

        @Override
        public String username() {
            return this.username;
        }

        @Override
        public Optional<String> password() {
            return Optional.ofNullable(this.password);
        }

        private class JdbcImpl implements Jdbc {

            @Override
            public String url() {
                return HibernateToolsConfigImpl.this.url;
            }
        }

    }

}
