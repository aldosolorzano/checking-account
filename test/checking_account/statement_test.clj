(ns checking-account.statement-test
  (:require [clojure.test :refer :all]
            [checking-account.statement :refer :all]
            [checking-account.db-fixture :as dbf]
            [checking-account.date_helpers :as d]))
(defn remove-atoms
  [statement]
  (reduce (fn [new tx]
    (let [date (first tx)
          txs (deref ((last tx) :transactions))]
      (conj new {date {:transactions txs}}))) {} statement))

(deftest Build-statement
  (testing "Get Balance"
    (is (= (remove-atoms (build-statement @dbf/transactions dbf/account)) (remove-atoms dbf/full-statement-no-balance)))))
