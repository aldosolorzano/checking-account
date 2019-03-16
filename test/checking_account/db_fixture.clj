(ns checking-account.db-fixture
  (:require [checking-account.date-helpers :as d]
            [checking-account.db :as db]))

(def accounts (atom {100 {:id 100 :tx-ids (atom [0 1 2 3 4 5 6])}}))

(def transactions (atom [{:id 0 :account 100 :description "Deposit"
                          :amount 1000 :date (d/parse-date "11/10") :type :deposit}
                          {:id 1 :account 100 :description "Purchase on Amazon"
                            :amount 3.34 :date (d/parse-date "16/10") :type :purchase}
                          {:id 2 :account 100 :description "Purchase on Uber"
                            :amount 45.23 :date (d/parse-date "14/10") :type :purchase}
                          {:id 3 :account 100 :description "Withdrawal"
                            :amount 180 :date (d/parse-date "17/10") :type :withdrawal}
                          {:id 4 :account 100 :description "Purchase of a flight"
                            :amount 800 :date (d/parse-date "12/10") :type :purchase}
                          {:id 5 :account 100 :description "Purchase of a expresso"
                            :amount 10 :date (d/parse-date "22/10") :type :purchase}
                          {:id 6 :account 100 :description "Deposit"
                            :amount 50 :date (d/parse-date "25/10") :type :deposit}]))
(def sort-transactions [{:id 0 :account 100 :description "Deposit"
                          :amount 1000 :date (d/parse-date "11/10") :type :deposit}
                          {:id 4 :account 100 :description "Purchase of a flight"
                          :amount 800 :date (d/parse-date "12/10") :type :purchase}
                          {:id 2 :account 100 :description "Purchase on Uber"
                          :amount 45.23 :date (d/parse-date "14/10") :type :purchase}
                          {:id 1 :account 100 :description "Purchase on Amazon"
                            :amount 3.34 :date (d/parse-date "16/10") :type :purchase}
                          {:id 3 :account 100 :description "Withdrawal"
                            :amount 180 :date (d/parse-date "17/10") :type :withdrawal}
                          {:id 5 :account 100 :description "Purchase of a expresso"
                            :amount 10 :date (d/parse-date "22/10") :type :purchase}
                          {:id 6 :account 100 :description "Deposit"
                            :amount 50 :date (d/parse-date "25/10") :type :deposit}])
(def account-txs sort-transactions)

(def full-statement {"11/10" {:transactions ["Deposit 1000"] :balance "1000.00"}
  "12/10" {:transactions ["Purchase of a flight 800"] :balance "200.00"}
  "14/10" {:transactions ["Purchase on Uber 45.23"] :balance "154.77"}
  "16/10" {:transactions ["Purchase on Amazon 3.34"] :balance "151.43"}
  "17/10" {:transactions ["Withdrawal 180"] :balance "-28.57"}
  "22/10" {:transactions ["Purchase of a expresso 10"] :balance "-38.57"}
  "25/10" {:transactions ["Deposit 50"] :balance "11.43"}})
(def between-dates-statement {"16/10" {:transactions ["Purchase on Amazon 3.34"] :balance "151.43"}
  "17/10" {:transactions ["Withdrawal 180"] :balance "-28.57"}
  "22/10" {:transactions ["Purchase of a expresso 10"] :balance "-38.57"}})

(def account-balance 11.43)
(def account (get @accounts 100))
(def account-id 100)
(def negative-period [{:principal "28.57" :start "17/10" :end "21/10"} {:principal "38.57" :start "22/10" :end "24/10"} ])
(def negative-period-debt [{:principal "28.57" :start "17/10" :end "21/10"} {:principal "38.57" :start "22/10"}])
(def no-end-negative [{:principal "28.57" :start "17/10"} {:principal "38.57" :start "22/10"}])
(def fresh-account {:id 102 :tx-ids (atom [])})
(def pos-account {:id 102 :tx-ids (atom [0 1 2 3])})
(def neg-account {:id 102 :tx-ids (atom [0 1 2 3 4 5])})
(def transaction-params {:description "Deposit"
                         :amount 180
                         :date "17/10"
                         :type "deposit"})
(def expected-tx-body {:id 6,
                       :account 100,
                       :date 971740800000,
                       :amount 180,
                       :description "Deposit",
                       :type :deposit})