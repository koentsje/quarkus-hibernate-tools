package io.quarkiverse.hibernate.cli;

import java.util.concurrent.Callable;

import jakarta.inject.Inject;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
import io.quarkiverse.hibernate.tools.runtime.HibernateToolsService;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "reveng", mixinStandardHelpOptions = true, version = "6.6.5.Final", subcommands = { ToJavaCommand.class })
public class RevengCommand implements Callable<Integer> {

    @Inject
    HibernateToolsConfig hibernateToolsConfig;

    @Override
    public Integer call() throws Exception {
        System.out.println("Hello from Quarkus PicoCLI Hibernate Tools:");
        HibernateToolsService.toJava(hibernateToolsConfig);
        return 0;
    }

}
