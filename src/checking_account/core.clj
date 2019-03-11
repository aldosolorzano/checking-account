(ns checking-account.core
  (:require [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.format :as f]))

(def accounts-db (atom {100 {:id 100 :tx-ids (atom [0 1 2 3 4 5])}
                     101 {:id 101 :tx-ids (atom [])}}))

(def transactions-db (atom [{:id 0 :account 100 :description "Deposit"
                          :amount 1000 :date "15/10" :type :deposit}
                          {:id 1 :account 100 :description "Purchase on Amazon"
                            :amount 3.34 :date "16/10" :type :purchase}
                         {:id 2 :account 100 :description "Purchase on Uber"
                            :amount 45.23 :date "16/10" :type :purchase}
                         {:id 3 :account 100 :description "Withdrawal"
                            :amount 180 :date "17/10" :type :withdrawal}
                         {:id 4 :account 100 :description "Purchase of a flight"
                            :amount 800 :date "18/10" :type :purchase}
                         {:id 5 :account 100 :description "Purchase of a expresso"
                            :amount 10 :date "22/10" :type :purchase}]))

(def statement-like { "10/5" {:date "10/5" :txs (atom ["Purchase on amazon 33.4", "Deposit"]) :balance 1234}
                     101 {:id 101 :tx-ids (atom [])}})
(defn create-account
  [accounts]
  (let [new-id (if (empty? accounts) 100 (inc (first (last accounts))))
        account {new-id {:id new-id :tx-ids (atom [])}}]
    (swap! accounts-db conj account)
    new-id))

(defn create-transaction
  [transactions account-id params]
  (let [new-id (if (empty? transactions) 0 (inc (get (last transactions) :id)))
        tx (conj {:id new-id :account account-id :date (t/now)} (update params :type keyword))]
    (swap! transactions-db conj tx)
    (swap! (get-in @accounts-db [account-id :tx-ids]) conj new-id)
    tx))

(defn get-account-by-id
  [accounts id]
  (let [account (get accounts (read-string (str id)))]
    account))

(defn compute-balance
  [transactions tx-ids]
  (let [balance (- (->> (map (fn [id] (get transactions id)) tx-ids)
                        (filter (fn [tx] (= (get tx :type) :deposit)))
                        (map (fn [tx] (get tx :amount)))
                        (reduce +))
                   (->> (map (fn [id] (get transactions id)) tx-ids)
                        (filter (fn [tx] (not= (get tx :type) :deposit)))
                        (map (fn [tx] (get tx :amount)))
                        (reduce +)))]
    balance))

(defn negative-balance
  [transactions account]
  (let [tx-ids (deref (get account :tx-ids))
        account-txs (map (fn [id] (get transactions id)) tx-ids)]
        (loop [remaining account-txs
               results []]
          (if (empty? remaining)
            results
            (do (let [tx (first remaining)
                  date (get tx :date)
                  next-remaining (rest remaining)
                  current-balance (compute-balance transactions (take (inc  (- (count tx-ids) (count remaining))) tx-ids))
                  current { :principal (* current-balance -1) :start (tx :date)}
                  next-results (if (< current-balance 0)
                                 (conj results  current)
                                 results)]
              (recur next-remaining next-results)))))))

(defn add-end-to-negative-txs
  [txs balance last-tx]
  (loop [remaining txs
         results []
         i 1
         next (get txs 1)]
  (if (empty? remaining)
    results
    (do (let [tx (first remaining)
          next-remaining (rest remaining)
          current (if (nil? next)
                    (do
                      (if (and (= i (count txs)) (pos? balance))
                        (assoc tx :end (last-tx :date))
                        tx))
                    (assoc tx :end (next :start)))
          next (get txs (inc i))
          next-results (conj results current)]
      (recur next-remaining next-results (inc i) next))))))
(defn get-balance
  [tx-ids]
  (let [balance (if (empty? tx-ids) 0.00 (compute-balance @transactions-db tx-ids))]
    (format "%.2f"  (float balance))))

(defn negative-periods
  [transactions account]
  (add-end-to-negative-txs
   (negative-balance transactions account)
   (compute-balance transactions (deref (account :tx-ids)))
   (last (map (fn [id] (get transactions id)) (deref (account :tx-ids)))))
  )
