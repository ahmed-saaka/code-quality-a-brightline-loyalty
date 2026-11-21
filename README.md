# Brightline Loyalty Points

A Java 21 Maven application that calculates loyalty points from a member's tier and purchase amount.

## Requirements and commands

- JDK 21
- Maven 3.9.9 is pinned by the included Maven Wrapper. Maven will be downloaded automatically the first time the wrapper runs.

### Run the unit tests (PowerShell)

```powershell
.\mvnw.cmd test
```

On macOS/Linux, use `./mvnw` instead of `.\mvnw.cmd`.

### Run the CLI demo

```powershell
.\mvnw.cmd compile
java -cp target/classes com.brightline.loyalty.app.Demo
```

### Generate the JaCoCo coverage report

```powershell
.\mvnw.cmd verify
```

The report is written to `target/site/jacoco/index.html`.

### Run PIT mutation testing

```powershell
.\mvnw.cmd test-compile org.pitest:pitest-maven:mutationCoverage
```

The report is written to `target/pit-reports/index.html`.

## Test verification

The latest verified unit-test run completed with 53 tests, 0 failures, 0 errors, and 0 skipped tests.

The latest PIT mutation-testing run generated 42 mutations, killed all 42, and achieved 100% mutation test strength. Line coverage for mutated classes was 96/98 (98%).

These results record the latest runs only; they are not a guarantee for future code changes.

## Structure and design

- `com.brightline.loyalty` contains the member and tier domain model, tier strategies, and the `MemberRepository` interface.
- `com.brightline.loyalty.app` contains the points-calculation use case and a thin CLI entry point.
- `com.brightline.loyalty.infrastructure` contains the in-memory repository adapter used by the demo.

The CLI wires the in-memory adapter into `PointsCalculator`. The calculator depends on the repository interface rather than a concrete storage class. There is no separate service package: points calculation is the single application use case, so another layer would add indirection without separating more responsibilities.

Each tier's multiplier is a `TierStrategy`. A `MembershipTier` enum makes unsupported or misspelled tiers impossible to store on a `Member`. The repository returns `Optional<Member>` so missing data is explicit; the calculator converts it into a clear `Member not found` error.

The one-argument `PointsCalculator` constructor supplies a default `TierStrategyFactory` and delegates to the two-argument constructor. There is one calculation method, `calculatePoints(String, BigDecimal)`; no legacy calculation overload remains.

In the original implementation, the points calculator constructed its own repository and held the tier conditional chain. Adding another tier or storage implementation therefore required editing or coupling to that class. In this design, storage is supplied at the application boundary and tier behavior is isolated behind the strategy contract.

Tier-specific calculation is delegated to `TierStrategy` implementations, but `TierStrategyFactory` currently selects them with a switch expression. To add a tier, add its value to `MembershipTier`, implement its `TierStrategy`, add a factory switch arm, and add tests for the new strategy and calculation.

## Behavior and safety

The rules for valid purchases are:

- One base point per GHS 10; fractional base points are truncated.
- STANDARD earns 1x, SILVER earns 1.5x (truncated), and GOLD earns 2x.
- Purchases strictly greater than GHS 500 receive 50 bonus points.
- Unknown members produce a `Member not found` error.

`DemoTest` captures and asserts this CLI output:

```text
Result m1 (STANDARD), GHS 100 -> 10
Result m2 (GOLD), GHS 250 -> 50
Result m3 (SILVER), GHS 500 -> 75
Error: Member not found
```

Zero and negative purchases are rejected before member lookup. These are intentional safeguards; valid-purchase calculations and the strict bonus boundary are preserved. See [ANALYSIS.md](./ANALYSIS.md) for the prioritized pre-change diagnosis and behavior comparison.

### Most significant risk in the original implementation

The original calculator did not validate purchase amounts before calculating base points. Its rule was one point per GHS 10, so a STANDARD member's GHS -100 purchase produced -10 points instead of being rejected. Without a defined refund or reversal flow, accepting negative purchases could corrupt a member's points balance. The original code did explicitly throw `IllegalArgumentException` for an unknown member; it did not swallow that error or return zero.

## Improvement beyond the brief

The implementation detects point-total overflow rather than allowing integer wraparound. `BigDecimal.intValueExact`, `Math.multiplyExact`, and `Math.addExact` make unsupported totals fail explicitly, and focused tests cover the overflow paths.