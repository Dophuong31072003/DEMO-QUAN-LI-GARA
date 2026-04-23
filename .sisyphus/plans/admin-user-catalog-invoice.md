# Admin/User Catalog + Invoice Modules

## TL;DR
> **Summary**: Add the first real garage workflow to the Jakarta EE app: admin manages spare parts and service offerings, users search/select them into a persisted work-order draft, and admin issues immutable invoices from that draft.
> **Deliverables**:
> - Catalog management for spare parts and service offerings
> - Persisted user work-order draft with editable line items until invoiced
> - Admin invoice issuance with stock revalidation and price snapshots
> - User invoice list/detail screens
> - TDD coverage for service/DAO flows plus executable HTTP QA scenarios
> **Effort**: Large
> **Parallel**: YES - 3 waves
> **Critical Path**: Test/persistence foundation → domain entities → DAOs → services → servlets/JSPs → end-to-end verification

## Context
### Original Request
- ADMIN: quản lí phụ tùng và dịch vụ.
- ADMIN: xem phụ tùng hoặc dịch vụ mà User đã chọn và cho phép tạo hóa đơn.
- USER: có thể search và chọn phụ tùng hoặc dịch vụ muốn sử dụng.
- USER: có thể xem hóa đơn.

### Interview Summary
- Keep architecture at DAO + Service + Entity + Servlet/JSP; do not introduce Repository pattern.
- Shared `User` table remains the auth source; role is `is_admin`.
- Registration stays out of scope; two seeded users remain the only accounts.
- User selection is a persisted `WorkOrder` draft visible to admin.
- Admin parts scope includes CRUD plus stock quantity.
- Invoice scope is minimal: `ISSUED` only; no payment tracking.
- User may continue editing the same work-order until it is invoiced.
- Test strategy is TDD.

### Metis Review (gaps addressed)
- Locked a line-item model instead of pretending top-level aggregates are enough.
- Locked invoice price snapshots and stock revalidation at issuance time.
- Locked one open work-order per user to avoid draft sprawl.
- Locked soft-delete behavior for catalog items instead of hard delete.
- Locked a service-owned atomic transaction for invoice issuance; executor must not rely on the current per-DAO transaction style for that workflow.

## Work Objectives
### Core Objective
Implement a decision-complete admin/user workflow where admins manage catalog data, users build one persisted work-order draft from searchable spare parts and services, admins review that draft, and admins issue an immutable invoice that the user can later view.

### Deliverables
- New entities for `SparePart`, `ServiceOffering`, `WorkOrder`, `WorkOrderPartLine`, `WorkOrderServiceLine`, `Invoice`, and `InvoiceLine`
- Supporting status enums/constants and JPA registration
- DAO layer for catalog, work order, and invoice data
- Service layer for catalog CRUD/search, work-order editing/submission, and invoice issuance
- Admin servlets/JSPs for parts, services, work orders, and invoices
- User servlets/JSPs for parts search, services search, work-order editing, and invoice viewing
- Automated tests for DAO/service logic plus reproducible HTTP QA scenarios

### Definition of Done (verifiable conditions with commands)
- `./mvnw test` passes with new service/DAO tests covering catalog CRUD, single-draft behavior, invoice issuance, stock failure, and price snapshots.
- `./mvnw compile` passes after all servlet/JSP wiring is complete.
- With the app deployed at `http://localhost:8080/quanligara-1.0-SNAPSHOT`, curl-based auth, role, catalog, work-order, and invoice scenarios all pass.
- Admin can create/edit/deactivate spare parts and service offerings.
- User can search catalog items, add/remove/update quantities in exactly one open work-order draft, and submit/revisit that draft.
- Admin can view user work orders and issue exactly one invoice per work order.
- Invoice data remains historically stable after catalog price changes.

### Must Have
- One open work-order per user at any time.
- `WorkOrder` statuses fixed to `DRAFT`, `SUBMITTED`, `INVOICED`.
- Invoice status fixed to `ISSUED` for this scope.
- Spare-part stock decrements only when invoice issuance succeeds.
- Invoice issuance revalidates current stock inside the same transaction.
- Invoice lines store snapshot code/name/unit price/quantity/line total.
- Catalog items use soft-delete/active flags, not destructive hard delete from the UI.
- User endpoints enforce ownership; admin endpoints enforce `is_admin`.

### Must NOT Have (guardrails, AI slop patterns, scope boundaries)
- No registration UI or registration endpoint expansion.
- No Repository pattern, REST framework migration, Spring migration, or new architectural layer.
- No payment tracking, `PAID`, `VOID`, refunds, discounts, taxes, reservation logic, or invoice editing/canceling.
- No vehicle/customer/appointment domains in this plan.
- No pagination, reporting, file upload, image upload, or advanced search beyond name/code keyword filtering.
- No hard delete for referenced catalog rows.
- No business-rule enforcement in JSPs alone; all state transitions must be validated server-side.

## Verification Strategy
> ZERO HUMAN INTERVENTION - all verification is agent-executed.
- Test decision: TDD with JUnit 5. Add a dedicated test persistence setup so DAO/service tests do not depend on manual MySQL manipulation.
- QA policy: Every task includes automated or scriptable validation. Browser/manual-only checks are not acceptable.
- Base URL for HTTP QA: `http://localhost:8080/quanligara-1.0-SNAPSHOT`
- Seed credentials: `admin/admin`, `user/user`
- Evidence: `.sisyphus/evidence/task-{N}-{slug}.{ext}`

## Execution Strategy
### Parallel Execution Waves
> Target: 5-8 tasks per wave. <3 per wave (except final) = under-splitting.
> Extract shared dependencies as Wave-1 tasks for max parallelism.

Wave 1: test/persistence foundation, status design, catalog entities, work-order entities, invoice entities
Wave 2: DAO layer, catalog service, work-order service, invoice service
Wave 3: admin catalog web layer, admin work-order/invoice web layer, user catalog/work-order/invoice web layer, route/ownership hardening, end-to-end smoke coverage

