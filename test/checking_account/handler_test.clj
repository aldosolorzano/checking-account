(ns checking-account.handler-test
  (:require   [clojure.test :refer :all]
              [checking-account.handler :refer :all]
              [checking-account.core :refer :all]
              [checking-account.db-fixture :as dbf]
              [checking-account.db :as db]
              [checking-account.date-helpers :as d]
              [ring.mock.request :as mock]
              [cheshire.core :refer :all]))

(def invalid-account {:status  422
                      :headers {"Content-Type" "application/json"}
                      :body (generate-string {:errors "Account doesn't exists"})})
(deftest app-test
  (testing "/ path"
    (is (= (app (mock/request :get "/"))
                {:status  200
                 :headers {"Content-Type" "text/html; charset=utf-8"}
                 :body    "<h1>Checking Account</h1>"})))
  (testing "not-found"
    (is (= (app (mock/request :get "/not-found"))
                {:status  404
                 :headers {"Content-Type" "text/html; charset=utf-8"}
                 :body    "<h1>Page not found</h1>"})))
  ;TODO Fix test to run with other files.
  ; (testing "Balance"
  ;   (is (= (app (mock/request :get "/accounts/100/balance"))
  ;               {:status  200
  ;                :headers {"Content-Type" "application/json"}
  ;                :body    "-38.57"}))
  ;   (is (= (app (mock/request :get "/accounts/1234938373/balance")) invalid-account)))
  ;
  ; (testing "Statement"
  ;   (is (= (update (app (-> (mock/request :post "/accounts/100/statement")
  ;                   (mock/json-body {:init "10/10" :end "27/10"}))) :body parse-string)
  ;               {:status  200
  ;                :headers {"Content-Type" "application/json"}
  ;                :body    {"11/10" {"transactions" ["Deposit 1000"], "balance" "1000.00"}
  ;                          "12/10" {"transactions" ["Purchase of a flight 800"], "balance" "200.00"}
  ;                          "14/10" {"transactions" ["Purchase on Uber 45.23"], "balance" "154.77"}
  ;                          "16/10" {"transactions" ["Purchase on Amazon 3.34"], "balance" "151.43"}
  ;                          "17/10" {"transactions" ["Withdrawal 180"], "balance" "-28.57"}
  ;                          "22/10" {"transactions" ["Purchase of a expresso 10"], "balance" "-38.57"}}}))
  ;   (is (= (app (-> (mock/request :post "/accounts/10012/statement")
  ;                   (mock/json-body {:init "10/10" :end "27/10"})))
  ;          invalid-account)))
  ;
  ; (testing "Negative periods"
  ;   (is (= (update (app (mock/request :get "/accounts/100/negative-periods")) :body parse-string true)
  ;               {:status  200
  ;                :headers {"Content-Type" "application/json"}
  ;                :body    dbf/negative-period-debt}))
  ;   (is (= (app (mock/request :get "/accounts/1234938373/negative-periods")) invalid-account)))

  ; (testing "Create transaction"
  ;   (is (= (update (app (-> (mock/request :post "/accounts/100/transaction")
  ;                       (mock/json-body dbf/transaction-params)))
  ;                  :body parse-string true)
  ;                   {:status  200
  ;                    :headers {"Content-Type" "application/json"}
  ;                    :body (assoc
  ;                           (update
  ;                            (update dbf/expected-tx-body :date d/unparse-date) :type name) :id @db/tx-id)}))
  ;   (is (= (app (mock/request :post "/accounts/1234938373/transaction")) invalid-account)))

  (testing "Create account"
    (is (= (update (app (mock/request :post "/accounts")) :body parse-string true)
                {:status  200
                 :headers {"Content-Type" "application/json"}
                 :body    {:id @db/account-id}}))))

(deftest Build-response
  (testing "Build http response"
    (is (= (build-response {:id 1, :name "aldo"} 200)
             {:status  200
              :headers {"Content-Type" "application/json"}
              :body    {:id 1, :name "aldo"}}))
    (is (= (build-response {:errors "Invalid account"} 422)
             {:status  422
              :headers {"Content-Type" "application/json"}
              :body    {:errors "Invalid account"}}))))

(deftest Account-finder

  ; (testing "Find account and build json response"
  ;   (is (= (account-finder dbf/account-id get-balance)
  ;          {:status  200
  ;           :headers {"Content-Type" "application/json"}
  ;           :body "-38.57"})))
  (testing "When given account is Invalid"
    (is (= (account-finder 234 get-balance)
           (update invalid-account :body parse-string true)))))
