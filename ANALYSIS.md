# Brightline Loyalty – Code Quality Analysis

## 1. Executive Summary

Brightline Loyalty is a small Java 21 Maven application that calculates loyalty points for members based on their purchase amounts and membership tiers.

The initial implementation supports three membership tiers:

- `STANDARD`
- `SILVER`
- `GOLD`

The application uses a repository to retrieve member information and a `PointsCalculator` to calculate the points earned from a purchase.

The initial review identified several areas that could affect the correctness, maintainability, and testability of the application.

The most serious problem is the broad exception handler in `PointsCalculator.calculatePoints()`. It catches every `Exception` and returns `0`, making a genuine zero-point result indistinguishable from a missing member, invalid data, or an unexpected application failure.

Another major concern is the hard-coded membership-tier logic. Adding a new tier currently requires modifying the existing calculation method, which makes the design difficult to extend safely.

The existing test suite also provides limited confidence because some assertions do not verify exact results, and one test mocks the class it is supposed to test.

The recommended direction is to introduce clear responsibilities, constructor dependency injection, separate tier rules, explicit validation, and meaningful automated tests.

---

## 2. Project Overview

### 2.1 Project structure and technology

The application is a Maven-based Java project targeting Java 21.

The production code is organised under the package:

`com.brightline.loyalty`

The initial implementation contains five Java classes:

- `Demo`
- `Member`
- `MemberRepository`
- `InMemoryMemberRepository`
- `PointsCalculator`

### 2.2 Maven configuration

The supplied `pom.xml` defines the following configuration:

| Tool or dependency | Version | Purpose |
|---|---:|---|
| Java release | 21 | Java compilation target |
| JUnit Jupiter | 5.13.4 | Unit testing |
| Mockito Core | 5.18.0 | Creating and configuring test doubles |
| Mockito JUnit Jupiter | 5.18.0 | Integrating Mockito with JUnit 5 |
| JUnit Platform Launcher | Configured | Supporting test execution |
| Maven Surefire | 3.2.5 | Running tests during the Maven test phase |
| JaCoCo | 0.8.12 | Measuring code coverage |
| PIT | 1.16.1 | Mutation testing |
| PIT JUnit 5 plugin | 1.2.1 | Supporting JUnit 5 tests with PIT |

JUnit and Mockito are declared as test dependencies. Surefire, JaCoCo, and PIT are configured as Maven build plugins.

JaCoCo is configured to generate a coverage report during the `verify` phase. PIT is configured to target the `com.brightline.loyalty` package and its tests.

**Important:** Having these tools configured does not automatically guarantee good test coverage or reliable tests. The quality of the test cases and assertions still matters.

### 2.3 Current application responsibilities

The initial classes have the following responsibilities:

| Class | Current responsibility |
|---|---|
| `Demo` | Starts the demonstration and invokes the points calculator. |
| `Member` | Represents a loyalty member with an ID, name, and membership tier. |
| `MemberRepository` | Defines the contract for finding a member by ID. |
| `InMemoryMemberRepository` | Stores and retrieves the application's predefined members. |
| `PointsCalculator` | Retrieves members, calculates points, applies tier rules and bonuses, handles exceptions, and prints results. |

The main design concern is that `PointsCalculator` currently performs several different responsibilities instead of focusing on points calculation.

### 2.4 Current membership data

`InMemoryMemberRepository` contains three predefined members:

| Member ID | Member name | Membership tier |
|---|---|---|
| `m1` | Kofi Mensah | STANDARD |
| `m2` | Ama Owusu | GOLD |
| `m3` | Yaa Boateng | SILVER |

The repository's `findById(String id)` method returns `null` when the requested member does not exist.

The `MemberRepository` interface does not explicitly define how a missing member should be represented.

### 2.5 Current points calculation behaviour

The main calculation method is:

`PointsCalculator.calculatePoints(String memberId, double amount)`

The method currently follows these steps:

1. Retrieves a member using the supplied member ID.
2. Calculates the base points by dividing the purchase amount by 10.
3. Converts the result to an integer, which removes the fractional part.
4. Applies the member's membership-tier multiplier.
5. Adds 50 bonus points when the original purchase amount is greater than GHS 500.
6. Prints a success message.
7. Returns the calculated points.

The current tier multipliers are:

