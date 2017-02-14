(ns showrum.effects
  (:require [goog.net.XhrIo :as xhrio]
            [pushy.core :refer [set-token!]]
            [scrum.core :as scrum]
            [showrum.parser :as parser]
            [showrum.db :as db]))

(defn- db-initialized
  [r history decks-response]
  (-> decks-response .-target .getResponse parser/parse-decks db/init)
  (scrum/dispatch! r :initialized :decks (db/decks))
  (scrum/dispatch! r :initialized :db)
  (set-token! history "/presentation"))

(defn init-from-gist
  [r history gist-uri]
  (xhrio/send gist-uri (partial db-initialized r history)))
