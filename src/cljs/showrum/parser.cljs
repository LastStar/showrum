(ns showrum.parser
  (:require [clojure.string :refer [trim split-lines replace]]))

(defn parse-deck [yaml]
  (let [[_ preamble] (re-matches #"(?m)---\n([\s\S]*)\n---" yaml)
        attrs [:deck/author :deck/date :deck/title]]
    (into {}
          (map
           (fn [attr]
             (let [path (re-pattern
                         (str "[\\s\\S]*" (name attr) ": (.*)[\\s\\S]*"))
                   value (second (re-matches path preamble))]
               [attr value])) attrs))))

(defn- parse-bullets [body]
  (mapv #(replace (trim (last %)) #"\n" " ")
        (re-seq #"^\*([^*]*)" body)))

(defn parse-slide [md]
  (let [md (trim md)
        [_ main-header] (re-matches #"^# (.*)$" md)
        [_ header body] (re-matches #"(?m)^## (.*)$([\s\S]*)" md)
        bullets         (when-not (empty? body)
                          (parse-bullets (trim body)))]
    (let [slide {:slide/type
                 (if main-header
                   :type/main-header
                   (if bullets
                     :type/bullets
                     :type/header))
                 :slide/title (or main-header header)}]
      (if bullets
        (assoc slide :slide/bullets bullets)
        slide))))
