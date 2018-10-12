(ns mdc-rum.mixins
  (:require [rum.core :as rum]
            ["@material/ripple" :as ripple]
            ["@material/select" :as select]
            ["@material/textfield" :as text-field]
            [mdc-rum.core :as mdc]))


(def attach-text-field
  {:did-mount (fn [state]
                (-> state rum/dom-node text-field/MDCTextField.attachTo)
                state)})


(def attach-select
  {:did-mount (fn [state]
                (-> state rum/dom-node select/MDCSelect.attachTo)
                state)})


(def attach-ripple
  {:did-mount (fn [state]
                (-> state rum/dom-node ripple/MDCRipple.attachTo)
                state)})
