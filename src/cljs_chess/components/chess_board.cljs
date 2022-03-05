(ns cljs-chess.components.chess-board
  (:require [cljs-chess.components.chess-square :as cs]
            [reagent.core :as reagent]
            [taoensso.timbre :refer-macros [infof]]))


(defn row-key
  [tag row]
  (str tag "-row-" row))

(defn cell-key
  [tag row col]
  (str tag "-" row "-" col))

(defn on-click-handler
  [active-square row col event]
  (infof "Moving active square from %s to %s"
    @active-square
    [row col])
  (reset! active-square [row col]))

(defn chess-board
  [{:keys [rows cols tag square-size game-board]
    :or   {square-size 100}}]
  (let [active-square (reagent/atom nil)]
    (fn []
      [:div.chess-board {:style {:min-width (str (* square-size cols) "px")}}
       (doall
         (for [row (range rows)]
           ^{:key (row-key tag row)}
           [:div.board-row
            (doall
              (for [col  (range cols)
                    :let [id (cell-key tag row col)]]
                ^{:key id}
                [cs/chess-square {:id               id
                                  :on-click         (partial on-click-handler active-square row col)
                                  :background-color (when (= @active-square [row col])
                                                      "red")
                                  :piece            (get-in game-board [row col])}]))]))])))
