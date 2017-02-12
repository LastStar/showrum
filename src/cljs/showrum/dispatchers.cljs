(ns showrum.dispatchers
  (:require [scrum.dispatcher :as dispatcher]))

;; defmacro
(defmulti initialized identity)
(defmulti current identity)
(defmulti search identity)

(defmethod initialized
  :init [_ _ db]
  (assoc db :initialized {:db false :keyboard-loop false}))

(defmethod initialized :keyboard-loop
  [_ _ db]
  (assoc-in db [:initialized :keyboard-loop] true))

(defmethod initialized :db
  [_ _ db]
  (assoc-in db [:initialized :db] true))

(defmethod initialized :clear-db
  [_ _ db]
  (assoc-in db [:initialized :db] false))

(defmethod current :init
  [_ _ db]
  (assoc db :current {:deck-id 1 :slide 1 :slides-count 0 :search-result 0}))

(defmethod current :deck-id
  [_ [id] db]
  (-> db
      (assoc-in [:current :slide] 1)
      (assoc-in [:current :deck-id] id)))

(defmethod current :slides-count
  [_ [count] db]
  (assoc-in db [:current :slides-count] count))

(defmethod current :next-slide
  [_ _ db]
  (if (< (get-in db [:current :slide])
         (get-in db [:current :slides-count]))
    (update-in db [:current :slide] inc)
    db))

(defmethod current :prev-slide
  [_ _ db]
  (if (> (get-in db [:current :slide]) 1)
    (update-in db [:current :slide] dec)
    db))

(defmethod search :init
  [_ _ db]
  (assoc db :search {:active false :term "" :results []}))

(defmethod search :toggle-active
  [_ _ db]
  (update-in db [:search :active] not))

(defmethod search :term
  [_ [term] db]
  (-> db
      (assoc-in [:current :search-result] 0)
      (assoc-in [:search :term] term)))

(defmethod search :results
  [_ [results] db]
  (assoc-in db [:search :results] (vec results)))

(defmethod search :active-result
  [_ [index] db]
  (if (and (>= index 0)
           (< index (count (get-in db [:search :results]))))
    (assoc-in db [:current :search-result] index)
    db))

(defmethod search :next-result
  [_ _ db]
  (if (< (get-in db [:current :search-result])
         (dec (count (get-in db [:search :results]))))
    (update-in db [:current :search-result] inc)
    db))

(defmethod search :prev-result
  [_ _ db]
  (if (> (get-in db [:current :search-result]) 0)
    (update-in db [:current :search-result] dec)
    db))

(defmethod search :activate-result
  [_ _ db]
  (let [[deck-id _ slide _] (get (get-in db [:search :results])
                                 (get-in db [:current :search-result]))]
    (js/console.log "activating " deck-id slide)
    (-> db
        (assoc-in [:current :deck-id] deck-id)
        (assoc-in [:current :slide] slide)
        (assoc-in [:search :active] false))))

(dispatcher/register! :initialized initialized)
(dispatcher/register! :current current)
(dispatcher/register! :search search)

(defonce init-dispatchers
  (dispatcher/broadcast! :init))
