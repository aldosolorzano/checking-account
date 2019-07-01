(ns checking-account.statement
  (:require
   [clojure.string :as string]
   [checking-account.balance :as b]
   [checking-account.date-helpers :as d]
   [checking-account.db :as db]))

(defn interval-statement
  [date-params statement]
  (let [{:keys [init end]} date-params]
    (into {} (filter #(d/within-dates? init end (first %)) statement))))

(defn add-balance
  ([tx]
   (assoc tx :balance (:amount tx)))
  ([tx last-tx]
   (assoc tx :balance (+ (:amount tx) (:balance last-tx)))))

(defn desc-amount [tx]
  (string/join " " [(:description tx) (:amount tx)]))

(defn date-tx-body [[date txs]]
  {date (map desc-amount txs) :balance (b/format-float (:balance (last txs)))})

(defn flatten-description
  [statement]
  (map date-tx-body (seq statement)))

(defn build-statement
 [transactions date-params]
 (->> (b/build-balances transactions add-balance)
      (map #(update % :date d/unparse-date))
      (group-by :date)
      (interval-statement date-params)
      (flatten-description)))