| Membership tier | Multiplier |
|---|---:|
| STANDARD | 1x |
| SILVER | 1.5x |
| GOLD | 2x |

The calculation uses integer casting at intermediate stages. Therefore, fractional values are truncated rather than rounded.

For example:

- GHS 100: `(int) (100 / 10)` = 10 base points.
- GHS 250: `(int) (250 / 10)` = 25 base points.
- GHS 200: `(int) (200 / 10)` = 20 base points.

The Silver calculation for GHS 200 is `(int) (20 * 1.5)`, which produces 30 points.

The bonus condition is strictly greater than GHS 500. A purchase of exactly GHS 500 does not receive the additional 50 points.

### 2.6 Demonstrated application behaviour

The `Demo` class demonstrates three calls:

- `m1`, Kofi Mensah, STANDARD, GHS 100 → 10 points.
- `m2`, Ama Owusu, GOLD, GHS 250 → 50 points.
- An unknown member ID, GHS 100 → 0 points because the exception is caught and converted into zero.

The existing test suite also exercises the Silver member:

- `m3`, Yaa Boateng, SILVER, GHS 200 → 30 points.

These results describe the current implementation. In particular, the unknown-member result of zero should not be interpreted as a successful calculation. It is the result of hidden exception handling.

---

## 3. Production Code Quality Review

This section identifies the main issues observed in the production code, explains their impact, and recommends improvements.

| Area | Evidence (file:line) | Identified issue | Impact | Recommended improvement |
|---|---|---|---|---|
| Exception handling | `PointsCalculator.java:33–35` | `calculatePoints()` catches the broad `Exception` type and returns `0`. | Different failures are hidden and appear to be valid zero-point calculations. | Handle known failures explicitly and preserve unexpected exception information. |
| Class responsibilities | `PointsCalculator.java:10–35` | `PointsCalculator` performs member lookup, calculation, tier selection, bonus calculation, printing, and exception handling. | The class has several reasons to change and is difficult to test independently. | Separate lookup, calculation rules, and application coordination. |
| Dependency management | `PointsCalculator.java:8` | `PointsCalculator` creates an `InMemoryMemberRepository` directly. | The repository cannot easily be replaced with another implementation or a controlled test double. | Inject `MemberRepository` through the constructor. |
| Membership-tier logic | `PointsCalculator.java:17–24` | Tier calculations use a hard-coded `if`/`else` chain. | Introducing another tier requires editing existing calculation logic. | Introduce a common tier-rule abstraction and separate implementations. |
| Numeric constants | `PointsCalculator.java:15, 21, 23, 27–28` | Values such as `10`, `500`, `50`, `1.5`, and `2` are embedded in the calculation. | The business meaning of the values is not immediately clear, and changing the rules requires editing the method. | Use meaningful constants or dedicated rule implementations. |
| Membership-tier representation | `Member.java:6`, `PointsCalculator.java:18–23` | Tier names are stored and compared as plain strings. | Spelling mistakes and unsupported tier values can cause incorrect calculations without a clear error. | Use a controlled tier representation and validate external tier values. |
| Rounding and precision | `PointsCalculator.java:10, 15, 21` | Amount and intermediate calculations use `double` and are converted to integers. | Fractional points are truncated; binary floating-point representation can also make decimal currency arithmetic imprecise. | Confirm the required currency precision and rounding policy; consider integer minor currency units or `BigDecimal`, then test the policy. |
| Purchase validation | `PointsCalculator.java:10, 14–28` | There is no validation for negative, non-finite, or excessively large purchase amounts. | Invalid values can produce negative or unexpected point totals. | Validate purchase amounts before calculating points. |
| Member lookup | `InMemoryMemberRepository.java:17–18`, `MemberRepository.java:3–4` | Unknown IDs result in `null`. | The calculator dereferences a missing member and relies on the broad exception handler. | Define an explicit not-found contract using `Optional`, a dedicated exception, or a result type. |
| Console output | `PointsCalculator.java:31` | `calculatePoints()` uses `System.out.println()`. | Calculation logic produces an external side effect and is harder to reuse independently. | Move reporting or logging to the application layer. |
| Member validation | `Member.java:8–11` | `Member` assigns constructor values without validating them. | Objects may be created with missing or invalid member information. | Validate required fields and membership-tier values when creating a member. |
| Repository data | `InMemoryMemberRepository.java:8–14` | The in-memory repository creates its own predefined member data. | The current implementation is tied to fixed demonstration data. | Keep test or demonstration data in the in-memory implementation and inject the appropriate repository. |

