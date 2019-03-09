(ns checking-account.core
  (:require [org.httpkit.server :refer [run-server]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]))

(def accounts
  [{:id 100 :transactions []}
   {:id 101 :transactions []}
   ])

(def transactions
  [{:id 0 :account 100 :description "Initial"
    :amount 1000 :date "15/10" :type :deposit}
    {:id 1 :account 100 :description "Amazon"
      :amount 3.34 :date "16/10" :type :purchase}
   {:id 2 :account 100 :description "Uber"
      :amount 45.23 :date "16/10" :type :purchase}
   {:id 3 :account 100 :description ""
      :amount 180 :date "17/10" :type :withdrawal}
   ])

(defn get-account-by-id
  [id]
  (let [account (first (filter (fn [a] (= id (get a :id))) accounts))
        response {:status 200
                  :headers {"Content-Type" "application/json"}
                  :body (json/write-str account)}]
    response))

(defroutes app
           (GET "/" [] "<h1>Welcome</h1>")

           (GET "/accounts/:account-id" [account-id] (-> account-id
                                                read-string
                                                get-account-by-id))

           (route/not-found "<h1>Page not found</h1>"))

(defn -main [& args]
  (run-server app {:port 8080})
  (println "Server started on port 8080"))
