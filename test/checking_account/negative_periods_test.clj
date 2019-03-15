(ns checking-account.negative-periods-test
  (:require [clojure.test :refer :all]
            [checking-account.negative-periods :refer :all]
            [checking-account.db-fixture :as dbf]
            [checking-account.db :as db]))

(deftest Add-end-to-negative-txs
  (testing "Add end date to negative-periods"
    (is
     (= (add-end-to-negative-txs
         dbf/no-end-negative dbf/account-balance
         (last (db/account-txs @dbf/transactions dbf/account)))
        dbf/negative-period))
    (is
     (= (add-end-to-negative-txs
         dbf/no-end-negative -38.57
         (last (db/account-txs @dbf/transactions dbf/neg-account)))
        dbf/negative-period-debt))))
