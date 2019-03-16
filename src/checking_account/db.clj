(ns checking-account.db
  (:require [checking-account.date-helpers :as d]
            [clojure.string :as string]))

(def accounts (atom {100 {:id 100 :tx-ids (atom [0 1 2 3 4 5])}}))

(def transactions (atom [{:id 0 :account 100 :description "Deposit"
                          :amount 1000 :date (d/parse-date "11/10") :type :deposit}
                          {:id 1 :account 100 :description "Purchase on Amazon"
                            :amount 3.34 :date (d/parse-date "16/10") :type :purchase}
                          {:id 2 :account 100 :description "Purchase on Uber"
                            :amount 45.23 :date (d/parse-date "14/10") :type :purchase}
                          {:id 3 :account 100 :description "Withdrawal"
                            :amount 180 :date (d/parse-date "17/10") :type :withdrawal}
                          {:id 4 :account 100 :description "Purchase of a flight"
                            :amount 800 :date (d/parse-date "12/10") :type :purchase}
                          {:id 5 :account 100 :description "Purchase of a expresso"
                            :amount 10 :date (d/parse-date "22/10") :type :purchase}]))

(def tx-id (atom (dec (count @transactions))))

(def account-id (atom 100))

(defn build-account
  [id]
  {id {:id id :tx-ids (atom [])}})

(defn create-account
  []
  (let [id (swap! account-id inc)
        accs (swap! accounts conj (build-account id))
        res {:id id}]
    (print "Account created: ")
    (println res)
    res))

(defn build-tx
  [account params]
  (let [tx {:id (swap! tx-id inc)
            :account (account :id)
            :date (d/parse-date (params :date))
            :amount (params :amount)
            :description (params :description)
            :type (keyword (params :type))}]
    tx))

(defn errors-in-tx-params
  [params]
  (loop
    [remaining [:description :date :amount :type]
     errors []]
    (if (empty? remaining) errors
      (let [param (first remaining)
            err (condp = param
                  :description (if (not (string? (params param))) param false)
                  :date (if (not (d/valid-date? (params param))) param false)
                  :amount (if (not (number? (params param))) param false)
                  :type (if (not (string? (params param))) param false))
            next-errors (if (false? err) errors (conj errors err))]
        (recur (rest remaining) next-errors)))))

(defn create-transaction
  [placeholder account params]
    ; TODO remove placeholder, it is pased in handler.clj account-finder to valide account
    (let [param-errors (errors-in-tx-params params)
          response (if (empty? param-errors)
                (dosync ;Both txs need to be done to be accesible to other threads
                  ; params must be valid for atom swap in case y has to recall the function
                  (let [txs (swap! transactions conj (build-tx account params))
                  tx (last txs) ;Keep tx locally and avoid id mismatch
                  res (update tx :date d/unparse-date)]
                  (swap! (get-in @accounts [(account :id) :tx-ids]) conj (tx :id))
                  (print "Transaction created: ")
                  (println res)
                  res))
                  {:errors (string/join ", " (conj param-errors "Invalid tx params"))})]
      response))

(defn account-txs
  [transactions account]
  (-> (map (fn [id] (get transactions id)) (deref (account :tx-ids)))
      (d/sort-by-date)))

(defn get-account-by-id
  [accounts id]
  (let [account (accounts (read-string (str id)))]
    account))
