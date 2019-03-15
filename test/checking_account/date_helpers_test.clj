(ns checking-account.date-helpers-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [checking-account.date-helpers :refer :all]
            [checking-account.db-fixture :as dbf]))

(deftest Parse-date
  (testing "Correc date parsing to float"
    (is (= (parse-date "15/10") 971568000000))
    (is (= (parse-date "25/04") 956620800000))))

(deftest Unparse-date
  (testing "Unparse float date to custom format"
    (is (= (unparse-date 971568000000) "15/10"))
    (is (= (unparse-date 956620800000) "25/04"))))

(deftest Parse-to-date-time
  (testing "Parse float date to date time"
    (is (= (t/year (parse-to-date-time 971568000000)) 2000))
    (is (= (t/month (parse-to-date-time 971568000000)) 10))
    (is (= (t/day (parse-to-date-time 971568000000)) 15))))

(deftest Within-dates?
  (testing "Check if one date is between 2 dates interval"
    (is (false? (within-dates? "15/10" "01/12" "01/01")))
    (is (false? (within-dates? "01/05" "30/08" "30/08"))) ;End date is not inclusive
    (is (within-dates? "01/05" "01/09" "30/08"))
    (is (within-dates? "01/05" "01/09" "01/05"))
    (is (within-dates? "15/10" "01/12" "01/11"))))

(deftest Sort-by-date
  (testing "Sort by date vector of txs : transactions date are float values"
    (sort-by-date @dbf/transactions) dbf/sort-transactions))

(deftest Valid-date?
  (testing "Validate given dates")
  (is (valid-date? "15/12"))
  (is (valid-date? "1/2"))
  (is (false? (valid-date? "1")))
  (is (false? (valid-date? "34/12")))
  (is (false? (valid-date? 123)))
  (is (false? (valid-date? nil))))

(deftest Max-days-in-month
  (testing "Validate given dates"
    (is (= (max-days-in-month 2) 28))
    (is (= (max-days-in-month 4) 30))
    (is (= (max-days-in-month 5) 30))
    (is (= (max-days-in-month 9) 30))
    (is (= (max-days-in-month 11) 30))
    (is (= (max-days-in-month 1) 31))
    (is (= (max-days-in-month 3) 31))
    (is (= (max-days-in-month 6) 31))
    (is (= (max-days-in-month 7) 31))
    (is (= (max-days-in-month 8) 31))
    (is (= (max-days-in-month 12) 31))
    ))

(deftest Max-days-in-month
  (testing "Validate given dates"
    (is (= (dec-day "15/10") "14/10"))
    (is (= (dec-day "01/12") "30/11"))
    (is (= (dec-day "01/01") "31/12"))))
