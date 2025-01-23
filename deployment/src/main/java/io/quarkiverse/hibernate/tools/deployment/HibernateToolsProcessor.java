package io.quarkiverse.hibernate.tools.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.logging.Log;

class HibernateToolsProcessor {

    private static final String FEATURE = "hibernate-tools";

    private HibernateToolsConfig hibernateToolsConfig;

    @BuildStep
    FeatureBuildItem feature() {
        Log.info("Building FeatureBuildItem: " + FEATURE);
        Log.info("  - Configured datasource : " + hibernateToolsConfig.jdbc().url());
        return new FeatureBuildItem(FEATURE);
    }
}
