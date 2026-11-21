# Brightline Loyalty – Code Quality Analysis and Refactoring Report

## 1. Executive Summary

Brightline Loyalty is a Java 21 Maven application that calculates loyalty points for members based on their purchase amounts and membership tiers.

The original implementation supported three membership tiers:

- `STANDARD`
- `SILVER`
- `GOLD`

The initial codebase had several weaknesses affecting correctness, maintainability, and testability.

The most serious correctness issue was the lack of purchase-amount validation: a negative purchase could produce negative loyalty points. The tracked original code did not contain a broad exception handler; it explicitly threw `IllegalArgumentException` when a member was not found. Error swallowing is therefore not an original-code defect.

Other issues included:

- Hard-coded membership-tier calculations.
- Direct construction of the repository inside the calculator.
- Weak and ineffective test assertions.
- Missing validation for members and purchase amounts.
- Use of `double` for monetary values.
- Console output mixed with calculation logic.
- Missing-member lookup represented by `null`.

The application was refactored to address these problems through clearer separation of responsibilities, constructor dependency injection, tier strategies, explicit validation, `BigDecimal`, and stronger automated tests.

The refactored application was verified on **2026-11-21** using a clean Maven build. The current test count and PIT results are recorded in Section 7.

---

## 2. Original Codebase Diagnosis

### 2.1 Original project structure

The original implementation contained five Java classes:

| Class | Original responsibility |
|---|---|
| `Demo` | Started the demonstration and invoked the points calculator. |
| `Member` | Represented a loyalty member. |
| `MemberRepository` | Defined the member lookup contract. |
| `InMemoryMemberRepository` | Stored and retrieved predefined members. |
| `PointsCalculator` | Retrieved members, calculated points, applied tier rules, printed results, and explicitly rejected missing members. |

The main design concern was that `PointsCalculator` performed several different responsibilities instead of focusing on points calculation.

### 2.2 Original business rules

The original application used these membership multipliers:

| Membership tier | Multiplier |
|---|---:|
| STANDARD | 1x |
| SILVER | 1.5x |
| GOLD | 2x |

The original calculation rules included:

- One base point for every GHS 10 spent.
- Fractional base points were truncated.
- SILVER points were multiplied by 1.5.
- GOLD points were multiplied by 2.
- Purchases strictly greater than GHS 500 received an additional 50 points.

The bonus condition was strictly greater than GHS 500. A purchase of exactly GHS 500 did not qualify.

### 2.3 Original production code problems

| Area | Original problem | Impact |
|---|---|---|
| Exception handling | No broad catch existed; missing-member lookup explicitly threw `IllegalArgumentException`. | Failure was not swallowed, but missing-member failure had no dedicated exception type. |
| Class responsibilities | Calculator handled lookup, calculations, printing, and exception handling. | Difficult to maintain and test independently. |
| Dependency management | Calculator constructed `InMemoryMemberRepository` directly. | Difficult to replace storage or provide controlled test dependencies. |
| Tier calculations | Hard-coded conditional branches. | Adding a tier required modifying existing calculation logic. |
| Numeric constants | Business values were embedded in the method. | Rules were harder to understand and maintain. |
| Tier representation | Membership tiers were represented as strings. | Unsupported or misspelled values could enter the application. |
| Numeric precision | Monetary values used `double`. | Binary floating-point representation could introduce precision surprises. |
| Purchase validation | Null, zero, and negative amounts were not rejected. | A negative purchase could produce a negative points award. |
| Member lookup | The repository represented missing members with `null`. | Callers had to interpret nullable lookup results; the calculator explicitly threw for a missing ID. |
| Console output | Calculation method printed directly to the console. | Calculation logic had unnecessary side effects. |
| Member validation | Constructor did not validate required information. | Invalid member objects could be created. |

### 2.4 Most serious original problem: missing purchase validation