### Dependency Matrix (full, all tasks)
| Task | Depends On | Blocks |
|---|---|---|
| 1 | - | 2,3,4,5,6,7,8,9,10,11,15 |
| 2 | 1 | 3,4,5,7,8,10,11 |
| 3 | 1,2 | 6,9,12,14 |
| 4 | 1,2 | 7,10,13,14 |
| 5 | 1,2,4 | 8,11,13,14 |
| 6 | 3 | 9,12,15 |
| 7 | 4 | 10,13,14,15 |
| 8 | 4,5 | 11,13,14,15 |
| 9 | 6 | 12,14,15 |
| 10 | 7 | 13,14,15 |
| 11 | 7,8 | 13,14,15 |
| 12 | 3,6,9 | 15 |
| 13 | 4,5,7,8,10,11 | 15 |
| 14 | 3,4,5,7,8,9,10,11 | 15 |
| 15 | 12,13,14 | F1,F2,F3,F4 |

### Agent Dispatch Summary (wave → task count → categories)
- Wave 1 → 5 tasks → unspecified-high, deep
- Wave 2 → 6 tasks → unspecified-high, deep
- Wave 3 → 4 tasks → visual-engineering, unspecified-high, deep

## TODOs
> Implementation + Test = ONE task. Never separate.
> EVERY task MUST have: Agent Profile + Parallelization + QA Scenarios.

<!-- TASKS INSERT HERE -->

- [x] 1. Establish JPA transaction + test foundation for the new module

  **What to do**: Add a dedicated test persistence setup for TDD and a shared JPA utility for this module. Extend `pom.xml` with test-only database support (H2) and keep JUnit 5 as the primary runner. Create `src/test/resources/META-INF/persistence.xml` with a test persistence unit that uses in-memory H2 and auto-creates schema for tests only. Add a shared utility under `src/main/java/com/quanli/quanligara/util/` that owns the `EntityManagerFactory` for the new module and exposes read-only plus transactional execution so service-layer invoice issuance can run inside one transaction. Update `README.md` instructions only if needed to reflect that business data must no longer rely on destructive `create` semantics for day-to-day development; for the app runtime switch `src/main/resources/META-INF/persistence.xml` from `create` to `update`.
  **Must NOT do**: Do not migrate to Spring, CDI transactions, or a new ORM helper framework. Do not change the existing auth flow beyond what is required to compile against the shared persistence utility.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: touches Maven, persistence bootstrapping, and test architecture.
  - Skills: `[]` - No specialized framework skill is required.
  - Omitted: `["nextjs-react-typescript", "postgres-drizzle"]` - Not relevant to Jakarta EE/JPA work.

  **Parallelization**: Can Parallel: NO | Wave 1 | Blocks: 2,3,4,5,6,7,8,9,10,11,15 | Blocked By: none

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/resources/META-INF/persistence.xml` - Existing JPA registration point and current schema-generation setting.
  - Pattern: `src/main/java/com/quanli/quanligara/dao/UserDAO.java` - Current per-method EntityManager style that must NOT be reused for atomic invoice issuance.
  - Pattern: `pom.xml` - Existing JUnit/Hibernate/MySQL dependency structure.
  - Pattern: `README.md` - Current documented runtime assumptions and default Tomcat context path.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=JpaModuleBootstrapTest` passes using the new test persistence unit.
  - [ ] `./mvnw compile` passes after switching runtime schema generation from `create` to `update`.
  - [ ] New shared JPA utility supports running a callback inside one transaction without opening nested transactions.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Test persistence boots in memory
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=JpaModuleBootstrapTest`.
    Expected: Build exits 0 and creates all new module tables in H2 for the test run.
    Evidence: .sisyphus/evidence/task-1-jpa-foundation.txt

  Scenario: Runtime persistence no longer wipes business data on every startup
    Tool: Bash
    Steps: Inspect `src/main/resources/META-INF/persistence.xml` and run `./mvnw compile`.
    Expected: Runtime persistence file uses `update`; compile exits 0.
    Evidence: .sisyphus/evidence/task-1-jpa-foundation-runtime.txt
  ```

  **Commit**: YES | Message: `test(persistence): add transaction-safe module foundation` | Files: `[pom.xml, src/main/resources/META-INF/persistence.xml, src/test/resources/META-INF/persistence.xml, src/main/java/com/quanli/quanligara/util/*]`

