(ns checking-account.handler
  (:require   [checking-account.core :refer :all]
              [compojure.core :refer :all]
              [compojure.route :as route]
              [compojure.handler :as handler]
              [ring.middleware.json :as middleware]
              [clojure.data.json :as json]))

(defn build-json
  [params]
  (let [response{:status 200
                 :headers {"Conten-Type" "application/json"}
                 :body (json/write-str params)}]
    response))

(defroutes app-routes
  (GET "/" [] "<h1>Checking Account</h1>")
  (POST "/accounts" []
    (build-json {:id (create-account @accounts-db)}))

  (context "/accounts/:id" [id]
   (GET "/get-balance" [] (-> @accounts-db
                             (get-account-by-id id)
                             (get :tx-ids)
                             (deref)
                             (get-balance)
                             (build-json)))
           
    (POST "/transaction" req (-> (create-transaction @transactions-db (read-string id) (get-in req [:body]))
                                 (build-json))))

  (route/not-found "<h1>Page not found</h1>"))

  (def app
    (-> (handler/site app-routes)
        (middleware/wrap-json-body {:keywords? true})
        middleware/wrap-json-response))
