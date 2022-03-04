(ns cljs-chess.components.radio-group
  (:require [reagent-mui.components :refer [form-control
                                            form-label
                                            radio-group
                                            form-control-label
                                            radio]]
            [taoensso.timbre :refer-macros [info]]
            [reagent.core :as reagent]))

(defn handle-change
  [state event]
  ;;(info "Changing radio group!" (.. event -target -value))
  (let [new-value (.. event -target -value)]
    (reset! state new-value)
    new-value))

(defn basic-radio-group
  [{:keys [group-name state elements on-change]
    :or   {on-change identity}}]
  (fn []
    [form-control
     [form-label group-name]
     [radio-group {:value     @state
                   :on-change (fn [event]
                                (on-change (handle-change state event)))}
      (for [{:keys [value label]} elements]
        ^{:key label} [form-control-label {:value   value
                                           :label   label
                                           :control (reagent/as-element [radio])}])]]))
