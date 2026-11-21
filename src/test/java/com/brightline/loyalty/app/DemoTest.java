package com.brightline.loyalty.app;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoTest {

    @Test
    void printsExpectedResultsAndUnknownMemberFailure() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOut =
                     new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOut);
            Demo.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String lineSeparator = System.lineSeparator();
        String expectedOutput = String.join(
                lineSeparator,
                "Result m1 (STANDARD), GHS 100 -> 10",
                "Result m2 (GOLD), GHS 250 -> 50",
                "Result m3 (SILVER), GHS 500 -> 75",
                "Error: Member not found",
                "");

        assertEquals(
                expectedOutput,
                output.toString(StandardCharsets.UTF_8));
    }
}
