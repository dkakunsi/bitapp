# BitApp REST API Documentation

## Overview
BitApp is a personal finance management backend built with **Java 21**, **Javalin**, and **MongoDB**. It provides a RESTful API for managing users, accounts, loans, and transactions with JWT-based authentication.

**Technology Stack:**
- Language: Java 21
- Web Framework: Javalin
- Database: MongoDB
- Authentication: JWT (Bearer Token)
- Build Tool: Maven (multi-module)

**Base URL:** `http://localhost:8080` (default)

---

## Authentication
Most endpoints require JWT authentication using the `Authorization` header with a `Bearer` token.

```http
Authorization: Bearer <your-jwt-token>
```

**Status Codes:**
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - Valid token but insufficient permissions

---

## API Endpoints

### 1. User Management

#### 1.1 Register User
**POST** `/users`

Register a new user in the system. No authentication required.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "081234567890",
  "photoUrl": "https://example.com/photo.jpg"
}
```

**Validations:**
- `name` - **Required**, non-blank string
- `email` - **Required**, valid email format (regex: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$`)
- `phone` - Optional, string
- `photoUrl` - Optional, string

**Response (200 OK):**
```json
{
  "id": "user-123",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "081234567890",
  "photoUrl": "https://example.com/photo.jpg"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors (e.g., `name: invalid value, email: invalid value`)

---

#### 1.2 Get User Profile
**GET** `/users/{email}`

Retrieve user profile by email address.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `email` - User's email address

**Example:**
```http
GET /users/john.doe@example.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**Response (200 OK):**
```json
{
  "id": "user-123",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "081234567890",
  "photoUrl": "https://example.com/photo.jpg"
}
```

**Error Responses:**
- `401 Unauthorized` - Missing/invalid token
- `404 Not Found` - User not found

---

#### 1.3 Update User Profile
**PUT** `/users/{email}`

Update user profile information.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `email` - User's email address

**Request Body:**
```json
{
  "name": "John Updated",
  "phone": "089876543210",
  "photoUrl": "https://example.com/new-photo.jpg"
}
```

**Response (200 OK):**
```json
{
  "id": "user-123",
  "name": "John Updated",
  "email": "john.doe@example.com",
  "phone": "089876543210",
  "photoUrl": "https://example.com/new-photo.jpg"
}
```

---

### 2. Account Management

#### 2.1 Create Account
**POST** `/accounts`

Create a new financial account (wallet, bank account, cash, etc.).

**Authentication:** Required (Bearer Token)

**Request Body:**
```json
{
  "name": "Main Wallet",
  "type": "CASH",
  "themeColor": "#00AAFF"
}
```

**Validations:**
- `name` - **Required**, non-blank string
- `type` - **Required**, must be valid account type (e.g., `CASH`, `BANK`, `WALLET`)
- `themeColor` - Optional, string (hex color code)

