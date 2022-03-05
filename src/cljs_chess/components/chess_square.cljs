(ns cljs-chess.components.chess-square)

(defn get-image
  [{:keys [piece owner]}]
  (str "images/" owner "-" piece ".svg"))

(defn chess-square
  [{:keys [dim id style piece]
    :or   {dim "100px"}}]
  (println "PIECE" piece)
  [:div.background-darkgrey.hover-icon {:style (merge {:width               dim
                                                       :height              dim
                                                       :float               "left"
                                                       :background-size     "80% 80%"
                                                       :background-position "center"
                                                       :background-repeat   "no-repeat"
                                                       :background-image    (str "url(" (get-image piece) ")")}
                                                 style)
                                        :id    id}])