- [x] 2. Define shared statuses, item typing, and numbering rules

  **What to do**: Add explicit enum/domain contracts for `WorkOrderStatus` (`DRAFT`, `SUBMITTED`, `INVOICED`), `InvoiceStatus` (`ISSUED` only used now), and `InvoiceItemType` (`PART`, `SERVICE`). Define a deterministic invoice numbering rule in service code before implementation starts: `INV-YYYYMMDD-{6-digit-sequence}` generated at issuance time. Store statuses as `EnumType.STRING`. Document and enforce these invariants in service tests: one open work order per user, edit allowed in `DRAFT` and `SUBMITTED`, no edit in `INVOICED`, and invoice status must always be `ISSUED` in this scope.
  **Must NOT do**: Do not add `PAID`, `VOID`, or extra workflow states. Do not postpone the invoice-number format decision.

  **Recommended Agent Profile**:
  - Category: `quick` - Reason: small but cross-cutting domain contract task.
  - Skills: `[]` - No external skill needed.
  - Omitted: `["find-docs"]` - No external library/API ambiguity exists.

  **Parallelization**: Can Parallel: NO | Wave 1 | Blocks: 3,4,5,7,8,10,11 | Blocked By: 1

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/model/User.java` - Existing entity style and enum-free baseline.
  - Pattern: `.sisyphus/drafts/admin-user-modules.md` - Interview decisions already made for statuses and invoice scope.
  - External: `README.md` - Default runtime context path for later QA route contracts.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=WorkflowContractTest` passes and proves the locked status transitions.
  - [ ] All new status/item enums use `EnumType.STRING` in entity mappings.
  - [ ] Invoice number generation test proves the `INV-YYYYMMDD-XXXXXX` format.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Status contract is enforced by tests
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=WorkflowContractTest`.
    Expected: Tests pass for allowed transitions and fail when forced to edit an invoiced work order.
    Evidence: .sisyphus/evidence/task-2-workflow-contract.txt

  Scenario: Invoice numbering stays deterministic
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=InvoiceNumberGeneratorTest`.
    Expected: Generated numbers match `INV-YYYYMMDD-XXXXXX` with a six-digit suffix.
    Evidence: .sisyphus/evidence/task-2-invoice-number.txt
  ```

  **Commit**: NO | Message: `feat(domain): add workflow enums and numbering rules` | Files: `[src/main/java/com/quanli/quanligara/model/enums/*]`

- [x] 3. Implement catalog entities for spare parts and service offerings

  **What to do**: Create `SparePart` and `ServiceOffering` entities under `src/main/java/com/quanli/quanligara/model/`. `SparePart` fields: `id`, `code`, `name`, `description`, `unitName`, `unitPrice(BigDecimal)`, `stockQuantity(Integer)`, `isActive(boolean)`, `version(Long via @Version)`. `ServiceOffering` fields: `id`, `code`, `name`, `description`, `unitPrice(BigDecimal)`, `isActive(boolean)`. Apply the same JPA style as `User`: explicit `@Table`, `@Column`, generated `id`, `Serializable`, `equals/hashCode` by id. Register both entities in runtime and test persistence units. Enforce unique business codes at DAO/service level and via table constraints where supported.
  **Must NOT do**: Do not collapse parts and services into one table. Do not model hard deletes. Do not use `double`/`float` for prices.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: foundational domain modeling with JPA and concurrency guard for stock.
  - Skills: `[]` - Straight JPA/entity work.
  - Omitted: `["postgres-drizzle"]` - Wrong stack.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 6,9,12,14 | Blocked By: 1,2

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/model/User.java` - Follow annotation style, constructor/getter/setter structure, and id-based equality.
  - Pattern: `src/main/resources/META-INF/persistence.xml` - Add entity registrations.
  - Guardrail: Metis review requires soft-delete behavior and optimistic locking for stock-sensitive rows.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=CatalogEntityMappingTest` passes.
  - [ ] Runtime and test persistence units both register `SparePart` and `ServiceOffering`.
  - [ ] `SparePart` uses `@Version` and both entities map prices with `BigDecimal`.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Catalog entities map successfully
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=CatalogEntityMappingTest`.
    Expected: JPA boots and persists both catalog entity types without schema errors.
    Evidence: .sisyphus/evidence/task-3-catalog-entities.txt

  Scenario: Price and stock rules are structural
    Tool: Bash
    Steps: Inspect compiled tests plus source; run `./mvnw compile`.
    Expected: Prices are `BigDecimal`, `SparePart` has `@Version`, compile exits 0.
    Evidence: .sisyphus/evidence/task-3-catalog-structure.txt
  ```

  **Commit**: NO | Message: `feat(domain): add spare part and service offering entities` | Files: `[src/main/java/com/quanli/quanligara/model/SparePart.java, src/main/java/com/quanli/quanligara/model/ServiceOffering.java, src/main/resources/META-INF/persistence.xml, src/test/resources/META-INF/persistence.xml]`

- [x] 4. Implement work-order aggregate and editable line-item entities

  **What to do**: Create `WorkOrder`, `WorkOrderPartLine`, and `WorkOrderServiceLine`. `WorkOrder` fields: `id`, `user(ManyToOne User)`, `status(Enum STRING)`, `submittedAt(LocalDateTime nullable until first submit)`, `invoicedAt(LocalDateTime nullable)`. Add helper methods to expose whether editing is allowed based on status. `WorkOrderPartLine` fields: `id`, `workOrder(ManyToOne)`, `sparePart(ManyToOne)`, `quantity(Integer)`. `WorkOrderServiceLine` fields: `id`, `workOrder(ManyToOne)`, `serviceOffering(ManyToOne)`, `quantity(Integer default 1 but editable)`. Use cascade/orphan-removal only from `WorkOrder` to its line collections if it simplifies aggregate updates; keep catalog entities independent. Register all three entities in both persistence units.
  **Must NOT do**: Do not allow multiple open work orders per user. Do not store invoice snapshots here. Do not add customer/vehicle references.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: aggregate design with ownership and line-item relationships.
  - Skills: `[]` - Pure domain/JPA task.
  - Omitted: `["find-docs"]` - Current JPA usage is already established locally.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 7,10,13,14 | Blocked By: 1,2

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/model/User.java` - Base entity style.
  - Decision: User keeps exactly one open editable work order; editing is blocked only after `INVOICED`.
  - Decision: Admin must be able to see persisted user selections, so these rows are business records, not session state.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=WorkOrderEntityMappingTest` passes.
  - [ ] Work-order mapping supports one header with both part lines and service lines.
  - [ ] Work-order status is persisted as `EnumType.STRING` and editing helper logic is covered by tests.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Work-order aggregate persists mixed lines
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=WorkOrderEntityMappingTest`.
    Expected: One work order can persist both `WorkOrderPartLine` and `WorkOrderServiceLine` rows.
    Evidence: .sisyphus/evidence/task-4-work-order-entities.txt

  Scenario: Invoiced work orders are structurally non-editable
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=WorkOrderStatusRuleTest`.
    Expected: Tests prove edits are rejected once status becomes `INVOICED`.
    Evidence: .sisyphus/evidence/task-4-work-order-status.txt
  ```

  **Commit**: NO | Message: `feat(domain): add work order aggregate and selection lines` | Files: `[src/main/java/com/quanli/quanligara/model/WorkOrder*.java, src/main/resources/META-INF/persistence.xml, src/test/resources/META-INF/persistence.xml]`

- [x] 5. Implement invoice aggregate with immutable snapshot lines

  **What to do**: Create `Invoice` and `InvoiceLine`. `Invoice` fields: `id`, `invoiceNumber(unique)`, `workOrder(OneToOne or ManyToOne unique by business rule)`, `user(ManyToOne User)`, `status(Enum STRING)`, `issuedAt(LocalDateTime)`, `totalAmount(BigDecimal)`. `InvoiceLine` fields: `id`, `invoice(ManyToOne)`, `itemType(Enum STRING PART/SERVICE)`, `itemCode`, `itemName`, `unitPrice(BigDecimal)`, `quantity(Integer)`, `lineTotal(BigDecimal)`. Snapshot data must be copied from catalog/work-order state during issuance and never recomputed from live catalog prices after issuance. Register both entities in runtime and test persistence units.
  **Must NOT do**: Do not reference live catalog price from invoice detail rendering. Do not model payment data, taxes, discounts, or cancellation fields.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: invoice immutability is a core business invariant.
  - Skills: `[]` - No extra skill required.
  - Omitted: `["postgres-drizzle", "nextjs-react-typescript"]` - Irrelevant stack.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 8,11,13,14 | Blocked By: 1,2,4

  **References** (executor has NO interview context - be exhaustive):
  - Decision: Invoice scope is `ISSUED` only.
  - Decision: Price snapshots belong on invoice lines, not as live catalog lookups.
  - Risk: Metis flagged historical pricing as a critical guardrail.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=InvoiceEntityMappingTest` passes.
  - [ ] Invoice lines persist `itemType`, `itemCode`, `itemName`, `unitPrice`, `quantity`, and `lineTotal`.
  - [ ] Invoice status persists as `EnumType.STRING` and defaults to `ISSUED` in service flow tests.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Invoice mapping persists immutable line snapshots
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=InvoiceEntityMappingTest`.
    Expected: Invoice plus multiple snapshot lines persist without depending on live catalog joins.
    Evidence: .sisyphus/evidence/task-5-invoice-entities.txt

  Scenario: Invoice totals are numeric-safe
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=InvoiceAmountPrecisionTest`.
    Expected: Total calculations use `BigDecimal` and preserve exact decimal values.
    Evidence: .sisyphus/evidence/task-5-invoice-amounts.txt
  ```

  **Commit**: YES | Message: `feat(domain): add work order and invoice entities` | Files: `[src/main/java/com/quanli/quanligara/model/WorkOrder*.java, src/main/java/com/quanli/quanligara/model/Invoice*.java, src/main/resources/META-INF/persistence.xml, src/test/resources/META-INF/persistence.xml]`

- [x] 6. Implement DAO layer for spare parts and service offerings

  **What to do**: Add `SparePartDAO` and `ServiceOfferingDAO` using the established DAO style for basic queries, but backed by the shared JPA utility from Task 1. Required queries: `findById`, `findByCode`, `findActiveByKeyword(String q)`, `findAllForAdmin()`, `save`, `update`, `deactivate`, and `existsByCode`. `findActiveByKeyword` must search both `code` and `name` using case-insensitive keyword matching. `deactivate` must flip `isActive=false`; no physical delete method should be exposed to servlets.
  **Must NOT do**: Do not add public hard-delete methods. Do not mix service business logic into the DAO.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: multiple query paths, soft-delete semantics, and search behavior.
  - Skills: `[]` - Standard JPA task.
  - Omitted: `["find-docs"]` - Existing code patterns are sufficient.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 9,12,15 | Blocked By: 3

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/dao/UserDAO.java` - Use Optional/list return style and query naming conventions.
  - Guardrail: Catalog deletion must be soft-delete only.
  - Decision: User-facing search filters on `code` and `name`; only active rows appear to users.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=SparePartDaoTest,ServiceOfferingDaoTest` passes.
  - [ ] Admin queries return active and inactive rows; user queries return active rows only.
  - [ ] Keyword search matches both code and name fields case-insensitively.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: DAO search returns active catalog rows
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=SparePartDaoTest,ServiceOfferingDaoTest`.
    Expected: Search for `Brake` returns `Brake Pad`; deactivated rows are excluded from active search.
    Evidence: .sisyphus/evidence/task-6-catalog-dao.txt

  Scenario: DAO soft delete preserves row history
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=CatalogSoftDeleteDaoTest`.
    Expected: `deactivate` toggles `isActive=false` and row remains queryable in admin scope.
    Evidence: .sisyphus/evidence/task-6-catalog-soft-delete.txt
  ```

  **Commit**: NO | Message: `feat(dao): add catalog persistence queries` | Files: `[src/main/java/com/quanli/quanligara/dao/SparePartDAO.java, src/main/java/com/quanli/quanligara/dao/ServiceOfferingDAO.java, src/test/java/**/*Catalog*Test.java]`

- [x] 7. Implement DAO layer for work-order aggregate access

  **What to do**: Add DAO coverage for `WorkOrder`, `WorkOrderPartLine`, and `WorkOrderServiceLine`. Required behavior: load/create the single open work order for a user, fetch all open/submitted work orders for admin review, fetch one work order with both line collections, add/update/remove lines, and mark header status/timestamps. Define the canonical query rules now: user-facing reads always filter by `user.id`, admin list shows `DRAFT` and `SUBMITTED`, and once a work order is `INVOICED` it is excluded from the open-draft query.
  **Must NOT do**: Do not let DAO methods bypass ownership/status constraints. Do not create a second open work order if one already exists.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: aggregate queries with ownership/status constraints.
  - Skills: `[]` - No external skills needed.
  - Omitted: `["find-docs"]` - Existing JPA patterns already known.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 10,13,14,15 | Blocked By: 4

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/dao/UserDAO.java` - Optional/list semantics and transaction scaffolding baseline.
  - Decision: exactly one open work order per user.
  - Decision: user may edit after submit until invoiced; invoice locks the work order permanently.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=WorkOrderDaoTest` passes.
  - [ ] DAO guarantees one open work order per user by returning the existing header instead of creating a duplicate.
  - [ ] Admin query returns open/submitted work orders with line items available for detail view.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Single open work-order rule holds
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=WorkOrderDaoTest`.
    Expected: Repeated "get or create" calls for the same user return one header row until invoiced.
    Evidence: .sisyphus/evidence/task-7-work-order-dao.txt

  Scenario: Admin review query sees submitted selections
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=AdminWorkOrderQueryDaoTest`.
    Expected: Admin query returns submitted/open work orders with both part and service line collections eagerly accessible for detail rendering.
    Evidence: .sisyphus/evidence/task-7-admin-work-order-query.txt
  ```

  **Commit**: NO | Message: `feat(dao): add work order aggregate queries` | Files: `[src/main/java/com/quanli/quanligara/dao/WorkOrder*.java, src/test/java/**/*WorkOrderDaoTest.java]`