**Response (200 OK):**
```json
{
  "id": "acc-456",
  "user": "user-123",
  "name": "Main Wallet",
  "type": "CASH",
  "themeColor": "#00AAFF"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors (e.g., `name: invalid value, type: invalid value`)
- `401 Unauthorized` - Missing/invalid token

---

#### 2.2 Get Account by ID
**GET** `/accounts/{id}`

Retrieve account details by account ID.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `id` - Account ID

**Response (200 OK):**
```json
{
  "id": "acc-456",
  "user": "user-123",
  "name": "Main Wallet",
  "type": "CASH",
  "themeColor": "#00AAFF"
}
```

**Error Responses:**
- `401 Unauthorized` - Missing/invalid token
- `404 Not Found` - Account not found

---

#### 2.3 Get User Accounts
**GET** `/users/{userId}/accounts`

List all accounts belonging to a specific user.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `userId` - User ID

**Response (200 OK):**
```json
[
  {
    "id": "acc-456",
    "user": "user-123",
    "name": "Main Wallet",
    "type": "CASH",
    "themeColor": "#00AAFF"
  },
  {
    "id": "acc-789",
    "user": "user-123",
    "name": "Savings Account",
    "type": "BANK",
    "themeColor": "#FF5733"
  }
]
```

---

#### 2.4 Update Account
**PUT** `/accounts/{id}`

Update existing account information.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `id` - Account ID

**Request Body:**
```json
{
  "name": "Updated Wallet",
  "type": "WALLET",
  "themeColor": "#FF0000"
}
```

**Response (200 OK):**
```json
{
  "id": "acc-456",
  "user": "user-123",
  "name": "Updated Wallet",
  "type": "WALLET",
  "themeColor": "#FF0000"
}
```

---

#### 2.5 Remove Account
**DELETE** `/accounts/{id}`

Delete an account.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `id` - Account ID

**Response (200 OK):**
```json
{
  "id": "acc-456",
  "user": "user-123",
  "name": "Updated Wallet",
  "type": "WALLET",
  "themeColor": "#FF0000"
}
```

---

### 3. Loan Management

#### 3.1 Create Loan
**POST** `/loans`

Create a new loan (BORROW or LEND) with optional automatic disbursement to an account.

**Authentication:** Required (Bearer Token)

**Request Body:**
```json
{
  "type": "BORROW",
  "partyName": "Bank ABC",
  "title": "Car Loan",
  "description": "Loan for purchasing a car",
  "amount": 50000000,
  "currency": "IDR",
  "interestRate": 4.5,
  "date": "2024-06-15",
  "time": "14:30:00",
  "account": "acc-456"
}
```

**Validations:**
- `type` - **Required**, must be `BORROW` or `LEND`
- `partyName` - **Required**, non-blank string (lender/borrower name)
- `title` - **Required**, non-blank string
- `description` - Optional, string
- `amount` - **Required**, positive BigDecimal
- `currency` - Optional, defaults to `IDR` if not provided
- `interestRate` - **Required**, must be >= 0
- `date` - Optional, ISO date format (YYYY-MM-DD), defaults to current date
- `time` - Optional, ISO time format (HH:mm:ss), defaults to current time
- `account` - Optional, account ID for automatic disbursement
  - If provided but empty string → `400 Bad Request`
  - If provided but account doesn't exist → `404 Not Found`
  - If account belongs to another user → `403 Forbidden` or `404 Not Found`

**Business Rules:**
- When `account` is provided and valid:
  - **BORROW** loan → Creates **CREDIT** transaction to the account (money received)
  - **LEND** loan → Creates **DEBIT** transaction from the account (money sent)
- Account validation happens **before** loan creation
- Failed validation **does not** create a loan

**Response (200 OK):**
```json
{
  "id": "loan-789",
  "user": "user-123",
  "type": "BORROW",
  "partyName": "Bank ABC",
  "title": "Car Loan",
  "description": "Loan for purchasing a car",
  "amount": 50000000,
  "currency": "IDR",
  "interestRate": 4.5,
  "date": "2024-06-15",
  "time": "14:30"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors (e.g., `type: invalid value`, `amount: invalid value`, `account: invalid value`)
- `401 Unauthorized` - Missing/invalid token
- `403 Forbidden` - Account belongs to another user
- `404 Not Found` - Account not found

---

#### 3.2 Get Loan by ID
**GET** `/loans/{id}`

Retrieve loan details by loan ID.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `id` - Loan ID

**Response (200 OK):**
```json
{
  "id": "loan-789",
  "user": "user-123",
  "type": "BORROW",
  "partyName": "Bank ABC",
  "title": "Car Loan",
  "description": "Loan for purchasing a car",
  "amount": 50000000,
  "currency": "IDR",
  "interestRate": 4.5,
  "date": "2024-06-15",
  "time": "14:30"
}
```

**Error Responses:**
- `401 Unauthorized` - Missing/invalid token
- `404 Not Found` - Loan not found

---

#### 3.3 Get User Loans
**GET** `/users/{userId}/loans`

List all loans belonging to a specific user.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `userId` - User ID

**Example:**
```http
GET /users/user-123/loans
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**Response (200 OK):**
```json
[
  {
    "id": "loan-789",
    "user": "user-123",
    "type": "BORROW",
    "partyName": "Bank ABC",
    "title": "Car Loan",
    "amount": 50000000,
    "currency": "IDR",
    "interestRate": 4.5,
    "date": "2024-06-15",
    "time": "14:30"
  },
  {
    "id": "loan-101",
    "user": "user-123",
    "type": "LEND",
    "partyName": "Jane Smith",
    "title": "Personal Loan",
    "amount": 10000000,
    "currency": "IDR",
    "interestRate": 0,
    "date": "2024-07-01",
    "time": "10:00"
  }
]
```

**Error Responses:**
- `401 Unauthorized` - Missing/invalid token

---

#### 3.4 Update Loan
**PUT** `/loans/{id}`

Update existing loan information.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `id` - Loan ID

**Request Body:**
```json
{
  "partyName": "Bank XYZ",
  "title": "Updated Car Loan",
  "description": "Updated description",
  "amount": 55000000,
  "currency": "IDR",
  "interestRate": 5.0,
  "date": "2024-06-20",
  "time": "15:00:00"
}
```

**Note:** Loan `type` cannot be updated after creation.

**Response (200 OK):**
```json
{
  "id": "loan-789",
  "user": "user-123",
  "type": "BORROW",
  "partyName": "Bank XYZ",
  "title": "Updated Car Loan",
  "description": "Updated description",
  "amount": 55000000,
  "currency": "IDR",
  "interestRate": 5.0,
  "date": "2024-06-20",
  "time": "15:00"
}
```

---

#### 3.5 Remove Loan
**DELETE** `/loans/{id}`

Delete a loan and its associated disbursement transactions.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `id` - Loan ID

**Response (200 OK):**
```json
{
  "id": "loan-789",
  "user": "user-123",
  "type": "BORROW",
  "partyName": "Bank ABC",
  "title": "Car Loan",
  "amount": 50000000
}
```

---

### 4. Transaction Management

#### 4.1 Create Transaction
**POST** `/transactions`

Create a new financial transaction (CREDIT, DEBIT, or TRANSFER).

**Authentication:** Required (Bearer Token)

**Request Body (CREDIT):**
```json
{
  "type": "CREDIT",
  "title": "Salary Payment",
  "description": "Monthly salary",
  "destination": "acc-456",
  "amount": 15000000,
  "currency": "IDR",
  "category": "INCOME",
  "date": "2024-08-01",
  "time": "09:00:00"
}
```

**Request Body (DEBIT):**
```json
{
  "type": "DEBIT",
  "title": "Grocery Shopping",
  "description": "Weekly groceries",
  "source": "acc-456",
  "amount": 500000,
  "currency": "IDR",
  "category": "EXPENSE",
  "date": "2024-08-02",
  "time": "18:30:00"
}
```

**Request Body (TRANSFER):**
```json
{
  "type": "TRANSFER",
  "title": "Transfer to Savings",
  "description": "Monthly savings transfer",
  "source": "acc-456",
  "destination": "acc-789",
  "amount": 2000000,
  "currency": "IDR",
  "category": "TRANSFER",
  "date": "2024-08-05",
  "time": "12:00:00"
}
```

**Request Body (Loan Repayment):**
```json
{
  "type": "DEBIT",
  "title": "Loan Repayment",
  "description": "Monthly car loan installment",
  "source": "acc-456",
  "loan": "loan-789",
  "amount": 1500000,
  "currency": "IDR",
  "category": "LOAN_REPAYMENT"
}
```

**Validations:**
- `type` - **Required**, must be `CREDIT`, `DEBIT`, or `TRANSFER`
- `title` - **Required**, non-blank string
- `description` - Optional, string
- `amount` - **Required**, must be > 0
- `currency` - Optional, defaults to `IDR`
- `category` - **Required**, valid transaction category
- `source` - **Required for DEBIT and TRANSFER**, account ID
- `destination` - **Required for CREDIT and TRANSFER**, account ID
- `loan` - Optional, loan ID (links transaction to a loan)
- `date` - Optional, ISO date format (YYYY-MM-DD), defaults to current date
- `time` - Optional, ISO time format (HH:mm:ss), defaults to current time

**Business Rules:**
- If `loan` is provided, it must exist (otherwise `404 Not Found`)
- Accounts must exist and belong to the authenticated user

**Response (200 OK):**
```json
{
  "id": "txn-112",
  "user": "user-123",
  "type": "CREDIT",
  "title": "Salary Payment",
  "description": "Monthly salary",
  "destination": "acc-456",
  "amount": 15000000,
  "currency": "IDR",
  "category": "INCOME",
  "date": "2024-08-01",
  "time": "09:00"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors (e.g., `title: invalid value`, `amount: invalid value`, `source: invalid value`)
- `401 Unauthorized` - Missing/invalid token
- `404 Not Found` - Loan/account not found

---

#### 4.2 Get Transaction by ID
**GET** `/transactions/{id}`

Retrieve transaction details by transaction ID.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `id` - Transaction ID

**Response (200 OK):**
```json
{
  "id": "txn-112",
  "user": "user-123",
  "type": "CREDIT",
  "title": "Salary Payment",
  "description": "Monthly salary",
  "destination": "acc-456",
  "amount": 15000000,
  "currency": "IDR",
  "category": "INCOME",
  "date": "2024-08-01",
  "time": "09:00"
}
```

---

#### 4.3 Get User Transactions
**GET** `/users/{userId}/transactions`

List all transactions belonging to a specific user.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `userId` - User ID

**Response (200 OK):**
```json
[
  {
    "id": "txn-112",
    "user": "user-123",
    "type": "CREDIT",
    "title": "Salary Payment",
    "amount": 15000000,
    "currency": "IDR",
    "date": "2024-08-01",
    "time": "09:00"
  },
  {
    "id": "txn-113",
    "user": "user-123",
    "type": "DEBIT",
    "title": "Grocery Shopping",
    "amount": 500000,
    "currency": "IDR",
    "date": "2024-08-02",
    "time": "18:30"
  }
]
```

---

#### 4.4 Remove Transaction
**DELETE** `/transactions/{id}`

Delete a transaction.

**Authentication:** Required (Bearer Token)

**Path Parameters:**
- `id` - Transaction ID

**Response (200 OK):**
```json
{
  "id": "txn-112",
  "user": "user-123",
  "type": "CREDIT",
  "title": "Salary Payment",
  "amount": 15000000
}
```

---

## Error Handling

All endpoints follow consistent error response patterns:

| Status Code | Description |
|-------------|-------------|
| `200 OK` | Request successful |
| `400 Bad Request` | Validation errors or malformed request |
| `401 Unauthorized` | Missing or invalid JWT token |
| `403 Forbidden` | Valid token but insufficient permissions |
| `404 Not Found` | Resource not found |
| `500 Internal Server Error` | Unexpected server error |

**Error Response Format:**
```json
"error message describing the issue"
```

**Validation Error Format:**
```json
"field1: invalid value, field2: invalid value"
```

---

## Common Validation Rules

### Required Field Validations
- Empty strings are considered invalid
- `null` values for required fields return `400 Bad Request`
- Blank strings (whitespace only) are invalid

### Email Validation
- Pattern: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$`
- Must contain `@` and domain

### Amount Validations
- Must be positive (> 0)
- For loans: `interestRate` can be 0 or greater

### Account Reference Validations
- Empty string (`""`) → `400 Bad Request`
- Non-existent account → `404 Not Found`
- Account owned by different user → `403 Forbidden` or `404 Not Found`

### Date/Time Validations
- Date format: `YYYY-MM-DD` (ISO 8601)
- Time format: `HH:mm:ss` or `HH:mm`
- Invalid formats cause parsing errors

---

## Data Models

### User
```json
{
  "id": "string",
  "name": "string",
  "email": "string (email format)",
  "phone": "string (optional)",
  "photoUrl": "string (optional)"
}
```

### Account
```json
{
  "id": "string",
  "user": "string (user ID)",
  "name": "string",
  "type": "string (CASH|BANK|WALLET|...)",
  "themeColor": "string (optional, hex color)"
}
```

### Loan
```json
{
  "id": "string",
  "user": "string (user ID)",
  "type": "string (BORROW|LEND)",
  "partyName": "string",
  "title": "string",
  "description": "string (optional)",
  "amount": "number (BigDecimal)",
  "currency": "string (default: IDR)",
  "interestRate": "number (>= 0)",
  "date": "string (YYYY-MM-DD)",
  "time": "string (HH:mm)"
}
```

### Transaction
```json
{
  "id": "string",
  "user": "string (user ID)",
  "type": "string (CREDIT|DEBIT|TRANSFER)",
  "title": "string",
  "description": "string (optional)",
  "source": "string (account ID, optional)",
  "destination": "string (account ID, optional)",
  "loan": "string (loan ID, optional)",
  "amount": "number (> 0)",
  "currency": "string (default: IDR)",
  "category": "string",
  "date": "string (YYYY-MM-DD)",
  "time": "string (HH:mm)"
}
```

---

Source: module structure, domain use cases, and integration tests in backend/app and backend/domain.