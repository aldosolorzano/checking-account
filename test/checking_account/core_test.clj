(ns checking-account.core-test
  (:require [clojure.test :refer :all]
            [checking-account.core :refer :all]))

(def fresh-account {:id 102 :tx-ids (atom [])})
(def transaction-params {:description "Deposit" :amount 180 :date "17/10" :type "withdrawal"})

(deftest Checking-account
  (testing "Compute Balance"
    (is (= (compute-balance @transactions-db []) 0))
    (is (= (compute-balance @transactions-db (deref (get (get-account-by-id @accounts-db 100) :tx-ids))) 771.4300000000001)))

  (testing "Get Balance"
    (is (= (get-balance (deref (get (get-account-by-id @accounts-db 100) :tx-ids))) "771.43"))
    (is (= (get-balance []) "0.00"))
    (is (= (get-balance (deref (get (get-account-by-id @accounts-db 101) :tx-ids))) "0.00")))

  (testing "Create account"
    (let [account-id (create-account @accounts-db)]
      (is (= (get fresh-account :id) account-id))
      (is (empty? (deref (get-in @accounts-db [account-id :tx-ids]))))))

  (testing "Create transaction"
    (let [tx (create-transaction @transactions-db (get fresh-account :id) transaction-params)]
      (println tx)
      (is (= (get tx :id) 4))
      (is (= (get fresh-account :id) (get tx :account)))
      (is (= (get transaction-params :description) (get tx :description)))
      (is (= (keyword(get transaction-params :type)) (get tx :type)))
      (is (= (get transaction-params :amount) (get tx :amount)))
      )))
