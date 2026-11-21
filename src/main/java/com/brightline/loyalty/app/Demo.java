package com.brightline.loyalty.app;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Objects;

import com.brightline.loyalty.TierStrategyFactory;
import com.brightline.loyalty.infrastructure.InMemoryMemberRepository;

public class Demo {

    private final PrintStream out;
    private final PrintStream err;
    private final PointsCalculator calculator;

    public Demo() {
        this(System.out, System.err);
    }

    public Demo(PrintStream out, PrintStream err) {
        this.calculator = createCalculator();
        this.out = Objects.requireNonNull(out, "Output stream cannot be null");
        this.err = Objects.requireNonNull(err, "Error stream cannot be null");
    }

    private static PointsCalculator createCalculator() {
        return new PointsCalculator(
                new InMemoryMemberRepository(),
                new TierStrategyFactory());
    }

    public static void main(String[] args) {
        new Demo().run();
    }

    void run() {
        printCalculation(
                "Result m1 (STANDARD), GHS 100 -> ",
                "m1",
                new BigDecimal("100.00"));
        printCalculation(
                "Result m2 (GOLD), GHS 250 -> ",
                "m2",
                new BigDecimal("250.00"));
        printCalculation(
                "Result m3 (SILVER), GHS 500 -> ",
                "m3",
                new BigDecimal("500.00"));
        printCalculation(
                "Result unknown member, GHS 100 -> ",
                "unknown",
                new BigDecimal("100.00"));
    }

    void printCalculation(
            String description, String memberId, BigDecimal amount) {
        try {
            out.println(description
                    + calculator.calculatePoints(memberId, amount));
        } catch (MemberNotFoundException exception) {
            err.println("Error: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            err.println("Invalid input: " + exception.getMessage());
        }
    }
}
