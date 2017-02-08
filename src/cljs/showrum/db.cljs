(ns showrum.db
  (:require [cljs.spec :as s]
            [datascript.core :as d]))

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
    '[:find ?e ?do ?dt :where [?e :deck/order ?do] [?e :deck/title ?dt]]
    @conn)))

(defn search
  "Searches in the texts for the term"
  [term]
  (let [term-patt (re-pattern (str "(?i).*\\b" term ".*"))]
    (d/q
     '[:find ?e ?dt ?so ?st
       :in $ ?term
       :where
       [?se :slide/title ?st]
       [?se :slide/order ?so]
       [?e :deck/slides ?se]
       [?e :deck/title ?dt]
       [(re-matches ?term ?st)]]
     @conn term-patt)))

(defn init
  "Initializes the db"
  [decks-data]
  (d/reset-conn! conn (d/empty-db schema))
  (if (s/valid? :showrum.spec/decks decks-data)
    (do
      (d/transact! conn decks-data)
      (js/console.log "Init db" decks-data))
    (do
      (js/console.log (s/explain :showrum.spec/decks decks-data))
      (.alert js/window "Attention! Bad data!"))))