The original calculator converted the purchase amount to base points without first checking that the amount was positive. A STANDARD member's GHS -100 purchase therefore produced -10 points. The code had no refund/reversal rule that would make this negative award meaningful, so accepting it could corrupt a member's points balance.

The original implementation did **not** catch every exception or convert failures to zero. For an unknown member, it explicitly threw `IllegalArgumentException("Member not found")`. The correct improvement is to validate purchase amounts and use a more specific missing-member exception, not to claim that an original broad catch swallowed failures.

### 2.5 Original testing problems

The original test suite contained weak assertions.

For example:

- An assertion that only checked whether points were greater than zero did not verify the correct business result.
- A test mocked `PointsCalculator` itself instead of executing its real calculation method.
- An assertion using `assertNotNull()` on a primitive `int` did not meaningfully test the result.

The tests therefore provided insufficient confidence in the actual calculation behaviour.

---

## 3. Refactored Application Architecture

### 3.1 New package structure

The refactored application separates domain logic, application coordination, and infrastructure.

```text
com.brightline.loyalty
│
├── Member.java
├── MemberRepository.java
├── MembershipTier.java
├── TierStrategy.java
├── TierStrategyFactory.java
├── StandardTier.java
├── SilverTier.java
├── GoldTier.java
│
├── app
│   ├── Demo.java
│   └── PointsCalculator.java
│
└── infrastructure
    └── InMemoryMemberRepository.java
```

### 3.2 Responsibilities of the new components

| Component | Responsibility |
|---|---|
| `Member` | Represents a member and validates required member information. |
| `MembershipTier` | Defines the supported membership tiers using an enum. |
| `MemberRepository` | Defines the contract for member lookup. |
| `InMemoryMemberRepository` | Provides an in-memory implementation of member storage. |
| `TierStrategy` | Defines the common contract for membership-tier calculations. |
| `StandardTier` | Implements STANDARD point calculations. |
| `SilverTier` | Implements SILVER point calculations. |
| `GoldTier` | Implements GOLD point calculations. |
| `TierStrategyFactory` | Selects the appropriate strategy for a membership tier. |
| `PointsCalculator` | Coordinates the points calculation using its injected dependencies. |
| `Demo` | Creates application dependencies, starts the demonstration, and displays results. |

### 3.3 Separation of responsibilities

The new design separates the application into three main areas.

**Domain package**

Contains member information, membership tiers, repository contracts, and tier calculation rules.

**Application package**

Contains the points calculation use case and the demonstration entry point.

**Infrastructure package**

Contains the concrete in-memory repository implementation.

This arrangement makes it easier to replace the storage implementation without changing the domain model or directly coupling the calculator to a particular repository.

### 3.4 Dependency injection

The original calculator constructed its own repository.

The refactored calculator receives a `MemberRepository` through its constructor.

The application entry point creates the concrete repository and supplies it to the calculator.

This means the calculator depends on the repository abstraction rather than directly depending on `InMemoryMemberRepository`.

It also makes testing easier because tests can supply controlled repository implementations.

---

## 4. Improvements to the Production Code

### 4.1 Explicit membership tiers

The refactored application uses the `MembershipTier` enum:

- `STANDARD`
- `SILVER`
- `GOLD`

This prevents arbitrary strings from being stored as membership tiers in a `Member` object.

The `Member` constructor also validates required member information.

### 4.2 Explicit member lookup

The repository lookup contract now returns `Optional<Member>`.

This makes the possibility of a missing member explicit.

The calculator uses `orElseThrow()` to produce a `MemberNotFoundException` with the message:

`Member not found`

This gives callers a specific missing-member failure instead of the original generic `IllegalArgumentException`.

### 4.3 Tier strategy implementation

The refactored application introduces a common `TierStrategy` contract.

Each membership tier has its own implementation:

| Strategy | Behaviour |
|---|---|
| `StandardTier` | Returns the standard points without a multiplier. |
| `SilverTier` | Applies a 1.5 multiplier and truncates fractional points. |
| `GoldTier` | Applies a 2x multiplier. |

