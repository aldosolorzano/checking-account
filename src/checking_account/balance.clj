(ns checking-account.balance
  (:require [checking-account.date_helpers :as d]))

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
  [transactions account]
  (let [tx-ids (deref (get account :tx-ids))
        account-txs (d/sort-by-date (map (fn [id] (get transactions id)) tx-ids))]
        (loop [remaining account-txs
               results []]
          (if (empty? remaining)
            results
            (do (let [tx (first remaining)
                  date (d/unparse-date (tx :date))
                  next-remaining (rest remaining)
                  current-balance (compute-balance transactions (take (inc  (- (count tx-ids) (count remaining))) tx-ids)) ; bug
                  current { :principal (format-float (* current-balance -1)) :start date}
                  next-results (if (< current-balance 0)
                                 (conj results current)
                                 results)]
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
