(ns checking-account.negative-periods
  (:require [checking-account.balance :as b]
            [checking-account.date-helpers :as d]))

(defn abs-formatted [value]
  (b/format-float (Math/abs value)))

(defn add-end-date [txs]
  (for [i (iterate inc 0)
         :let [tx (get txs i)
               next-date (:start (get txs (inc i)))]
         :while (< i (count txs))]
    (if-not (nil? next-date)
      (assoc tx :end (d/dec-day next-date))
      tx)))

(defn build-period
  ([{:keys [amount date]}]
   {:principal amount :start (d/unparse-date date)})
  ([{:keys [amount date]} last-tx]
   {:principal (+ (:principal last-tx) amount) :start (d/unparse-date date)}))

(defn full-periods [txs]
  (-> (b/build-balances txs build-period)
      (add-end-date)))

(defn negative-periods [txs]
  (->> (full-periods txs)
       (filter #(neg? (:principal %)))
       (map #(update % :principal abs-formatted))))
