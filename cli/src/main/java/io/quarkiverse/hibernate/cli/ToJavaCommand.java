package io.quarkiverse.hibernate.cli;

import java.util.concurrent.Callable;

import picocli.CommandLine;

@CommandLine.Command(name = "to-java")
public class ToJavaCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("Generating Java from RevEng CLI!");
        return 0;
    }

}
