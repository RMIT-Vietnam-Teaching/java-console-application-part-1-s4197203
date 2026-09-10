# ClaimShield - Health Insurance Management System

**COSC3110/3111 Java Programming - Assignment 2 Part 2**

## 1. Project Overview

A Java-based console application with role-based access control for health insurance administrators, claims officers, and customers. Features full CRUD operations, business rule validation, search/filter, dynamic co-pay calculations, activity logging, membership tiers, financial reports, and file-based data persistence.

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

| User ID | Username | Password | Name | Email |
|---------|----------|----------|------|-------|
| admin01 | admin01 | admin123 | Nguyen Minh Admin | minh.admin@claimshield.com |
| admin02 | admin02 | admin456 | Tran Thi Boss | thi.boss@claimshield.com |

### Claims Officer (view/process claims, view customers)

| User ID | Username | Password | Name | Email |
|---------|----------|----------|------|-------|
| off01 | officer01 | off123 | Le Van Officer | van.officer@claimshield.com |
| off02 | officer02 | off456 | Pham Thi Agent | thi.agent@claimshield.com |
| off03 | officer03 | off789 | Hoang Van处理 | van.hoang@claimshield.com |

### Customer (view own profile/cards/claims, submit claims)

| User ID | Username | Password | Name | Email | Linked Customer |
|---------|----------|----------|------|-------|-----------------|
| cust01 | nguyenva | pass123 | Nguyen Van An | van.an@gmail.com | c-1000001 |
| cust02 | tranthib | pass456 | Tran Thi Binh | thi.binh@gmail.com | c-1000002 |
| cust03 | levanc | pass789 | Le Van Cuong | van.cuong@gmail.com | c-1000003 |
| cust04 | phamthid | pass101 | Pham Thi Dung | thi.dung@gmail.com | c-1000004 |
| cust05 | hoangvane | pass202 | Hoang Van Em | van.em@gmail.com | c-1000005 |
| cust06 | vothif | pass303 | Vo Thi Phuong | thi.phuong@gmail.com | c-1000006 |
| cust07 | dangvang | pass404 | Dang Van Giang | van.giang@gmail.com | c-1000007 |
| cust08 | buithih | pass505 | Bui Thi Huong | thi.huong@gmail.com | c-1000008 |
| cust09 | dovani | pass606 | Do Van Ich | van.ich@gmail.com | c-1000009 |
| cust10 | ngothik | pass707 | Ngo Thi Kieu | thi.kieu@gmail.com | c-1000010 |

## 4. Data Persistence

All system data is stored in pipe-delimited text files under the `data/` directory:

- `data/users.txt` -- User accounts (Admin, ClaimsOfficer, Customer)
- `data/customers.txt` -- Customer profiles with card reference and total approved claim amount
- `data/cards.txt` -- Insurance card records
- `data/claims.txt` -- Claim records
- `data/logs.txt` -- Activity log (all user actions with timestamps)

Dates are serialized in ISO-8601 format (e.g., `2026-07-10T14:30:00`).

**Sample Data:** 26 users, 20 customers, 23 cards, 41 claims pre-loaded.

**Saving:** Use the **Save and Logout** option to persist all changes.

### File Formats

**users.txt** `userId|username|password|fullName|email|role|status|customerId[|dependents]`

```
admin01|admin01|admin123|Nguyen Minh Admin|minh.admin@claimshield.com|Admin|Active
off01|officer01|off123|Le Van Officer|van.officer@claimshield.com|ClaimsOfficer|Active
cust01|nguyenva|pass123|Nguyen Van An|van.an@gmail.com|Customer|Active|c-1000001
```

**customers.txt** `id|fullName|customerType|parentPolicyHolderId|cardReference|totalApprovedClaimAmount`

```
c-1000001|Nguyen Van An|PolicyHolder|null|1000000001|12500.00
c-2000001|Nguyen Thi Lan|Dependent|c-1000001|1000000002|0.00
```

**cards.txt** `cardNumber|cardHolderId|policyOwnerId|expirationDate`

```
1000000001|c-1000001|c-1000001|2027-12-31T23:59:59
```

**claims.txt** `id|claimDate|insuredPersonId|cardNumber|examDate|documents|amount|status|processedBy`

```
f-1234567890|2026-06-15T10:30:00|c-1000001|1000000001|2026-06-14T09:00:00|doc1.pdf;doc2.pdf|1500.00|Done|off01
```

## 5. Key Features and Business Rules

### Role-Based Access Control (RBAC)

| Role | Capabilities |
|------|-------------|
| **Admin** | Full access: manage users, customers, cards, claims, view reports, view logs |
| **Claims Officer** | View/process claims, view customers/cards, search, statistics |
| **Customer** | View own profile/cards/claims, view membership tier, submit new claims |

### Dashboard System
Each user role has a dedicated dashboard displayed after login via the abstract `displayDashboard()` method on the `User` class, demonstrating polymorphism.

