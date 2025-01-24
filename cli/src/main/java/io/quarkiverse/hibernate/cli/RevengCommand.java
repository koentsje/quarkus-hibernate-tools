package io.quarkiverse.hibernate.cli;

import java.util.concurrent.Callable;

import jakarta.inject.Inject;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsService;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "reveng", mixinStandardHelpOptions = true, version = "7.0.0.Beta3", subcommands = { ToJavaCommand.class })
public class RevengCommand implements Callable<Integer> {

    @Inject
    HibernateToolsService hibernateToolsService;

    @Override
    public Integer call() throws Exception {
        hibernateToolsService.perform();
        return 0;
    }

}
