(ns checking-account.balance-test
  (:require [clojure.test :refer :all]
            [checking-account.balance :refer :all]
            [checking-account.db :as db]
            [checking-account.db-fixture :as dbf]
            [checking-account.date-helpers :as d]))

(deftest Format-float
  (testing "Compute balance given txs & tx-ids"
    (is (= (format-float 888.12343523) "888.12"))
    (is (= (format-float 44) "44.00"))
    (is (= (format-float 44.4) "44.40"))
    (is (= (format-float -51.888) "-51.89"))
    (is (= (format-float -51.8) "-51.80"))
    (is (= (format-float -51) "-51.00"))))

(deftest Compute-balance
  (testing "Compute balance given txs & tx-ids"
    (is (= (compute-balance @db/transactions []) 0))
    (is (= (Math/floor (compute-balance @db/transactions [0 1 2 3 4])) -29.0))
    (is (= (Math/floor (compute-balance @db/transactions [0 1 2 3])) 771.0))))

(deftest Negative-balances
  (testing "Build vector of maps with negative periods"
      (is (= (negative-balances (d/sort-by-date (db/account-txs @dbf/transactions (@db/accounts 100)))) dbf/no-end-negative))
      (is (= (negative-balances (d/sort-by-date (db/account-txs @dbf/transactions dbf/neg-account))) dbf/no-end-negative))
      (is (empty? (negative-balances [])))
      (is (empty? (negative-balances (d/sort-by-date (db/account-txs  @dbf/transactions dbf/pos-account)))))
    ))

(deftest Add-balance-to-statement
  (testing "Add balance to statement and return a vector"
      (is (= (add-balance-to-statement @dbf/transactions dbf/full-statement-no-balance {:init "11/10" :end "27/10"}) dbf/full-statement))
      (is (= (add-balance-to-statement @dbf/transactions dbf/full-statement-no-balance {:init "16/10" :end "23/10"}) dbf/between-dates-statement))
    ))
