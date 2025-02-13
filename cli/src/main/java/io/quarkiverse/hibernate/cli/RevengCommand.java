package io.quarkiverse.hibernate.cli;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.maven.model.Model;

import io.quarkus.bootstrap.BootstrapException;
import io.quarkus.bootstrap.app.AdditionalDependency;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;
import io.quarkus.bootstrap.resolver.maven.workspace.ModelUtils;
import io.quarkus.bootstrap.utils.BuildToolHelper;
import io.quarkus.builder.BuildChainBuilder;
import io.quarkus.builder.BuildStepBuilder;
import io.quarkus.deployment.QuarkusAugmentor;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.configuration.BuildTimeConfigurationReader;
import io.quarkus.deployment.configuration.definition.RootDefinition;
import io.quarkus.deployment.pkg.builditem.ArtifactResultBuildItem;
import io.quarkus.deployment.pkg.builditem.DeploymentResultBuildItem;
import io.quarkus.deployment.pkg.builditem.NativeImageBuildItem;
import io.quarkus.deployment.pkg.builditem.ProcessInheritIODisabledBuildItem;
import io.quarkus.dev.spi.DevModeType;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
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
            //            System.out.println("Quarkus augment class loader : " + quarkusClassLoader.getClass().getName());
            //            Class<?> smallRyeConfigProviderResolverClass = lookup("io.smallrye.config.SmallRyeConfigProviderResolver",
            //                    quarkusClassLoader);
            //            System.out.println(
            //                    "SmallRyeConfigProviderResolverClass: " + smallRyeConfigProviderResolverClass.getName().getClass());
            //            Object smallRyeProviderResolver = runClassMethod(smallRyeConfigProviderResolverClass, "instance");
            //            System.out.println("SmallRyeConfigProviderResolver: " + smallRyeProviderResolver);
            //            SmallRyeConfigProviderResolver smallRyeConfigProviderResolver = ((SmallRyeConfigProviderResolver) SmallRyeConfigProviderResolver
            //                    .instance());
            //Config config = smallRyeConfigProviderResolver.getConfig(quarkusClassLoader);
            //System.out.println("Config found: " + config);
            //            SmallRyeConfigProviderResolver smallRyeConfigProviderResolver = ((SmallRyeConfigProviderResolver) SmallRyeConfigProviderResolver
            //                    .instance());
            //            Config config = smallRyeConfigProviderResolver.getConfig(quarkusClassLoader);
            //            System.out.println("Config found: " + config);
            //            for (ConfigSource cs : config.getConfigSources()) {
            //                System.out.println(cs.getName());
            //            }
            //
            //            ConfigValue configValue = (ConfigValue) config.getConfigValue("quarkus.datasource.jdbc.url");
            //            System.out.println("JDBC URL Config Value: " + configValue);
            //            System.out.println("  JDBC URL: " + configValue.getValue());
        }
        // acceptSomeStuff();
        //        try {
        //            doSomeStuff();
        //        } catch (Throwable t) {
        //            System.out.println("What the fuck happens??? " + t.getMessage());
        //        }
        //     HibernateToolsService.toJava(hibernateToolsConfig);
        return 0;
    }

    private void handleMaven() {
        try {
            Path mavenPomPath = mavenPom();
            System.out.println("maven pom location : " + mavenPomPath);
            Model model = ModelUtils.readModel(mavenPomPath);
            System.out.println("target folder is : " + model.getBuild().getOutputDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Could not read maven pom file", e);
        }
    }

    private void handleGradle() {
        System.out.println("handling gradle case");
    }

    private Path mavenPom() {
        return projectRoot().resolve("pom.xml");
    }

    private void acceptSomeStuff() {
        System.out.println("Accepting some stuff");
        Path projectRoot = projectRoot();
        System.out.println("Project root: " + projectRoot);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            CuratedApplication curatedApplication = QuarkusBootstrap
                    .builder()
                    .setApplicationRoot(projectRoot)
                    .setProjectRoot(projectRoot)
                    .build()
                    .bootstrap();
            System.out.println("Curated application: " + curatedApplication);
            accept(curatedApplication, resultMap);
            for (String k : resultMap.keySet()) {
                System.out.println(k + " -> " + resultMap.get(k));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSomeStuff() {
        System.out.println("Doing some stuff");
        Path projectRoot = projectRoot();
        System.out.println("Project root: " + projectRoot);
        try {
            //            CuratedApplication curatedApplication = QuarkusBootstrap
            //                    .builder()
            //                    .setApplicationRoot(projectRoot)
            //                    .setProjectRoot(projectRoot)
            //                    .build()
            //                    .bootstrap();
            CuratedApplication curatedApplication = createMavenBuilder(projectRoot).build().bootstrap();
            System.out.println("Curated application: " + curatedApplication);
            //            QuarkusClassLoader quarkusClassLoader = curatedApplication.createDeploymentClassLoader();
            QuarkusClassLoader quarkusClassLoader = curatedApplication.createDeploymentClassLoader();

            try {
                Enumeration<URL> urls = quarkusClassLoader.getResources("application.properties");
                Iterator<URL> urlIterator = urls.asIterator();
                int i = 0;
                while (urlIterator.hasNext()) {
                    System.out.println("url" + i++ + " = " + urlIterator.next());
                }
            } catch (IOException e) {
                System.out.println("foobar");
            }

            System.out.println("Quarkus ClassLoader: " + quarkusClassLoader);

            SmallRyeConfigBuilder smallRyeConfigBuilder = ConfigUtils
                    .emptyConfigBuilder()
                    .forClassLoader(quarkusClassLoader);

            System.out.println("SmallRyeConfigBuilder is created: " + smallRyeConfigBuilder.getClass().getName());

            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

            try {

                Thread.currentThread().setContextClassLoader(quarkusClassLoader);

                System.out.println("Doing stuff with quarkus classloader");

                SmallRyeConfig smallRyeConfig = smallRyeConfigBuilder.build();

                System.out.println("SmallRyeConfig is built: " + smallRyeConfig.getClass().getName());

                //                SmallRyeConfig config = ConfigUtils.emptyConfigBuilder().build();
                //
                //                System.out.println("Config found: " + config.getClass().getName());
                //                for (ConfigSource cs : config.getConfigSources()) {
                //                    System.out.println(cs.getName());
                //                }
                //
                //                ConfigValue configValue = (ConfigValue) config.getConfigValue("quarkus.datasource.jdbc.url");
                //                System.out.println("JDBC URL Config Value: " + configValue);
                //                System.out.println("  JDBC URL: " + configValue.getValue());

                //                List<Class<?>> configRoots = List.of(HibernateToolsConfig.class);
                //                BuildTimeConfigurationReader buildTimeConfigurationReader = new BuildTimeConfigurationReader(configRoots);
                //                System.out.println("buildTimeConfigurationReader was created succesfully! "
                //                        + buildTimeConfigurationReader.getClass().getName());
                //                SmallRyeConfig config = buildTimeConfigurationReader.initConfiguration(
                //                        LaunchMode.DEVELOPMENT, new Properties(), new Properties(), new HashMap<>());
                //                System.out.println("Configuration is initiated " + config.getClass().getName());

            } catch (Throwable t) {

                System.out.println("Something really bad happened! " + t.getMessage());

            } finally {

                System.out.println("Restoring the original classloader");
                Thread.currentThread().setContextClassLoader(originalClassLoader);

            }

        } catch (BootstrapException e) {

            System.out.println("BootstrapException: " + e.getMessage());

            //            e.printStackTrace();
        } catch (Throwable t) {

            System.out.println("Something seriously bad happened!" + t.getMessage());

        }
    }

    private Path projectRoot() {
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    }

    public void accept(CuratedApplication curatedApplication, Map<String, Object> resultMap) {

        System.out.println("Curated Application is accepted!");

        QuarkusClassLoader classLoader = curatedApplication.getOrCreateAugmentClassLoader();

        try (QuarkusClassLoader deploymentClassLoader = curatedApplication.createDeploymentClassLoader()) {
            QuarkusBootstrap quarkusBootstrap = curatedApplication.getQuarkusBootstrap();
            QuarkusAugmentor.Builder builder = QuarkusAugmentor.builder()
                    .setRoot(quarkusBootstrap.getApplicationRoot())
                    .setClassLoader(classLoader)
                    .addFinal(ApplicationClassNameBuildItem.class)
                    .setTargetDir(quarkusBootstrap.getTargetDirectory())
                    .setDeploymentClassLoader(curatedApplication.createDeploymentClassLoader())
                    .setBuildSystemProperties(quarkusBootstrap.getBuildSystemProperties())
                    .setRuntimeProperties(quarkusBootstrap.getRuntimeProperties())
                    .setEffectiveModel(curatedApplication.getApplicationModel());
            if (quarkusBootstrap.getBaseName() != null) {
                builder.setBaseName(quarkusBootstrap.getBaseName());
            }
            if (quarkusBootstrap.getOriginalBaseName() != null) {
                builder.setOriginalBaseName(quarkusBootstrap.getOriginalBaseName());
            }

            boolean auxiliaryApplication = curatedApplication.getQuarkusBootstrap().isAuxiliaryApplication();
            builder.setAuxiliaryApplication(auxiliaryApplication);
            builder.setAuxiliaryDevModeType(
                    curatedApplication.getQuarkusBootstrap().isHostApplicationIsTestOnly() ? DevModeType.TEST_ONLY
                            : (auxiliaryApplication ? DevModeType.LOCAL : null));
            builder.setLaunchMode(LaunchMode.NORMAL);
            builder.setRebuild(quarkusBootstrap.isRebuild());
            builder.setLiveReloadState(
                    new LiveReloadBuildItem(false, Collections.emptySet(), new HashMap<>(), null));
            for (AdditionalDependency i : quarkusBootstrap.getAdditionalApplicationArchives()) {
                //this gets added to the class path either way
                //but we only need to add it to the additional app archives
                //if it is forced as an app archive
                if (i.isForceApplicationArchive()) {
                    builder.addAdditionalApplicationArchive(i.getResolvedPaths());
                }
            }
            builder.addBuildChainCustomizer(new Consumer<BuildChainBuilder>() {
                @Override
                public void accept(BuildChainBuilder builder) {
                    final BuildStepBuilder stepBuilder = builder.addBuildStep((ctx) -> {
                        ctx.produce(new ProcessInheritIODisabledBuildItem());
                    });
                    stepBuilder.produces(ProcessInheritIODisabledBuildItem.class).build();
                }
            });
            builder.excludeFromIndexing(quarkusBootstrap.getExcludeFromClassPath());
            builder.addFinal(GeneratedClassBuildItem.class);
            builder.addFinal(MainClassBuildItem.class);
            builder.addFinal(GeneratedResourceBuildItem.class);
            builder.addFinal(TransformedClassesBuildItem.class);
            builder.addFinal(DeploymentResultBuildItem.class);
            // note: quarkus.package.type is deprecated
            boolean nativeRequested = "native".equals(System.getProperty("quarkus.package.type"))
                    || "true".equals(System.getProperty("quarkus.native.enabled"));
            boolean containerBuildRequested = Boolean.getBoolean("quarkus.container-image.build");
            if (nativeRequested) {
                builder.addFinal(NativeImageBuildItem.class);
            }
            if (containerBuildRequested) {
                //TODO: this is a bit ugly
                //we don't necessarily need these artifacts
                //but if we include them it does mean that you can auto create docker images
                //and deploy to kube etc
                //for an ordinary build with no native and no docker this is a waste
                builder.addFinal(ArtifactResultBuildItem.class);
            }

            try {

                System.out.println("Beginning doing the real stuff");
                ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                //QuarkusBuildCloseablesBuildItem buildCloseables = new QuarkusBuildCloseablesBuildItem();

                try {
                    System.out.println("About to install deployment class loader");
                    Thread.currentThread().setContextClassLoader(builder.getDeploymentClassLoader());
                    System.out.println("Doing stuff with deployment class loader");
                    //BuildChainBuilder chainBuilder = BuildChain.builder();
                    //System.out.println("chainBuilder is created : " + chainBuilder.getClass().getName());
                    //    chainBuilder.setClassLoader(builder.getDeploymentClassLoader());
                    //
                    try {
                        BuildTimeConfigurationReader reader = new BuildTimeConfigurationReader(
                                builder.getDeploymentClassLoader());
                        System.out.println("BuildTimeConfigurationReader was created: " + reader.getClass().getName());
                        SmallRyeConfig src = reader.initConfiguration(
                                builder.getLaunchMode(),
                                builder.getBuildSystemProperties() == null ? new Properties()
                                        : builder.getBuildSystemProperties(),
                                builder.getRuntimeProperties() == null ? new Properties() : builder.getRuntimeProperties(),
                                curatedApplication.getApplicationModel().getPlatformProperties());
                        QuarkusConfigFactory.setConfig(src);
                        BuildTimeConfigurationReader.ReadResult readResult = reader.readConfiguration(src);

                        System.out.println("Dumping the BuildTimeConfigurationReader.ReadResult config mappings:");
                        for (RootDefinition rd : readResult.getAllRoots()) {
                            System.out.println(rd.getName());
                        }

                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                } finally {
                    System.out.println("About to swap back classloaders");
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                    System.out.println("ClassLoaders are swapped back");
                }

            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

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
                result.toFile().mkdirs();
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
