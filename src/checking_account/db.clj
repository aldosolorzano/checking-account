(ns checking-account.db
  (:require [checking-account.date-helpers :as d]
            [clojure.string :as string]))

(def accounts (atom {100 {:id 100 :tx-ids (ref [1 2 3 4 5 6])}
                     200 {:id 200 :tx-ids (ref [])}}))

(def transactions (ref {1 {:id 1
                            :account 100
                            :description "Deposit"
                            :amount 1000
                            :date (d/parse-date "11/10/2019")
                            :type :deposit}
                         2 {:id 2
                            :account 100
                            :description "Purchase on Amazon"
                            :amount -3.34
                            :date (d/parse-date "16/10/2019")
                            :type :purchase}
                         3 {:id 3
                            :account 100
                            :description "Purchase on Uber"
                            :amount -45.23
                            :date (d/parse-date "14/10/2019")
                            :type :purchase}
                         4 {:id 4
                            :account 100
                            :description "Withdrawal"
                            :amount -180
                            :date (d/parse-date "17/10/2019")
                            :type :withdrawal}
                         5 {:id 5
                            :account 100
                            :description "Purchase of a flight"
                            :amount -800
                            :date (d/parse-date "12/10/2019")
                            :type :purchase}
                         6 {:id 6
                            :account 100
                            :description "Purchase of a expresso"
                            :amount -10
                            :date (d/parse-date "22/10/2019")
                            :type :purchase}}))

(def tx-validators  {:description string?
                     :date d/valid-date?
                     :amount number?
                     :type string?})

(def tx-id (atom 6))

(def account-id (atom 100))

(defn build-account [id]
  {id {:id id :tx-ids (atom [])}})

(defn create-account []
  (let [id (swap! account-id inc)
        res {:id id}]
    (swap! accounts conj (build-account id)
    (print "Account created: ")
    (println res)
    res)))

(defn build-tx [params]
  (let [{:keys [date amount type description]} params]
    {:date (d/parse-date date)
    :amount (if (= type :deposit) amount (- amount))
    :description description
    :type (keyword type)}))

(defn errors-in-tx-params [tx-params]
  (for [[param type] (seq tx-validators)
        :when (false? (type (param tx-params)))]
        param))

(defn save-transaction [account tx-params]
  (dosync
    (let [id (swap! tx-id inc)
          tx (assoc (build-tx tx-params) :id id :account (:id account))]
     ; Use of alter to apply STM dependency rules
     (alter transactions conj {id tx})
     (alter (:tx-ids account) conj id))))

(defn create-transaction [placeholder account params]
  ; TODO remove placeholder, it is pased in handler.clj account-finder to valide account
  (let [param-errors (errors-in-tx-params params)]
    (if (empty? param-errors)
     (update (save-transaction account params) :date d/unparse-date)
     {:errors (conj param-errors "Invalid tx params")})))

(defn account-txs [transactions account]
  (->> (map #(get transactions %) (deref (:tx-ids account)))
       (sort-by :date)))

(defn get-account-by-id [accounts id]
  (get accounts (read-string (str id))))
