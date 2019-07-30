# checking-account

A checking account from a bank allows for putting (deposits, salaries, credits)
or taking (purchases, withdrawals, debits) money at any given time.

You can also check what is your current balance or your account statement,
containing all operations that happened between two dates, along with the
account's daily balance.

## FIXES & CHANGES
> I found out that my concurrency was a mess, it's really hard to keep track of the index in a vector when dealing with concurrency  in the system. My solution was very simple, store transactions in a hash-map and use a counter as an index generator. This way the tx-id stored in the account doesn't need to match with a specific index in the vector.

> Most of the code was refactored, I tried to rely in functional style like map, reduce, apply and build a specific function following the same principles. This helped me to reuse a base function and send functions as parameter to extend its functionality.

> Got rid of db_fixture.clj and include precise parameters per function. Readability was improved.

> No need to reinvent the wheel with date-helpers. I changed the date library to one that is easier to use and has a decrease day function.

> I was not able to fix full test suite including the handler tests, sometimes it works and some times it doesn't. Please got to de test section for more information.


## Solution
I decided to store accounts in a nested map to be flexible with account numbers, each account contains a vector ref that keeps a record of the transactions done by the account.

Transactions are stored in hash-map of maps containing the corresponding information.

Both accounts and transactions are ref's to ensure it can run in a concurrent-safe way and use alter to apply STM dependency rules.

To keep the next tx-id or account id, I created 2 global atoms contain each of the id's.

Every time a new account is created the atom is increased by one using swap to secure it from other threads. Also the new-tx-id is added to the account tx-ids ref.

## Dependencies
Clojure 1.10.0

Leiningen 2.9.1

## Installation
```
git clone https://github.com/aldosolorzano/checking-account.git
```
## Usage

```
lein ring server
```
This will open a window at your localhost:3000

The application is preloaded with an account and some transactions to test end-points
right away.

## Examples
Use account number **100** or create a new one and create transactions to test endpoints
```
/accounts/100/end-point
```
### Common errors
##### status code - 422
#### GET `/accounts/invalid-account/balance`
This error is thrown when an invalid account is given
``` json
{
    "errors": "Account doesn't exists"
}
```
#### POST `/accounts/100/statement`
This error is thrown when no body is given in post request
``` json
{
    "errors": ":init, :end, required. Add to body"
}
```
#### POST `/accounts/100/transaction`
This error is thrown when an invalid date is given
``` json
{ "description": "Deposit", "amount": 100, "date": 1234, "type": "deposit"}
```
##### Response
``` json
{
     "errors": ":date, Invalid tx params"
}
```
### Get balance - GET `/accounts/:id/balance`
#### Response Body
##### status code - 200
The preloaded account is in negative balance
``` json
-38.57
```

### Get negative periods - GET `/accounts/:id/negative-periods`
#### Response Body
##### status code - 200
The preloaded account is in negative balance, no end date included
``` json
[
    {
        "principal": "25.23",
        "start": "14/10/2019",
        "end": "15/10/2019"
    },
    {
        "principal": "28.57",
        "start": "16/10/2019",
        "end": "21/10/2019"
    },
    {
        "principal": "38.57",
        "start": "22/10/2019"
    }
]
```

### Statement - POST `/accounts/:id/statement`
``` json
{ "init": "11/10/2019", "end": "14/10/2019" }
```

| Parameter | Description  |
| --------- |------------- |
| **init** | Inclusive initial date.  |
| **end**  | Inclusive end date. |

#### Response Body
##### status code - 200
``` json
[
    {
        "11/10/2019": [
            "Deposit 1000"
        ],
        "balance": "1000.00"
    },
    {
        "12/10/2019": [
            "Purchase of a flight -800"
        ],
        "balance": "200.00"
    },
    {
        "14/10/2019": [
            "Purchase on Uber -45.23",
            "Withdrawal -180"
        ],
        "balance": "-25.23"
    }
]
```

### Create transaction - POST `/accounts/:id/transaction`
``` json
{ "description": "Deposit", "amount": 100, "date": "25/10/2019", "type": "deposit"}
```

| Parameter | Description  |
| --------- |------------- |
| **description** | Brief description of transaction(string)|
| **amount**  | Transaction amount(int or float)|
| **date**  | Day, month and year of transaction|
| **type**  | Transactions are divided in three types deposit, purchase, withdrawal|

#### Response Body
##### status code - 200
``` json
{
    "id": 7,
    "account": 100,
    "date": "25/10/2019",
    "amount": 100,
    "description": "Deposit",
    "type": "deposit"
}
```
### Create transaction - POST `/accounts`

#### Response Body
##### status code - 200
``` json
{"id": 101}
```
### Test
```
lein test
```
### Bugs
There is a bug with mock/compojure in the **test/checking_account/handler_test.clj** file, if running `lein test` fails, try running just the file with the command below, it will work correctly. Otherwise, try running the command `lein test` a couple more times.

```
lein test test/checking_account/handler_test.clj
```

## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