### 3.1 Exception handling hides failures

**Observed issue** (`PointsCalculator.java:33–35`)

The calculation method uses:

`catch (Exception e)`

and returns `0` when an exception occurs.

**Why this is a problem**

The method uses the same return value for two very different situations:

- A valid calculation that earns zero points.
- A failed calculation that did not complete successfully.

For example, when a member ID is unknown, the repository returns `null`. The calculator then tries to access the member's tier, resulting in a `NullPointerException`. The broad catch hides that exception and returns zero.

The caller cannot tell that the member was not found.

The same problem could occur if a future repository implementation experiences a failure.

**Recommended improvement**

Use explicit failure handling. A missing member should produce a clear not-found outcome, while unexpected operational failures should retain their original cause and be handled at an appropriate application boundary.

Zero should represent a valid calculation result, not every possible failure.

### 3.2 Multiple responsibilities in PointsCalculator

**Observed issue** (`PointsCalculator.java:10–35`)

`PointsCalculator` is responsible for:

- Finding members.
- Calculating base points.
- Applying membership multipliers.
- Applying bonus points.
- Printing results.
- Catching exceptions.

**Why this is a problem**

A change to one responsibility can affect the entire method.

For example, adding a new membership tier requires editing the calculation method. Changing how members are retrieved also affects the same class.

This makes the class harder to maintain and test.

**Recommended improvement**

Separate the responsibilities into focused components.

The points calculator should focus on calculating points. A service or application-level component can coordinate member lookup and calculation, while separate tier-rule implementations handle membership-specific earning rules.

### 3.3 Hard-coded repository construction

**Observed issue** (`PointsCalculator.java:8`)

`PointsCalculator` directly creates an `InMemoryMemberRepository`.

**Why this is a problem**

The calculator is tied to one concrete repository implementation.

It is difficult to supply a different repository, such as a database-backed implementation, without changing the calculator itself.

It also makes testing dependency failures more difficult because a test cannot easily provide a repository that returns a missing member or throws a controlled exception.

**Recommended improvement**

Accept `MemberRepository` through constructor injection.

This allows the application to supply the appropriate implementation while keeping the calculator independent of the storage mechanism.

### 3.4 Hard-coded membership-tier calculations

**Observed issue** (`PointsCalculator.java:17–24`)

The calculator uses conditional branches to identify STANDARD, SILVER, and GOLD.

**Why this is a problem**

When a new membership tier is introduced, the existing calculation method must be modified.

Repeated changes to the same method increase the risk of accidentally changing existing tier behaviour.

**Recommended improvement**

Introduce a `TierPointsRule` abstraction that defines the common behaviour expected from every membership-tier rule.

Each tier can have its own implementation. A resolver or registry can select the correct rule based on the member's tier.

This allows an additional tier to be introduced by supplying and registering a new implementation instead of adding another conditional branch to the calculator.

### 3.5 Magic numbers and unclear business rules

**Observed issue** (`PointsCalculator.java:15, 21, 23, 27–28`)

The calculation method contains unexplained numeric values:

- `10` — purchase amount divisor.
- `500` — purchase threshold for the bonus.
- `50` — bonus points.
- `1.5` — Silver multiplier.
- `2` — Gold multiplier.

**Why this is a problem**

Someone reading the method must work out what each number represents.

If the loyalty programme changes its rules, developers must locate the correct values in the implementation.

**Recommended improvement**

Give the values meaningful names or place them in the relevant tier and bonus rule implementations.

For example, the bonus threshold and bonus amount should be clearly identified as separate business rules.

### 3.6 Numeric precision and rounding

**Observed issue** (`PointsCalculator.java:10, 15, 21`)

The method converts intermediate results to integers.

**Why this is a problem**

Casting a `double` to an `int` truncates the fractional part. It does not round to the nearest integer.

The order of calculations can therefore affect the final result.

The amount itself is also represented as a `double` (`PointsCalculator.java:10`). Binary floating-point does not represent every decimal fraction exactly, so using it for currency can introduce precision surprises around decimal purchase values. The code does not establish whether those surprises currently affect a particular supported input; this is a design risk to resolve by confirming the currency scale and calculation policy. Integer minor currency units or `BigDecimal` are possible representations, depending on the domain requirements.

