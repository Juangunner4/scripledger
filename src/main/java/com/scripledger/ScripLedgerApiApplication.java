package com.scripledger;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class ScripLedgerApiApplication {
    public static void main(String... args) {
        Quarkus.run(args);
    }
}
