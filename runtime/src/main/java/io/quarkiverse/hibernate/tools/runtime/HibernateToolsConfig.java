package io.quarkiverse.hibernate.tools.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.datasource")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface HibernateToolsConfig {

    /**
     * the datasource jdbc properties
     */
    Jdbc jdbc();

    interface Jdbc {

        /**
         * the datasource jdbc url
         */
        @WithDefault("<datasource jdbc url>")
        String url();

    }

}
