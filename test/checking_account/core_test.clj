(ns checking-account.core-test
  (:require [clojure.test :refer :all]
            [checking-account.core :refer :all]
            [checking-account.db :as db]
            [checking-account.date_helpers :as d]))

(def fresh-account {:id 102 :tx-ids (atom [])})
(def account-id 100)

(deftest Get-balance
  (testing "Get Balance"
    (is (= (get-balance @db/transactions (@db/accounts account-id)) "-38.57"))
    (is (= (get-balance @db/transactions {:id 102 :tx-ids (atom [0 1 2 3])}) "771.43"))
    (is (= (get-balance @db/transactions {:id 102 :tx-ids (atom [0 1 2 3 4])}) "-28.57"))
    (is (= (get-balance @db/transactions fresh-account) "0.00"))))

(def negative-body [{:principal "28.57" :start "18/10" :end "22/10"} {:principal "38.57" :start "22/10"}])

(deftest Negative-periods
  (testing "Build vector of maps with negative periods"
      (is (= (negative-periods @db/transactions (@db/accounts account-id)) negative-body))
      (is (empty? (negative-periods @db/transactions fresh-account)))))

(def statement[ "15/10" {:transactions ["Deposit 1000"] :balance "1000.00"}
    "16/10" {:transactions ["Purchase on Amazon 3.34" "Purchase on Uber 45.23"] :balance "951.43"}
    "17/10" {:transactions ["Withdrawal 180"] :balance "771.43"}
    "18/10" {:transactions ["Purchase of a flight 800"] :balance "-28.57"}
    "22/10" {:transactions ["Purchase of a expresso 10"] :balance "-38.57"}])

(deftest Get-statement
  (testing "Build array with statement info"
      (is (= (get-statement @db/transactions (@db/accounts account-id) {:init "15/10" :end "23/10"}) statement))
      (is (= (get-statement @db/transactions (@db/accounts account-id) {:init "16/10" :end "19/10"}) (subvec statement 2 8)))
      (is (empty? (get-statement @db/transactions fresh-account {:init "15/10" :end "23/10"})))))
