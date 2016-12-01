(ns showrum.spec
  (:require
   [cljs.spec :as s]))

(def slide-type? #{:type/main-header :type/header :type/bullets})

(s/def :db/id number?)

(s/def :deck/author string?)
(s/def :deck/date string?)
(s/def :deck/place string?)
(s/def :deck/title string?)
(s/def :deck/order number?)
(s/def :deck/slides (s/coll-of :db/id))
(s/def ::deck (s/keys :req [:db/id :deck/author :deck/date :deck/place :deck/title :deck/order :deck/slides]))

(s/def :slide/order number?)
(s/def :slide/type slide-type?)
(s/def :slide/title string?)
(s/def :slide/bullets (s/coll-of string?))
(s/def :slide/notes string?)
(s/def ::slide (s/keys :req [:db/id :slide/order :slide/type :slide/title]
                       :opt [:slide/bullets :slide/notes]))

(s/def ::decks (s/* (s/alt :slide ::slide :deck ::deck)))