- [x] 8. Implement DAO layer and transaction path for invoice issuance

  **What to do**: Add `InvoiceDAO` plus any supporting query helpers needed for invoice lines and issuance lookup. More importantly, implement the transaction path that allows service code to issue an invoice atomically: fetch the target work order and its lines, lock/reload stock-sensitive spare parts, validate stock, decrement stock, create invoice header/lines, and mark the work order `INVOICED` in one transaction. The exact transaction boundary must live in the service layer but DAO methods used by that flow must accept the active `EntityManager` from Task 1 instead of opening their own transaction.
  **Must NOT do**: Do not open separate DAO-local transactions during invoice issuance. Do not create an invoice before stock validation succeeds.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: this is the main correctness-sensitive transaction design task.
  - Skills: `[]` - No special library skill required.
  - Omitted: `["postgres-drizzle", "langgraph-fundamentals"]` - Not relevant.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 11,13,14,15 | Blocked By: 4,5

  **References** (executor has NO interview context - be exhaustive):
  - Guardrail: Metis identified current per-DAO transactions as incompatible with atomic invoice issuance.
  - Decision: stock decreases only on successful invoice issuance.
  - Decision: insufficient stock must fail the whole issuance and leave stock plus work-order state unchanged.
  - Pattern: `src/main/java/com/quanli/quanligara/util/*` from Task 1 - Use the shared transactional runner.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=InvoiceDaoTransactionTest` passes.
  - [ ] A failed stock validation rolls back invoice row creation, invoice lines, stock decrement, and work-order status updates.
  - [ ] The issuance path can read back one invoice by work order and one invoice list by user.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Invoice issuance transaction commits atomically
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=InvoiceDaoTransactionTest`.
    Expected: Successful issuance writes invoice header/lines, decrements stock, and updates work-order status in one commit.
    Evidence: .sisyphus/evidence/task-8-invoice-transaction.txt

  Scenario: Insufficient stock rolls back everything
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=InvoiceDaoRollbackTest`.
    Expected: No invoice rows are created, stock remains unchanged, and work-order status remains pre-issuance.
    Evidence: .sisyphus/evidence/task-8-invoice-rollback.txt
  ```

  **Commit**: YES | Message: `feat(dao): add atomic invoice issuance persistence path` | Files: `[src/main/java/com/quanli/quanligara/dao/Invoice*.java, src/test/java/**/*Invoice*Dao*Test.java, src/main/java/com/quanli/quanligara/util/*]`

