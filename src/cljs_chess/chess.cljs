(ns cljs-chess.chess
  (:require [cljs-chess.utils.chess.board :as chess-board]
            [cljs-chess.chess-constants :refer [TURN-ORDER TWO-SQUARES-LEFT TWO-SQUARES-RIGHT PAWN-DIRECTION]]
            [cljs-chess.utils.chess.pieces :as chess-pieces]
            [cljs-chess.utils.chess.proposed-moves :as proposed-moves]
            [cljs-chess.utils.generic :refer [some-fn*]
             :refer-macros [with-log]]
            [cljs-chess.utils.geometry :as geom]
            [taoensso.timbre :refer-macros [debugf infof]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn next-player
  [player]
  (get TURN-ORDER player))

(defn move-piece!
  [state-ref {:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (chess-board/find-piece (proposed-moves/get-board proposed-move) piece)]
    (infof "Moving %s from %s to %s" piece old-loc new-loc)
    (swap! state-ref update-in [:board] dissoc old-loc)
    (swap! state-ref assoc-in [:board new-loc] (assoc piece :moved? true))
    (swap! state-ref assoc :active-player (next-player (chess-pieces/owner piece)))
    #_(cond
        (promotion? proposed-move) (promote-pawn! proposed-move)
        (castle? proposed-move)    (castle-rook! proposed-move))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Movement policy
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def valid-knight-movement?
  (partial proposed-moves/valid-jump? geom/L-movement?))

(def valid-king-movement?
  (partial proposed-moves/valid-slide? geom/single-square-move?))

(def valid-bishop-movement?
  (partial proposed-moves/valid-slide? geom/diagonal?))

(def valid-rook-movement?
  (partial proposed-moves/valid-slide? (some-fn* geom/horizontal?
                                                 geom/vertical?)))

(def valid-queen-movement?
  (partial proposed-moves/valid-slide? (some-fn* geom/horizontal?
                                                 geom/vertical?
                                                 geom/diagonal?)))

(defn valid-castle-movement?
  [proposed-move]
  {:pre [(chess-pieces/king? (proposed-moves/from-piece proposed-move))]}
  (let [delta (geom/delta (proposed-moves/to-location proposed-move)
                          (proposed-moves/from-location proposed-move))]
    (or (= [0  2] delta)
        (= [0 -2] delta))))

(defn lookup-castling-rook
  [proposed-move]
  {:pre [(chess-pieces/king? (proposed-moves/from-piece proposed-move))]}
  #_(infof "%s %s" [(geom/delta new-loc (chess-board/where-am-i state piece))
                    (chess-pieces/owner piece)]
           (chess-board/find-loc state [7 7]))
  (let [board (proposed-moves/get-board proposed-move)]
    (case [(geom/delta (proposed-moves/to-location proposed-move)
                       (proposed-moves/from-location proposed-move))
           (chess-pieces/owner (proposed-moves/from-piece proposed-move))]
      [TWO-SQUARES-LEFT  "white"] (chess-board/find-loc board [7 0])
      [TWO-SQUARES-RIGHT "white"] (chess-board/find-loc board [7 7])
      [TWO-SQUARES-LEFT  "black"] (chess-board/find-loc board [0 0])
      [TWO-SQUARES-RIGHT "black"] (chess-board/find-loc board [0 7])
      nil)))

(defn castling-blocked?
  [proposed-move]
  (->> (geom/path-between (proposed-moves/from-location proposed-move)
                          (proposed-moves/to-location proposed-move))
       (butlast)
       (chess-board/blockers (proposed-moves/get-board proposed-move))
       (seq)
       (some?)))

(defn castle-states
  [proposed-move]
  {:pre [(chess-pieces/king? (proposed-moves/from-piece proposed-move))]}
  (let [board      (proposed-moves/get-board proposed-move)
        from-loc   (proposed-moves/from-location proposed-move)
        from-piece (proposed-moves/from-piece proposed-move)
        to-loc     (proposed-moves/to-location proposed-move)
        locs       (geom/path-between from-loc to-loc)]
    (map (fn [loc]
           (-> board
               (dissoc from-loc)
               (assoc loc from-piece)))
         (concat locs
                 [to-loc
                  from-loc]))))

(declare check?)

