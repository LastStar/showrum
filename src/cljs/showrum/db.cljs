(ns showrum.db
  (:require [datascript.core :as d]))

(def conn
  (d/create-conn
   {:slide/bullets {:db/cardinality :db.cardinality/many}}))

(def data
  [{:db/id -1
    :deck/author  "Josef \"pepe\" Pospíšil"
    :deck/date "21. - 25. 11. 2016"}
   {:db/id -2
    :slide/order 1
    :slide/type :type/header
    :slide/title "Contemporary Frontend Development"}
   {:db/id -3
    :slide/order 2
    :slide/type :type/bullets
    :slide/title "Organization"
    :slide/bullets ["AM" "PM"]}
   {:db/id -4
    :slide/order 3
    :slide/type :type/bullets
    :slide/title "AM"
    :slide/bullets ["Theory" "Story"]}])

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

(defn bullets-for
  "Returns all bullets for slide"
  [id]
  (d/q
   '[:find ?si
     :in $ ?id
     :where
     [?id :slide/bullets ?si]]
   @conn id))

(defn deck
  "Returns deck with author and date"
  []
  (first (d/q
          '[:find ?da ?dd
            :where
            [?e :deck/author ?da]
            [?e :deck/date ?dd]]
          @conn)))

(defn init
  "Initializes the db"
  []
  (when-not (deck) (d/transact! conn data)))
