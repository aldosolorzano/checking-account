(ns checking-account.statement
  (:require [clojure.string :as string]
            [checking-account.balance :as b]
            [checking-account.date-helpers :as d]
            [checking-account.db :as db]
            ))

(defn build-statement
  [transactions account]
  (let [tx-ids (deref (get account :tx-ids))
        account-txs (db/account-txs transactions account)] ;sorted txs
        (loop [remaining account-txs
               results {}]
          (if (empty? remaining)
            results
            (do (let [tx (first remaining)
                  date (d/unparse-date (get tx :date))
                  next-remaining (rest remaining)
                  next-results (if (= (get results date) nil)
                            (conj results {date { :tx-ids (atom [(tx :id)]) :transactions (atom [(string/join " " [(tx :description) (tx :amount)])])}})
                            (do
                              (dosync
                                (swap! (get-in results [date :transactions]) conj (string/join " " [(tx :description) (tx :amount)]))
                                (swap! (get-in results [date :tx-ids]) conj (tx :id)))
                              results))]
              (recur next-remaining next-results)))))))
