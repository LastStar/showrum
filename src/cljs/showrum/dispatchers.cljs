(ns showrum.dispatchers
  (:require
   [scrum.core :as scrum]
   [scrum.router.controller :as router]))

(defmulti initialized identity)
(defmulti current identity)
(defmulti search identity)

(defmethod initialized :init
  [_ _ db]
  (assoc db :gist false :db false :keyboard-loop false))

(defmethod initialized :keyboard-loop
  [_ _ db]
  (assoc db :keyboard-loop true))

(defmethod initialized :db
  [_ _ db]
  (assoc db :db true))

(defmethod initialized :gist
  [_ _ gist]
  (assoc gist :gist true))

(defmethod initialized :decks
  [_ [decks] db]
  (assoc db :decks decks))

(defmethod current :init
  [_ _ db]
  (assoc db :deck-id 1 :slide 1 :slides-count 0 :result 0))

(defmethod current :deck-id
  [_ [id] db]
  (assoc db :slide 1 :deck-id id))

(defmethod current :slide
  [_ [id] db]
  (assoc db :slide id))

(defmethod current :slides-count
  [_ [count] db]
  (assoc db :slides-count count))

(defmethod current :next-slide
  [_ _ db]
  (if (< (get db :slide)
         (get db :slides-count))
    (update db :slide inc)
    db))

(defmethod current :prev-slide
  [_ _ db]
  (if (> (get db :slide) 1)
    (update db :slide dec)
    db))

(defmethod search :init
  [_ _ db]
  (assoc db :active false :term "" :results [] :result 0))

(defmethod search :toggle-active
  [_ _ db]
  (update db :active not))

(defmethod search :term
  [_ [term] db]
  (assoc db :result 0 :term term))

(defmethod search :results
  [_ [results] db]
  (assoc db :results (vec results)))

(defmethod search :active-result
  [_ [index] db]
  (if (and (>= index 0)
           (< index (count (get db :results))))
    (assoc db :result index)
    db))

(defmethod search :next-result
  [_ _ db]
  (if (< (get db :result)
         (dec (count (get db :results))))
    (update db :result inc)
    db))

(defmethod search :prev-result
  [_ _ db]
  (if (> (get db :result) 0)
    (update db :result dec)
    db))

(defmethod search :deactivate-and-clear
  [_ _ db]
  (assoc db :term "" :active false))

(defonce reconciler
  (scrum/reconciler (atom {})
                    {:initialized initialized
                     :current current
                     :search search
                     :router router/control}))

(defn reconcile
  []
  (scrum/broadcast! reconciler :init)
  reconciler)
