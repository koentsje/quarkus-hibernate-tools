package io.quarkiverse.hibernate.tools.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class HibernateToolsProcessor {

    private static final String FEATURE = "hibernate-tools";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
