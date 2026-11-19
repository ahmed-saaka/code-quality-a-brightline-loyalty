package com.brightline.loyalty;
import java.math.BigDecimal;

// A tiny runner so you can see the calculator work before you change anything.
public class Demo {
    public static void main(String[] args) {
        MemberRepository repository = new InMemoryMemberRepository();
        PointsCalculator calculator = new PointsCalculator(repository);
        

        System.out.println("Result m1 (STANDARD), GHS 100  -> " + calculator.calculatePoints("m1", new BigDecimal("100.00")));
        System.out.println("Result m2 (GOLD), GHS 250      -> " + calculator.calculatePoints("m2",  new BigDecimal("250.00")));
        System.out.println("Result unknown member, GHS 100 -> " + calculator.calculatePoints("unknown", new BigDecimal("100.00")));
    }
}
