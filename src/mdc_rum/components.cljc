(ns mdc-rum.components
  (:require [rum.core :as rum]
            [mdc-rum.core :as mdc]
            [mdc-rum.mixins :as mixins]))

(rum/defc text-field < mixins/attach-text-field rum/static
  [opts input-name label]
  (let [id    (keyword (str (name input-name) "-input"))
        value (:value opts)]
    [:div.mdc-text-field
     [:input.mdc-text-field__input (merge {:id id :name input-name} opts)]
     [:label.mdc-floating-label
      {:for   input-name}
      label]]))


(rum/defc textarea < mixins/attach-text-field rum/static
  [opts input-name label]
  (let [id (keyword (str (name input-name) "-multiline"))]
    [:div.mdc-text-field.mdc-text-field--textarea
     [:textarea.mdc-text-field__input (merge {:id id :name input-name :rows 12 :cols 60} opts)]
     [:label.mdc-text-field__label {:for input-name} label]]))


(rum/defc dialog-button < mixins/attach-ripple rum/static
  [opts label]
  [:button.mdc-button.mdc-button__dialog__footer_button opts label])


(rum/defc button < mixins/attach-ripple rum/static
  [opts label]
  [:button.mdc-button opts label])

(rum/defc secondary-button < mixins/attach-ripple rum/static
  [opts label]
  [:button.mdc-button.mdc-theme--secondary opts label])

(rum/defc raised-button < mixins/attach-ripple rum/static
  [opts label]
  [:button.mdc-button.mdc-button--raised opts label])

(rum/defc link-button < mixins/attach-ripple rum/static
  [opts label]
  [:a.mdc-button opts label])

(rum/defc dense-link-button < mixins/attach-ripple rum/static
  [opts label]
  [:a.mdc-button.mdc-button--dense opts label])

(rum/defc dense-button < mixins/attach-ripple rum/static
  [opts label]
  [:button.mdc-button.mdc-button--dense opts label])

(rum/defc card-button < mixins/attach-ripple rum/static
  [opts label]
  [:button.mdc-button.mdc-button--compact.mdc-card__action opts label])

(rum/defc card-link < mixins/attach-ripple rum/static
  [opts label]
  [:a.mdc-button.mdc-button--compact.mdc-card__action opts label])

(rum/defc icon-button < mixins/attach-ripple rum/static
  [opts label]
  [mdc/icon-link opts label])

(rum/defc checkbox
  [opts]
  [:div.mdc-checkbox
   [:input.mdc-checkbox__native-control (merge {:type "checkbox"} opts)]
   [:div.mdc-checkbox__background
    [:svg.mdc-checkbox__checkmark
     {:view-box "0 0 24 24"}
     [:path.mdc-checkbox__checkmark__path
      {:fill "none"
       :stroke "white"
       :d "M1.73,12.91 8.1,19.28 22.79,4.59"}]]]])

(rum/defc select < mixins/attach-select rum/static
  [{:keys [id label options value on-change] :as opts}]
  [:div.mdc-select
   [:select.mdc-select__native-control
    (dissoc opts :options :label)
    (for [[value label] options]
      [:option {:key (str id value) :value value} label])]
   [:label.mdc-floating-label label]
   [:div.mdc-line-ripple]])
