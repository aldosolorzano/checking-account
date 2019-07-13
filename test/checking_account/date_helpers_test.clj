(ns checking-account.date-helpers-test
  (:require [clojure.test :refer :all]
            [java-time :as t]
            [checking-account.date-helpers :refer :all]))

(def test-format "dd/MM/yyyy")

(deftest Parse-date
  (testing "Correc date parsing to float"
    (is (.equals (parse-date "15/10/2019") (t/local-date test-format "15/10/2019"))
    (is (.equals (parse-date "25/04/2019") (t/local-date test-format "25/04/2019"))))))

(deftest Unparse-date
  (testing "Unparse float date to custom format"
    (is (= (unparse-date (t/local-date test-format "15/10/2019")) "15/10/2019"))
    (is (= (unparse-date (t/local-date test-format "25/04/2019")) "25/04/2019"))))

(deftest Within-dates?
  (testing "Check if one date is between 2 dates interval"
    (is (false? (within-dates? "15/10/2019" "01/12/2019" "01/01/2019")))
    (is (false? (within-dates? "01/05/2019" "30/08/2019" "31/08/2019")))
    (is (within-dates? "01/05/2019" "01/09/2019" "30/08/2019"))
    (is (within-dates? "01/05/2019" "01/09/2019" "01/05/2019"))
    (is (within-dates? "01/05/2019" "01/09/2019" "01/09/2019"))
    (is (within-dates? "01/05/2019" "01/09/2019" "01/05/2019"))
    (is (within-dates? "15/10/2019" "01/12/2019" "01/11/2019"))))

(deftest Valid-date?
  (testing "Validate given dates"
    (is (valid-date? "15/12/2019"))
    (is (valid-date? "01/02/2019"))
    (is (false? (valid-date? "1")))
    (is (false? (valid-date? "34/12/2019")))
    (is (false? (valid-date? 123)))
    (is (false? (valid-date? nil)))))

(deftest Dec-day
  (testing "Substract one day from date"
    (is (= (dec-day "15/10/2019")) "14/10/2019")
    (is (= (dec-day "01/12/2019")) "30/11/2019")
    (is (= (dec-day "01/01/2019")) "31/12/2018")))
