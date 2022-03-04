(ns cljs-chess.components.secondary-button)

(defn secondary-button
  [{:keys [on-click text style]}]
  [:input.btn-secondary {:type     "button"
                         :value    text
                         :style    (or style {})
                         :on-click on-click}])
