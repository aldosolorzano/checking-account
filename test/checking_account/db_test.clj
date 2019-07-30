(ns checking-account.db-test
  (:require
   [clojure.test :refer :all]
   [checking-account.db :refer :all]
   [checking-account.date-helpers :as d]))

(def main-account (get @accounts 100))

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
    (is (= (build-tx tx) valid-tx))
    (is (= (build-tx (assoc tx :type "deposit")) (assoc valid-tx :amount 500 :type :deposit)))))


(deftest Save-transaction
  (testing "saving transaction in atom"
    (is (= (dissoc (save-transaction {:id 100 :tx-ids (ref [1 2])} tx) :id)
           (assoc valid-tx :date "22/10/2019" :account 100)))))

(deftest Create-transaction
  (testing "Apply save-transaction & error validation"
    (is (= (dissoc (create-transaction :_ main-account tx) :id)
           (assoc valid-tx :date "22/10/2019" :account 100)))
    (is (= (create-transaction :_ main-account {:amount 15}) {:errors '("Invalid tx params" :description :date :type)}))))

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
