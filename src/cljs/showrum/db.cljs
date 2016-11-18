(ns showrum.db
  (:require [datascript.core :as d]
            [showrum.data :as data]))


(defonce schema
  {:deck/slides {:db/cardinality :db.cardinality/many
                 :db/valueType   :db.type/ref}})

(defonce conn
  (d/create-conn schema))

(defn deck
  "Returns deck with author and date"
  [id]
  (d/entity @conn id))

(defn decks
  "Returns all decks in db"
  []
  (sort-by
   second
   (d/q
    '[:find ?e ?do ?dt
      :where
      [?e :deck/order ?do]
      [?e :deck/title ?dt]]
    @conn)))

(defn init
  "Initializes the db"
  []
  (d/reset-conn! conn (d/empty-db schema))
  (d/transact! conn data/decks))
