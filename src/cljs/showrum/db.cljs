(ns showrum.db
  (:require [datascript.core :as d]))

(def conn
  (d/create-conn
   {:slide/items {:db/cardinality :db.cardinality/many}}))

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
    :slide/items ["AM" "PM"]}
   {:db/id -4
    :slide/order 3
    :slide/type :type/bullets
    :slide/title "AM"
    :slide/items ["Theory" "Story"]}])

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

(defn slide-by-order
  "Returns slide with given order"
  [order]
  (first
   (d/q
    '[:find ?e ?order ?sy ?st
      :in $ ?order
      :where
      [?e :slide/order ?order]
      [?e :slide/type ?sy]
      [?e :slide/title ?st]]
    @conn
    order)))

(defn items-for
  "Returns all items for slide"
  [id]
  (d/q
   '[:find ?si
     :in $ ?id
     :where
     [?id :slide/items ?si]]
   @conn id))

(defn author
  "Returns deck author"
  []
  (d/q
   '[:find ?da
     :where
     [?e :deck/author ?da]]
   @conn))
