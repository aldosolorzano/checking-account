# checking-account

A checking account from a bank allows for putting (deposits, salaries, credits)
or taking (purchases, withdrawals, debits) money at any given time.

You can also check what is your current balance or your account statement,
containing all operations that happened between two dates, along with the
account's daily balance.
## Solution
I decided to store accounts in a nested map to be flexible with account numbers, each account contains a vector atom that keeps record of the transactions done by the account.

Transactions are a vector of maps contain the corresponding information.

Both accounts and transactions are atoms to ensure it can run in a concurrent-safe way. To keep the next tx-id or account id, I created 2 global atoms contain each of the id's.

Every time a new account is created the atom is increased by one using swap to secure it from other threads. Also the new-tx-id is added to the account tx-ids atom, this transactions are done with a dosync too preserve same information and if one of them fails nothing is added to the data structure.
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
        "principal": "28.57",
        "start": "17/10",
        "end": "21/10"
    },
    {
        "principal": "38.57",
        "start": "22/10"
    }
]
```

### Statement - POST `/accounts/:id/statement`
``` json
{ "init": "11/10", "end": "14/10" }
```

| Parameter | Description  |
| --------- |------------- |
| **init** | Inclusive initial date, only include day and month(example above).  |
| **end**  | Exclusive end date. Example above will return dates until 13/10|

#### Response Body
##### status code - 200
``` json
{
    "11/10": {
        "transactions": [
            "Deposit 1000"
        ],
        "balance": "1000.00"
    },
    "12/10": {
        "transactions": [
            "Purchase of a flight 800"
        ],
        "balance": "200.00"
    }
}
```

### Create transaction - POST `/accounts/:id/transaction`
``` json
{ "description": "Deposit", "amount": 100, "date": "25/10", "type": "deposit"}
```

| Parameter | Description  |
| --------- |------------- |
| **description** | Brief description of transaction(string)|
| **amount**  | Transaction amount(int or float)|
| **date**  | Day and month of transaction|
| **type**  | Transactions are divided in three types deposit, purchase, withdrawal|

#### Response Body
##### status code - 200
``` json
{
    "id": 6,
    "account": 100,
    "date": "25/10",
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
There is a bug with mock/compojure in the **test/checking_account/handler_test.clj** file, to run all tests inside the file please
uncomment commented lines and run
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
