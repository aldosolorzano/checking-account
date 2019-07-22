(ns checking-account.db-test
  (:import java.util.concurrent.Executors)
  (:require
   [clojure.test :refer :all]
   [checking-account.db :refer :all]
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

(def local-accounts [(get @accounts 100) (get @accounts 200)])

(defn call-create-tx []
  (create-transaction "_"  (rand-nth local-accounts) body-params))

(deftest Concurrency-test
  (testing "Correct id generation"
    (dothreads! call-create-tx :threads 100 :times 100)
    (is (= (count @transactions) (last (sort (flatten (map (comp deref :tx-ids) local-accounts))))))
    (is (= @tx-id (count @transactions)))))

(deftest Account-txs
  (println (account-txs @transactions (first local-accounts))))
