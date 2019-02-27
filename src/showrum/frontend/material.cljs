(ns showrum.frontend.material
  (:require
   ["react" :as react]
   ["@material/textfield" :as text-field]
   ["@material/ripple" :as ripple]
   [hx.react :as hx]
   [hx.hooks :as hooks]))


(hx/defnc FloatingLabel [{:keys [input-name label]}]
  [:label 
   {:class "mdc-floating-label" :for input-name}
   label])

(hx/defnc TextField [props]
  (let [ref (react/useRef nil)]
    (hooks/<-effect (fn []
                      (when (.-current ref)
                        (text-field/MDCTextField.attachTo (.-current ref)))
                      #()))
    [:div
     {:class "mdc-text-field" :ref ref}
     [:input
      (merge {:class "mdc-text-field__input"}
             (dissoc props :children))]
     (:children props)]))

(hx/defnc Button [props]
  (let [ref (react/useRef nil)]
    (hooks/<-effect (fn []
                      (when (.-current ref)
                        (ripple/MDCRipple.attachTo (.-current ref)))
                      #()))
    [:button
     (merge {:class "mdc-button" :ref ref}
            (dissoc props :children))
     (:children props)]))