For example, a GHS 225 purchase produces:

- Base points: `(int) (225 / 10)` = 22.
- Silver points: `(int) (22 * 1.5)` = 33.

A different approach that preserves the fraction until after applying the multiplier could produce a different result.

**Recommended improvement**

The business should define whether points are rounded, truncated, or calculated using another policy.

The same policy should be applied consistently and covered by tests. The business should also confirm the currency representation and precision requirements. Consider integer minor currency units or `BigDecimal` rather than `double` for monetary values; the appropriate choice depends on the accepted input scale and required rounding behaviour.

### 3.7 Missing purchase validation

**Observed issue** (`PointsCalculator.java:10, 14–28`)

The method accepts a primitive `double` without checking whether the value is suitable for a purchase.

**Why this is a problem**

Negative values can generate negative points. Special floating-point values such as `NaN` and infinity are also accepted by the Java `double` type.

The application should not silently process values that do not represent valid purchases.

**Recommended improvement**

Validate purchase amounts before calculation.

The validation rules should explicitly define how the application handles:

- Negative amounts.
- Zero amounts.
- `NaN`.
- Positive and negative infinity.
- Excessively large amounts.

Zero should not automatically be treated as invalid without a business requirement. The current implementation produces zero points for a zero-value purchase, so the expected business behaviour should be confirmed before changing it.

### 3.8 Member data validation

**Observed issue** (`Member.java:8–11`)

The `Member` constructor directly assigns the supplied ID, name, and tier without validating them.

**Why this is a problem**

A `Member` object could be created with a missing ID, missing name, or missing tier.

An unsupported tier could also enter the system and later produce an incorrect calculation.

**Recommended improvement**

Validate required member information when a `Member` is created.

Membership-tier values should be controlled so that unsupported or incorrectly formatted values are not silently accepted.

---

## 4. SOLID Principles Assessment

The SOLID principles provide a way to assess whether the application is organised for maintainability, extension, and reliable collaboration between classes.

### 4.1 Single Responsibility Principle (SRP) — Violated

**Meaning**

A class should have one main responsibility and one primary reason to change.

**Observation** (`PointsCalculator.java:10–35`)

`PointsCalculator.calculatePoints()` handles several responsibilities:

- Member lookup.
- Base-point calculation.
- Tier selection.
- Bonus calculation.
- Console output.
- Exception handling.

**Why it violates SRP**

These responsibilities can change independently.

For example, a change to the member storage mechanism should not require modifying the points calculation logic. Similarly, changing a tier multiplier should not require changing member lookup or console reporting.

**Recommended improvement**

Separate member lookup, tier rules, calculation, and application coordination into focused components.

The calculator can still coordinate calculation-related rules, but it should not also manage storage and user-facing output.

### 4.2 Open/Closed Principle (OCP) — Violated

**Meaning**

Software components should be open for extension but closed for unnecessary modification.

**Observation** (`PointsCalculator.java:17–23`)

The calculator directly checks for STANDARD, SILVER, and GOLD using an `if`/`else` structure.

**Why it violates OCP**

Introducing a new tier requires changing the existing calculation method.

This makes the method increasingly dependent on the number of membership tiers.

**Recommended improvement**

Introduce a common `TierPointsRule` abstraction.

Each tier should provide its own implementation of the required calculation behaviour.

A resolver or registry should identify the correct rule. A new tier can then be introduced through a new implementation and registration, without adding another tier-specific branch to the existing calculator.

### 4.3 Liskov Substitution Principle (LSP) — No confirmed direct violation

**Meaning**

An implementation should be usable wherever its abstraction is expected without breaking the behaviour promised by that abstraction.

**Observation** (`MemberRepository.java:3–4`, `InMemoryMemberRepository.java:17–18`)

`InMemoryMemberRepository` implements `MemberRepository`.

However, the interface does not specify what should happen when a member cannot be found.

The current implementation returns `null`, but another implementation might throw an exception or use a different result.

**Assessment**

There is no clear, demonstrated LSP violation in the current code because only one repository implementation is provided.

However, the unclear lookup contract creates a risk that future implementations will behave inconsistently.

**Recommended improvement**

Define one explicit lookup contract and require every repository implementation to follow it.

For example, the interface could return `Optional<Member>` or define a consistent not-found exception.

