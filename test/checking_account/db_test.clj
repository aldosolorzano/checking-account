(ns checking-account.db-test
  (:require [clojure.test :refer :all]
            [checking-account.db :refer :all]
            [checking-account.date-helpers :as d]
            [checking-account.db-fixture :as dbf]))

(deftest Build-account
  (testing "Build new account body"
    (let [id 102
          account-body (update-in (build-account id) [id :tx-ids] deref)]
          (is (= account-body {id (update dbf/fresh-account :tx-ids deref)}))
          (is (empty? (get-in account-body [id :tx-ids]))))))

(deftest Create-account
  (testing "Create account"
    (let [new-account-id (inc @account-id)
          new-account (create-account)]
          (is (= @account-id new-account-id))
          (is (= new-account {:id new-account-id})))
    (let [new-account-id (inc @account-id)
          new-account (create-account)]
          (is (= @account-id new-account-id))
          (is (= new-account {:id new-account-id})))))

(deftest Build-tx
  (testing "Build tx body"
    (let [account dbf/account
          new-tx-id (inc @tx-id)
          tx-body (build-tx account dbf/transaction-params)
          expected-body (update (conj tx-body {:id new-tx-id :account (account :id)}) :type keyword)]
          (is (= @tx-id new-tx-id))
          (is (= tx-body dbf/expected-tx-body)))))

(def invalid-params {:errors ":description, :date, :amount, :type, Invalid tx params"})
(deftest Create-transaciton
  (testing "Create transaction"
    (let [new-tx-id (inc @tx-id)
          new-tx (create-transaction transactions (@accounts 100) dbf/transaction-params)
          expected-tx (update (update dbf/expected-tx-body :date d/unparse-date) :id inc)]
          (is (= @tx-id new-tx-id))
          (is (= (last (deref (get-in @accounts [100 :tx-ids]))) new-tx-id))
          (is (= new-tx expected-tx))))
  (testing "Create transaction with invalid params"
    (is (= (create-transaction transactions (@accounts 100) (assoc dbf/transaction-params :date "123"))
           {:errors ":date, Invalid tx params"}))
    (is (= (create-transaction transactions (@accounts 100) {}) invalid-params))
    (is (= (create-transaction transactions (@accounts 100) {:description 1 :amount "one" :date "11" :type 1324})
           invalid-params))))

(deftest Account-txs
  (testing "Get sorted account tx")
    (is (= (account-txs @dbf/transactions dbf/account) dbf/account-txs)))

(deftest Get-account-by-id
  (testing "Get sorted account tx"
    (is (= (update (get-account-by-id @dbf/accounts 100) :tx-ids deref) (update dbf/account :tx-ids deref)))))

(deftest Valid-tx-params
  (testing "Validate transaction params"
    (is (empty? (errors-in-tx-params dbf/transaction-params)))
    (is (= (errors-in-tx-params (assoc dbf/transaction-params :date "123")) [:date]))
    (is (= (errors-in-tx-params {}) [:description :date :amount :type]))))
