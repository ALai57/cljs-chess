(ns cljs-chess.components.chess-board
  (:require [cljs-chess.components.chess-square :as cs]))

(defn- board-row
  [{:keys [cols tag row game-board]}]
  [:div.board-row {:style {:display  "block"
                           :overflow "auto"}}
   (for [x    (range cols)
         :let [t (str tag "-" x "-" row)]]
     ^{:key t} [cs/chess-square {:id    t
                                 :style {:border "4px solid black"}
                                 :piece (get-in game-board [row x])}])])

(defn chess-board
  [{:keys [rows cols tag game-board]}]
  [:div.chess-board
   (for [y    (range rows)
         :let [t (str tag "-row-" y)]]
     ^{:key t} [board-row {:cols       cols
                           :tag        tag
                           :row        y
                           :game-board game-board}])])
