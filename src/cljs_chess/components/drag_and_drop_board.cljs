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

(defn drag-and-drop-board
  [{:keys [rows cols tag square-size state on-drop item-type]
    :or   {square-size 100
           can-drop?   (constantly true)}}]
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
                   :let [id            (cell-key tag row col)
                         loc           [row col]
                         current-piece (get state loc)]]
               ^{:key id}
               [dnds/drag-and-drop-square
                {:id        id
                 :accept    "KNIGHT"
                 :coords    loc
                 :on-drop   (partial on-drop loc)
                 :can-drop? (partial can-drop? state loc)
                 :piece     (when current-piece
                              [:f> (partial item-type current-piece)])}]))]))]]))


;; Chess related
(defn horizontal?
  [[y1 x1 :as old-loc]
   [y2 x2 :as new-loc]]
  (= y1 y2))

(defn vertical?
  [[y1 x1 :as old-loc]
   [y2 x2 :as new-loc]]
  (= x1 x2))

(defn diagonal?
  [[y1 x1 :as old-loc]
   [y2 x2 :as new-loc]]
  (->> (map (comp abs -) old-loc new-loc)
       (apply =)))

(defn abs
  [x]
  (if (pos? x) x (- x)))

(defn direction
  [old-loc new-loc]
  (let [v (map - new-loc old-loc)]
    (map (fn [x] (/ x (apply max (map abs v)))) v)))

(defn distance
  [[y1 x1 :as old-loc]
   [y2 x2 :as new-loc]]
  (cond
    (horizontal? old-loc new-loc) (abs (- x1 x2))
    (vertical?   old-loc new-loc) (abs (- y1 y2))
    (diagonal?   old-loc new-loc) (abs (- y1 y2))
    :else 0))

(defn path-beetween
  [old-loc new-loc]
  (let [step (partial map + (direction old-loc new-loc))
        n    (dec (distance old-loc new-loc))]
    (->> (step old-loc)
         (iterate step)
         (take n)))
  #_(println "UNIT DIRECTION" (direction old-loc new-loc)))

(defn blockers
  [state item old-loc new-loc]
  (let [pts (path-beetween old-loc new-loc)]
    ;;(println pts "BETWEEN" old-loc new-loc)
    (reduce (fn [acc pt]
              (if-let [blocker (chess/lookup-loc state pt)]
                (conj acc blocker)
                acc))
            #{}
            pts)))

(defn slide-blocked?
  [state item old-loc new-loc]
  (seq (blockers state item old-loc new-loc)))

(defn valid-endpoint?
  [state item old-loc new-loc]
  (not= (chess/piece-owner item)
        (chess/piece-owner (get state new-loc))))


(def MOVEMENT-POLICY
  {"rook" (fn [state item old-loc new-loc]
            (and (or (horizontal? old-loc new-loc)
                     (vertical? old-loc new-loc))
                 (not (slide-blocked? state item old-loc new-loc))
                 (valid-endpoint? state item old-loc new-loc)))
   "bishop" (fn [state item old-loc new-loc]
              (and (diagonal? old-loc new-loc)
                   (not (slide-blocked? state item old-loc new-loc))
                   (valid-endpoint? state item old-loc new-loc)))})

(defn can-drop?
  [state new-loc item monitor]
  (let [[old-loc] (chess/lookup-piece state item)
        policy    (get MOVEMENT-POLICY (chess/piece-type item) (constantly true))]
    ;;(infof "Checking if %s can be moved from %s to %s" item old-loc new-loc)
    (policy state item old-loc new-loc)))

(defn on-drop-handler
  [state new-coords item monitor]
  (infof "Drop-handler: Dropping Item %s at Coordinate %s on Board"
         item new-coords @state)
  #_(infof "Drop-handler: Dropping Item %s at Coordinate %s on Board %s"
           item new-coords @state)
  (let [[old-coords piece :as result] (chess/lookup-piece @state item)]
    (chess/move-piece! state old-coords new-coords)))
