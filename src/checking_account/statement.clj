(ns checking-account.statement
  (:require [clojure.string :as string]
            [checking-account.balance :as b]
            [checking-account.date-helpers :as d]
            [checking-account.db :as db]))

(defn interval-statement
  [statement date-params]
  (into {}(filter (fn [tx]
            (if (d/within-dates? (date-params :init) (date-params :end)  (first tx)) tx)) statement)))

(defn build-statement
  [transactions account date-params]
  (let [tx-ids (deref (get account :tx-ids))
        account-txs (db/account-txs transactions account)] ;sorted txs
        (loop [remaining account-txs
               results {}
               balance 0]
          (if (empty? remaining)
            (interval-statement results date-params)
            (do (let [tx (first remaining)
                  date (d/unparse-date (get tx :date))
                  next-remaining (rest remaining)
                  current-balance (if (= (tx :type) :deposit)
                                    (+ balance (tx :amount))
                                    (- balance (tx :amount)))
                  next-results (if (= (get results date) nil)
                                (conj results {date {:transactions [(string/join " " [(tx :description) (tx :amount)])] :balance (b/format-float current-balance)}})
                                (do
                                  (dosync
                                    (-> (update-in results [date :transactions]) conj (string/join " " [(tx :description) (tx :amount)])
                                        (update-in  [date :balance]) b/format-float current-balance))
                                  results))]
              (recur next-remaining next-results current-balance)))))))
