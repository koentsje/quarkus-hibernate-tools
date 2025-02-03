package io.quarkiverse.hibernate.cli;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
import io.quarkus.bootstrap.BootstrapException;
import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.deployment.configuration.BuildTimeConfigurationReader;
import io.quarkus.deployment.configuration.definition.RootDefinition;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import io.smallrye.config.SmallRyeConfig;
import picocli.CommandLine;

@CommandLine.Command(name = "to-java")
public class ToJavaCommand implements Callable<Integer> {

    private Path getApplicationPath() {
        return new File(".").getAbsoluteFile().toPath();
    }

    private QuarkusBootstrap buildQuarkusBootsTrap() {
        Path currentPath = getApplicationPath();
        System.out.println(currentPath);
        return QuarkusBootstrap
                .builder()
                .setApplicationRoot(currentPath)
                .setProjectRoot(currentPath)
                .build();
    }

    private BuildTimeConfigurationReader createBuildTimeConfigurationReader() {
        QuarkusBootstrap qbt = buildQuarkusBootsTrap();
        System.out.println("Quarkus bootstrap is built");
        try (CuratedApplication curatedApplication = qbt.bootstrap()) {
            ClassLoader classLoader = curatedApplication.createDeploymentClassLoader();
            return new BuildTimeConfigurationReader(classLoader);
        } catch (BootstrapException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void dosomeinit(SmallRyeConfig smallRyeConfig) {
        //        BuildTimeConfigurationReader buildTimeConfigurationReader = createBuildTimeConfigurationReader();
        //        SmallRyeConfig smallRyeConfig = buildTimeConfigurationReader.initConfiguration(
        //                LaunchMode.DEVELOPMENT,
        //                new Properties(),
        //                new Properties(),
        //                // otherwise creating the SmallRyeConfig throws an exception
        //                Map.of("platform.quarkus.native.builder-image", "<<ignored>>"));
        System.out.println("Dumping the config sources: ");
        for (ConfigSource cs : smallRyeConfig.getConfigSources()) {
            System.out.println("  - " + cs.getName());
        }
        System.out.println("Dumping the property names: ");
        for (String pn : smallRyeConfig.getPropertyNames()) {
            System.out.println("  - " + pn);
        }
        try {
            HibernateToolsConfig hibernateToolsConfig = smallRyeConfig.getConfigMapping(HibernateToolsConfig.class);
            System.out.println("Hibernate Tools config is created!");
            System.out.println("Configured JDBC URL: " + hibernateToolsConfig.jdbc().url());
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
    }

    private void doSomeMoreStuff() {
        ClassLoader ocl = Thread.currentThread().getContextClassLoader();
        try {
            QuarkusBootstrap qbt = buildQuarkusBootsTrap();
            System.out.println("Quarkus bootstrap is built" + qbt);
            CuratedApplication ca = qbt.bootstrap();
            System.out.println("Curated Application is bootstrapped: " + ca);
            ClassLoader ncl = ca.createDeploymentClassLoader();
            System.out.println("Deployment ClassLoader is created: " + ncl);
            Thread.currentThread().setContextClassLoader(ncl);
            System.out.println("Class loaders are swapped");
            URL url = ncl.getResource("application.properties");
            System.out.println("Url of application.properties: " + url);
            tryToFindHibernateToolsConfig();
            BuildTimeConfigurationReader btcr = new BuildTimeConfigurationReader(ncl);
            SmallRyeConfig src = btcr.initConfiguration(
                    LaunchMode.DEVELOPMENT,
                    new Properties(),
                    new Properties(),
                    ca.getApplicationModel().getPlatformProperties());
            //            dosomeinit(src);
            QuarkusConfigFactory.setConfig(src);
            BuildTimeConfigurationReader.ReadResult readResult = btcr.readConfiguration(src);
            //            inspectReadResult(readResult);
        } catch (Throwable t) {
            System.out.println("Exception!");
            System.out.println(t.getMessage());
            t.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(ocl);
        }
    }

    private void inspectReadResult(BuildTimeConfigurationReader.ReadResult readResult) {
        for (RootDefinition rd : readResult.getAllRoots()) {
            System.out.println(rd.getName() + " -> " + rd.getPrefix());
        }
    }

    private void tryToFindHibernateToolsConfig() {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.foo.GreetingResource");
            System.out.println("class HibernateToolsConfig found!");
        } catch (Throwable e) {
            System.out.println("class HibernateToolsConfig not found...");
        }
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Generating Java from RevEng CLI!");
        doSomeMoreStuff();
        System.out.println("Java was generated...");
        return 0;
    }

}
