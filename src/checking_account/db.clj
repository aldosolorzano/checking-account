(ns checking-account.db
  (:require [checking-account.date_helpers :as d]))

(def accounts (atom {100 {:id 100 :tx-ids (atom [0 1 2 3 4 5])}
                     101 {:id 101 :tx-ids (atom [])}}))


(def transactions (atom [{:id 0 :account 100 :description "Deposit"
                          :amount 1000 :date (d/parse-date "15/10") :type :deposit}
                          {:id 1 :account 100 :description "Purchase on Amazon"
                            :amount 3.34 :date (d/parse-date "16/10") :type :purchase}
                         {:id 2 :account 100 :description "Purchase on Uber"
                            :amount 45.23 :date (d/parse-date "16/10") :type :purchase}
                         {:id 3 :account 100 :description "Withdrawal"
                            :amount 180 :date (d/parse-date "17/10") :type :withdrawal}
                         {:id 4 :account 100 :description "Purchase of a flight"
                            :amount 800 :date (d/parse-date "18/10") :type :purchase}
                         {:id 5 :account 100 :description "Purchase of a expresso"
                            :amount 10 :date (d/parse-date "22/10") :type :purchase}]))
(def tx-id (atom (dec (count @transactions))))

(defn create-account
  []
  (let [new-id (if (empty? @accounts) 100 (inc (first (last @accounts))))
        account {new-id {:id new-id :tx-ids (atom [])}}]
    (swap! accounts conj account)
    new-id))

(defn build-tx
  [account params]
  (let [tx {:id (swap! tx-id inc)
            :account (account :id)
            :date (d/parse-date (params :date))
            :amount (params :amount)
            :description (params :description)
            :type (keyword (params :type))}]
    tx))

(defn create-transaction
  [txss account params]
    (dosync ;Both txs need to be done to be accesible to other threads
      (let [txs (swap! transactions conj (build-tx account params))
            tx (last txs)] ;Keep tx locally and avoid id mismatch
      (swap! (get-in @accounts [(account :id) :tx-ids]) conj (tx :id))
      (update tx :date d/unparse-date))))
