(ns checking-account.date-helpers
  (:require
   [java-time :as t]))

(def custom-formatter "dd/MM/yyyy")

(defn parse-date [date]
  (t/local-date custom-formatter date))

(defn unparse-date [date]
  (t/format custom-formatter date))

(defn valid-date? [date]
  (try (parse-date date)
    (catch Exception e false)))

(defn within-dates? [init end date]
  (let [init-p (parse-date init)
        end-p  (parse-date end)
        date-p (parse-date date)]
    (and (>= (t/time-between init-p date-p :days) 0) (>= (t/time-between date-p end-p :days) 0))))

(defn sort-by-date
  [transactions]
  (sort-by :date transactions))

(defn dec-day [date]
  (unparse-date (t/minus (parse-date date) (t/days 1))))
