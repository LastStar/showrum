(ns showrum.effects
  (:require [goog.net.XhrIo :as xhrio]
            [scrum.core :as scrum]
            [showrum.parser :as parser]
            [showrum.db :as db]))

(defn- db-initialized
  [r decks-response]
  (-> decks-response .-target .getResponse parser/parse-decks db/init)
  (scrum/dispatch! r :initialized :decks (db/decks))
  (scrum/dispatch! r :initialized :db))

(defn init-from-gist
  [r gist-uri]
  (xhrio/send gist-uri (partial db-initialized r)))
