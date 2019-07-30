(ns checking-account.statement-test
  (:require
    [clojure.test :refer :all]
    [checking-account.statement :refer :all]
    [checking-account.date-helpers :as d]))

(deftest Add-balance
  (testing "Convert :amount key to balance, if to txs given, add them up")
    (is (= (add-balance {:amount 20}) {:amount 20 :balance 20}))
    (is (= (add-balance {:amount 20} {:balance 20}) {:amount 20 :balance 40})))

(deftest Desc-amount
  (testing "Given a tx with :description & amount, should return a string of the values"
    (is (= (desc-amount {:description "blah" :amount 208}) "blah 208"))))

(def txs [{:description "blah" :amount 208} {:description "desc" :amount 1 :balance 209}])

(deftest Date-tx-body
  (testing "Given a vector with [date txs], apply desc-amount to :txs and add day format balance"
    (is (= (date-tx-body ["12/12" txs]) {"12/12" '("blah 208" "desc 1") :balance "209.00"}))))

(def statement [["12/12" txs] ["12/13" txs]])

(deftest Flatten-description
  (testing "Given a matrix with [date txs], map with date-tx-body "
    (is (= (flatten-description []) '()))
    (is (= (flatten-description statement) (seq [{"12/12" ["blah 208" "desc 1"] :balance "209.00"}
                                                 {"12/13" ["blah 208" "desc 1"] :balance "209.00"}])))))

(def statement-date [["25/02/2019" "txs"] ["28/04/2019" "txs"] ["31/12/2019" "txs"]])

(deftest Interval-statement
  (testing "Get statement between dates"
    (is (= (interval-statement {:init "01/02/2019" :end "03/02/2019"} statement-date) {}))
    (is (= (interval-statement {:init "25/02/2019" :end "31/12/2019"} statement-date) (into {} statement-date)))
    (is (= (interval-statement {:init "25/02/2019" :end "28/04/2019"} statement-date) (into {} [["25/02/2019" "txs"] ["28/04/2019" "txs"]])))))

(def db-txs [{:date (d/parse-date "25/02/2019") :description "blah" :amount 208}
             {:date (d/parse-date "25/02/2019") :description "more" :amount 20}
             {:date (d/parse-date "28/04/2019") :description "desc" :amount -20}])

(def expected-full-statement (seq [{"25/02/2019" (seq ["blah 208" "more 20"]) :balance "228.00"}
                                   {"28/04/2019" (seq ["desc -20"]) :balance "208.00"}]))

(def expected-partial-statement (seq [{"28/04/2019" (seq ["desc -20"]) :balance "208.00"}]))

(deftest Build-statement
  ; Expects transactions sorted.by :date, to work as desire for the API
  (testing "Build full statement, group txs by date and filter txs within given dates"
    (is (= (build-statement db-txs {:init "01/02/2019" :end "03/02/2019"}) '()))
    (is (= (build-statement db-txs {:init "01/02/2019" :end "03/12/2019"}) expected-full-statement))
    (is (= (build-statement db-txs {:init "26/02/2019" :end "30/04/2019"}) expected-partial-statement))))
