package io.quarkiverse.hibernate.tools.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.datasource")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface HibernateToolsConfig {

    /**
     * the datasource jdbc properties
     */
    Jdbc jdbc();

    /**
     * the datasource user name
     */
    @WithDefault("user name")
    String username();

    /**
     * the datasource user password
     */
    Optional<String> password();

    interface Jdbc {

        /**
         * the datasource jdbc url
         */
        @WithDefault("<datasource jdbc url>")
        String url();

    }

}