The `TierStrategyFactory` selects the appropriate implementation based on the member's `MembershipTier`.

This removes the tier-specific conditional chain from the points calculator.

A missing registry entry raises `IllegalStateException`, identifying a wiring error rather than invalid purchase input.

A registry supplied to the factory maps each enum value to a strategy, so a composition root can register another strategy without adding a switch arm to the factory. Adding a tier still requires updating `MembershipTier`, implementing a strategy, and adding the mapping to the configured registry. The no-argument factory constructor is retained as a convenience for built-in tiers, so extending that default registry still requires updating the constructor.

### 4.4 Monetary precision with BigDecimal

The refactored calculator uses `BigDecimal` for purchase amounts instead of `double`.

For example:

`new BigDecimal("100.00")`

This avoids relying on binary floating-point representation for monetary values.

The calculator also uses exact integer conversion when converting base points into an integer.

### 4.5 Purchase validation

The new calculator validates purchase amounts before proceeding with member lookup.

The implemented validation rejects:

- Null purchase amounts.
- Zero purchase amounts.
- Negative purchase amounts.

This prevents invalid purchase values from entering the calculation process.

### 4.6 Overflow protection

The refactored application includes explicit overflow detection.

It uses:

- `BigDecimal.intValueExact()` when converting calculated values into integers.
- `Math.multiplyExact()` for multiplication that could overflow.
- `Math.addExact()` when adding bonus points.

This prevents integer overflow from silently producing incorrect point totals.

### 4.7 Separation of console output

The original calculator printed directly to the console.

In the refactored design, the `Demo` class handles console output.

The calculator focuses on its calculation responsibility and can be used without printing demonstration messages.

---

## 5. Refactored Business Behaviour

The refactored application preserves the main loyalty calculation rules.

| Rule | Implemented behaviour |
|---|---|
| Base points | One point per GHS 10. |
| Fractional base points | Truncated. |
| STANDARD | 1x. |
| SILVER | 1.5x, truncated. |
| GOLD | 2x. |
| Bonus threshold | Purchase must be strictly greater than GHS 500. |
| Bonus points | Additional 50 points. |
| Unknown member | Produces `Member not found`. |

The bonus is added after the tier multiplier.

For example, a GOLD member spending GHS 600 earns:

- Base points: 600 ÷ 10 = 60.
- GOLD calculation: 60 × 2 = 120.
- Bonus: 50.
- Total: 170 points.

The bonus is not multiplied by the membership tier.

### 5.1 Demonstration output

The `Demo` class demonstrates the following calculations:

| Member | Tier | Purchase | Expected points |
|---|---|---:|---:|
| m1 | STANDARD | GHS 100 | 10 |
| m2 | GOLD | GHS 250 | 50 |
| m3 | SILVER | GHS 500 | 75 |
| unknown | — | GHS 100 | Member not found |

Expected console output:

```text
Result m1 (STANDARD), GHS 100 -> 10
Result m2 (GOLD), GHS 250 -> 50
Result m3 (SILVER), GHS 500 -> 75
Error: Member not found
```

---

## 6. SOLID Principles Assessment After Refactoring

### 6.1 Single Responsibility Principle (SRP)

**Assessment: Improved**

The original calculator handled member lookup, calculations, tier selection, console output, and exception handling.

The refactored application separates these responsibilities across focused components.

For example:

- `Member` represents member information.
- `InMemoryMemberRepository` handles in-memory storage.
- Tier strategies handle membership-specific calculations.
- `PointsCalculator` coordinates points calculation.
- `Demo` handles application startup and demonstration output.

Each component now has a clearer responsibility.

### 6.2 Open/Closed Principle (OCP)

**Assessment: Improved, with registry wiring as a remaining responsibility**

The original calculator contained hard-coded tier conditions.

The refactored application introduces a common `TierStrategy` abstraction and separate implementations.

