package io.quarkiverse.hibernate.tools.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HibernateToolsService {

    @Inject
    HibernateToolsConfig hibernateToolsConfig;

    public void perform() {
        System.out.println("Hello from HibernateToolsService");
        System.out.println("  -> Configured datasource: " + hibernateToolsConfig.jdbc().url());
    }

}
