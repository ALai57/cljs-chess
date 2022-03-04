(ns cljs-chess.components.hover-box)

(defn hover-box
  [{:keys [dim id style]
    :or   {dim "100px"}}]
  [:div.background-darkgrey.hover-icon {:style (merge {:width  dim
                                                       :height dim
                                                       :float  "left"}
                                                 style)
                                        :id    id}])
