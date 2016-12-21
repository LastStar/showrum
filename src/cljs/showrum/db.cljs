(ns showrum.db
  (:require [cljs.spec :as s]
            [datascript.core :as d]
            [showrum.parser :as parser]
            [showrum.spec]
            [goog.net.XhrIo :as xhrio]))

(defonce schema
  {:deck/slides {:db/cardinality :db.cardinality/many
                 :db/valueType   :db.type/ref}})

(defonce conn
  (d/create-conn schema))

(defonce initialized (atom nil))

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

(defn init-local-storage
  "Initializes the local storage"
  []
  (when-not (.getItem js/localStorage "current-deck")
    (.setItem js/localStorage "current-deck" 1))
  (when-not (.getItem js/localStorage "current-slide")
    (.setItem js/localStorage "current-slide" 1)))

(defn init
  "Initializes the db"
  [decks-data]
  (js/console.log decks-data)
  (d/reset-conn! conn (d/empty-db schema))
  (let [decks-data (parser/parse-decks (-> decks-data .-target .getResponse))]
    (if (s/valid? :showrum.spec/decks decks-data)
      (do
        (reset! initialized true)
        (d/transact! conn decks-data)
        (js/console.log (decks)))
      (do
        (js/console.log (s/explain :showrum.spec/decks decks-data))
        (.alert js/window "Attention! Bad data!")))))

(defn init-from-gist [gist]
  (xhrio/send gist init))
