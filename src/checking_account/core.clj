(ns checking-account.core
  (:require [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.format :as f]
            [clj-time.coerce :as c]))
; Date functions
(def custom-formatter (f/formatter "dd/MM"))

(defn parse-date
  [date]
  (c/to-long (f/parse custom-formatter date)))

(defn unparse-date
  [date]
  (f/unparse custom-formatter (c/from-long date)))

(defn parse-to-date-time
  [date]
  (t/date-time (t/year (c/from-long date)) (t/month (c/from-long date)) (t/day (c/from-long date))))

(defn format-float
  [number]
    (format "%.2f"  (float number)))

(defn within-dates?
  [init end date]
  (t/within? (t/interval (parse-to-date-time (parse-date init)) (parse-to-date-time (parse-date end)))
              (parse-to-date-time (parse-date date))))

(defn txs-within-dates
  [transactions init end]
  (flatten (filter (fn [tx] (within-dates? init end (first tx))) transactions)))


(def accounts-db (atom {100 {:id 100 :tx-ids (atom [0 1 2 3 4 5 6])}
                     101 {:id 101 :tx-ids (atom [])}}))

(def transactions-db (atom [{:id 0 :account 100 :description "Deposit"
                          :amount 1000 :date (parse-date "15/10") :type :deposit}
                          {:id 1 :account 100 :description "Purchase on Amazon"
                            :amount 3.34 :date (parse-date "16/10") :type :purchase}
                         {:id 2 :account 100 :description "Purchase on Uber"
                            :amount 45.23 :date (parse-date "16/10") :type :purchase}
                         {:id 3 :account 100 :description "Withdrawal"
                            :amount 180 :date (parse-date "17/10") :type :withdrawal}
                         {:id 4 :account 100 :description "Purchase of a flight"
                            :amount 800 :date (parse-date "18/10") :type :purchase}
                         {:id 5 :account 100 :description "Purchase of a expresso"
                            :amount 10 :date (parse-date "22/10") :type :purchase}
                         {:id 6 :account 100 :description "Deposit"
                            :amount 100 :date (parse-date "10/10") :type :deposit}]))
(defn sort-by-date
  [transactions]
  (sort-by :date < transactions))

(defn create-account
  [accounts]
  (let [new-id (if (empty? accounts) 100 (inc (first (last accounts))))
        account {new-id {:id new-id :tx-ids (atom [])}}]
    (swap! accounts-db conj account)
    new-id))

(defn create-transaction
  [transactions account-id params]
  (let [new-id (if (empty? transactions) 0 (inc (get (last transactions) :id)))
        tx (conj {:id new-id :account account-id :date (parse-date (params :date))} (update params :type keyword))]
    (dosync ;Both txs need to be done to be accesible to other threads
      (swap! transactions-db conj tx)
      (swap! (get-in @accounts-db [account-id :tx-ids]) conj new-id))
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
        account-txs (sort-by-date (map (fn [id] (get transactions id)) tx-ids))]
        (loop [remaining account-txs
               results []]
          (if (empty? remaining)
            results
            (do (let [tx (first remaining)
                  date (unparse-date (tx :date))
                  next-remaining (rest remaining)
                  current-balance (compute-balance transactions (take (inc  (- (count tx-ids) (count remaining))) tx-ids))
                  current { :principal (format-float (* current-balance -1)) :start date}
                  next-results (if (< current-balance 0)
                                 (conj results current)
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
                        (assoc tx :end (unparse-date (last-tx :date)))
                        tx))
                    (assoc tx :end (next :start)))
          next (get txs (inc i))
          next-results (conj results current)]
      (recur next-remaining next-results (inc i) next))))))

(defn get-balance
  [tx-ids]
  (let [balance (if (empty? tx-ids) 0.00 (compute-balance @transactions-db tx-ids))]
    (format-float  balance)))

(defn negative-periods
  [transactions account]
  (add-end-to-negative-txs
   (negative-balance transactions account)
   (compute-balance transactions (deref (account :tx-ids)))
   (last (map (fn [id] (get transactions id)) (deref (account :tx-ids)))))
  )

(defn build-statement
  [transactions account]
  (let [tx-ids (deref (get account :tx-ids))
        account-txs (sort-by-date (map (fn [id] (get transactions id)) tx-ids))]
        (loop [remaining account-txs
               results {}]
          (if (empty? remaining)
            results
            (do (let [tx (first remaining)
                  date (unparse-date (get tx :date))
                  next-remaining (rest remaining)
                  next-results (if (= (get results date) nil)
                            (conj results {date { :tx-ids (atom [(tx :id)]):transactions (atom [(string/join " " [(tx :description) (tx :amount)])])}})
                            (do
                              (dosync ;Both txs need to be done to be accesible to other threads
                                (swap! (get-in results [date :transactions]) conj (string/join " " [(tx :description) (tx :amount)]))
                                (swap! (get-in results [date :tx-ids]) conj (tx :id)))
                              results))]
              (recur next-remaining next-results)))))))

(defn add-balance-to-statement
  [transactions statement]
  (let [balances (reduce (fn [new tx]
                          (let [prev-balance (if (nil? (last new)) 0 (last new))]
                          (conj new (+ prev-balance(compute-balance transactions (deref ((last tx) :tx-ids))))))) [] statement)]
      (loop [remaining statement
        results {}
          i 0]
          (if (empty? remaining)
          results
          (do (let [tx (first remaining)
                next-remaining (rest remaining)
                next-results (conj results {(first tx) {:transactions (deref ((last tx) :transactions)) :balance (format-float (get balances i))}})]
          (recur next-remaining next-results (inc i))))))))

(defn get-statement
  [transactions account date-params]
  (txs-within-dates
   (add-balance-to-statement transactions (build-statement transactions account))
   (date-params :init) (date-params :end)))
