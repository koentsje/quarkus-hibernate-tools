package io.quarkiverse.hibernate.tools.runtime;

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
public class gHibernateToolsService {

    public static void toJava(HibernateToolsConfig hibernateToolsConfig) {
        System.out.println("Starting generation of Java Files");
        Exporter javaExporter = ExporterFactory.createExporter(ExporterType.JAVA);
        javaExporter.getProperties().put(
                ExporterConstants.METADATA_DESCRIPTOR,
                createJdbcDescriptor(setupReverseEngineeringStrategy(), new Properties()));
        javaExporter.start();
        System.out.println("Java file generation Finished");
    }

    private static RevengStrategy setupReverseEngineeringStrategy() {
        RevengStrategy strategy = RevengStrategyFactory.createReverseEngineeringStrategy(
                null,
                null);
        strategy.setSettings(new RevengSettings(strategy));
        return strategy;
    }

    private static MetadataDescriptor createJdbcDescriptor(RevengStrategy strategy, Properties properties) {
        return MetadataDescriptorFactory
                .createReverseEngineeringDescriptor(
                        strategy,
                        properties);
    }

}
