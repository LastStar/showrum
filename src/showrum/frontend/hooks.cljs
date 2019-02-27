(ns showrum.frontend.hooks
  (:require
   ["react" :as react]
   [hx.react :as hx]))

(defn <-derive
  "Takes an atom and a function that provides derivation of
  atom value. It returns the derivate and rerenders the component,
  if derivate changes."
  ;; If no function is passed in, we assume we want value of the atom
  ([a] (<-derive a identity []))
  ;; if no deps are passed in, we assume we only want to run
  ;; subscrib/unsubscribe on mount/unmount
  ([a f] (<-derive a f []))
  ([a f deps]
   ;; create a react/useState hook to track and trigger renders
   (let [[v u] (react/useState (f @a))]
     ;; react/useEffect hook to create and track the subscription to the iref
     (react/useEffect
      (fn []
        (let [id (gensym "<-derive")]
          (add-watch a id
                     ;; update the react state on each change
                     (fn [_ _ s s']
                       (let [od (f s)
                             nd (f s')]
                         (if-not (= od nd) (u nd)))))
          ;; return a function to tell react hook how to unsubscribe
          #(remove-watch a id)))
      ;; pass in deps vector as an array
      (clj->js deps))
     ;; return value of useState on each run
     v)))
