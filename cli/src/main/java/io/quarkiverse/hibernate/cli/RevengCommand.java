package io.quarkiverse.hibernate.cli;

import java.util.concurrent.Callable;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "reveng", mixinStandardHelpOptions = true, version = "6.6.6.Final", subcommands = { ToJavaCommand.class })
public class RevengCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("Running Hibernate Tools reveng command");
        HibernateToolsConfig hibernateToolsConfig = ConfigUtil.readConfiguration();
        System.out.println("JDBC URL = " + hibernateToolsConfig.jdbc().url());
        System.out.println("Username = " + hibernateToolsConfig.username());
        System.out.println("Password = " + hibernateToolsConfig.password());
        return 0;
    }

}
