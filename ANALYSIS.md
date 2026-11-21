# Brightline Loyalty – Code Quality Analysis and Refactoring Report

## 1. Executive Summary

Brightline Loyalty is a Java 21 Maven application that calculates loyalty points for members based on their purchase amounts and membership tiers.

The original implementation supported three membership tiers:

- `STANDARD`
- `SILVER`
- `GOLD`

The initial codebase had several weaknesses affecting correctness, maintainability, and testability.

The most serious issue was the broad exception handler in `PointsCalculator.calculatePoints()`. It caught every `Exception` and returned `0`, making a failed calculation indistinguishable from a valid zero-point result.

Other issues included:

- Hard-coded membership-tier calculations.
- Direct construction of the repository inside the calculator.
- Weak and ineffective test assertions.
- Missing validation for members and purchase amounts.
- Use of `double` for monetary values.
- Console output mixed with calculation logic.
- Missing-member lookup represented by `null`.

The application was refactored to address these problems through clearer separation of responsibilities, constructor dependency injection, tier strategies, explicit validation, `BigDecimal`, and stronger automated tests.

The refactored application was successfully tested using a clean Maven build. All **53 tests passed**, with no failures, errors, or skipped tests.

Additional verification using PIT mutation testing achieved **100% mutation test strength**, with all 42 generated mutations killed.

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
| `PointsCalculator` | Retrieved members, calculated points, applied tier rules, printed results, and handled exceptions. |

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
| Exception handling | Broad `catch (Exception)` returned zero. | Hidden failures appeared to be successful calculations. |
| Class responsibilities | Calculator handled lookup, calculations, printing, and exception handling. | Difficult to maintain and test independently. |
| Dependency management | Calculator constructed `InMemoryMemberRepository` directly. | Difficult to replace storage or provide controlled test dependencies. |
| Tier calculations | Hard-coded conditional branches. | Adding a tier required modifying existing calculation logic. |
| Numeric constants | Business values were embedded in the method. | Rules were harder to understand and maintain. |
| Tier representation | Membership tiers were represented as strings. | Unsupported or misspelled values could enter the application. |
| Numeric precision | Monetary values used `double`. | Binary floating-point representation could introduce precision surprises. |
| Purchase validation | Invalid amounts were not adequately validated. | Negative or unusual values could affect calculations. |
| Member lookup | Missing members were represented by `null`. | The calculator could encounter an unclear failure. |
| Console output | Calculation method printed directly to the console. | Calculation logic had unnecessary side effects. |
| Member validation | Constructor did not validate required information. | Invalid member objects could be created. |

### 2.4 Most serious original problem: exception handling

The original calculator caught every exception and returned `0`.

This created ambiguity because the same return value could represent:

- A valid calculation earning zero points.
- A missing member.
- Invalid input.
- A repository failure.
- An unexpected programming error.

For example, an unknown member caused a `NullPointerException` when the calculator attempted to access the member's tier. The broad exception handler concealed the problem and returned zero.

This prevented the caller from knowing whether the calculation had actually succeeded.

**Required improvement:** A failed calculation should produce a meaningful failure outcome rather than being silently converted into a valid points result.

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

The calculator uses `orElseThrow()` to produce an `IllegalArgumentException` with the message:

`Member not found`

This is clearer than allowing a `NullPointerException` to occur and hiding it through broad exception handling.

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

A new tier still requires adding its enum value, implementing its strategy, and updating the factory. However, the calculator itself does not need another tier-specific conditional branch.

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

**Assessment: Improved, with a remaining factory modification**

The original calculator contained hard-coded tier conditions.

The refactored application introduces a common `TierStrategy` abstraction and separate implementations.

This allows tier-specific calculation behaviour to be added without introducing another conditional branch in `PointsCalculator`.

However, the current `TierStrategyFactory` uses a switch statement, so a new tier still requires an update to the enum and factory.

