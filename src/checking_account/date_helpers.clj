(ns checking-account.date_helpers
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

(defn within-dates?
  [init end date]
  (t/within? (t/interval (parse-to-date-time (parse-date init)) (parse-to-date-time (parse-date end)))
              (parse-to-date-time (parse-date date))))

(defn txs-within-dates
  [transactions init end]
  (flatten (filter (fn [tx] (within-dates? init end (first tx))) transactions)))

(defn sort-by-date
  [transactions]
  (sort-by :date < transactions))
