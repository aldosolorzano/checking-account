(ns checking-account.core
  (:require   [checking-account.balance :as b]
              [checking-account.negative-periods :as np]
              [checking-account.date-helpers :as d]
              [checking-account.statement :as st]
              [checking-account.db :as db]))

(defn get-balance
  [transactions account]
  (let [tx-ids (deref (account :tx-ids))
        balance (if (empty? tx-ids) 0.00 (b/compute-balance transactions tx-ids))]
    (b/format-float  balance)))

(defn negative-periods
  [transactions account]
  (np/add-end-to-negative-txs
   (b/negative-balances (db/account-txs transactions account))
   (b/compute-balance transactions (deref (account :tx-ids)))
   (last (db/account-txs transactions account))))

(defn get-statement
  [transactions account date-params]
   (b/add-balance-to-statement transactions (st/build-statement transactions account) date-params))