- [x] 9. Implement CatalogService for admin CRUD and user search

  **What to do**: Add `CatalogService` that wraps `SparePartDAO` and `ServiceOfferingDAO`. Admin-side methods: create, update, deactivate, list-all, load-for-edit. User-side methods: search active parts by keyword and search active services by keyword. Validate unique codes, require non-negative stock, require positive prices, and normalize empty descriptions to empty string rather than null if that simplifies JSP rendering. Return DTOs only if absolutely required for JSP simplicity; otherwise keep entity-backed rendering.
  **Must NOT do**: Do not let servlets write directly to DAOs. Do not allow negative stock or zero/negative prices.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: business-rule layer over DAO CRUD/search.
  - Skills: `[]` - Standard service work.
  - Omitted: `["find-docs"]` - No external uncertainty.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 12,14,15 | Blocked By: 6

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/service/AuthService.java` - Service wraps DAO, adds validation/business rules.
  - Decision: admin catalog supports CRUD + stock for parts; services have no stock field.
  - Decision: user search is simple keyword search on code/name only.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=CatalogServiceTest` passes.
  - [ ] Duplicate codes are rejected at service level before persistence.
  - [ ] Invalid prices/stock are rejected with deterministic validation messages or exceptions.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Catalog service accepts valid admin CRUD commands
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=CatalogServiceTest`.
    Expected: Valid create/update/deactivate paths pass and persist expected values.
    Evidence: .sisyphus/evidence/task-9-catalog-service.txt

  Scenario: Catalog service rejects invalid business inputs
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=CatalogValidationServiceTest`.
    Expected: Duplicate codes, negative stock, and non-positive prices are rejected without partial writes.
    Evidence: .sisyphus/evidence/task-9-catalog-validation.txt
  ```

  **Commit**: NO | Message: `feat(service): add catalog business rules` | Files: `[src/main/java/com/quanli/quanligara/service/CatalogService.java, src/test/java/**/*CatalogService*Test.java]`

- [x] 10. Implement WorkOrderService for single-draft editing and submission

  **What to do**: Add `WorkOrderService` as the only write path for user selections. Required methods: get-or-create current draft for user, add/update/remove spare-part line, add/update/remove service line, submit draft, reload editable draft, and list submitted/open work orders for admin review. Business rules: exactly one open draft per user; adding an existing line updates quantity instead of duplicating it; removing the last line leaves the work order header in place unless the implementation explicitly removes empty non-submitted drafts; `submit` stamps `submittedAt` once and moves `DRAFT -> SUBMITTED`; subsequent user edits are allowed but must keep status `SUBMITTED`; any attempt to edit an invoiced work order must fail.
  **Must NOT do**: Do not decrement stock here. Do not allow a user to load or mutate another user’s work order.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: this task locks the user-edit lifecycle and ownership rules.
  - Skills: `[]` - No special skill required.
  - Omitted: `["find-docs"]` - Local domain decisions are already made.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 13,14,15 | Blocked By: 7

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/service/AuthService.java` - Service-level validation and DAO orchestration baseline.
  - Decision: user may edit after submit until invoice issuance.
  - Decision: one open work order per user.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=WorkOrderServiceTest` passes.
  - [ ] Adding the same part/service twice updates quantity instead of creating duplicate lines.
  - [ ] `submit` transitions `DRAFT` to `SUBMITTED` and blocks nothing except cross-user access.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: User draft editing stays within one open work order
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=WorkOrderServiceTest`.
    Expected: Same user always gets the same open draft until invoiced; duplicate adds merge quantities.
    Evidence: .sisyphus/evidence/task-10-work-order-service.txt

  Scenario: Ownership and status rules are enforced
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=WorkOrderOwnershipServiceTest`.
    Expected: User cannot mutate another user’s draft and cannot edit after status `INVOICED`.
    Evidence: .sisyphus/evidence/task-10-work-order-ownership.txt
  ```

  **Commit**: NO | Message: `feat(service): add work order draft lifecycle` | Files: `[src/main/java/com/quanli/quanligara/service/WorkOrderService.java, src/test/java/**/*WorkOrderService*Test.java]`