(defn valid-castle?
  [{:keys [state piece new-loc] :as proposed-move}]
  {:pre [(chess-pieces/king? piece)]}
  (let [king piece
        rook (lookup-castling-rook proposed-move)]
    #_(infof "Castling rook %s" rook)
    #_(infof "Valid-castle-movement? %s" (valid-castle-movement? proposed-move))
    #_(infof "First king movement? %s First Rook movement? %s"
             (chess-pieces/first-move? king)
             (chess-pieces/first-move? rook))
    ;;(infof "Castle states: %s" (castle-states proposed-move))
    (and (valid-castle-movement? proposed-move)
         (chess-pieces/first-move? king)
         (chess-pieces/first-move? rook)
         (not (castling-blocked? proposed-move))
         (not-any? (partial check? king) (castle-states proposed-move)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pawn fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn valid-pawn-taking-movement?
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [board     (proposed-moves/get-board proposed-move)
        [old-loc] (chess-board/find-piece board piece)
        step      (get PAWN-DIRECTION (chess-pieces/owner piece))
        d         (map - new-loc old-loc step)]
    (or (= [0  1] d)
        (= [0 -1] d))))

(defn valid-pawn-take?
  [{:keys [state piece new-loc] :as proposed-move}]
  (and (valid-pawn-taking-movement? proposed-move)
       (proposed-moves/end-in-enemy-space? proposed-move)))

(defn valid-pawn-movement?
  [{:keys [state piece new-loc] :as proposed-move}]
  (true? (let [board     (proposed-moves/get-board proposed-move)
               [old-loc] (chess-board/find-piece board piece)
               step      (get PAWN-DIRECTION (chess-pieces/owner piece))]
           (or (and (= new-loc (map + old-loc step))
                    (chess-board/empty-square? board new-loc))
               (and (= new-loc (map + old-loc step step))
                    (chess-board/empty-square? board new-loc)
                    (chess-pieces/first-move? piece)
                    (not (proposed-moves/slide-blocked? proposed-move)))))))

(def MOVEMENT-POLICY
  {"rook"   (with-log valid-rook-movement?)
   "queen"  (with-log valid-queen-movement?)
   "king"   (some-fn* (with-log valid-king-movement?)
                      (with-log valid-castle?))
   "knight" (with-log valid-knight-movement?)
   "pawn"   (some-fn* (with-log valid-pawn-movement?)
                      (with-log valid-pawn-take?))
   "bishop" (with-log valid-bishop-movement?)})

(def ALWAYS-VALID-POLICY (constantly true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: Castling, check?, pawn promotion, en-passant, turn color indicator
(defn -valid-movement?
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [proposed-movement-valid? (get MOVEMENT-POLICY
                                      (chess-pieces/type
                                       (proposed-moves/from-piece proposed-move)))]
    (proposed-movement-valid? proposed-move)))

(def valid-movement?
  (memoize -valid-movement?))

(defn -allowed-action?
  [state new-loc piece monitor]
  (debugf "Checking if %s can be moved to %s" piece new-loc)
  (let [pred (every-pred (with-log proposed-moves/belongs-to-active-player?)
                         (with-log valid-movement?))
        result (pred {:state   state
                      :piece   piece
                      :new-loc new-loc})]
    (debugf "%s %s be moved to %s" piece (if result "can" "cannot") new-loc)
    result))

(def allowed-action?
  (memoize -allowed-action?))

(defn on-drop-handler
  "state-ref is a reference to state (to get the value, must deref)"
  [state-ref new-loc piece monitor]
  ;; Add an additional %s in the infof to print the board state
  (infof "Drop-handler: Dropping piece %s at Coordinate %s on Board" piece new-loc @state-ref)
  (move-piece! state-ref
               {:state   @state-ref
                :piece   piece
                :new-loc new-loc}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Check
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn threatened?
  [target-piece board aggressor-piece]
  (valid-movement? {:state   {:board board}
                    :piece   aggressor-piece
                    :new-loc (chess-board/where-am-i state target-piece)}))

(defn check?
  [piece board]
  {:pre [(chess-pieces/king? piece)]}
  (->> board
       (chess-board/get-pieces)
       (map (partial threatened? piece board))
       (some identity)
       (boolean)))
