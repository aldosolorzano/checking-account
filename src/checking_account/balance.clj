(ns checking-account.balance
  (:require [checking-account.date-helpers :as d]
            [checking-account.db :as db]))

(defn format-float
  [number]
  (format "%.2f"  (float number)))

(defn compute-balance
  [transactions]
  (loop [remaining transactions
         balance 0]
    (if (empty? remaining)
      balance
      (do
        (let [tx (first remaining)
          next-remaining (rest remaining)
          current-balance (if (= (tx :type) :deposit)
                            (+ balance (tx :amount))
                            (- balance (tx :amount)))]
        (recur next-remaining current-balance))))))

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
