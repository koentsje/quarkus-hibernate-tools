package io.quarkiverse.hibernate.cli;

import java.util.concurrent.Callable;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "reveng", mixinStandardHelpOptions = true, version = "6.6.6.Final", subcommands = { ToJavaCommand.class })
public class RevengCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("Running Hibernate Tools reveng command");
        ConfigUtil.doTheThing();
        return 0;
    }

}
