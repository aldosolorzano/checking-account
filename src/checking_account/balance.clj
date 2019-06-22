(ns checking-account.balance
  (:require [checking-account.date-helpers :as d]
            [checking-account.db :as db]))

(defn format-float
  [number]
  (format "%.2f"  (float number)))

(defn compute-balance
  [transactions]
  (apply + (map :amount transactions)))

(defn build-period
  [{:keys [amount date]} balance]
  {:principal (+ balance amount) :start (d/unparse-date date) })

(defn conj-results
  [results tx]
  (conj results (build-period tx (:principal (last results)))))

(defn build-balances
  [transactions]
  (reduce #(conj-results % %2) [(build-period (first transactions) 0)] (rest transactions)))

(defn negative-balances
  [transactions]
  (->> (build-balances transactions)
       (filter #(neg? (:principal %)))
       (map #(assoc % :principal  (format-float (- (:principal %))))))) ; convert balance to pos
