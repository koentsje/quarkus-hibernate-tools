package io.quarkiverse.hibernate.tools.deployment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

import io.quarkiverse.hibernate.tools.runtime.HibernateToolsConfig;
import io.quarkiverse.hibernate.tools.runtime.HibernateToolsService;

@CommandDefinition(name = "to-java", description = "Generate Java files from the database tables", aliases = { "j" })
public class ToJavaCommand implements Command<CommandInvocation> {

    HibernateToolsConfig hibernateToolsConfig;

    ToJavaCommand(HibernateToolsConfig hibernateToolsConfig) {
        this.hibernateToolsConfig = hibernateToolsConfig;
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream savedOutputPrintStream = System.out;
        System.setOut(new PrintStream(byteArrayOutputStream));
        HibernateToolsService.perform(hibernateToolsConfig);
        commandInvocation.getShell().writeln("Hibernate Tools Java File Generation : ");
        commandInvocation.getShell().write(byteArrayOutputStream.toString());
        System.setOut(savedOutputPrintStream);
        return CommandResult.SUCCESS;
    }

}
