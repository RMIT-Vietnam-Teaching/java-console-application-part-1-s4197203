# ClaimShield - Health Insurance Management System

**COSC3110/3111 Java Programming - Assignment 2 Part 1**

## 1. Project Overview

A Java-based console application with role-based access control for health insurance administrators, claims officers, and customers. Features full CRUD operations, business rule validation, search/filter, co-pay calculations, activity logging, membership tiers, financial reports, and file-based data persistence.

**Author:** Nguyen Khanh Nguyen - s4197203

## 2. Setup and Execution

This project uses standard Java SE constructs with no external dependencies.

**Compile:**

```bash
javac -d bin -sourcepath src src/Main.java
```

**Run:**

```bash
java -cp bin Main
```

## 3. Login Credentials

The system requires login. Use any of the following accounts:

### Admin (full access)

| User ID | Password | Name |
|---------|----------|------|
| admin01 | admin123 | Nguyen Minh Admin |
| admin02 | admin456 | Tran Thi Boss |

### Claims Officer (view/process claims, view customers)

| User ID | Password | Name |
|---------|----------|------|
| off01 | off123 | Le Van Officer |
| off02 | off456 | Pham Thi Agent |
| off03 | off789 | Hoang Van处理 |

### Customer (view own profile/cards/claims, submit claims)

| User ID | Password | Name | Linked Customer |
|---------|----------|------|-----------------|
| cust01 | pass123 | Nguyen Van An | c-1000001 |
| cust02 | pass456 | Tran Thi Binh | c-1000002 |
| cust03 | pass789 | Le Van Cuong | c-1000003 |
| cust04 | pass101 | Pham Thi Dung | c-1000004 |
| cust05 | pass202 | Hoang Van Em | c-1000005 |
| cust06 | pass303 | Vo Thi Phuong | c-1000006 |
| cust07 | pass404 | Dang Van Giang | c-1000007 |
| cust08 | pass505 | Bui Thi Huong | c-1000008 |
| cust09 | pass606 | Do Van Ich | c-1000009 |
| cust10 | pass707 | Ngo Thi Kieu | c-1000010 |

## 4. Data Persistence

All system data is stored in pipe-delimited text files under the `data/` directory:

- `data/users.txt` — User accounts (Admin, ClaimsOfficer, Customer)
- `data/customers.txt` — Customer profiles
- `data/cards.txt` — Insurance card records with membership tier
- `data/claims.txt` — Claim records
- `data/logs.txt` — Activity log (all user actions with timestamps)

Dates are serialized in ISO-8601 format (e.g., `2026-07-10T14:30:00`).

**Sample Data:** 20 users, 17 customers, 16 cards, 15 claims pre-loaded.

**Saving:** Use the **Save and Logout** option to persist all changes.

### File Formats

**users.txt** `userId|password|fullName|role|status|customerId`

```
admin01|admin123|Nguyen Minh Admin|Admin|Active
off01|off123|Le Van Officer|ClaimsOfficer|Active
cust01|pass123|Nguyen Van An|Customer|Active|c-1000001
```

**customers.txt** `id|fullName|customerType|parentPolicyHolderId`

```
c-1000001|Nguyen Van An|PolicyHolder|null
c-2000001|Nguyen Thi Lan|Dependent|c-1000001
```

**cards.txt** `cardNumber|cardHolderId|policyOwnerId|expirationDate|membershipTier`

```
1000000001|c-1000001|c-1000001|2027-12-31T23:59:59|Gold
```

**claims.txt** `id|claimDate|insuredPersonId|cardNumber|examDate|documents|amount|status`

```
f-1234567890|2026-06-15T10:30:00|c-1000001|1000000001|2026-06-14T09:00:00|doc1.pdf;doc2.pdf|1500.0|Done
```

## 5. Key Features and Business Rules

### Role-Based Access Control (RBAC)

| Role | Capabilities |
|------|-------------|
| **Admin** | Full access: manage users, customers, cards, claims, view reports, view logs |
| **Claims Officer** | View/process claims, view customers/cards, search, statistics |
| **Customer** | View own profile/cards/claims, submit new claims |

### CRUD Operations
- Add, view, update, and delete customer profiles (PolicyHolders and Dependents)
- Register, update, and remove insurance cards linked to customers
- Create claims, attach documents, update statuses, and delete claims
- Cascading deletes: removing a customer removes all associated cards and claims

### Membership Tiers & Co-Pay System

| Tier | Coverage Rate | Customer Co-Pay |
|------|--------------|-----------------|
| Basic | 70% | 30% |
| Silver | 80% | 20% |
| Gold | 90% | 10% |
| Platinum | 95% | 5% |

Co-pay is calculated as: `Claim Amount x (1 - Coverage Rate)`

### Reports (Admin only)
- **Financial Report:** Total claims, amounts by status, tier breakdown
- **Officer Performance Report:** All claims officers and their status
- **Membership Tier Summary:** Policy holders per tier with coverage rates

### Activity Logging
All user actions (login, CRUD operations, report generation) are logged to `data/logs.txt` with timestamps. Admins can view recent logs from the menu.

### Search & Filter
- Search customers by name (partial match)
- Filter customers by type (PolicyHolder or Dependent)
- Filter claims by status (New, Processing, Done)
- View claims and cards for a specific customer
- Sort claims by date (newest first) or amount (highest first)

### Validation Rules

| Rule | Description |
|------|-------------|
| Customer ID | Must be `c-` followed by exactly 7 digits (e.g., `c-1234567`) |
| Claim ID | Must be `f-` followed by exactly 10 digits (e.g., `f-1234567890`) |
| Card Number | Must be exactly 10 digits |
| Duplicate IDs | Customer IDs, claim IDs, and card numbers must be unique |
| Claim Amount | Must be a positive number greater than zero |
| Date Logic | Exam date must be on or before the claim date; before card expiration |
| Document Format | Must end in `.pdf` and follow `ClaimId_CardNumber_DocumentName.pdf` |
| Status Workflow | Forward only: New -> Processing -> Done |
| Dependent Parent | Must reference an existing PolicyHolder |

## 6. OOP Design

- **Abstract class:** `User` (base for Admin, ClaimsOfficer, PolicyHolder, Dependent)
- **Enums:** `ClaimStatus`, `CustomerType`, `UserRole`, `UserStatus`, `MembershipTier`
- **Interfaces:** `ClaimManageable`, `UserManageable`
- **Polymorphism:** User hierarchy with role-specific behavior
- **Encapsulation:** Private fields with getters/setters
- **Custom Exceptions:** `AuthenticationException`, `InvalidStatusTransitionException`, `InvalidClaimDateException`
