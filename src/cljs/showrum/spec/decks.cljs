(ns showrum.spec.decks
  (:require [cljs.spec :as s]))

(def slide-type? #{:type/main-header :type/header :type/bullets
                   :type/text :type/image :type/code})

(s/def :slide/order number?)
(s/def :slide/type slide-type?)
(s/def :slide/title string?)
(s/def :slide/bullets (s/coll-of string?))
(s/def :slide/text string?)
(s/def :slide/image string?)
(s/def :slide/notes string?)
(s/def ::basic-slide
  (s/keys :req [:slide/type :slide/title :slide/order]
          :opt [:slide/notes]))
(defmulti slide-type :slide/type)
(defmethod slide-type :type/main-header [_]
  ::basic-slide)
(defmethod slide-type :type/header [_]
  ::basic-slide)
(defmethod slide-type :type/bullets [_]
  (s/merge ::basic-slide (s/keys :req [:slide/bullets])))
(defmethod slide-type :type/text [_]
  (s/merge ::basic-slide (s/keys :req [:slide/text])))
(defmethod slide-type :type/image [_]
  (s/merge ::basic-slide (s/keys :req [:slide/image])))
(defmethod slide-type :type/code [_]
  (s/merge ::basic-slide (s/keys :req [:slide/code])))
(s/def ::slide (s/multi-spec slide-type :slide/type))

(s/def :deck/author string?)
(s/def :deck/date string?)
(s/def :deck/place string?)
(s/def :deck/title string?)
(s/def :deck/order number?)
(s/def :deck/slides (s/coll-of ::slide))
(s/def ::deck (s/keys :req [:deck/author :deck/date :deck/title]
                      :opt [:db/id :deck/slides :deck/order :deck/place]))

(s/def ::decks (s/+ (s/alt :slide ::slide :deck ::deck)))