This allows tier-specific calculation behaviour to be added without introducing another conditional branch in `PointsCalculator`.

`TierStrategyFactory` now accepts an immutable strategy registry. Applications that inject a registry can add a tier mapping in their composition code without changing the factory.

The convenience no-argument factory retains a built-in registry and must be updated if that default is to include a new tier. This keeps existing construction straightforward while enabling explicit registration for applications that need it.

### 6.3 Liskov Substitution Principle (LSP)

**Assessment: Supported by the current strategy and repository abstractions**

The tier implementations follow the common `TierStrategy` contract.

The in-memory repository implements the `MemberRepository` contract.

These abstractions provide consistent expected behaviour for their implementations.

The current test suite verifies the existing implementations, although it does not prove every possible future implementation will satisfy the contracts.

### 6.4 Interface Segregation Principle (ISP)

**Assessment: Followed**

The `MemberRepository` interface remains focused on member lookup.

The `TierStrategy` interface defines the behaviour needed from a tier strategy.

The interfaces avoid forcing implementations to provide unrelated methods.

### 6.5 Dependency Inversion Principle (DIP)

**Assessment: Improved**

The calculator now receives the `MemberRepository` abstraction through constructor injection.

The application entry point supplies the concrete `InMemoryMemberRepository`.

This separates the high-level calculation use case from the concrete storage implementation.

### 6.6 SOLID summary

| Principle | Assessment | Evidence |
|---|---|---|
| SRP | Improved | Responsibilities are separated across domain, application, and infrastructure components. |
| OCP | Improved, with registry wiring | The calculator and factory have no tier-specific branching; the enum and the registry supplied by the composition root need a new entry. The built-in default registry also needs extending if used. |
| LSP | Supported by current abstractions | Repository and strategy implementations follow their defined contracts. |
| ISP | Followed | Interfaces remain focused. |
| DIP | Improved | The calculator receives the repository abstraction through constructor injection. |

---

## 7. Refactored Test Suite and Verification

### 7.1 Improvements to test quality

The refactored test suite uses real calculation objects and tests their actual behaviour. Small repository fixtures are used where a test needs to verify validation order; the project does not add a mocking framework dependency.

The tests now use exact assertions for expected points.

### 7.2 Test coverage areas

The current test suite includes tests for:

**Member validation**

- Valid member creation.
- Null member IDs.
- Empty and whitespace member IDs.
- Invalid member names.
- Null membership tiers.

**Repository behaviour**

- Saving members.
- Finding existing members.
- Handling missing members.
- Duplicate members.
- Invalid repository inputs.

**Tier strategies**

- STANDARD calculations.
- SILVER calculations.
- GOLD calculations.
- Negative point validation.
- Fractional Silver point truncation.
- Integer overflow.

**Points calculation**

- Standard member calculations.
- Silver member calculations.
- Gold member calculations.
- Unknown member handling.
- Null purchase amount.
- Zero purchase amount.
- Negative purchase amount.
- Null and blank member IDs.
- Bonus threshold boundaries.
- Fractional Silver calculations.
- Integer overflow during base-point conversion.
- Integer overflow when adding the bonus.
- Injected repository behaviour.
- Required dependency validation.

**Factory behaviour**

- Selecting STANDARD.
- Selecting SILVER.
- Selecting GOLD.
- Rejecting null tier values.

### 7.3 Bonus boundary testing

The tests verify three important purchase amounts:

| Purchase amount | Bonus behaviour |
|---|---|
| GHS 499.99 | No bonus. |
| GHS 500.00 | No bonus. |
| GHS 500.01 | Bonus applies. |

These tests protect the requirement that the purchase must be strictly greater than GHS 500.

Additional tests verify that GHS 9.99 earns zero base points and that the SILVER and GOLD multipliers are applied before a bonus on purchases above GHS 500. The expected totals for GHS 600 are 140 for SILVER and 170 for GOLD.

### 7.4 Maven verification result

