(ns showrum.parser)

(defn parse [md]
  (let [[_ preamble] (re-matches #"(?m)---\n([\s\S]*)\n---" md)]
    (let [[_ author] (re-matches #"[\s\S]*author: (.*)[\s\S]*" preamble)
          [_ date]   (re-matches #"[\s\S]*date: (.*)[\s\S]*" preamble)
          [_ title]  (re-matches #"[\s\S]*title: (.*)[\s\S]*" preamble)]
      [{:deck/author author
        :deck/date date
        :deck/title title}])))
