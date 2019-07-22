(ns checking-account.db
  (:require [checking-account.date-helpers :as d]
            [clojure.string :as string]))

(def accounts (atom {100 {:id 100 :tx-ids (atom [1 2 3 4 5 6 7])}
                     200 {:id 200 :tx-ids (atom [])}}))

(def transactions (atom {1 {:id 1 :account 100 :description "Deposit"
                           :amount 1000 :date (d/parse-date "11/10/2019") :type :deposit}
                         2 {:id 2 :account 100 :description "Purchase on Amazon"
                            :amount -3.34 :date (d/parse-date "16/10/2019") :type :purchase}
                         3 {:id 3 :account 100 :description "Purchase on Uber"
                            :amount -45.23 :date (d/parse-date "14/10/2019") :type :purchase}
                         4 {:id 4 :account 100 :description "Withdrawal"
                            :amount -180 :date (d/parse-date "17/10/2019") :type :withdrawal}
                         5 {:id 5 :account 100 :description "Purchase of a flight"
                            :amount -800 :date (d/parse-date "12/10/2019") :type :purchase}
                         6 {:id 6 :account 100 :description "Purchase of a expresso"
                            :amount -10 :date (d/parse-date "22/10/2019") :type :purchase}
                         7 {:id 7 :account 100 :description "Deposit"
                          :amount -500 :date (d/parse-date "22/10/2019") :type :purchase}}))

(def tx-id (atom (count @transactions)))

(def account-id (atom 100))

(defn build-account [id]
  {id {:id id :tx-ids (atom [])}})

(defn create-account []
  (let [id (swap! account-id inc)
        accs (swap! accounts conj (build-account id))
        res {:id id}]
    (print "Account created: ")
    (println res)
    res))

(defn build-tx [params]
  (let [{:keys [date amount type description]} params]
    {:date (d/parse-date date)
    :amount (if (= type :deposit) amount (- amount))
    :description description
    :type (keyword type)}))

(defn errors-in-tx-params [params]
  (loop
    [remaining [:description :date :amount :type]
     errors []]
    (if (empty? remaining) errors
      (let [param (first remaining)
            err (case param
                  :description (if-not (string? (params param)) param false)
                  :date (if-not (d/valid-date? (params param)) param false)
                  :amount (if-not (number? (params param)) param false)
                  :type (if-not (string? (params param)) param false))
            next-errors (if (false? err) errors (conj errors err))]
        (recur (rest remaining) next-errors)))))

(defn save-transaction [account tx-params]
  (dosync
    (let [id (swap! tx-id inc)
          tx (assoc (build-tx tx-params) :id id)]
     (swap! transactions conj {id tx})
     (swap! (:tx-ids account) conj id))))

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