### 4.4 Interface Segregation Principle (ISP) — Currently followed

**Meaning**

A class should not be forced to implement methods it does not need.

**Observation** (`MemberRepository.java:3–4`)

`MemberRepository` contains only one method:

`findById(String id)`

The current implementation is not required to implement unrelated operations.

**Assessment**

The interface is small and focused. There is no identified ISP violation in the supplied implementation.

**Recommended improvement**

Keep repository interfaces focused. Add new operations only when they are required, and avoid expanding one interface with unrelated responsibilities.

### 4.5 Dependency Inversion Principle (DIP) — Violated

**Meaning**

High-level components should depend on abstractions rather than directly depending on lower-level implementation details.

**Observation** (`PointsCalculator.java:8`)

Although `MemberRepository` exists as an abstraction, `PointsCalculator` creates an `InMemoryMemberRepository` directly.

**Why it violates DIP**

The calculator is still coupled to the concrete in-memory implementation.

This makes it difficult to replace the storage mechanism or supply a controlled repository in tests.

**Recommended improvement**

Inject the `MemberRepository` abstraction through the constructor.

The application should decide which repository implementation to provide.

Tier rules should also be supplied through an abstraction or registry rather than being selected through hard-coded tier branches.

### 4.6 SOLID assessment summary

| Principle | Assessment | Main reason |
|---|---|---|
| SRP | Violated | The calculator handles several independent responsibilities. |
| OCP | Violated | Adding a tier requires modifying existing calculation logic. |
| LSP | No confirmed direct violation | The repository lookup contract is unclear, but only one implementation is supplied. |
| ISP | Followed | The repository interface contains one focused operation. |
| DIP | Violated | The calculator constructs a concrete repository instead of receiving the abstraction. |

---

## 5. Existing Test Suite Assessment

The project includes JUnit 5 and Mockito, but the quality of the existing tests is not sufficient to provide strong confidence in the points-calculation behaviour.

The three existing test methods are reviewed below.

### 5.1 `awardsPointsToAMember()` (`PointsCalculatorTest.java:11–14`)

**Current behaviour**

The test creates a real `PointsCalculator`, calculates points for member `m1` using a GHS 100 purchase, and checks that the result is greater than zero.

**Problem**

The assertion is too weak.

The expected result is 10 points, but the test would still pass if the calculator returned 11, 20, or 100 points.

It verifies that the result is positive rather than verifying that the business calculation is correct.

**Recommended improvement**

Use an exact assertion to check that the expected result is 10.

The test should verify the actual business rule, not simply that a positive integer was returned.

### 5.2 `calculatesGoldPoints()` (`PointsCalculatorTest.java:18–22`)

**Current behaviour**

The test creates a Mockito mock of `PointsCalculator`, configures the mock to return 50 for a particular method call, and then asserts that the returned value is 50.

**Problem**

The test does not execute the real `PointsCalculator.calculatePoints()` method.

It only verifies the behaviour of the configured mock.

It would still pass if the real Gold calculation were incorrect or removed completely.

**Recommended improvement**

Create a real calculator and provide its dependencies through injection.

The test should execute the actual calculation for the Gold member and assert that the result is exactly 50 for a GHS 250 purchase.

Mockito should be used to mock dependencies such as `MemberRepository`, not the class whose calculation behaviour is being tested.

### 5.3 `awardsPointsForAPurchase()` (`PointsCalculatorTest.java:26–29`)

**Current behaviour**

The test calculates points for member `m3` using a GHS 200 purchase and calls `assertNotNull(points)`.

**Problem**

The variable `points` is a primitive `int`.

A primitive integer cannot be `null`, so the assertion does not meaningfully test the calculation.

The expected result is 30 points, but the test does not verify that value.

**Recommended improvement**

Use an exact assertion to check that the Silver calculation returns 30 points.

### 5.4 Appropriate use of Mockito

Mockito is useful when a class depends on another class or interface that needs to be controlled during a test.

For this application, suitable dependencies to mock include:

- `MemberRepository`.
- A tier-rule collaborator, if the calculator depends on one.

Examples of meaningful Mockito tests include:

- Confirming that the repository is called with the correct member ID.
- Confirming that invalid input is rejected before a repository lookup, if that is the intended design.
- Simulating a missing member.
- Simulating a repository failure.
- Checking how the application responds when a required tier rule is unavailable.

