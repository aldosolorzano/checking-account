(ns checking-account.negative-periods
  (:require [checking-account.balance :as b]
            [checking-account.date-helpers :as d]
            ))

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
                        (assoc tx :end (d/dec-day (d/unparse-date (last-tx :date))))
                        tx))
                    (assoc tx :end (d/dec-day(next :start))))
          next (get txs (inc i))
          next-results (conj results current)]
      (recur next-remaining next-results (inc i) next))))))