The design therefore improves extensibility but does not eliminate every modification when a new tier is introduced.

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
| OCP | Improved, with limitation | Tier strategies remove tier-specific branches from the calculator, but the factory and enum require updates for new tiers. |
| LSP | Supported by current abstractions | Repository and strategy implementations follow their defined contracts. |
| ISP | Followed | Interfaces remain focused. |
| DIP | Improved | The calculator receives the repository abstraction through constructor injection. |

---

## 7. Refactored Test Suite and Verification

### 7.1 Improvements to test quality

The refactored test suite uses real calculation objects and tests their actual behaviour.

Mockito is used to control dependencies where appropriate rather than mocking the class whose calculation logic is being tested.

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

### 7.4 Maven verification result

The project was tested using:

```powershell
.\mvnw.cmd clean test
```

The clean build deleted previous compiled output, recompiled the production and test source files, and executed the tests.

**Verified result:**

| Result | Count |
|---|---:|
| Tests run | 53 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |

**Build result: BUILD SUCCESS**

This confirms that the current production code compiles and all 53 automated tests pass in the clean Maven test run.

### 7.5 Mutation testing with PIT

The project was also checked using PIT mutation testing:

```powershell
.\mvnw.cmd test-compile org.pitest:pitest-maven:mutationCoverage
```

PIT introduces small changes (mutations) into the production code to check whether the automated tests detect those changes.

**Verified results:**

| Result | Count |
|---|---:|
| Mutations generated | 42 |
| Mutations killed | 42 |
| Mutations survived | 0 |
| Mutation test strength | 100% |
| Line coverage of mutated classes | 98% (96/98) |

All 42 generated mutations were killed by the test suite. This indicates that the tests detected the behavioural changes introduced by the mutations.

The PIT report is available at:

```text
target/pit-reports/index.html
```

The 98% line coverage figure refers specifically to the classes analysed by PIT. It should not be interpreted as a claim that every class in the entire application has 98% coverage.

---

## 8. Remaining Considerations and Future Improvements

The refactored application is significantly better structured, but there are still areas that could be improved as the application grows.

### 8.1 Tier registration

The current factory uses a switch statement.

If additional membership tiers are introduced, the enum and factory must be updated.

A registry-based approach could reduce the need to modify the factory whenever another strategy is added.

### 8.2 More precise error types

The current implementation uses `IllegalArgumentException` for a missing member.

A dedicated `MemberNotFoundException` could communicate that failure more specifically if the application grows and needs to distinguish different error conditions.

### 8.3 Application-level failure handling

The current `Demo` handles the unknown-member exception and displays a message.

A larger application may require a dedicated application boundary for logging, translating, and presenting different failures.

### 8.4 Further verification improvements

JaCoCo and PIT have both been used to assess the test suite.

The PIT results show 98% line coverage across mutated classes and 100% mutation test strength.

Future work could focus on investigating the remaining uncovered lines and maintaining strong mutation-test results as new functionality is introduced.

A passing unit-test suite is useful evidence, but coverage and mutation results provide additional insight into the quality of the tests.

---

## 9. Conclusion

The original Brightline Loyalty application provided a basic implementation of loyalty-point calculations, but its design contained weaknesses that affected reliability, maintainability, and testability.

The most serious issue was the broad exception handler that converted failures into zero points. This made it difficult for callers to distinguish successful calculations from failed operations.

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

The clean Maven test run completed successfully with **53 tests passing and no failures or errors**.

PIT mutation testing also achieved **100% mutation test strength**, with all 42 generated mutations killed and no surviving mutations.

The refactored application now has a clearer structure, more explicit failure handling, and stronger automated verification. Although the tier factory still requires modification when a new tier is introduced, the tier-specific calculation logic is now separated from the main calculator.

These improvements provide a stronger foundation for maintaining and extending Brightline Loyalty while protecting the correctness of its existing calculation rules.