The main calculation result should still come from the real class being tested.

### 5.5 Missing test scenarios

The current suite does not adequately cover the following scenarios:

**Membership-tier calculations**

- STANDARD exact points.
- SILVER exact points.
- GOLD exact points.
- Unsupported membership tier.
- Tier names with unexpected casing or whitespace.

**Purchase and bonus boundaries**

- GHS 499.99.
- GHS 500.
- GHS 500.01.
- A purchase greater than GHS 500 with the expected bonus.
- Fractional purchase amounts.
- Silver calculations where intermediate values contain fractions.

**Invalid purchase values**

- Negative amounts.
- Zero amounts, according to the agreed business rule.
- Very small positive amounts.
- `Double.NaN`.
- Positive infinity.
- Negative infinity.
- Extremely large finite values.

**Member lookup and dependency failures**

- Unknown member ID.
- Missing member returned by the repository.
- Repository exception.
- Unsupported tier rule.
- Unexpected calculation failure.

**Dependency and design behaviour**

- Calculator construction with an injected repository.
- Correct interaction with repository or tier-rule collaborators.
- Existing tier behaviour remaining correct when another tier is introduced.

### 5.6 Test quality recommendations

The test suite should use:

- Exact assertions for expected point totals.
- Separate tests for successful calculations and failure conditions.
- Boundary-value tests for the bonus threshold.
- Tests for each membership-tier rule.
- Mockito for dependencies rather than the class under test.
- Exception assertions for invalid inputs and missing members.

Code coverage and mutation testing can provide additional information, but they do not replace meaningful assertions.

---

## 6. Exception Handling Assessment

Exception handling is particularly important in this application because the current implementation hides failures by returning zero.

### 6.1 Unknown member ID

**Current behaviour**

`InMemoryMemberRepository.findById()` returns `null` when the supplied ID does not exist.

The calculator then attempts to access the member's tier. This causes a `NullPointerException`, which is caught by the broad exception handler.

The method returns zero.

**Problem**

The caller is not informed that the member could not be found.

**Recommended improvement**

Represent the missing-member condition explicitly.

A suitable design could use `Optional<Member>` or a dedicated `MemberNotFoundException`.

### 6.2 Null purchase amount

**Current behaviour**

The method accepts a primitive `double`.

A primitive `double` cannot hold `null`.

If a caller supplies a `Double` object containing `null`, Java attempts to unbox it before invoking the method. This can cause a `NullPointerException` at the caller.

**Problem**

The method cannot directly validate a null value through its current primitive parameter.

**Recommended improvement**

If the application needs to accept nullable external input, validate it at the API or application boundary before converting it into a valid purchase amount.

A domain input object can also help ensure that invalid values are rejected before calculation.

### 6.3 Zero purchase amount

**Current behaviour**

A zero purchase produces zero base points.

**Problem**

The current method returns zero for this legitimate calculation, but also returns zero for caught exceptions.

This makes the return value ambiguous.

**Recommended improvement**

Define whether zero-value purchases are allowed by the business rules.

If allowed, zero points should be a valid result. It should not be treated as a calculation failure.

### 6.4 Negative purchase amount

**Current behaviour**

Negative amounts can produce negative points.

For example:

- STANDARD, GHS -100 → -10 points.
- GOLD, GHS -100 → -20 points.

**Problem**

A negative purchase amount is not validated before the calculation.

**Recommended improvement**

Reject negative purchase amounts before calculating points, assuming the loyalty programme does not explicitly support negative purchases or refunds.

Refund handling, if required, should be defined as a separate business rule rather than accidentally producing negative points.

### 6.5 NaN, infinity, and large values

**Current behaviour**

The `double` type accepts `NaN`, positive infinity, and negative infinity.

These values can behave unexpectedly when converted to an integer.

For Java narrowing conversion from `double` to `int`:

- `NaN` converts to `0`.
- Positive infinity converts to `Integer.MAX_VALUE`.
- Negative infinity converts to `Integer.MIN_VALUE`.
- Values outside the integer range are limited to the relevant integer boundary.

**Problem**

The application can produce misleading or extreme point totals without clearly rejecting invalid purchase values.

**Recommended improvement**

Validate that purchase amounts are finite and within the supported business range before calculating points.

### 6.6 Unsupported membership tier

**Current behaviour**

