package io.quarkiverse.hibernate.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.hibernate.tool.api.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

public class RevengCommandTest {

    final private CommandLine commandLine = new CommandLine(new RevengCommand());
    final private StringWriter stringWriter = new StringWriter();

    @BeforeEach
    void beforeEach() {
        commandLine.setOut(new PrintWriter(stringWriter));
    }

    @Test
    void testVersion() {
        commandLine.execute("-V");
        assertEquals(Version.versionString() + "\n", stringWriter.toString());
    }

}
