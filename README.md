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

The verified run on **2026-11-21** completed with 60 tests, 0 failures, 0 errors, and 0 skipped tests.

The PIT run on **2026-11-21** generated 48 mutations, all 48 killed, with 0 surviving and 0 without coverage. PIT reported 100% mutation test strength and 100% line coverage (112/112 lines) in mutated classes. These results are specific to that run, not a guarantee for future changes.

The CLI is a simple demonstration: ordinary results go to standard output, while missing-member and invalid-input messages go to standard error. This is not a production logging setup.

## Structure and design

- `com.brightline.loyalty` contains the member and tier domain model, tier strategies, and the `MemberRepository` interface.
- `com.brightline.loyalty.app` contains the points-calculation use case and a thin CLI entry point.
- `com.brightline.loyalty.infrastructure` contains the in-memory repository adapter used by the demo.

The CLI wires the in-memory adapter into `PointsCalculator`. The calculator depends on the repository interface rather than a concrete storage class. There is no separate service package: points calculation is the single application use case, so another layer would add indirection without separating more responsibilities.

Each tier's multiplier is a `TierStrategy`. A `MembershipTier` enum makes unsupported or misspelled tiers impossible to store on a `Member`. The repository returns `Optional<Member>` so missing data is explicit; the calculator converts it into a clear `Member not found` error.

The one-argument `PointsCalculator` constructor supplies a default `TierStrategyFactory` and delegates to the two-argument constructor. The CLI uses the two-argument constructor so its dependencies are explicit. There is one calculation method, `calculatePoints(String, BigDecimal)`; no legacy calculation overload remains.

In the original implementation, the points calculator constructed its own repository and held the tier conditional chain. Adding another tier or storage implementation therefore required editing or coupling to that class. In this design, storage is supplied at the application boundary and tier behavior is isolated behind the strategy contract.

Tier-specific calculation is delegated to `TierStrategy` implementations. `TierStrategyFactory` accepts an immutable registry of `MembershipTier` to `TierStrategy` mappings, so an application composition root can register a new strategy without adding a factory switch arm. An unregistered tier indicates a wiring error and fails with `IllegalStateException`. To add a tier, add its value to `MembershipTier`, implement its strategy, include the mapping in the configured registry, and add tests. The no-argument factory constructor remains a convenience registry for the built-in tiers and must be extended if it is still used as the default wiring.

## Behavior and safety

The rules for valid purchases are:

- One base point per GHS 10; fractional base points are truncated.
- STANDARD earns 1x, SILVER earns 1.5x (truncated), and GOLD earns 2x.
- Purchases strictly greater than GHS 500 receive 50 bonus points.
- Unknown members produce a `Member not found` error.

`DemoTest` captures and asserts standard output:

```text
Result m1 (STANDARD), GHS 100 -> 10
Result m2 (GOLD), GHS 250 -> 50
Result m3 (SILVER), GHS 500 -> 75
```

The missing-member message (`Error: Member not found`) is sent to standard error. Zero and negative purchases are rejected before member lookup and reported separately as invalid input. These are intentional safeguards; valid-purchase calculations and the strict bonus boundary are preserved. See [ANALYSIS.md](./ANALYSIS.md) for the prioritized pre-change diagnosis and behavior comparison.

### Most significant risk in the original implementation

The original calculator did not validate purchase amounts before calculating base points. Its rule was one point per GHS 10, so a STANDARD member's GHS -100 purchase produced -10 points instead of being rejected. Without a defined refund or reversal flow, accepting negative purchases could corrupt a member's points balance. The original code had no broad exception handler: it explicitly threw `IllegalArgumentException` for an unknown member, and did not swallow that error or return zero.

## Improvement beyond the brief

The implementation detects point-total overflow rather than allowing integer wraparound. `BigDecimal.intValueExact`, `Math.multiplyExact`, and `Math.addExact` make unsupported totals fail explicitly, and focused tests cover the overflow paths.