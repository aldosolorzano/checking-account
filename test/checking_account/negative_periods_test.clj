(ns checking-account.negative-periods-test
  (:require
   [clojure.test :refer :all]
   [checking-account.negative-periods :refer :all]
   [checking-account.date-helpers :as d]))

(deftest Abs-formatted
  (testing "Given a int or float, returns a formatted absolute value"
    (is (= (abs-formatted -20) "20.00"))
    (is (= (abs-formatted 28) "28.00"))))

(def txs [{:principal -200 :start "28/12/2012"}
          {:principal -28 :start "02/01/2013"}
          {:principal 35 :start "26/01/2013"}])

(def expected-txs (seq [{:principal -200 :start "28/12/2012" :end "01/01/2013" }
                        {:principal -28 :start "02/01/2013" :end "25/01/2013" }
                        {:principal 35 :start "26/01/2013"}]))

(deftest Add-end-date
  (testing "Add end date of the negative period using dec-day fn, except to last one"
    (is (= (add-end-date txs) expected-txs)))
  (testing "If start date equals nex-date, end should not apply dec-day"
    (let [txs [{:principal -200 :start "28/12/2012"} {:principal -15 :start "28/12/2012"}]
          expected-txs (seq [{:principal -200 :start "28/12/2012" :end "28/12/2012"}
                             {:principal -15 :start "28/12/2012"}])])
    (is (= (add-end-date txs) expected-txs))))

(def tx {:date (d/parse-date "28/12/2012") :amount 200})

(deftest Build-period
  (testing "Builds negative-periods body, and keeps an accum of current balance"
    (is (= (build-period tx) {:principal 200 :start "28/12/2012"}))
    (is (= (build-period tx {:principal 350}) {:principal 550 :start "28/12/2012"}))))

(def db-txs [{:date (d/parse-date "28/12/2012") :amount -200}
             {:date (d/parse-date "02/01/2013") :amount 172}
             {:date (d/parse-date "26/01/2013") :amount 63}])

(deftest Full-periods
  (testing "Calling b/build-balances and add-end-date"
    (is (= (full-periods []) '()))
    (is (= (full-periods db-txs) expected-txs))))

(defn expected-neg-periods [txs]
  (map #(update % :principal abs-formatted) txs))

(deftest Add-end-to-negative-txs
  (testing "Apply full-periods, filter negative balances and apply abs-format"
    (is (= (negative-periods []) '()))
    (is (= (negative-periods db-txs) (drop-last (expected-neg-periods expected-txs))))))


(def no-end-expected (seq [{:principal -200 :start "28/12/2012" :end "01/01/2013" }
                           {:principal -28 :start "02/01/2013"}]))

(deftest Add-end-to-negative-txs-exclude-end-date
  (testing "Apply full-periods, exclude last-end-date, negative balance"
    (is (= (negative-periods (pop db-txs)) (expected-neg-periods no-end-expected)))))
