package io.quarkiverse.hibernate.cli;

import java.util.concurrent.Callable;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(name = "reveng", mixinStandardHelpOptions = true, version = "7.0.0.Beta3", subcommands = { ToJavaCommand.class })
public class RevengCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("Hello from RevEng CLI!");
        return 0;
    }

}
