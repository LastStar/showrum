(ns showrum.db
  (:require [datascript.core :as d]))

(def conn
  (d/create-conn))

(def deck
  [{:db/id -1
    :deck/author  "Josef \"pepe\" Pospíšil"}
   {:db/id -2
    :slide/order 1
    :slide/type :type/header
    :slide/title "Contemporary Frontend Development"}
   {:db/id -3
    :slide/order 2
    :slide/type :type/bullets
    :slide/title "Organization"
    :slide/items ["First" "Second"]}])

(defn init
  "Initializes the db"
  []
  (d/transact! conn deck))

(defn slides
  "Returns all slides"
  []
  (sort-by
   second
   (d/q
    '[:find ?e ?so ?sy ?st
      :where
      [?e :slide/order ?so]
      [?e :slide/type ?sy]
      [?e :slide/title ?st]]
    @conn)))

(defn items-for
  "Returns all items for slide"
  [id]
  (d/q
   '[:find ?si
     :in $ ?id
     :where
     [?id :slide/items ?si]]
   @conn id))

[?e :slide/items ?si]
(defn author
  "Returns deck author"
  []
  (d/q
   '[:find ?da
     :where
     [?e :deck/author ?da]]
   @conn))
