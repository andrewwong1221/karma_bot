(ns karma-bot.db
  (:require [clojure.java.jdbc :as sql])
  (:gen-class))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})
(def testdata
  {:id "Andrew"
   :val "300"})
(defn create-db []
  "Creates the database required to store the values"
  (try (sql/with-connection db
         (sql/create-table :karma
                           [:id    :text]
                           [:value :integer]))
       (catch Exception e (println e))))

(defn loadData []
  "Loads all the id value data pairing into a map"
  (try (sql/with-connection db)
       (sql/with-query-results rows
         ["SELECT * FROM karma"]
         rows)
       (catch Exception e (println e))))

(defn storeData [data]
  "Saves data map into database"
  (try (sql/with-connection db)
       (sql/update-or-insert-values :karma [] data)
       (catch Exception e (println e))))
