package io.quarkiverse.hibernate.tools.deployment;

import java.util.List;

import org.aesh.command.*;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;

@GroupCommandDefinition(name = "reveng", description = "Perform reverse engineering from the database", aliases = { "r" })
public class RevengCommand implements GroupCommand<CommandInvocation> {

    HibernateToolsConfig hibernateToolsConfig;

    RevengCommand(HibernateToolsConfig hibernateToolsConfig) {
        this.hibernateToolsConfig = hibernateToolsConfig;
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) {
        commandInvocation.getShell().writeln("Hibernate Tools Reverse Engineering Configuration :");
        commandInvocation.getShell().writeln("  datasource : " + hibernateToolsConfig.jdbc().url());
        return CommandResult.SUCCESS;
    }

    @Override
    public List<Command<CommandInvocation>> getCommands() {
        return List.of(new ToJavaCommand(hibernateToolsConfig));
    }
}
