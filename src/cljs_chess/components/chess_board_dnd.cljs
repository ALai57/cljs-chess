(ns cljs-chess.components.chess-board-dnd
  (:require [cljs-chess.components.chess-square-dnd :as cs]
            [cljs-chess.chess :as chess]
            [reagent.core :as reagent]
            [taoensso.timbre :refer-macros [infof debugf]]
            ["react-dnd" :as rdnd]
            ["react-dnd-html5-backend" :as dnd-backend]))

(defn row-key
  [tag row]
  (str tag "-row-" row))

(defn cell-key
  [tag row col]
  (str tag "-" row "-" col))

(def dnd-provider
  (reagent/adapt-react-class rdnd/DndProvider))

(def html5-backend
  dnd-backend/HTML5Backend)

(defn on-drop-handler
  [state new-coords item monitor]
  (infof "Drop-handler: Dropping Item %s at Coordinate %s on Board %s"
    item new-coords @state)
  (let [[old-coords piece :as result] (chess/lookup-piece state item)]
    (chess/move-piece! state old-coords new-coords)))

(defn chess-board
  [{:keys [rows cols tag square-size game-board on-drop]
    :or   {square-size 100}}]
  (fn [{:keys [rows cols tag square-size game-board]
        :or   {square-size 100}}]
    [dnd-provider {:backend html5-backend}
     [:div.chess-board {:style {:min-width (str (* square-size cols) "px")}}
      (doall
        (for [row (range rows)]
          ^{:key (row-key tag row)}
          [:div.board-row
           (doall
             (for [col  (range cols)
                   :let [id (cell-key tag row col)]]
               ^{:key id}
               [cs/chess-square-dnd
                {:id      id
                 :coords  [row col]
                 :on-drop (partial on-drop [row col])
                 :piece   (get game-board [row col])}]))]))]]))
