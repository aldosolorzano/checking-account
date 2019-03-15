(ns checking-account.balance
  (:require [checking-account.date-helpers :as d]
            [checking-account.db :as db]))

(defn format-float
  [number]
  (format "%.2f"  (float number)))

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

(defn negative-balances
  [transactions]
  (loop [remaining transactions
         results []
         prev-balance 0]
    (if (empty? remaining)
      results
      (do
        (let [tx (first remaining)
          date (d/unparse-date (tx :date))
          next-remaining (rest remaining)
          current-balance (if (= (tx :type) :deposit)
                            (+ prev-balance (tx :amount))
                            (- prev-balance (tx :amount)))
          next-results (if (neg? current-balance)
                         (conj results {:principal (format-float (* current-balance -1)) :start date})
                         results)]
        (recur next-remaining next-results current-balance))))))

(defn add-balance-to-statement
  [transactions statement date-params]
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
                date (first tx)
                next-results (if (d/within-dates? (date-params :init) (date-params :end)  date)
                               (conj results {date {:transactions (deref ((last tx) :transactions)) :balance (format-float (balances i))}})
                               results)]
          (recur next-remaining next-results (inc i))))))))