The project was tested using:

```powershell
.\mvnw.cmd clean test
```

The clean build deleted previous compiled output, recompiled the production and test source files, and executed the tests.

**Verified result:**

Run date: **2026-11-21**

| Result | Count |
|---|---:|
| Tests run | 60 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |

**Build result: BUILD SUCCESS**

This records the result of that clean Maven test run; it is not a guarantee for future changes.

### 7.5 Mutation testing with PIT

The project was also checked using PIT mutation testing:

```powershell
.\mvnw.cmd test-compile org.pitest:pitest-maven:mutationCoverage
```

PIT introduces small changes (mutations) into the production code to check whether the automated tests detect those changes.

**Verified results:**

Run date: **2026-11-21**

| Result | Count |
|---|---:|
| Mutations generated | 48 |
| Mutations killed | 48 |
| Mutations survived | 0 |
| Mutations without coverage | 0 |
| Mutation test strength | 100% |
| Line coverage of mutated classes | 100% (112/112) |

All 48 generated mutations were killed; none survived or lacked coverage.

The PIT report is available at:

```text
target/pit-reports/index.html
```

The 100% line coverage figure (112/112 lines) refers specifically to the classes analysed by PIT. It should not be interpreted as a claim that every class in the entire application has 100% coverage.

---

## 8. Remaining Considerations and Future Improvements

The refactored application is significantly better structured, but there are still areas that could be improved as the application grows.

### 8.1 Tier registration

The factory now accepts a strategy registry, so callers can supply new tier mappings without modifying factory logic. The no-argument convenience constructor still defines the built-in registry and must be updated if it remains the wiring choice for a new default tier.

### 8.2 More precise error types

Missing members now produce `MemberNotFoundException`, while invalid inputs continue to produce `IllegalArgumentException`. A broader application may still translate these exceptions at its own boundary.

### 8.3 Application-level failure handling

The `Demo` writes ordinary output to `System.out` and missing-member or invalid-input messages to `System.err`. This is appropriate for a small CLI demonstration, not a production logging strategy.

A larger application may require a dedicated application boundary for logging, translating, and presenting different failures.

### 8.4 Further verification improvements

JaCoCo and PIT have both been used to assess the test suite.

The PIT run's line-coverage and mutation-strength results are specific to the verification date in Section 7 and apply only to classes PIT analyzed.

Future work could focus on investigating the remaining uncovered lines and maintaining strong mutation-test results as new functionality is introduced.

A passing unit-test suite is useful evidence, but coverage and mutation results provide additional insight into the quality of the tests.

---

## 9. Conclusion

The original Brightline Loyalty application provided a basic implementation of loyalty-point calculations, but its design contained weaknesses that affected reliability, maintainability, and testability.

The most serious original issue was missing purchase validation, which allowed negative purchase amounts to produce negative points. The original implementation did not have a broad exception handler: it explicitly threw `IllegalArgumentException` for an unknown member.

The refactoring addressed the main issues by:

1. Separating application, domain, and infrastructure responsibilities.
2. Introducing constructor dependency injection.
3. Replacing plain-string membership tiers with an enum.
4. Introducing separate tier strategy implementations.
5. Replacing `null`-based member lookup with `Optional<Member>`.
6. Using `BigDecimal` for monetary values.
7. Validating purchase amounts and member information.
8. Adding explicit overflow protection.
9. Moving console output out of the calculation logic.
10. Strengthening automated tests with exact assertions and boundary-value scenarios.

The clean Maven and PIT results, with their run date and exact counts, are recorded in Section 7. These are run-specific results and not guarantees for later changes.

The refactored application now has a clearer structure, more explicit failure handling, and stronger automated verification. New tier strategies can be supplied through registry wiring; the built-in default registry remains a convenience that must be extended when a new default tier is introduced.

These improvements provide a stronger foundation for maintaining and extending Brightline Loyalty while protecting the correctness of its existing calculation rules.