package io.quarkiverse.hibernate.tools.runtime;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import jakarta.inject.Singleton;

import org.hibernate.tool.api.export.Exporter;
import org.hibernate.tool.api.export.ExporterConstants;
import org.hibernate.tool.api.export.ExporterFactory;
import org.hibernate.tool.api.export.ExporterType;
import org.hibernate.tool.api.metadata.MetadataDescriptor;
import org.hibernate.tool.api.metadata.MetadataDescriptorFactory;
import org.hibernate.tool.api.reveng.RevengSettings;
import org.hibernate.tool.api.reveng.RevengStrategy;
import org.hibernate.tool.api.reveng.RevengStrategyFactory;

@Singleton
public class HibernateToolsService {

    public static void toJava(HibernateToolsConfig hibernateToolsConfig) {
        System.out.println("Starting generation of Java Files");
        Exporter javaExporter = ExporterFactory.createExporter(ExporterType.JAVA);
        javaExporter.getProperties().put(
                ExporterConstants.METADATA_DESCRIPTOR,
                createJdbcDescriptor(setupReverseEngineeringStrategy(), loadProperties(hibernateToolsConfig)));
        javaExporter.getProperties().put(
                ExporterConstants.DESTINATION_FOLDER, getTargetFolder());
        URL url = Thread.currentThread().getContextClassLoader().getResource("application.properties");
        System.out.println("url: " + url);
        javaExporter.start();
        System.out.println("Java file generation Finished");
    }

    private static File getTargetFolder() {
        Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        File result = new File(root.toFile(), "target/generated-sources");
        final var mkdirs = result.mkdirs();
        return result;
    }

    private static RevengStrategy setupReverseEngineeringStrategy() {
        RevengStrategy strategy = RevengStrategyFactory.createReverseEngineeringStrategy(
                null,
                null);
        strategy.setSettings(new RevengSettings(strategy));
        return strategy;
    }

    private static Properties loadProperties(HibernateToolsConfig hibernateToolsConfig) {
        Properties result = new Properties();
        result.put("hibernate.connection.url", hibernateToolsConfig.jdbc().url());
        System.out.println("hibernate.connection.url : " + result.get("hibernate.connection.url"));
        result.put("hibernate.connection.username", hibernateToolsConfig.username());
        System.out.println("hibernate.connection.username : " + result.get("hibernate.connection.username"));
        result.put("hibernate.connection.password", hibernateToolsConfig.password());
        System.out.println("hibernate.connection.password : " + result.get("hibernate.connection.password"));
        return result;
    }

    private static MetadataDescriptor createJdbcDescriptor(RevengStrategy strategy, Properties properties) {
        return MetadataDescriptorFactory
                .createReverseEngineeringDescriptor(
                        strategy,
                        properties);
    }

}
