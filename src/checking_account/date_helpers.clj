(ns checking-account.date-helpers
  (:require [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.format :as f]
            [clj-time.coerce :as c]))
; Date functions
(def custom-formatter (f/formatter "dd/MM"))

(defn parse-date
  [date]
  (c/to-long (f/parse custom-formatter date)))

(defn unparse-date
  [date]
  (f/unparse custom-formatter (c/from-long date)))

(defn parse-to-date-time
  [date]
  (t/date-time (t/year (c/from-long date)) (t/month (c/from-long date)) (t/day (c/from-long date))))

(defn valid-date?
  [date]
  (try (parse-date date)
    (catch Exception e false)))

(defn within-dates?
  [init end date]
  (t/within? (t/interval (parse-to-date-time (parse-date init)) (parse-to-date-time (parse-date end)))
              (parse-to-date-time (parse-date date))))

(defn sort-by-date
  [transactions]
  (sort-by :date < transactions))

(defn max-days-in-month
  [month]
  (let [months #{4, 5, 9, 11}
        max-day (condp = (contains? months month)
                  true 30
                  false (if (= 2 month) 28 31))]
    max-day))

(defn dec-day
  [date-string]
  (let [date (f/parse custom-formatter date-string)
        day (dec (t/day date))
        month (t/month date)
        year (t/year date)
        new-day (cond
                  (pos? day) day
                  (zero? day) (do (if (pos? (dec month))
                               (max-days-in-month (dec month))
                               (max-days-in-month 12)))) ;if january then go to december
        new-month (if (= day new-day)
                    month
                    (do (if (pos? (dec month)) (dec month) 12))) ;if january then go to december
        new-year (if (= month new-month) year (dec year))
        new-date (f/unparse custom-formatter (t/date-time new-year new-month new-day))]
    new-date))
