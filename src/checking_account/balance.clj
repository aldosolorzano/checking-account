(ns checking-account.balance)

(defn format-float [number]
  (format "%.2f"  (float number)))

(defn compute-balance [transactions]
  (apply + (map :amount transactions)))

(defn conj-results [builder results tx]
  (if (empty? results)
   (conj results (builder tx))
   (conj results (builder tx (last results)))))

; transactions -> vector/sequence of transactions.
; builder -> function that uses tx's body to return data. One or two args
; returns a vector of the data given by the builder function.
(defn build-balances [transactions builder]
  (reduce #(conj-results builder % %2) [] transactions))
