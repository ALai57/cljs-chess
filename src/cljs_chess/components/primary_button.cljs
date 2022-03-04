(ns cljs-chess.components.primary-button)

(defn primary-button
  [{:keys [on-click text]}]
  [:input.btn.btn-primary.btn-block {:type    "button"
                                     :value   text
                                     :onClick on-click}])
