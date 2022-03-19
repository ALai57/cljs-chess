(ns cljs-chess.components.drag-and-drop-board
  (:require ["react-dnd" :as rdnd]
            ["react-dnd-html5-backend" :as dnd-backend]
            [cljs-chess.chess :as chess]
            [cljs-chess.utils.geometry :as geom]
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
  [{:keys [rows cols tag square-size state on-drop item-type]
    :or   {square-size DEFAULT-SQUARE-SIZE
           can-drop?   (constantly true)}}]
  (fn [{:keys [rows cols tag square-size state]
        :or   {square-size DEFAULT-SQUARE-SIZE}}]
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


;; Chess related
(defn path-beetween
  [old-loc new-loc]
  (let [step (partial map + (geom/direction old-loc new-loc))
        n    (dec (geom/distance old-loc new-loc))]
    (->> (step old-loc)
         (iterate step)
         (take n)))
  #_(println "UNIT DIRECTION" (direction old-loc new-loc)))

(defn blockers
  [state item old-loc new-loc]
  (let [pts (geom/path-beetween old-loc new-loc)]
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

(defn end-in-enemy-space?
  "Does the move end in a space with an enemy?"
  [state item old-loc new-loc]
  (when-let [owner (chess/piece-owner (get state new-loc))]
    (not= (chess/piece-owner item)
          owner)))

(defn valid-knight-move?
  [old-loc new-loc]
  (= [1 2]
     (sort (map (comp geom/abs -) old-loc new-loc))))

(defn valid-pawn-take?
  [state item old-loc new-loc]
  (let [step (get PAWN-DIRECTION (chess/piece-owner item))
        d    (map - new-loc old-loc step)]
    ;;(println step d)
    (and (or (= [0  1] d)
             (= [0 -1] d))
         (end-in-enemy-space? state item old-loc new-loc))))

(defn empty-square?
  [state loc]
  (nil? (get state loc)))

(def UP [-1 0])
(def DOWN [1 0])

(def PAWN-DIRECTION
  {"black" DOWN
   "white" UP})

(def MOVEMENT-POLICY
  {"rook"   (fn [state item old-loc new-loc]
              (and (or (geom/horizontal? old-loc new-loc)
                       (geom/vertical? old-loc new-loc))
                   (not (slide-blocked? state item old-loc new-loc))
                   (valid-endpoint? state item old-loc new-loc)))
   "queen"  (fn [state item old-loc new-loc]
              (and (or (geom/horizontal? old-loc new-loc)
                       (geom/vertical? old-loc new-loc)
                       (geom/diagonal? old-loc new-loc))
                   (not (slide-blocked? state item old-loc new-loc))
                   (valid-endpoint? state item old-loc new-loc)))
   "king"   (fn [state item old-loc new-loc]
              (and (= 1 (geom/distance old-loc new-loc))
                   (valid-endpoint? state item old-loc new-loc)))
   "knight" (fn [state item old-loc new-loc]
              (and (valid-knight-move? old-loc new-loc)
                   (valid-endpoint? state item old-loc new-loc)))
   "pawn"   (fn [state item old-loc new-loc]
              (let [step (get PAWN-DIRECTION (chess/piece-owner item))]
                (or (and (= new-loc (map + old-loc step))
                         (empty-square? state new-loc))
                    (valid-pawn-take? state item old-loc new-loc))))
   "bishop" (fn [state item old-loc new-loc]
              (and (geom/diagonal? old-loc new-loc)
                   (not (slide-blocked? state item old-loc new-loc))
                   (valid-endpoint? state item old-loc new-loc)))})

(def ALWAYS-VALID-POLICY (constantly true))

(defn can-drop?
  [state new-loc item monitor]
  (let [[old-loc] (chess/lookup-piece state item)
        policy    (get MOVEMENT-POLICY (chess/piece-type item) ALWAYS-VALID-POLICY)]
    ;;(infof "Checking if %s can be moved from %s to %s" item old-loc new-loc)
    (policy state item old-loc new-loc)))

(defn on-drop-handler
  [state new-coords item monitor]
  ;; Add an additional %s to print the board state
  (infof "Drop-handler: Dropping Item %s at Coordinate %s on Board"
         item new-coords @state)
  (let [[old-coords piece :as result] (chess/lookup-piece @state item)]
    (chess/move-piece! state old-coords new-coords)))
