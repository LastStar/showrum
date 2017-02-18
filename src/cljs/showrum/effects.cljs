(ns showrum.effects
  (:require [goog.net.XhrIo :as xhrio]
            [pushy.core :refer [set-token!]]
            [scrum.core :as scrum]
            [showrum.parser :as parser]
            [showrum.db :as db]))

(defn- db-initialized
  [reconciler history decks-response]
  (-> decks-response .-target .getResponse parser/parse-decks db/init)
  (scrum/dispatch! reconciler :initialized :decks (db/decks))
  (scrum/dispatch! reconciler :initialized :db)
  (set-token! history "/presentation"))

(defn init-from-gist
  [reconciler history gist-uri]
  (scrum/dispatch! reconciler :initialized :gist)
  (xhrio/send gist-uri (partial db-initialized reconciler history)))

(defn search
  [reconciler term]
  (scrum/dispatch! reconciler :search :results (db/search term)))

(defn go-home
  [reconciler history]
  (set-token! history "/")
  (scrum/dispatch! reconciler :router :push [:index nil nil]))
