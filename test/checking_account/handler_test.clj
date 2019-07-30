(ns checking-account.handler-test
  (:require   [clojure.test :refer :all]
              [checking-account.handler :refer :all]
              [checking-account.core :refer :all]
              [checking-account.db :as db]
              [checking-account.date-helpers :as d]
              [ring.mock.request :as mock]
              [cheshire.core :refer :all]))

(def invalid-account {:status  422
                      :headers {"Content-Type" "application/json"}
                      :body (generate-string {:errors "Account doesn't exists"})})

(def tx-params {:description "Large purchase"
                :amount 500
                :date "22/10/2019"
                :type "purchase"})

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

  (testing "Balance"
    (is (= (:status (app (mock/request :get "/accounts/100/balance"))) 200))
    (is (= (app (mock/request :get "/accounts/1234938373/balance")) invalid-account)))
  ;
  (testing "Statement"
    (is (= (:status (app (-> (mock/request :post "/accounts/100/statement")
                      (mock/json-body {:init "10/10/2019" :end "27/10/2019"})))) 200))

    (is (= (update (app (-> (mock/request :post "/accounts/100/statement")
                    (mock/json-body {:init "10/10"}))) :body parse-string true)
           {:status  422
            :headers {"Content-Type" "application/json"}
            :body {:errors ":end, required. Add to body"}}))

    (is (= (update (app (mock/request :post "/accounts/100/statement")) :body parse-string true)
           {:status  422
            :headers {"Content-Type" "application/json"}
            :body {:errors ":init, :end, required. Add to body"}}))

    (is (= (app (-> (mock/request :post "/accounts/10012/statement")
                    (mock/json-body {:init "10/10" :end "27/10"})))
           invalid-account)))
  ;
  (testing "Negative periods"
    (is (= (:status (app (mock/request :get "/accounts/100/negative-periods")) 200)))
    (is (= (app (mock/request :get "/accounts/1234938373/negative-periods")) invalid-account)))

  (testing "Create transaction"
    (is (= (update (app (mock/request :post "/accounts/100/transaction"))
                   :body parse-string true)
                    {:status  422
                     :headers {"Content-Type" "application/json"}
                     :body {:errors ":description, :date, :amount, :type, required. Add to body"}}))

    (is (= (update (app (-> (mock/request :post "/accounts/100/transaction")
                    (mock/json-body {:description "Purchase" :amount 234}))) :body parse-string true)
           {:status  422
            :headers {"Content-Type" "application/json"}
            :body {:errors ":date, :type, required. Add to body"}}))

    (is (= (:status (app (-> (mock/request :post "/accounts/100/transaction")
                             (mock/json-body tx-params)))) 200))

    (is (= (app (-> (mock/request :post "/accounts/100123/transaction")
                    (mock/json-body tx-params)))
           invalid-account))))

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
  (testing "Find account and build json response"
    (is (= (:status (account-finder 100 get-balance)) 200)))

  (testing "When given account is Invalid"
    (is (= (account-finder 234 get-balance)
           (update invalid-account :body parse-string true)))))

(deftest Validate-params
  (testing "Validate params"
    (is (= (validate-params {} [:init :end])
          {:errors ":init, :end, required. Add to body"}))
    (is (validate-params {:inti "12/10" :end "11/10"} [:init :end]))
    (is (= (validate-params {:amount 235 :type "deposit"} [:description :date :amount :type])
          {:errors ":description, :date, required. Add to body"}))))
