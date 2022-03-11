(ns cljs-chess.components.drag-and-drop-board
  (:require [cljs-chess.components.drag-and-drop-square :as dnds]
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

(defn drag-and-drop-board
  [{:keys [rows cols tag square-size state on-drop item-type]
    :or   {square-size 100}}]
  (fn [{:keys [rows cols tag square-size state]
        :or   {square-size 100}}]
    [dnd-provider {:backend html5-backend}
     [:div.chess-board {:style {:min-width (str (* square-size cols) "px")}}
      (doall
        (for [row (range rows)]
          ^{:key (row-key tag row)}
          [:div.board-row
           (doall
             (for [col  (range cols)
                   :let [id  (cell-key tag row col)
                         loc [row col]]]
               ^{:key id}
               [dnds/drag-and-drop-square
                {:id      id
                 :accept  "KNIGHT"
                 :coords  loc
                 :on-drop (partial on-drop loc)
                 :piece   (when-let [current-piece (get state loc)]
                            [:f> (partial item-type current-piece)])}]))]))]]))
