(ns cljs-chess.components.grid
  (:require [cljs-chess.components.hover-box :as hb]))

(defn grid-row
  [{:keys [cols tag row]}]
  [:div.grid-row {:style {:display  "block"
                          :overflow "auto"}}
   (for [x    (range cols)
         :let [t (str tag "-" x "-" y)]]
     ^{:key t} [hb/hover-box {:id    t
                              :style {:margin       "1px"
                                      :border-style "solid"
                                      :border       "6px solid black"}}])])

(defn grid
  [{:keys [rows cols tag]}]
  [:div.grid
   (for [y    (range rows)
         :let [t (str tag "-row-" y)]]
     ^{:key t} [grid-row {:cols cols
                          :tag  tag
                          :row  y}])])
