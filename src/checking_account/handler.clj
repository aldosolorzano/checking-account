(ns checking-account.handler
  (:require   [checking-account.core :refer :all]
              [checking-account.db :as db]
              [compojure.core :refer :all]
              [compojure.route :as route]
              [compojure.handler :as handler]
              [ring.middleware.json :as middleware]))

(defn build-response
  [params status]
  (let [response{:status status
                 :headers {"Conten-Type" "application/json"}
                 :body params}]
    response))

(defn account-finder ;Not pure
  ([account-id f]
   (account-finder account-id f nil))
  ([account-id f req]
  (let [account (get-account-by-id @db/accounts account-id)
        status (if (nil? account) 422 200)
        body (if (nil? account)
                   {:errors "Account doesn't exists"}
                   (do
                     (if (nil? req)
                      (f @db/transactions account)
                      (f @db/transactions account req))))]
    (build-response body status))))

(defroutes app-routes
  (GET "/" [] "<h1>Checking Account</h1>")
  (POST "/accounts" []
    (build-response (db/create-account) 200))

  (context "/accounts/:id" [id]

    (GET "/balance" [] (account-finder id get-balance))

    (POST "/transaction" req (account-finder id db/create-transaction (get-in req [:body])))

    (GET "/negative-periods" [] (account-finder id negative-periods))

    (POST "/statement" req (account-finder id get-statement (get-in req[:body]))))

  (route/not-found "<h1>Page not found</h1>"))

  (def app
    (-> (handler/site app-routes)
        (middleware/wrap-json-body {:keywords? true})
        middleware/wrap-json-response))
