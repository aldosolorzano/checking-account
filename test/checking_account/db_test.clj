(ns checking-account.db-test
  (:require
   [clojure.test :refer :all]
   [checking-account.db :refer :all]
   [checking-account.date-helpers :as d]))

(def local-accounts [(get @accounts 100) (get @accounts 200)])

(def tx {:description "Large purchase"
         :amount 500
         :date "22/10/2019"
         :type "purchase"})

(deftest Errors-in-tx-params
  (testing "validate each tx param applying validator fn, defined in file"
    (is (= (errors-in-tx-params {:amount 15}) '(:description :date :type)))
    (is (empty? (errors-in-tx-params tx)))))

(def valid-tx {:date (d/parse-date "22/10/2019")
               :amount -500
               :description "Large purchase"
               :type :purchase})

(deftest Build-tx
  (testing "building correct tx body"
    (is (= (build-tx tx) valid-tx))))


(deftest Save-transaction
  (testing "saving transaction in atom"
    (is (= (save-transaction {:tx-ids (ref [1 2])} tx) (conj [1 2] @tx-id)))))

(def account {:tx-ids (ref [1 2])})
(def txs {1 {:date (d/parse-date "11/10/2019")}
          2 {:date (d/parse-date "08/05/2019")}
          3 {:date (d/parse-date "25/06/2019")}})

(deftest Account-txs
  (testing "return account transactions sorted"
    (is (empty? (account-txs txs {:tx-ids (ref [])})))
    (is (= (account-txs txs account) [{:date (d/parse-date "08/05/2019")}
                                      {:date (d/parse-date "11/10/2019")}]))))

(deftest Get-account-by-id
  (testing "fetching account from accounts hash-map"
    (is (= (get-account-by-id @accounts 100) (get @accounts 100)))
    (is (= (get-account-by-id @accounts "100") (get @accounts 100)))
    (is (nil? (get-account-by-id @accounts "500")))))
