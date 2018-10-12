(ns showrum.frontend.presenter.db
  (:require [cljs.spec :as s]
            [datascript.core :as d]
            [showrum.spec]))

(defonce schema
  {:deck/slides {:db/cardinality :db.cardinality/many
                 :db/valueType   :db.type/ref}})

(defonce conn
  (d/create-conn schema))

(defn decks
  "Returns all decks in db"
  []
  (mapv
   #(d/entity @conn (first %))
   (sort-by second
            (d/q '[:find ?e :where [?e :deck/order ?do]] @conn))))

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
       [?e  :deck/slides ?se]
       [?e  :deck/title ?dt]
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
