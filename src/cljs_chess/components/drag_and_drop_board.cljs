(ns cljs-chess.components.drag-and-drop-board
  (:require ["react-dnd" :as rdnd]
            ["react-dnd-html5-backend" :as dnd-backend]
            [cljs-chess.chess :as chess]
            [cljs-chess.components.drag-and-drop-square :as dnds]
            [reagent.core :as reagent]
            [taoensso.timbre :refer-macros [infof debugf]]))

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

(def DEFAULT-SQUARE-SIZE 60)

(defn drag-and-drop-board
  [{:keys [rows cols tag square-size state on-drop can-drop? item-type]
    :or   {square-size DEFAULT-SQUARE-SIZE
           can-drop?   (constantly true)}}]
  (fn [{:keys [rows cols tag square-size state can-drop?]
        :or   {square-size DEFAULT-SQUARE-SIZE
               can-drop?   (constantly true)}}]
    [dnd-provider {:backend html5-backend}
     [:div.chess-board {:style {:min-width (str (* square-size cols) "px")}}
      (doall
       (for [row (range rows)]
         ^{:key (row-key tag row)}
         [:div.board-row
          (doall
           (for [col  (range cols)
                 :let [id            (cell-key tag row col)
                       loc           [row col]
                       current-piece (get state loc)]]
             ^{:key id}
             [dnds/drag-and-drop-square
              {:id        id
               :accept    "PIECE"
               :dim       (str square-size "px")
               :coords    loc
               :on-drop   (partial on-drop loc)
               :can-drop? (partial can-drop? state loc)
               :piece     (when current-piece
                            [:f> (partial item-type current-piece)])}]))]))]]))
