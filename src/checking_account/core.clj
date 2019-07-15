(ns checking-account.core
  (:require   [checking-account.balance :as b]
              [checking-account.negative-periods :as np]
              [checking-account.date-helpers :as d]
              [checking-account.statement :as st]
              [checking-account.db :as db]))

(defn get-balance
  [transactions account]
  (-> (b/compute-balance (db/account-txs transactions account)
      (b/format-float))))

(defn negative-periods
  [transactions account]
  (np/negative-periods (db/account-txs transactions account)))

(defn get-statement
  [transactions account date-params]
   (let [init (date-params :init)
         end  (date-params :end)]
     (if (and (d/valid-date? init) (d/valid-date? end))
       (st/build-statement (db/account-txs transactions account) date-params)
       {:errors "Invalid date interval, try :init 11/10 :end 26/10"})))
