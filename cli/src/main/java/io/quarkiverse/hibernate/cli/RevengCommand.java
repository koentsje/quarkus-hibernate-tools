package io.quarkiverse.hibernate.cli;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
import io.quarkus.bootstrap.BootstrapException;
import io.quarkus.bootstrap.app.AdditionalDependency;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.builder.BuildChain;
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
import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "reveng", mixinStandardHelpOptions = true, version = "6.6.5.Final", subcommands = { ToJavaCommand.class })
public class RevengCommand implements Callable<Integer> {

    @Inject
    HibernateToolsConfig hibernateToolsConfig;

    @Override
    public Integer call() throws Exception {
        System.out.println("Hello from Quarkus PicoCLI Hibernate Tools:");
        acceptSomeStuff();
        // doSomeStuff();
        //     HibernateToolsService.toJava(hibernateToolsConfig);
        return 0;
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
            CuratedApplication curatedApplication = QuarkusBootstrap
                    .builder()
                    .setApplicationRoot(projectRoot)
                    .setProjectRoot(projectRoot)
                    .build()
                    .bootstrap();
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
            QuarkusClassLoader.Builder configCLBuilder = QuarkusClassLoader.builder("Reveng CL", quarkusClassLoader, false);
            SmallRyeConfigProviderResolver smallRyeConfigProviderResolver = ((SmallRyeConfigProviderResolver) SmallRyeConfigProviderResolver
                    .instance());
            Config config = smallRyeConfigProviderResolver.getConfig(quarkusClassLoader);
            System.out.println("Config found: " + config);
            for (ConfigSource cs : config.getConfigSources()) {
                System.out.println(cs.getName());
            }

            ConfigValue configValue = (ConfigValue) config.getConfigValue("quarkus.datasource.jdbc.url");
            System.out.println("JDBC URL Config Value: " + configValue);
            System.out.println("  JDBC URL: " + configValue.getValue());
        } catch (BootstrapException e) {
            e.printStackTrace();
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
                QuarkusBuildCloseablesBuildItem buildCloseables = new QuarkusBuildCloseablesBuildItem();

                try {

                    Thread.currentThread().setContextClassLoader(builder.getDeploymentClassLoader());
                    BuildChainBuilder chainBuilder = BuildChain.builder();
                    chainBuilder.setClassLoader(builder.getDeploymentClassLoader());

                    BuildTimeConfigurationReader reader = new BuildTimeConfigurationReader(builder.getDeploymentClassLoader());
                    SmallRyeConfig src = reader.initConfiguration(
                            builder.getLaunchMode(),
                            builder.getBuildSystemProperties() == null ? new Properties() : builder.getBuildSystemProperties(),
                            builder.getRuntimeProperties() == null ? new Properties() : builder.getRuntimeProperties(),
                            curatedApplication.getApplicationModel().getPlatformProperties());
                    QuarkusConfigFactory.setConfig(src);
                    BuildTimeConfigurationReader.ReadResult readResult = reader.readConfiguration(src);

                    System.out.println("Dumping the BuildTimeConfigurationReader.ReadResult config mappings:");
                    for (RootDefinition rd : readResult.getAllRoots()) {
                        System.out.println(rd.getName());
                    }

                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }

            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            /*
             * try {
             *
             * BuildResult buildResult = builder.build().run();
             * Map<String, byte[]> result = new HashMap<>();
             * for (GeneratedClassBuildItem i : buildResult.consumeMulti(GeneratedClassBuildItem.class)) {
             * result.put(i.internalName() + ".class", i.getClassData());
             * }
             * for (GeneratedResourceBuildItem i : buildResult.consumeMulti(GeneratedResourceBuildItem.class)) {
             * result.put(i.getName(), i.getData());
             * }
             * for (Map.Entry<Path, Set<TransformedClassesBuildItem.TransformedClass>> entry : buildResult
             * .consume(TransformedClassesBuildItem.class).getTransformedClassesByJar().entrySet()) {
             * for (TransformedClassesBuildItem.TransformedClass transformed : entry.getValue()) {
             * if (transformed.getData() != null) {
             * result.put(transformed.getFileName(), transformed.getData());
             * } else {
             * System.out.println("Unable to remove resource " + transformed.getFileName()
             * + " as this is not supported in JBangf");
             * }
             * }
             * }
             * resultMap.put("files", result);
             * final List<String> javaargs = new ArrayList<>();
             * javaargs.add("-Djava.util.logging.manager=org.jboss.logmanager.LogManager");
             * javaargs.add(
             * "-Djava.util.concurrent.ForkJoinPool.common.threadFactory=io.quarkus.bootstrap.forkjoin.QuarkusForkJoinWorkerThreadFactory"
             * );
             * resultMap.put("java-args", javaargs);
             * resultMap.put("main-class", buildResult.consume(MainClassBuildItem.class).getClassName());
             * if (nativeRequested) {
             * resultMap.put("native-image", buildResult.consume(NativeImageBuildItem.class).getPath());
             * }
             * } catch (RuntimeException e) {
             * throw e;
             * } catch (Exception e) {
             * throw new RuntimeException(e);
             * }
             *
             *
             */
        }

    }

}
