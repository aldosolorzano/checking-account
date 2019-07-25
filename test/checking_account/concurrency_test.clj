(ns checking-account.concurrency-test
  (:import java.util.concurrent.Executors)
  (:require
   [clojure.test :refer :all]
   [checking-account.db :as db]
   [checking-account.date-helpers :as d]))

(def thread-pool
  (Executors/newFixedThreadPool
  (+ 2 (.availableProcessors (Runtime/getRuntime)))))

(defn dothreads!
  [f & {thread-count :threads
        exec-count   :times
        :or {thread-count 1 exec-count 1}}]
  (dotimes [t thread-count]
    (.submit thread-pool
             #(dotimes [_ exec-count] (f)))))

(def body-params {:amount 100 :date "28/08/2019" :type "deposit" :description "blaj"})

(def local-accounts [(get @db/accounts 100) (get @db/accounts 200)])

(defn call-create-tx []
  (db/create-transaction "_"  (rand-nth local-accounts) body-params))

(defn duplicates? [xs]
  (not= (count (distinct xs)) (count xs)))

(defn ownership-of-txs [{:keys [id tx-ids]}]
  (empty? (->> (map #(:account (get @db/transactions %)) @tx-ids)
               (filter #(not= id %)))))

(def tx-ids-in-accounts (flatten (map (comp deref :tx-ids) local-accounts)))

(deftest Concurrency-test
  (testing "No tx-id duplicates & every tx-id must exists in @transactions"
    (dothreads! call-create-tx :threads 100 :times 100)
    (is (false? (duplicates? tx-ids-in-accounts)))
    (is (= (empty? (filter nil? (map #(get @db/transactions %) tx-ids-in-accounts))))))

  (testing "Match account id with account tx holder"
    (is (true? (ownership-of-txs (first local-accounts))))
    (is (true? (ownership-of-txs (second local-accounts))))))
