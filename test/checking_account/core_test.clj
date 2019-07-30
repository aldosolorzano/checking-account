(ns checking-account.core-test
  (:require [clojure.test :refer :all]
            [checking-account.core :refer :all]
            [checking-account.date-helpers :as d]))

(def fresh-account {:tx-ids (ref [])})
(def account {:id 100 :tx-ids (ref [0 1 2 3])})
(def transactions [{:id 0 :account 100 :description "Deposit"
                    :amount 1000 :date (d/parse-date "11/10/2019") :type :deposit}
                   {:id 1 :account 100 :description "Purchase on Amazon"
                    :amount 3.34 :date (d/parse-date "16/10/2019") :type :purchase}
                   {:id 2 :account 100 :description "Purchase on Uber"
                    :amount -2000 :date (d/parse-date "14/10/2019") :type :purchase}
                   {:id 3 :account 100 :description "Purchase on Uber"
                    :amount 997.66 :date (d/parse-date "18/10/2019") :type :purchase}])

(deftest Get-balance
  (testing "Get Balance"
    (is (= (get-balance transactions account) "1.00"))
    (is (= (get-balance transactions fresh-account) "0.00"))))

(def expected-neg-periods (seq [{:principal "1000.00", :start "14/10/2019", :end "15/10/2019"}
                                {:principal "996.66", :start "16/10/2019", :end "17/10/2019"}]))

(deftest Negative-periods
  (testing "Build vector of maps with negative periods"
    (is (= (negative-periods transactions account) expected-neg-periods))
    (is (empty? (negative-periods transactions fresh-account)))))

(def statement (seq [{"11/10/2019" (seq ["Deposit 1000"]), :balance "1000.00"}
                     {"14/10/2019" (seq ["Purchase on Uber -2000"]) :balance "-1000.00"}
                     {"16/10/2019" (seq ["Purchase on Amazon 3.34"]) :balance "-996.66"}
                     {"18/10/2019" (seq ["Purchase on Uber 997.66"]) :balance "1.00"}]))

(deftest Get-statement
  (testing "Build array with statement info"
    (is (= (get-statement transactions account {:init "11/10/2019" :end "26/10/2019"}) statement))
    (is (= (get-statement transactions account {:init 2 :end 1}) {:errors "Invalid date interval, try :init 11/10/2019 :end 26/10/2019"}))
    (is (= (get-statement transactions account {}) {:errors "Invalid date interval, try :init 11/10/2019 :end 26/10/2019"}))
    (is (empty? (get-statement transactions fresh-account {:init "15/10/2019" :end "23/10/2019"})))))