The calculator checks for STANDARD and SILVER, and then GOLD through its conditional logic.

An unsupported tier can fall through the tier conditions without receiving a defined multiplier.

The application may therefore return base points without identifying the unsupported tier.

**Problem**

An incorrect or unsupported tier is treated as if the calculation were valid.

**Recommended improvement**

Use a controlled tier representation and a resolver that produces a clear unsupported-tier outcome when no matching rule exists.

### 6.7 Repository failure

**Current behaviour**

The calculator constructs its own repository, making it difficult to supply a repository that deliberately fails during a test.

If a future repository implementation throws an exception, the broad catch in the calculator will return zero.

**Problem**

The caller cannot distinguish a repository failure from a valid calculation.

**Recommended improvement**

Inject the repository and define how infrastructure failures are handled.

Known lookup failures should be represented explicitly. Unexpected infrastructure failures should retain their original cause when translated at an appropriate application boundary.

### 6.8 Appropriate exception types

Depending on the final design, meaningful exceptions could include:

| Exception | When it may be appropriate |
|---|---|
| `MemberNotFoundException` | The requested member does not exist. |
| `InvalidPurchaseAmountException` | A purchase amount violates the defined validation rules. |
| `UnsupportedMembershipTierException` | No registered rule exists for the member's tier. |
| `PointsCalculationException` | An unexpected calculation or operational failure needs to be exposed with additional context. |

These exceptions should only be introduced where they communicate a meaningful failure condition.

Creating many custom exceptions without a clear purpose would add unnecessary complexity.

### 6.9 Where try/catch should be used

A try/catch block should be used when the application has a specific reason to handle or translate an exception.

For example:

- Translating a repository infrastructure failure into an application-level failure.
- Recording an unexpected operational error before propagating it.
- Handling a failure at a boundary where the application can provide a meaningful response.

A broad catch around the entire calculation method is not appropriate because it hides programming errors and unrelated failure conditions.

---

## 7. Prioritised List of Identified Problems

The following list ranks the main issues according to their impact on application correctness, maintainability, and testability.

| Priority | Problem | Reason for priority |
|---:|---|---|
| 1 | Broad exception handling that returns zero | Hides failures and makes the calculation result unreliable. |
| 2 | Hard-coded membership-tier logic | Makes adding a new tier require changes to existing calculation logic. |
| 3 | Direct construction of the repository | Prevents easy dependency replacement and controlled testing. |
| 4 | Missing-member lookup returns null | Causes an unclear failure that is hidden by the exception handler. |
| 5 | Missing purchase validation | Allows invalid values to affect loyalty-point calculations. |
| 6 | Multiple responsibilities in PointsCalculator | Makes the class difficult to maintain and test independently. |
| 7 | Weak and ineffective tests | Gives insufficient confidence that the production logic works correctly. |
| 8 | Console output inside the calculation method | Creates unnecessary side effects and reduces reusability. |
| 9 | Magic numbers and plain-string tier values | Makes business rules harder to understand and maintain. |
| 10 | Unsupported tiers silently fall through | Can produce incorrect results without a clear failure. |

### 7.1 Highest-priority issue: failure converted into zero

The most serious issue is the broad exception handler.

The main purpose of the application is to calculate loyalty points accurately. However, the current method can return zero even when the calculation did not successfully complete.

This makes it impossible for the caller to reliably distinguish:

- A valid zero-point result.
- A missing member.
- An invalid purchase amount.
- An unsupported membership tier.
- A repository failure.
- An unexpected programming error.

This should be addressed before extending the application with additional membership tiers.

---

## 8. Recommended Refactoring Direction

The refactoring should improve the design without introducing unnecessary complexity.

The aim is to make the application easier to test, maintain, and extend while preserving the intended existing business rules.

### 8.1 Proposed responsibilities

**MemberRepository**

- Provides member lookup.
- Defines explicit behaviour when a member does not exist.
- Is supplied to higher-level components through dependency injection.

**Member**

- Represents a loyalty member.
- Stores the member's ID, name, and tier.
- Validates required information when an object is created.

**PointsCalculator**

- Focuses on calculating points.
- Uses the appropriate tier rule.
- Does not directly create the repository.
- Does not print success messages.
- Does not convert every exception into zero.

**TierPointsRule**

- Defines the common contract for membership-tier calculations.
- Allows different tier implementations to follow the same contract.
- Makes it possible to introduce a new tier without adding another conditional branch to the existing calculator.

