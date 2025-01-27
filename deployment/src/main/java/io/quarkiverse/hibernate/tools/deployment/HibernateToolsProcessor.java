package io.quarkiverse.hibernate.tools.deployment;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ConsoleCommandBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.logging.Log;

class HibernateToolsProcessor {

    private static final String FEATURE = "hibernate-tools";

    private HibernateToolsConfig hibernateToolsConfig;

    @BuildStep
    FeatureBuildItem feature() {
        Log.info("Building FeatureBuildItem: " + FEATURE);
        Log.info("  - Configured datasource : " + hibernateToolsConfig.jdbc().url());
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ConsoleCommandBuildItem revengCommand() {
        return new ConsoleCommandBuildItem(new RevengCommand());
    }

    @CommandDefinition(name = "reveng", description = "Perform reverse engineering from the database", aliases = { "r" })
    public static class RevengCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
            commandInvocation.getShell().writeln("Hello from Reveng execution in Quarkus Terminal!");
            return CommandResult.SUCCESS;
        }
    }

}
