package io.quarkiverse.hibernate.tools.runtime;

import jakarta.inject.Singleton;

@Singleton
public class HibernateToolsService {

    public static void perform(HibernateToolsConfig hibernateToolsConfig) {
        System.out.println("  -> Configured datasource: " + hibernateToolsConfig.jdbc().url());
    }

}
