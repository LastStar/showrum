(ns showrum.parser)

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

(defn parse-slide [md]
  (let [[_ main-header] (re-matches #"^# (.*)$" md)
        [_ header body] (re-matches #"(?m)^## (.*)$([\s\S]*)" md)
        body            (when-not (empty? body) (clojure.string/trim body))
        bullets         (when body (mapv #(let [[_ text] (re-matches #"^\* (.*)" %)] text)
                                         (clojure.string/split-lines body)))]
    (let [slide {:slide/type  (if main-header
                                :type/main-header
                                (if body
                                  :type/bullets
                                  :type/header))
                 :slide/title (or main-header header)}]
      (if bullets
        (assoc slide :slide/bullets bullets)
        slide))))