- [x] 11. Implement InvoiceService for issuance, stock revalidation, and snapshots

  **What to do**: Add `InvoiceService` as the only place allowed to issue an invoice. On issue: load the targeted work order with lines; verify it belongs to a real user and is not already invoiced; re-read each `SparePart` inside the active transaction; validate requested quantity against current stock; decrement stock; build invoice header; copy immutable `InvoiceLine` snapshots from current catalog code/name/unit price; calculate `lineTotal` and `totalAmount`; generate invoice number; set work-order status to `INVOICED`; stamp `invoicedAt`; persist once. Add read methods for admin invoice list/detail and user-owned invoice list/detail.
  **Must NOT do**: Do not permit double issuance for the same work order. Do not read historical invoice totals from live catalog rows. Do not allow invoice issuance from an empty work order.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: highest-risk business transaction and historical correctness logic.
  - Skills: `[]` - No library-specific skill needed.
  - Omitted: `["postgres-drizzle", "find-docs"]` - Not relevant.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 13,14,15 | Blocked By: 7,8

  **References** (executor has NO interview context - be exhaustive):
  - Decision: stock decrements only at issuance time.
  - Decision: invoice lines are immutable snapshots.
  - Decision: invoice scope is `ISSUED` only.
  - Guardrail: insufficient stock must roll back the entire transaction.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test -Dtest=InvoiceServiceTest` passes.
  - [ ] Issuing an invoice from a valid work order decrements stock exactly once and marks the work order `INVOICED`.
  - [ ] Repricing a catalog item after issuance does not alter stored invoice totals or line prices.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Happy-path issuance succeeds atomically
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=InvoiceServiceTest`.
    Expected: Invoice is created once, work order becomes `INVOICED`, and part stock decreases by the issued quantity.
    Evidence: .sisyphus/evidence/task-11-invoice-service.txt

  Scenario: Snapshot pricing is immutable
    Tool: Bash
    Steps: Run `./mvnw test -Dtest=InvoiceSnapshotServiceTest`.
    Expected: Changing catalog price after issuance does not change stored invoice line prices or total.
    Evidence: .sisyphus/evidence/task-11-invoice-snapshot.txt
  ```

  **Commit**: YES | Message: `feat(service): add invoice issuance and snapshot logic` | Files: `[src/main/java/com/quanli/quanligara/service/InvoiceService.java, src/test/java/**/*InvoiceService*Test.java]`

- [x] 12. Create shared JSP fragments and fixed admin route contract

  **What to do**: Add shared JSP fragments under `src/main/webapp/WEB-INF/views/common/` for admin navigation and user navigation to avoid duplicating navbar markup across all new pages. Keep current landing pages but update them to link into the new modules. Lock the admin route contract to these servlet endpoints and JSPs:
  - `GET /admin/parts` → `WEB-INF/views/admin/parts.jsp`
  - `POST /admin/parts` create
  - `POST /admin/parts/update` update
  - `POST /admin/parts/deactivate` soft delete
  - `GET /admin/services` → `WEB-INF/views/admin/services.jsp`
  - `POST /admin/services` create
  - `POST /admin/services/update` update
  - `POST /admin/services/deactivate` soft delete
  Update `AdminServlet` dashboard links so the admin landing page visibly reaches `parts`, `services`, `work-orders`, and `invoices`.
  **Must NOT do**: Do not invent a new routing root outside `/admin/*`. Do not replace JSP with another templating engine.

  **Recommended Agent Profile**:
  - Category: `visual-engineering` - Reason: shared JSP layout plus admin route/navigation fit-and-finish.
  - Skills: `[]` - Current JSP stack is simple enough without extra skills.
  - Omitted: `["frontend-ui-ux"]` - Nice-to-have only; not required for decision-complete delivery.

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: 15 | Blocked By: 3,6,9

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/webapp/WEB-INF/views/admin/dashboard.jsp` - Existing admin page styling and navbar concept.
  - Pattern: `src/main/java/com/quanli/quanligara/controller/AdminServlet.java` - Dashboard forwarding pattern.
  - Pattern: `src/main/java/com/quanli/quanligara/controller/auth/AuthFilter.java` - `/admin/*` is already protected.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw compile` passes with the shared JSP fragments and admin route servlets in place.
  - [ ] `GET /admin/parts` and `GET /admin/services` both resolve for an authenticated admin session.
  - [ ] Admin dashboard contains visible links to the new admin module routes.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Admin routes are wired and reachable
    Tool: Bash
    Steps: Login as `admin` via curl cookie jar, then request `/admin/parts` and `/admin/services`.
    Expected: Both requests return `200` and render page titles `Parts` and `Services`.
    Evidence: .sisyphus/evidence/task-12-admin-routes.txt

  Scenario: Admin dashboard links expose the module entry points
    Tool: Playwright
    Steps: Login as `admin`, open `/admin`, assert links with text `Parts`, `Services`, `Work Orders`, and `Invoices` are visible.
    Expected: All four links exist and navigate to `/admin/*` routes.
    Evidence: .sisyphus/evidence/task-12-admin-dashboard.png
  ```

  **Commit**: NO | Message: `feat(web): add shared jsp fragments and admin route shell` | Files: `[src/main/webapp/WEB-INF/views/common/*.jspf, src/main/webapp/WEB-INF/views/admin/*.jsp, src/main/java/com/quanli/quanligara/controller/*]`

- [x] 13. Implement admin work-order review and invoice issuance screens

  **What to do**: Add admin servlets and JSPs for work-order review and invoice viewing using the fixed routes:
  - `GET /admin/work-orders` → list all `DRAFT` + `SUBMITTED` work orders with user name, status, line counts, and submitted timestamp
  - `GET /admin/work-orders/detail?id={workOrderId}` → detail view with selected parts/services and current prices
  - `POST /admin/invoices/issue` with `workOrderId`
  - `GET /admin/invoices` → list issued invoices
  - `GET /admin/invoices/detail?id={invoiceId}` → immutable invoice detail
  Detail page must expose a single `Issue Invoice` button only when the work order is not already invoiced and contains at least one line. If issuance fails because stock is insufficient, return to detail page with a deterministic error message naming the failing part code and available stock.
  **Must NOT do**: Do not let admin mutate invoice lines after issuance. Do not hide issuance failures behind generic errors.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: combines service calls, role-gated routes, and admin JSP detail views.
  - Skills: `[]` - JSP/Servlet work does not need a special skill.
  - Omitted: `["frontend-ui-ux"]` - Functional JSP screens are sufficient.

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: 15 | Blocked By: 4,5,7,8,10,11

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/controller/AdminServlet.java` - Basic servlet forwarding convention.
  - Pattern: `src/main/webapp/WEB-INF/views/admin/dashboard.jsp` - Existing admin visual baseline.
  - Pattern: `src/main/java/com/quanli/quanligara/controller/auth/AuthFilter.java` - `/admin/*` protection and 403 behavior.
  - Decision: admin can review user selections and issue one invoice; invoice scope is issued-only.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw compile` passes with admin work-order/invoice servlets and JSPs.
  - [ ] Admin can list open work orders, open one detail page, and issue one invoice from it.
  - [ ] Insufficient stock returns a deterministic error on the detail page and does not create an invoice.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Admin issues invoice successfully
    Tool: Playwright
    Steps: Login as `admin`; open `/admin/work-orders`; click a detail link for a seeded/open work order; click `Issue Invoice`; follow redirect to `/admin/invoices/detail?id=*`.
    Expected: Invoice detail page shows status `ISSUED`, immutable lines, and the linked work order is no longer issuable.
    Evidence: .sisyphus/evidence/task-13-admin-issue-invoice.png

  Scenario: Admin sees explicit stock failure
    Tool: Playwright
    Steps: Create/select a work order whose part quantity exceeds stock; login as `admin`; open `/admin/work-orders/detail?id=*`; click `Issue Invoice`.
    Expected: Same detail page reloads with visible error text containing the failing part code and available stock; no invoice detail redirect occurs.
    Evidence: .sisyphus/evidence/task-13-admin-stock-failure.png
  ```

  **Commit**: YES | Message: `feat(web): add admin work order review and invoice issuance` | Files: `[src/main/java/com/quanli/quanligara/controller/admin/*.java, src/main/webapp/WEB-INF/views/admin/*.jsp]`

- [x] 14. Implement user catalog search, selection, work-order, and invoice screens

  **What to do**: Add user servlets and JSPs using only `/home/*` routes so existing filter protection remains valid:
  - `GET /home/parts?q=` → searchable parts page with rows showing code, name, price, stock, quantity input, and `Add` button
  - `POST /home/parts/select`
  - `GET /home/services?q=` → searchable services page with code, name, price, quantity input, and `Add` button
  - `POST /home/services/select`
  - `GET /home/work-order` → current draft detail with editable quantities, remove buttons, subtotal preview, and `Submit to Admin` button
  - `POST /home/work-order/update-part`
  - `POST /home/work-order/update-service`
  - `POST /home/work-order/remove-part`
  - `POST /home/work-order/remove-service`
  - `POST /home/work-order/submit`
  - `GET /home/invoices` and `GET /home/invoices/detail?id={invoiceId}`
  Lock the HTML contract for automation: part/service search inputs use `[name="q"]`; quantity inputs use `[name="quantity"]`; add buttons include `data-item-code`; work-order rows include `data-line-type="part|service"`; submit button text is exactly `Submit to Admin`.
  **Must NOT do**: Do not introduce `/user/*` routes. Do not let user see invoices or work orders owned by another account.

  **Recommended Agent Profile**:
  - Category: `visual-engineering` - Reason: user-facing JSP screens plus consistent automation-friendly selectors.
  - Skills: `[]` - Plain JSP/Servlet UI task.
  - Omitted: `["frontend-ui-ux"]` - Avoid scope drift into redesign work.

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: 15 | Blocked By: 3,4,5,7,8,9,10,11

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/webapp/WEB-INF/views/user/home.jsp` - Existing user page styling baseline.
  - Pattern: `src/main/java/com/quanli/quanligara/controller/HomeServlet.java` - Simple user servlet forwarding pattern.
  - Pattern: `src/main/java/com/quanli/quanligara/controller/auth/AuthFilter.java` - `/home/*` protection already exists.
  - Decision: user can keep editing until invoiced, then can only view the invoice.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw compile` passes with all `/home/*` servlets and JSPs.
  - [ ] User can search active parts/services, add them to the current work order, update quantities, and submit the draft.
  - [ ] User can list only their own issued invoices and open their own invoice detail page.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: User search and selection persist correctly
    Tool: Playwright
    Steps: Login as `user`; open `/home/parts`; type `Brake` into `[name="q"]`; click add on item with `data-item-code="BRAKE-PAD"` using quantity `2`; open `/home/services`; add `OIL-CHANGE` quantity `1`; open `/home/work-order`.
    Expected: Work-order page shows one part line quantity `2` and one service line quantity `1` after reload.
    Evidence: .sisyphus/evidence/task-14-user-selection.png

  Scenario: User sees only owned invoices
    Tool: Bash
    Steps: Login as `user` with curl cookie jar; request `/home/invoices`; then request `/home/invoices/detail?id=<invoice-belonging-to-user>` and one non-owned invoice id.
    Expected: Owned invoice returns `200`; non-owned invoice returns `403` or a safe not-found response; no cross-user data leaks.
    Evidence: .sisyphus/evidence/task-14-user-invoice-ownership.txt
  ```

  **Commit**: YES | Message: `feat(web): add user catalog, work order, and invoice views` | Files: `[src/main/java/com/quanli/quanligara/controller/home/*.java, src/main/webapp/WEB-INF/views/user/*.jsp]`

- [ ] 15. Harden route ownership and execute end-to-end smoke verification

  **What to do**: Finalize route/ownership checks across all new servlets, ensure admin/user separation stays server-side, and add reproducible smoke verification for the locked business flow. Every user-facing detail endpoint must verify `currentUser.id` ownership before loading a work order or invoice. Every admin mutation route must remain under `/admin/*`. Add or update service/DAO tests to cover insufficient stock rollback and snapshot pricing if not already complete. Then run the exact smoke flow below and archive evidence. Required flow data:
  - Admin creates part `BRAKE-PAD`, name `Brake Pad`, stock `10`, price `100000`
  - Admin creates service `OIL-CHANGE`, name `Oil Change`, price `250000`
  - User selects `BRAKE-PAD x2` and `OIL-CHANGE x1`
  - Admin issues invoice
  - Stock becomes `8`
  - Admin reprices `BRAKE-PAD` to `120000`
  - User invoice still shows `100000` for the line price
  Use the default base URL from README: `http://localhost:8080/quanligara-1.0-SNAPSHOT`.
  **Must NOT do**: Do not accept vague “page looks right” validation. Do not skip explicit HTTP status checks for auth and role guards.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: combines security hardening and cross-module verification.
  - Skills: `[]` - Direct curl/Playwright/JUnit execution is sufficient.
  - Omitted: `["review-work"]` - Reserved for post-implementation review orchestration, not this implementation slice itself.

  **Parallelization**: Can Parallel: NO | Wave 3 | Blocks: F1,F2,F3,F4 | Blocked By: 12,13,14

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `src/main/java/com/quanli/quanligara/controller/auth/LoginServlet.java` - Existing session attribute key `currentUser` and redirect behavior.
  - Pattern: `src/main/java/com/quanli/quanligara/controller/auth/AuthFilter.java` - Existing role guard style.
  - Seed: `src/main/java/com/quanli/quanligara/listener/AppStartupListener.java` - Existing seeded users `admin/admin` and `user/user`.
  - Base URL: `README.md` - Default Tomcat context path guidance.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `./mvnw test` passes.
  - [ ] `./mvnw compile` passes.
  - [ ] The exact curl/Playwright smoke scenarios below pass against the deployed app.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Auth guard
    Tool: Bash
    Steps: Run `curl -i http://localhost:8080/quanligara-1.0-SNAPSHOT/home/work-order`.
    Expected: `302` with `Location` header ending in `/login`.
    Evidence: .sisyphus/evidence/task-15-auth-guard.txt

  Scenario: Role guard
    Tool: Bash
    Steps: Login as `user` using a cookie jar; request `http://localhost:8080/quanligara-1.0-SNAPSHOT/admin/parts`.
    Expected: `403`.
    Evidence: .sisyphus/evidence/task-15-role-guard.txt

  Scenario: Admin catalog create
    Tool: Playwright
    Steps: Login as `admin`; open `/admin/parts`; create `BRAKE-PAD` stock `10` price `100000`; open `/admin/services`; create `OIL-CHANGE` price `250000`.
    Expected: Both rows appear in their admin lists.
    Evidence: .sisyphus/evidence/task-15-admin-catalog.png

  Scenario: User search + selection persistence
    Tool: Playwright
    Steps: Login as `user`; search `/home/parts?q=Brake`; add `BRAKE-PAD x2`; search `/home/services?q=Oil`; add `OIL-CHANGE x1`; open `/home/work-order` and reload.
    Expected: Persisted lines remain with quantities `2` and `1` after reload.
    Evidence: .sisyphus/evidence/task-15-user-selection.png

  Scenario: Invoice issuance happy path
    Tool: Playwright
    Steps: Login as `admin`; issue invoice for the user draft; open the resulting invoice detail.
    Expected: One invoice is created, work order is locked/invoiced, and `BRAKE-PAD` stock displays as `8` in admin parts list.
    Evidence: .sisyphus/evidence/task-15-invoice-happy-path.png

  Scenario: Snapshot pricing
    Tool: Playwright
    Steps: Login as `admin`; update `BRAKE-PAD` price to `120000`; login as `user`; open invoice detail.
    Expected: Invoice line still shows unit price `100000`, not `120000`.
    Evidence: .sisyphus/evidence/task-15-snapshot-pricing.png

  Scenario: Insufficient stock failure
    Tool: Playwright
    Steps: Create/select a draft with `BRAKE-PAD` quantity above current stock; login as `admin`; attempt invoice issuance.
    Expected: Failure message appears, no new invoice is created, and stock remains unchanged.
    Evidence: .sisyphus/evidence/task-15-insufficient-stock.png
  ```

  **Commit**: YES | Message: `test(qa): verify auth, stock, and invoice snapshots end to end` | Files: `[src/main/java/com/quanli/quanligara/controller/**/*.java, src/test/java/**/*.java, src/main/webapp/WEB-INF/views/**/*.jsp]`

## Final Verification Wave (MANDATORY — after ALL implementation tasks)
> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.
> **Do NOT auto-proceed after verification. Wait for user's explicit approval before marking work complete.**
> **Never mark F1-F4 as checked before getting user's okay.** Rejection or user feedback -> fix -> re-run -> present again -> wait for okay.
- [ ] F1. Plan Compliance Audit — oracle
- [ ] F2. Code Quality Review — unspecified-high
- [ ] F3. Real Manual QA — unspecified-high (+ playwright if UI)
- [ ] F4. Scope Fidelity Check — deep

## Commit Strategy
- Commit after each completed vertical slice, not after every file.
- Recommended commit sequence:
  1. `test(persistence): add domain test harness for catalog and invoice flows`
  2. `feat(domain): add catalog, work order, and invoice entities`
  3. `feat(service): add catalog, work order, and invoice business flows`
  4. `feat(web): add admin and user catalog/invoice screens`
  5. `test(qa): add smoke coverage for auth, stock, and invoice snapshots`

## Success Criteria
- Admin can manage spare parts and service offerings without affecting historical invoices.
- User can search and build exactly one persisted work-order draft.
- Admin can see that draft and issue one immutable invoice from it.
- Stock decreases exactly once and only on successful issuance.
- Price changes after issuance do not alter old invoices.
- All new logic is covered by TDD and executable smoke verification.
