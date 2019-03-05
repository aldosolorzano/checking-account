(ns checking-account.core-test
  (:require [clojure.test :refer :all]
            [checking-account.core :refer :all]))

(def fresh-account {:id 102 :tx-ids (atom [])})

(deftest Checking-account
  (testing "Compute Balance"
    (is (= (compute-balance @transactions-db []) 0))
    (is (= (compute-balance @transactions-db (deref (get (get-account-by-id @accounts-db 100) :tx-ids))) 771.4300000000001)))

  (testing "Get Balance"
    (is (= (get-balance (deref (get (get-account-by-id @accounts-db 100) :tx-ids))) "771.43"))
    (is (= (get-balance []) "0.00"))
    (is (= (get-balance (deref (get (get-account-by-id @accounts-db 101) :tx-ids))) "0.00")))

  (testing "Create account"
    (let [account (create-account @accounts-db)
          id (get fresh-account :id)]
      (is (= (get fresh-account :id) (get-in account [id :id])))
      (is (empty? (deref (get-in account [id :tx-ids])))))))
