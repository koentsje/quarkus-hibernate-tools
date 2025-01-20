package io.quarkiverse.hibernate.cli;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "reveng")
public class RevEngCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("Hello from RevEng CLI!");
        return 0;
    }

}
