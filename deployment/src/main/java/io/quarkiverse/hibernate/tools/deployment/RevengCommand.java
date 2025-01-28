package io.quarkiverse.hibernate.tools.deployment;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;

@CommandDefinition(name = "reveng", description = "Perform reverse engineering from the database", aliases = { "r" })
public class RevengCommand implements Command {

    HibernateToolsConfig hibernateToolsConfig;

    RevengCommand(HibernateToolsConfig hibernateToolsConfig) {
        this.hibernateToolsConfig = hibernateToolsConfig;
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
        commandInvocation.getShell().writeln("Hello from Reveng execution in Quarkus Terminal!");
        commandInvocation.getShell().writeln("  - Configured database: " + hibernateToolsConfig.jdbc().url());
        return CommandResult.SUCCESS;
    }
}
