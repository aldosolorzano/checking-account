(ns checking-account.core-test
  (:require [clojure.test :refer :all]
            [checking-account.core :refer :all]
            [checking-account.db-fixture :as dbf]
            [checking-account.date-helpers :as d]))

(deftest Get-balance
  (testing "Get Balance"
    (is (= (get-balance @dbf/transactions (@dbf/accounts dbf/account-id)) "11.43"))
    (is (= (get-balance @dbf/transactions {:id 102 :tx-ids (atom [0 1 2 3])}) "771.43"))
    (is (= (get-balance @dbf/transactions {:id 102 :tx-ids (atom [0 1 2 3 4])}) "-28.57"))
    (is (= (get-balance @dbf/transactions dbf/fresh-account) "0.00"))))

(deftest Negative-periods
  (testing "Build vector of maps with negative periods"
      (is (= (negative-periods @dbf/transactions (@dbf/accounts dbf/account-id)) dbf/negative-period))
      (is (= (negative-periods @dbf/transactions dbf/neg-account) dbf/negative-period-debt))
      (is (empty? (negative-periods @dbf/transactions dbf/fresh-account)))))

(deftest Get-statement
  (testing "Build array with statement info"
      (is (= (get-statement @dbf/transactions (@dbf/accounts dbf/account-id) {:init "11/10" :end "26/10"}) dbf/full-statement))
      (is (= (get-statement @dbf/transactions (@dbf/accounts dbf/account-id) {:init "16/10" :end "23/10"}) dbf/between-dates-statement))
      (is (empty? (get-statement @dbf/transactions dbf/fresh-account {:init "15/10" :end "23/10"})))))
