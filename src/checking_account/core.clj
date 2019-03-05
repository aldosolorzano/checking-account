(ns checking-account.core
  (:require [clojure.string :as string]
            [org.httpkit.server :refer [run-server]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]))

(def accounts-db (atom {100 {:id 100 :tx-ids (atom [0 1 2 3])}
                     101 {:id 101 :tx-ids (atom [])}}))

(def transactions-db (atom [{:id 0 :account 100 :description "Deposit"
                          :amount 1000 :date "15/10" :type :deposit}
                          {:id 1 :account 100 :description "Purchase on Amazon"
                            :amount 3.34 :date "16/10" :type :purchase}
                         {:id 2 :account 100 :description "Purchase on Uber"
                            :amount 45.23 :date "16/10" :type :purchase}
                         {:id 3 :account 100 :description ""
                            :amount 180 :date "17/10" :type :withdrawal}]))

(defn build-json
  [params]
  (let [response{:status 200
                 :headers {"Conten-Type" "application/html"}
                 :body (json/write-str params)}]
    response))

(defn create-account
  [accounts]
  (let [new-id (if (empty? accounts) 100 (inc (first (last accounts))))
        account {new-id {:id new-id :tx-ids (atom [])}}]
    (swap! accounts-db conj account)
    account))

(defn create-transaction
  [transactions account params]
  (let [new-id (if (empty? transactions) 0 (inc (get (last transactions) :id)))
        account-id (get account :id)
        tx (conj {:id new-id :account account-id} params)]
    (swap! @transactions-db conj tx)
    (swap! (get-in @accounts-db [account-id :tx-ids]) conj new-id)
    tx))

(defn get-account-by-id
  [accounts id]
  (let [account (get accounts (read-string (str id)))]
    account))

(defn compute-balance
  [transactions tx-ids]
  (let [balance (- (->> (map (fn [id] (get transactions id)) tx-ids)
                        (filter (fn [tx] (= (get tx :type) :deposit)))
                        (map (fn [tx] (get tx :amount)))
                        (reduce +))
                   (->> (map (fn [id] (get transactions id)) tx-ids)
                        (filter (fn [tx] (not= (get tx :type) :deposit)))
                        (map (fn [tx] (get tx :amount)))
                        (reduce +)))]
    balance))

(defn get-balance
  [tx-ids]
  (let [balance (if (empty? tx-ids) 0.00 (compute-balance @transactions-db tx-ids))]
    (format "%.2f"  (float balance))))

(defroutes app
  (GET "/" [] "<h1>Checking Account</h1>")
  (context "/accounts/:id" [id]
   (GET "/get-balance" [] (-> @accounts-db
                             (get-account-by-id id)
                             (get :tx-ids)
                             (deref)
                             (get-balance)
                             (build-json))))
  (route/not-found "<h1>Page not found</h1>"))

(defn -main [& args]
  (run-server app {:port 8080})
  (println "checking-account started on port 8080"))
