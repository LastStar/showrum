(ns scrum.router.controller)

(def initial-state
  {:route nil})

(defmulti control (fn [action] action))

(defmethod control :default [_ _ state]
  state)

(defmethod control :init []
  initial-state)

(defmethod control :push [_ [[route params query]] state]
  (assoc state :route [route params query]))