### CRUD Operations
- Add, view, update, and delete customer profiles (PolicyHolders and Dependents)
- Register, update, and remove insurance cards linked to customers
- Create claims, attach documents, update statuses, and delete claims
- Cascading deletes: removing a customer removes all associated cards and claims

### Membership Tiers & Co-Pay System

Membership tiers are determined **dynamically** based on a customer's total approved claim spending:

| Tier | Threshold | Co-Pay Discount | Effective Co-Pay Rate |
|------|-----------|-----------------|----------------------|
| Standard | $0 - $1,999 | 0% | 30.0% |
| Silver | $2,000 - $4,999 | 5% | 28.5% |
| Gold | $5,000 - $9,999 | 10% | 27.0% |
| Platinum | $10,000+ | 15% | 25.5% |

**Base co-pay rate:** 30% (defined as constant in business logic layer)

**Co-pay calculation:**
```
Effective Co-Pay Rate = 30% x (1 - tierDiscount)
Customer Co-Pay Amount = claimAmount x Effective Co-Pay Rate
Insurance Payout Amount = claimAmount - Customer Co-Pay Amount
```

Tier is recalculated dynamically when viewing customer details or generating reports. No co-pay field is stored on the Claim entity.

### Reports (Admin only)
- **Financial Report:** Total claims, amounts by status, tier breakdown with co-pay and payout totals
- **Financial Report by Date Range:** Filtered approved claims with co-pay and payout totals within a specific timeframe
- **Officer Performance Report:** Claims processed per officer with co-pay collected and payout disbursed
- **Membership Tier Summary:** Policy holders per tier with coverage rates, total claims, co-pay, and payout breakdowns

### Search & Filter
- Search customers by name (partial match)
- Filter customers by type (PolicyHolder or Dependent)
- Filter claims by status (New, Processing, Done)
- View claims and cards for a specific customer
- Sort claims by date (newest first) or amount (highest first)
- Filter claims by date range
- View claims by PolicyHolder family group

### Activity Logging
Every add, update, and delete action must append an audit entry recording timestamp, userId, actionPerformed, and targetEntityId. Admins can view recent logs from the menu.

## 6. Validation Rules

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
| Custom Exceptions | `InvalidStatusTransitionException` for illegal status changes, `InvalidClaimDateException` for date violations |

## 7. OOP Design

- **Abstract class:** `User` (base for Admin, ClaimsOfficer, PolicyHolder, Dependent) with abstract `displayDashboard()` method
- **Enums:** `ClaimStatus`, `CustomerType`, `UserRole`, `UserStatus`, `MembershipTier`
- **Interfaces:** `ClaimManageable`, `UserManageable` (decoupling business logic from UI)
- **Polymorphism:** User hierarchy with role-specific behavior, dynamic dashboard display
- **Encapsulation:** Private fields with getters/setters
- **Custom Exceptions:** `AuthenticationException`, `InvalidStatusTransitionException`, `InvalidClaimDateException`
- **Dynamic Tier System:** Customer's membership tier is derived from total approved claim spending, not stored on cards

## 8. Sample Data Summary

| Entity | Count |
|--------|-------|
| Users | 26 (2 Admin, 3 ClaimsOfficer, 21 Customer) |
| Customers | 20 (12 PolicyHolders, 8 Dependents) |
| Insurance Cards | 23 |
| Claims | 41 (mixed New/Processing/Done statuses) |

## 9. System Architecture

```
src/
  Main.java                    # Entry point with welcome banner
  model/
    User.java                  # Abstract base class
    Admin.java                 # Admin user
    ClaimsOfficer.java         # Claims officer user
    PolicyHolder.java          # Customer - primary account owner
    Dependent.java             # Customer - family member
    Customer.java              # Insured individual with dynamic tier
    InsuranceCard.java         # Insurance card entity
    Claim.java                 # Insurance claim entity
    ClaimStatus.java           # NEW -> PROCESSING -> DONE
    CustomerType.java          # POLICY_HOLDER, DEPENDENT
    MembershipTier.java        # Dynamic tier with co-pay discounts
    UserRole.java              # ADMIN, CLAIMS_OFFICER, CUSTOMER
    UserStatus.java            # ACTIVE, INACTIVE
  manager/
    ClaimManager.java          # Business logic and validation
    FileManager.java           # File I/O persistence
  service/
    AuthenticationService.java  # Login/logout
    ActivityLogger.java        # Audit trail
    ReportService.java         # Analytics and reports
    UserManager.java           # User CRUD
  ui/
    ConsoleUI.java             # Interactive menu system
    InputHelper.java           # Validated input utilities
  interfaces/
    ClaimManageable.java       # Claim operations contract
    UserManageable.java        # User operations contract
  util/
    Validator.java             # Centralized validation rules
  exceptions/
    AuthenticationException.java
    InvalidStatusTransitionException.java
    InvalidClaimDateException.java
```
