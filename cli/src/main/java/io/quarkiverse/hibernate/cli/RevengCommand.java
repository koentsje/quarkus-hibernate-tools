package io.quarkiverse.hibernate.cli;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import io.quarkus.bootstrap.BootstrapException;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;
import io.quarkus.bootstrap.resolver.maven.workspace.ModelUtils;
import io.quarkus.bootstrap.utils.BuildToolHelper;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "reveng", mixinStandardHelpOptions = true, version = "6.6.6.Final", subcommands = { ToJavaCommand.class,
        FooCommand.class })
public class RevengCommand implements Callable<Integer> {

    //    @Inject
    //    HibernateToolsConfig hibernateToolsConfig;

    private Class<?> lookup(String className, ClassLoader loader) {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private Object runMethod(Object object, String methodName, ClassLoader loader) {
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, new Class[] { ClassLoader.class });
            System.out.println("Method found: " + method);
            return object.getClass().getDeclaredMethod(methodName, new Class[] { ClassLoader.class }).invoke(object, loader);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Object construct(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object runGetConfigValueMethod(Object receiver, String configName) {
        Object result = null;
        try {
            Method m = receiver.getClass().getDeclaredMethod("getConfigValue", new Class[] { String.class });
            System.out.println("getConfigValue method is found: " + m);
            result = m.invoke(receiver, configName);
            System.out.println("returning result: " + result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Object runGetValueMethod(Object receiver) {
        Object result = null;
        try {
            Method m = receiver.getClass().getDeclaredMethod("getValue");
            System.out.println("getValue method was found: " + m);
            result = m.invoke(receiver);
            System.out.println("returning result: " + result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Hello from Quarkus PicoCLI Hibernate Tools:");
        try (CuratedApplication curatedApplication = createCuratedApplication(projectRoot())) {
            System.out.println("Curated application was created: " + curatedApplication);
            QuarkusClassLoader quarkusClassLoader = curatedApplication.createDeploymentClassLoader();
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(quarkusClassLoader);
                System.out.println("Quarkus augment class loader : " + quarkusClassLoader.getClass().getName());
                Class<?> smallRyeConfigProviderResolverClass = lookup("io.smallrye.config.SmallRyeConfigProviderResolver",
                        quarkusClassLoader);
                System.out.println(
                        "SmallRyeConfigProviderResolverClass: " + smallRyeConfigProviderResolverClass.getName().getClass());
                Object smallRyeProviderResolver = construct(smallRyeConfigProviderResolverClass);
                System.out.println("smallRyeProviderResolver is created: " + smallRyeProviderResolver.getClass().getName());
                Object config = runMethod(smallRyeProviderResolver, "getConfig", quarkusClassLoader);
                System.out.println("config is created : " + config);
                Object configValue = runGetConfigValueMethod(config, "quarkus.datasource.jdbc.url");
                System.out.println("JDBC URL Config Value: " + configValue);
                Object value = runGetValueMethod(configValue);
                System.out.println("  JDBC URL: " + runGetValueMethod(configValue));

            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }
        return 0;
    }

    private Path projectRoot() {
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    }

    private QuarkusBootstrap.Builder createQuarkusBootstrapBuilder() {
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

    private QuarkusBootstrap.Builder createMavenBuilder(Path projectRootPath) {
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

    private Path getMavenClassesPath(Path projectRootPath) {
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

    private QuarkusBootstrap.Builder createGradleBuilder(Path projectRootPath) {
        throw new RuntimeException("not yet implemented");
    }

    private QuarkusBootstrap.Builder createInitialBuilder(Path projectRootPath) {
        return QuarkusBootstrap.builder()
                .setBaseClassLoader(RevengCommand.class.getClassLoader())
                .setIsolateDeployment(true)
                .setMode(QuarkusBootstrap.Mode.DEV);
    }

    private CuratedApplication createCuratedApplication(Path projectRootPath) {
        try {
            return createQuarkusBootstrapBuilder().build().bootstrap();
        } catch (BootstrapException e) {
            throw new RuntimeException("Problem while bootstrapping Quarkus", e);
        }
    }

}
