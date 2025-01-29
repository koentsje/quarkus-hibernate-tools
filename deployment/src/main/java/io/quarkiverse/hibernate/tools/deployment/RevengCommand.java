package io.quarkiverse.hibernate.tools.deployment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
import io.quarkiverse.hibernate.tools.runtime.HibernateToolsService;

@CommandDefinition(name = "reveng", description = "Perform reverse engineering from the database", aliases = { "r" })
public class RevengCommand implements Command {

    HibernateToolsConfig hibernateToolsConfig;

    RevengCommand(HibernateToolsConfig hibernateToolsConfig) {
        this.hibernateToolsConfig = hibernateToolsConfig;
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream savedOutputPrintStream = System.out;
        System.setOut(new PrintStream(byteArrayOutputStream));
        HibernateToolsService.perform(hibernateToolsConfig);
        commandInvocation.getShell().writeln("Hello from Quarkus DevUI CLI Hibernate Tools!");
        commandInvocation.getShell().writeln(byteArrayOutputStream.toString());
        System.setOut(savedOutputPrintStream);
        return CommandResult.SUCCESS;
    }
}
