(ns checking-account.balance-test
  (:require
   [clojure.test :refer :all]
   [checking-account.balance :refer :all]))

(deftest Format-float
  (testing "Format float numbers"
    (is (= (format-float 888.12343523) "888.12"))
    (is (= (format-float -51.888) "-51.89"))))

(def txs [{ :amount 128 } { :amount 1000 } { :amount -200 }])

(deftest Compute-balance
  (testing "Compute balance given array of hashes with a key :amount"
    (is (= (compute-balance []) 0))
    (is (= (compute-balance txs) (apply + (map :amount txs))))))

(defn builder-test
  ([{:keys [amount] }] {:accum amount})
  ([{:keys [amount]} las-tx] {:accum (+ amount (:accum las-tx))}))

(deftest Conj-results
  (testing "apply builder function to given tx & last tx from the results vector"
    (is (= (conj-results builder-test [] (first txs)) [{:accum 128}]))
    (is (= (conj-results builder-test [{:accum 128}] (second txs)) [{:accum 128} {:accum 1128}]))))

(defn builder-swop
  ([{:keys [amount] }] {:swop 2})
  ([{:keys [amount]} las-tx] {:swop 2}))

(deftest Build-balances
  (testing "Given a vector & a builder fn, builder's results is conj to a new vector"
    (is (= (build-balances txs builder-test) [{:accum 128} {:accum 1128} {:accum 928}]))
    (is (= (build-balances txs builder-swop) [{:swop 2} {:swop 2} {:swop 2}]))))
