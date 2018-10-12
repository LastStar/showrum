(ns mdc-rum.core)

(def ^:const top-app-bar :header.mdc-top-app-bar.mdc-top-app-bar--fixed)

(def ^:const top-app-bar-row :div.mdc-top-app-bar__row)

(def ^:const top-app-bar-section-start :section.mdc-top-app-bar__section.mdc-top-app-bar__section--align-start)

(def ^:const top-app-bar-section-end :section.mdc-top-app-bar__section.mdc-top-app-bar__section--align-end)

(def ^:const top-app-bar-title :span.mdc-top-app-bar__title)

(def ^:const adjust-fixed-toolbar [:div.mdc-toolbar-fixed-adjust])

(def ^:const layout-grid :div.mdc-layout-grid)

(def ^:const layout-grid-inner :div.mdc-layout-grid__inner)

(def ^:const layout-cell-1 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-1)

(def ^:const layout-cell-2 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-2)

(def ^:const layout-cell-3 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-3)

(def ^:const layout-cell-4 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-4)

(def ^:const layout-cell-5 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-5)

(def ^:const layout-cell-6 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-6)

(def ^:const layout-cell-7 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-7)

(def ^:const layout-cell-8 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-8)

(def ^:const layout-cell-9 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-9)

(def ^:const layout-cell-10 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-10)

(def ^:const layout-cell-11 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-11)

(def ^:const layout-cell-12 :div.mdc-layout-grid__cell.mdc-layout-grid__cell--span-12)

(def ^:const section-elevation-1 :section.mdc-elevation--z1)

(def ^:const section-elevation-3 :section.mdc-elevation--z3)

(def ^:const section-elevation-9 :section.mdc-elevation--z9)

(def ^:const dialog :aside.mdc-dialog)

(def ^:const dialog-surface :div.mdc-dialog__surface)

(def ^:const dialog-header :header.mdc-dialog__header)

(def ^:const dialog-header-title :h2.mdc-dialog__header__title)

(def ^:const dialog-body :div.mdc-dialog__body)

(def ^:const dialog-footer :footer.mdc-dialog__footer)

(def ^:const dialog-backdrop :div.mdc-dialog__backdrop)

(def ^:const short-form-field :div.mdc-form-field.short)

(def ^:const select :select.mdc-select)

(def ^:const option :option.mdc-list-item)

(def ^:const pw-validation-message :p.mdc-text-field-helptext.mdc-text-field-helptext--persistent.mdc-text-field-helptext--validation-msg)

(def ^:const drawer :aside.mdc-drawer)

(def ^:const drawer-content :nav.mdc-drawer__content)

(def ^:const unordered-list :ul.mdc-list)

(def ^:const list-item :li.mdc-list-item)

(def ^:const form-field :div.mdc-form-field)

(def ^:const nav-list :nav.mdc-list)

(def ^:const a-list-item :a.mdc-list-item)

(def ^:const list-item-icon :i.material-icons.mdc-list-item__start-detail)

(def ^:const item-selected :mdc-permanent-drawer--selected)

(def ^:const list-divider [:li {:role "separator" :class "mdc-list-divider"}])

(def ^:const card :div.mdc-card)

(def ^:const card-media :div.mdc-card__media)

(def ^:const card-primary :section.mdc-card__primary)

(def ^:const card-large-title :div.mdc-card__title.mdc-card__title--large)

(def ^:const card-supporting-text :section.mdc-card__supporting-text)

(def ^:const card-title :div.mdc-card__title)

(def ^:const card-actions :div.mdc-card__actions)

(def ^:const tab-bar :nav.mdc-tab-bar)

(def ^:const tab :a.mdc-tab)

(def ^:const tab-indicator :span.mdc-tab-bar__indicator)

(def ^:const active-tab :a.mdc-tab.mdc-tab--active)

(def ^:const icon-menu [:a.material-icons "menu"])

(def ^:const icon-link :a.material-icons)

(def ^:const icon :span.material-icons)

(def ^:const typo-headline-6 :div.mdc-typography--headline6)

(def ^:const typo-headline-5 :div.mdc-typography--headline5)

(def ^:const typo-headline-4 :div.mdc-typography--headline4)

(def ^:const typo-headline-3 :div.mdc-typography--headline3)

(def ^:const typo-headline-2 :div.mdc-typography--headline2)

(def ^:const typo-headline-1 :div.mdc-typography--headline1)

(def ^:const typo-display-4 :div.mdc-typography--display4)

(def ^:const typo-display-3 :div.mdc-typography--display3)

(def ^:const typo-display-2 :div.mdc-typography--display2)

(def ^:const typo-display-1 :div.mdc-typography--display1)

(def ^:const typo-title :div.mdc-typography--title)

(def ^:const typo-body-1 :p.mdc-typography--body1)

(def ^:const typo-body-2 :p.mdc-typography--body2)
