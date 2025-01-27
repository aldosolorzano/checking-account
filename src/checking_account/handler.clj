(ns checking-account.handler
  (:require   [checking-account.core :refer :all]
              [checking-account.db :as db]
              [compojure.core :refer :all]
              [compojure.route :as route]
              [compojure.handler :as handler]
              [clojure.string :as string]
              [ring.middleware.json :as middleware]))

(defn build-response
  [params status]
  (let [response{:status status
                 :headers {"Content-Type" "application/json"}
                 :body params}]
    response))

(defn account-finder
  ([account-id f]
   (account-finder account-id f nil))
  ([account-id f req]
  (let [account (db/get-account-by-id @db/accounts account-id)
        status (if (nil? account) 422 200)
        body (if (nil? account)
                   {:errors "Account doesn't exists"}
                   (do
                     (if (nil? req)
                      (f @db/transactions account)
                      (f @db/transactions account req))))]
    (build-response body status))))

(defn validate-params
  [req expected]
  (let [err (reduce
             (fn [errors param]
               (if (nil? (req param))
                 (conj errors param)
                  errors)) [] expected)]
    (if (empty? err)
      true
      {:errors (string/join ", " (conj err "required. Add to body"))})))

(defroutes app-routes
  (GET "/" [] "<h1>Checking Account</h1>")
  (POST "/accounts" []
    (build-response (db/create-account) 200))

  (context "/accounts/:id" [id]

    (GET "/balance" [] (account-finder id get-balance))

    (POST "/transaction" req (let [body (get-in req [:body])
                                   validation (validate-params (if (nil? body) {} body) [:description :date :amount :type])]
                               (if (true? validation)
                                 (account-finder id db/create-transaction body)
                                 (build-response validation 422))))

    (GET "/negative-periods" [] (account-finder id negative-periods))

    (POST "/statement" req (let [body (get-in req [:body])
                                   validation (validate-params (if (nil? body) {} body) [:init :end])]
                               (if (true? validation)
                                 (account-finder id get-statement body)
                                 (build-response validation 422)))))

  (route/not-found "<h1>Page not found</h1>"))

  (def app
    (-> (handler/site app-routes)
        (middleware/wrap-json-body {:keywords? true})
        middleware/wrap-json-response))
