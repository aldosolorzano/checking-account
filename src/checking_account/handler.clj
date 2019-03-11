(ns checking-account.handler
  (:require   [checking-account.core :refer :all]
              [compojure.core :refer :all]
              [compojure.route :as route]
              [compojure.handler :as handler]
              [ring.middleware.json :as middleware]
              [clojure.data.json :as json]))

(defn build-response
  [params]
  (let [response{:status 200
                 :headers {"Conten-Type" "application/json"}
                 :body params}]
    response))

(defroutes app-routes
  (GET "/" [] "<h1>Checking Account</h1>")
  (POST "/accounts" []
    (build-response {:id (create-account @accounts-db)}))

  (context "/accounts/:id" [id]
   (GET "/get-balance" [] (-> @accounts-db
                             (get-account-by-id id)
                             (get :tx-ids)
                             (deref)
                             (get-balance)
                             (build-response)))

    (POST "/transaction" req (-> (create-transaction @transactions-db (read-string id) (get-in req [:body]))
                                 (build-response)))

    (GET "/negative-periods" [] (-> (negative-periods @transactions-db (get-account-by-id @accounts-db id))
                                    (build-response))))
  (route/not-found "<h1>Page not found</h1>"))

  (def app
    (-> (handler/site app-routes)
        (middleware/wrap-json-body {:keywords? true})
        middleware/wrap-json-response))
