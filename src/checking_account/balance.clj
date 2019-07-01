(ns checking-account.balance
  (:require
   [checking-account.date-helpers :as d]
   [checking-account.db :as db]))

(defn format-float
  [number]
  (format "%.2f"  (float number)))

(defn compute-balance
  [transactions]
  (apply + (map :amount transactions)))

(defn build-period
  ([{:keys [amount date]}]
   (hash-map :principal amount :start (d/unparse-date date)))
  ([{:keys [amount date]} last-tx]
   (hash-map :principal (+ (:principal last-tx) amount) :start (d/unparse-date date))))

(defn conj-results
  [builder results tx]
  (if (empty? results)
   (conj results (builder tx))
   (conj results (builder tx (last results)))))

; transactions -> vector/sequence of transactions.
; builder -> function that uses tx's body to return data.
; returns a vector of the data given by the builder function.
(defn build-balances
  [transactions builder]
  (reduce #(conj-results builder % %2) [] transactions))

(defn negative-balances
  [transactions]
  (->> (build-balances transactions build-period)
       (filter #(neg? (:principal %)))
       (map #(assoc % :principal  (format-float (Math/abs (:principal %))))))) ; convert balance to pos