**Tier-rule implementations**

- Provide the calculation behaviour for individual tiers.
- Keep tier-specific rules separate from the main calculation logic.

**Tier registry or resolver**

- Identifies the appropriate tier rule for a member.
- Provides a clear failure when a required rule is unavailable.

**Application service**

- Coordinates member lookup and points calculation.
- Handles the overall application workflow.
- Provides an appropriate place for application-level logging and failure translation.

**Purchase validation**

- Validates purchase values before calculation.
- Ensures the calculator receives valid input according to the agreed business rules.

**Composition root**

- Creates the concrete repository and tier-rule implementations.
- Injects the required dependencies into the application components.

### 8.2 Suggested design for future membership tiers

A new tier should follow the same contract as the existing tier implementations.

For example, introducing a PLATINUM tier should involve:

1. Creating a new PLATINUM tier-rule implementation.
2. Registering the new rule with the tier resolver or registry.
3. Ensuring the member data can represent the new tier.
4. Adding tests for the new tier's calculation.
5. Verifying that the existing STANDARD, SILVER, and GOLD calculations remain unchanged.

The existing calculator should not need a new PLATINUM-specific conditional branch.

This approach supports the Open/Closed Principle while keeping the design understandable.

### 8.3 Numeric and business-rule decisions

Before changing the calculation formula, the business rules should be confirmed.

The following decisions are important:

| Business rule | Decision required |
|---|---|
| Base points | Confirm whether every GHS 10 earns one point. |
| Fractional amounts | Confirm whether the calculation truncates or rounds. |
| Silver multiplier | Confirm how fractional Silver points are handled. |
| Bonus threshold | Confirm that the bonus applies only when the original purchase amount is greater than GHS 500. |
| Bonus amount | Confirm that the additional award is 50 points. |
| Zero-value purchase | Confirm whether zero-value purchases are accepted. |
| Negative purchase | Define how refunds or negative purchase amounts should be handled. |
| Large amounts | Define a supported purchase range and prevent integer overflow. |

The refactoring should not silently change the existing loyalty rules while improving the code structure.

### 8.4 Recommended testing direction

The updated test suite should verify both successful calculations and failure scenarios.

**Successful calculations**

- STANDARD member earns the expected points.
- SILVER member earns the expected points.
- GOLD member earns the expected points.
- A purchase greater than GHS 500 receives the correct bonus.

**Boundary tests**

- GHS 499.99.
- GHS 500.
- GHS 500.01.
- Fractional purchase amounts.
- Fractional tier calculations.

**Validation tests**

- Negative purchase amount.
- Zero purchase amount according to the confirmed business rule.
- `NaN`.
- Positive and negative infinity.
- Excessively large purchase amounts.

**Failure tests**

- Unknown member ID.
- Unsupported membership tier.
- Missing tier rule.
- Repository exception.
- Unexpected operational failure.

**Extension tests**

- New tier rule can be registered.
- The new tier produces its expected points.
- Existing tiers continue to produce their expected results.

Mockito should be used to control dependencies such as the repository or tier-rule collaborators. The tests should execute the real class whose behaviour is being tested and assert meaningful results.

---

## 9. Conclusion

The initial Brightline Loyalty implementation provides a simple starting point for calculating loyalty points, but its current design has weaknesses that affect reliability, maintainability, and testability.

The most serious issue is the broad exception handler that converts every caught failure into zero points. This hides important errors and makes it difficult for callers to know whether a calculation succeeded.

The hard-coded tier logic is another important concern because adding a new membership tier requires modifying the existing calculation method. This creates unnecessary coupling and makes future extensions more difficult.

The existing tests also need improvement. Assertions should verify exact expected point totals, and Mockito should be used to mock dependencies rather than the class under test.

The recommended refactoring should focus on:

1. Defining clear exception and failure behaviour.
2. Validating member information and purchase amounts.
3. Introducing constructor dependency injection.
4. Separating membership-tier rules through a common abstraction.
5. Keeping calculation logic separate from member lookup and console output.
6. Strengthening JUnit tests with exact assertions and boundary cases.
7. Ensuring a new membership tier can be added without modifying the established tier-calculation logic.

These improvements will provide a clearer foundation for extending Brightline Loyalty while protecting the correctness of its existing membership-tier calculations.