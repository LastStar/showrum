(ns showrum.parser
  (:require [clojure.string :refer [trim split-lines replace split]]))

(defn parse-preamble [yaml]
  (let [[_ preamble] (re-matches #"---\n([\s\S]*)\n---\n[\s\S]*" yaml)
        attrs [:deck/author :deck/date :deck/title]]
    (into {}
          (map
           (fn [attr]
             (let [path (re-pattern
                         (str "[\\s\\S]*" (name attr) ": (.*)[\\s\\S]*"))
                   value (second (re-matches path preamble))]
               [attr value])) attrs))))

(defn- parse-bullets [body]
  (->> body
       (re-seq #"^\*([^*]*)")
       (mapv #(-> % last trim (replace #"\n" " ")))))

(defn- parse-text [body]
  (->> body
       (re-seq #"^[^*^!]*$")
       (mapv #(-> % trim (replace #"\n" " ")))
       first))

(defn- parse-image [body]
  (->> body
       (re-seq #"^\!.*$")
       (mapv #(-> % trim (replace #"\n" "")))
       first))

(defn- parse-code [body]
  (->> body
       (re-seq #"```([^`]*)```")
       (mapv #(-> (last %) trim))
       first))

(defn parse-slide [md order]
  (let [md          (trim md)
        [_ h1]      (re-matches #"^# (.*)$" md)
        [_ h2 body] (re-matches #"(?m)^## (.*)$([\s\S]*)" md)
        bullets     (when-not (empty? body)
                      (parse-bullets (trim body)))
        text        (when-not (empty? body)
                      (parse-text (trim body)))
        image       (when-not (empty? body)
                      (parse-image (trim body)))
        code        (when-not (empty? body)
                      (parse-code (trim body)))]
    (cond-> {:slide/order order}
      h1            (assoc :slide/type :type/main-header :slide/title h1)
      h2            (assoc :slide/type :type/header :slide/title h2)
      (seq bullets) (assoc :slide/type :type/bullets :slide/bullets bullets)
      (seq text)    (assoc :slide/type :type/text :slide/text text)
      (seq image)   (assoc :slide/type :type/image :slide/image image)
      (seq code)    (assoc :slide/type :type/code :slide/code code))))

(defn parse-deck
  ([doc order]
   (let [slides (vec (map-indexed
                      (fn [i item] (parse-slide item (inc i)))
                      (rest (remove empty? (map trim (split doc #"\n---\n"))))))]
     (assoc (parse-preamble doc)
            :deck/order order
            :deck/slides slides)))
  ([doc] (parse-deck doc 1)))

(defn parse-decks [doc]
  (let [deck-docs (split doc #"\n===\n")]
    (flatten
     (map-indexed
      (fn [i deck-doc] (parse-deck (trim deck-doc) (inc i)))
      deck-docs))))
