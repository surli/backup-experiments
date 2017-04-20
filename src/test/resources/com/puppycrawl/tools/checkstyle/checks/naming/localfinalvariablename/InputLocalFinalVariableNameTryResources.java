package com.puppycrawl.tools.checkstyle.checks.naming.localfinalvariablename;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Contains test cases regarding checking local
 * final variable name in try-with-resources statement:
 *
 * @author Valeria Vasylieva
 **/
public class InputLocalFinalVariableNameTryResources {

    void method() throws Exception {
        final String fileName = "Test";
        final BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileName), StandardCharsets.UTF_8));
        try {
        }
        finally {
            br.close();
        }
    }

    void method2() throws Exception {
        final String fileName = "Test";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileName), StandardCharsets.UTF_8))) {
        }
        finally {

        }
    }

    void method3() throws Exception {
        final String fileName = "Test";
        try (final BufferedReader BR = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileName), StandardCharsets.UTF_8))) {
        }
        finally {

        }
    }
}
