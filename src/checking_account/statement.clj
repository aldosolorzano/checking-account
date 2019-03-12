(ns checking-account.statement
  (:require [clojure.string :as string]
            [checking-account.balance :as b]
            [checking-account.date_helpers :as d]
            ))

(defn build-statement
  [transactions account]
  (let [tx-ids (deref (get account :tx-ids))
        account-txs (d/sort-by-date (map (fn [id] (get transactions id)) tx-ids))]
        (loop [remaining account-txs
               results {}]
          (if (empty? remaining)
            results
            (do (let [tx (first remaining)
                  date (d/unparse-date (get tx :date))
                  next-remaining (rest remaining)
                  next-results (if (= (get results date) nil)
                            (conj results {date { :tx-ids (atom [(tx :id)]):transactions (atom [(string/join " " [(tx :description) (tx :amount)])])}})
                            (do
                              (dosync
                                (swap! (get-in results [date :transactions]) conj (string/join " " [(tx :description) (tx :amount)]))
                                (swap! (get-in results [date :tx-ids]) conj (tx :id)))
                              results))]
              (recur next-remaining next-results)))))))

(defn get-statement
  [transactions account date-params]
  (d/txs-within-dates
   (b/add-balance-to-statement transactions (build-statement transactions account))
   (date-params :init) (date-params :end)))
