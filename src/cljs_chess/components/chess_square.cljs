(ns cljs-chess.components.chess-square
  (:require [taoensso.timbre :refer-macros [infof]]))

(defn get-image
  [{:keys [piece owner]}]
  (str "images/" owner "-" piece ".svg"))

(defn chess-square
  [{:keys [dim id style piece on-click background-color]
    :or   {dim "100px"}}]
  (infof "PIECE %s ACTIVE? %s" piece active?)
  [:div.background-darkgrey.hover-icon.chess-square
   {:style    (merge {:width               dim
                      :height              dim
                      :float               "left"
                      :background-size     "80% 80%"
                      :background-position "center"
                      :background-repeat   "no-repeat"
                      :background-image    (str "url(" (get-image piece) ")")
                      :background-color    background-color}
                style)
    :on-click on-click
    :id       id}])
