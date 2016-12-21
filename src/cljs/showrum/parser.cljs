(ns showrum.parser
  (:require [clojure.string :refer [trim split-lines replace split]]))

(defn parse-deck [yaml]
  (let [[_ preamble] (re-matches #"---\n([\s\S]*)\n---[\s\S]*" yaml)
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
       (mapv #(-> % trim (replace #"\n" " ")))
       first))

(defn parse-slide [md]
  (let [md              (trim md)
        [_ main-header] (re-matches #"^# (.*)$" md)
        [_ header body] (re-matches #"(?m)^## (.*)$([\s\S]*)" md)
        bullets         (when-not (empty? body)
                          (parse-bullets (trim body)))
        text            (when-not (empty? body)
                          (parse-text (trim body)))
        image           (when-not (empty? body)
                          (parse-image (trim body)))]
    (cond-> {}
      main-header   (assoc :slide/type :type/main-header :slide/title main-header)
      header        (assoc :slide/type :type/header :slide/title header)
      (seq bullets) (assoc :slide/type :type/bullets :slide/bullets bullets)
      (seq text)    (assoc :slide/type :type/text :slide/text text)
      (seq image)   (assoc :slide/type :type/image :slide/image image))))

(defn parse-decks [doc]
  (let [deck (parse-deck doc)
        slides-docs (remove empty? (map trim (split doc #"---")))]
    (into [deck]
          (map #(parse-slide %) (rest slides-docs)))))
