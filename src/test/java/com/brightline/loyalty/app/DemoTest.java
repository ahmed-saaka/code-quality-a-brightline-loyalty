package com.brightline.loyalty.app;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoTest {

    @Test
    void printsExpectedResultsAndSendsUnknownMemberFailureToErrorStream() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        try (PrintStream capturedOut =
                     new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream capturedErr =
                     new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            new Demo(capturedOut, capturedErr).run();
        }

        String lineSeparator = System.lineSeparator();
        String expectedOutput = String.join(
                lineSeparator,
                "Result m1 (STANDARD), GHS 100 -> 10",
                "Result m2 (GOLD), GHS 250 -> 50",
                "Result m3 (SILVER), GHS 500 -> 75",
                "");
        String expectedErrors = String.join(
                lineSeparator,
                "Error: Member not found",
                "");

        assertEquals(
                expectedOutput,
                output.toString(StandardCharsets.UTF_8));
        assertEquals(
                expectedErrors,
                errors.toString(StandardCharsets.UTF_8));
    }

    @Test
    void mainRunsTheDemonstrationAndReportsMissingMemberToErrorStream() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        try (PrintStream capturedOut =
                     new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream capturedErr =
                     new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOut);
            System.setErr(capturedErr);

            Demo.main(new String[0]);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        String lineSeparator = System.lineSeparator();
        String expectedOutput = String.join(
                lineSeparator,
                "Result m1 (STANDARD), GHS 100 -> 10",
                "Result m2 (GOLD), GHS 250 -> 50",
                "Result m3 (SILVER), GHS 500 -> 75",
                "");
        String expectedErrors = String.join(
                lineSeparator,
                "Error: Member not found",
                "");

        assertEquals(
                expectedOutput,
                output.toString(StandardCharsets.UTF_8));
        assertEquals(
                expectedErrors,
                errors.toString(StandardCharsets.UTF_8));
    }

    @Test
    void reportsInvalidInputSeparatelyOnErrorStream() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        try (PrintStream capturedOut =
                     new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream capturedErr =
                     new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            Demo demo = new Demo(capturedOut, capturedErr);
            demo.printCalculation("Invalid purchase -> ", "m1", BigDecimal.ZERO);
        }

        assertEquals("", output.toString(StandardCharsets.UTF_8));
        assertEquals(
                "Invalid input: Purchase amount must be greater than zero"
                        + System.lineSeparator(),
                errors.toString(StandardCharsets.UTF_8));
    }
}
