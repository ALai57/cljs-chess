(ns cljs-chess.components.side-menu
  (:require [reagent-mui.components :refer [drawer list list-item
                                            list-item-icon list-item-text
                                            button]]
            [cljs-chess.components.radio-group :refer [basic-radio-group]]
            [reagent-mui.icons.settings :refer [settings]]
            [taoensso.timbre :refer-macros [info]]
            [re-frame.core :refer [dispatch]]
            [reagent.core :as reagent]))

(defn keydown?
  [event]
  (= "keydown" (.-type event)))

(defn toggle-drawer
  [ratom open]
  (fn [event]
    (when-not (keydown? event)
      (reset! ratom open))))

(defn drawer-contents
  [{:keys [notification-type]}]
  [list
   [list-item
    [basic-radio-group {:group-name "Notification Settings"
                        :state      notification-type
                        :on-change  (fn [new-value]
                                      (dispatch [:change-notification-type (keyword new-value)]))
                        :elements   [{:value "modal"
                                      :label "Modal"}
                                     {:value "snackbar"
                                      :label "Snackbar"}]}]]])

(defn side-menu
  [{:keys [expand-button notification-type]}]
  (let [showing (reagent/atom false)
        current-notification-settings (reagent/atom notification-type)]
    (fn []
      [:div
       [expand-button {:on-click (toggle-drawer showing true)}]
       [:div
        [drawer {:anchor             "left"
                 :open               @showing
                 :variant            "temporary"
                 :transitionDuration 500
                 :on-close           (toggle-drawer showing false)}
         [drawer-contents {:notification-type current-notification-settings}]]]])))
