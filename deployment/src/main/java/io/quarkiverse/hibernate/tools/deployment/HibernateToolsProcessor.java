package io.quarkiverse.hibernate.tools.deployment;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ConsoleCommandBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.logging.Log;

class HibernateToolsProcessor {

    private static final String FEATURE = "hibernate-tools";

    HibernateToolsConfig hibernateToolsConfig;

    @BuildStep
    FeatureBuildItem feature() {
        Log.info("Building FeatureBuildItem: " + FEATURE);
        Log.info("  - Configured datasource : " + hibernateToolsConfig.jdbc().url());
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ConsoleCommandBuildItem revengCommand() {
        return new ConsoleCommandBuildItem(new RevengCommand(hibernateToolsConfig));
    }

}
