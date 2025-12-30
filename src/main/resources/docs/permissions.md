# Permissions

| State      | Deposit (Money In) | Withdraw (Money Out) | Transfer (Send Money) | Receive Transfer |
| ---------- | ------------------ | -------------------- | --------------------- | ---------------- |
| UNVERIFIED | Allowed            | Blocked              | Blocked               | Allowed          |
| VERIFIED   | Allowed            | Allowed              | Allowed               | Allowed          |
| SUSPENDED  | Limited*           | Blocked              | Blocked               | Limited*         |
| CLOSED     | Blocked            | Blocked              | Blocked               | Blocked          |

> **Note**: Limited means the account can only make deposits and receive transfers.
