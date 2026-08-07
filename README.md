# ClaimShield - Health Insurance Management System

**COSC3110/3111 Java Programming - Assignment 2 Part 1**

## 1. Project Overview

A Java-based console application for health insurance administrators to manage customer profiles, insurance cards, and medical claims. Provides full CRUD operations, business rule validation, search and filter capabilities, system statistics, and file-based data persistence.

**Author:** Nguyen Khanh Nguyen - s4197203

## 2. Setup and Execution

This project uses standard Java SE constructs with no external dependencies.

**Compile:**

```bash
javac -d bin src/model/*.java src/manager/*.java src/ui/*.java src/Main.java
```

**Run:**

```bash
java -cp bin Main
```

## 3. Data Persistence

All system data is stored in pipe-delimited text files under the `data/` directory:

- `data/customers.txt` — Customer records
- `data/cards.txt` — Insurance card records
- `data/claims.txt` — Claim records

Dates are serialized in ISO-8601 format (e.g., `2026-07-10T14:30:00`) and parsed back into `LocalDateTime` objects on startup.

**Sample Data:** The system ships with 17 customers, 16 insurance cards, and 16 claims pre-loaded to demonstrate scalability (exceeding the 15-record minimum).

**Saving:** Use the **Save and Exit** option in the main menu to persist all changes back to the text files before closing.

### File Formats

**customers.txt** `id|fullName|customerType|parentPolicyHolderId`

```
c-1000001|Nguyen Van An|PolicyHolder|null
c-2000001|Nguyen Thi Lan|Dependent|c-1000001
```

**cards.txt** `cardNumber|cardHolderId|policyOwnerId|expirationDate`

```
1000000001|c-1000001|c-1000001|2027-12-31T23:59:59
```

**claims.txt** `id|claimDate|insuredPersonId|cardNumber|examDate|documents|amount|status`

```
f-1234567890|2026-06-15T10:30:00|c-1000001|1000000001|2026-06-14T09:00:00|doc1.pdf;doc2.pdf|1500.0|Done
```

## 4. Key Features and Business Rules

### CRUD Operations
- Add, view, update, and delete customer profiles (PolicyHolders and Dependents)
- Register, update, and remove insurance cards linked to customers
- Create claims, attach documents, update statuses, and delete claims
- Cascading deletes: removing a customer removes all associated cards and claims

### Search & Filter
- Search customers by name (partial match)
- Filter customers by type (PolicyHolder or Dependent)
- Filter claims by status (New, Processing, Done)
- View claims and cards for a specific customer
- Sort claims by date (newest first) or amount (highest first)

### System Statistics
- Aggregated dashboard showing customer breakdown, card count, claim counts by status, total claim value, average claim, and largest claim

### Validation Rules
| Rule | Description |
|------|-------------|
| Customer ID | Must be `c-` followed by exactly 7 digits (e.g., `c-1234567`) |
| Claim ID | Must be `f-` followed by exactly 10 digits (e.g., `f-1234567890`) |
| Card Number | Must be exactly 10 digits |
| Duplicate IDs | Customer IDs, claim IDs, and card numbers must be unique |
| Claim Amount | Must be a positive number greater than zero |
| Date Logic | Exam date must be on or before the claim date; Exam date must be before the card expiration date |
| Document Format | Must end in `.pdf` and follow the pattern `ClaimId_CardNumber_DocumentName.pdf` |
| Status Workflow | Claims progress forward only: New → Processing → Done. Backward or same-status transitions are rejected |
| Dependent Parent | Dependents must reference an existing PolicyHolder as their parent |
