(ns checking-account.statement-test
  (:require [clojure.test :refer :all]
            [checking-account.statement :refer :all]
            [checking-account.db-fixture :as dbf]
            [checking-account.date-helpers :as d]))

(deftest Build-statement
  (testing "Build statement"
    (is (= (build-statement @dbf/transactions dbf/account {:init "11/10" :end "27/10"}) dbf/full-statement))
    (is (= (build-statement @dbf/transactions dbf/account {:init "16/10" :end "23/10"}) dbf/between-dates-statement))))

(deftest Interval-statement
  (testing "Get statement between dates"
    (is (= (interval-statement dbf/full-statement {:init "11/10" :end "27/10"}) dbf/full-statement))
    (is (= (interval-statement dbf/full-statement {:init "16/10" :end "23/10"}) dbf/between-dates-statement))))
