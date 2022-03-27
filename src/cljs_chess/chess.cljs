(ns cljs-chess.chess
  (:require [cljs-chess.utils.chess.board :as chess-board]
            [cljs-chess.chess-constants :refer [TURN-ORDER TWO-SQUARES-LEFT TWO-SQUARES-RIGHT PAWN-DIRECTION]]
            [cljs-chess.utils.chess.pieces :as chess-pieces]
            [cljs-chess.utils.chess.proposed-moves :as proposed-moves]
            [cljs-chess.utils.generic :refer [some-fn* update-when]]
            [cljs-chess.utils.geometry :as geom]
            [taoensso.timbre :refer-macros [infof]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn next-player
  [player]
  (get TURN-ORDER player))

(defn move-piece!
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (chess-board/find-piece @state piece)]
    (infof "Moving %s from %s to %s" piece old-loc new-loc)
    (swap! state dissoc old-loc)
    (swap! state assoc
           new-loc        (assoc piece :moved? true)
           :active-player (next-player (chess-pieces/owner piece)))
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
  [{:keys [state piece new-loc] :as proposed-move}]
  {:pre [(chess-pieces/king? piece)]}
  (let [delta (geom/delta new-loc (chess-board/where-am-i state piece))]
    (or (= [0  2] delta)
        (= [0 -2] delta))))

(defn lookup-castling-rook
  [{:keys [state piece new-loc] :as proposed-move}]
  {:pre [(chess-pieces/king? piece)]}
  #_(infof "%s %s" [(geom/delta new-loc (chess-board/where-am-i state piece))
                    (chess-pieces/owner piece)]
           (chess-board/find-loc state [7 7]))
  (case [(geom/delta new-loc (chess-board/where-am-i state piece))
         (chess-pieces/owner piece)]
    [TWO-SQUARES-LEFT  "white"] (chess-board/find-loc state [7 0])
    [TWO-SQUARES-RIGHT "white"] (chess-board/find-loc state [7 7])
    [TWO-SQUARES-LEFT  "black"] (chess-board/find-loc state [0 0])
    [TWO-SQUARES-RIGHT "black"] (chess-board/find-loc state [0 7])
    nil))

(defn castling-blocked?
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (chess-board/find-piece state piece)]
    (->> (geom/path-between old-loc new-loc)
         (butlast)
         (chess-board/blockers state)
         (seq)
         (some?))))

(defn castle-states
  [{:keys [state piece new-loc] :as proposed-move}]
  {:pre [(chess-pieces/king? piece)]}
  (let [king-loc (chess-board/where-am-i state piece)
        locs     (geom/path-between king-loc new-loc)]
    (map (fn [loc]
           (-> state
               (dissoc king-loc)
               (assoc loc piece)))
         (concat locs
                 [new-loc
                  king-loc]))))

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
    (and (valid-castle-movement? proposed-move)
         (chess-pieces/first-move? king)
         (chess-pieces/first-move? rook)
         (not (castling-blocked? proposed-move))
         (not-any? check? (castle-states proposed-move)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pawn fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn valid-pawn-taking-movement?
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (chess-board/find-piece state piece)
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
  (true? (let [[old-loc] (chess-board/find-piece state piece)
               step      (get PAWN-DIRECTION (chess-pieces/owner piece))]
           (or (and (= new-loc (map + old-loc step))
                    (chess-board/empty-square? state new-loc))
               (and (= new-loc (map + old-loc step step))
                    (chess-board/empty-square? state new-loc)
                    (chess-pieces/first-move? piece)
                    (not (proposed-moves/slide-blocked? proposed-move)))))))

(def MOVEMENT-POLICY
  {"rook"   valid-rook-movement?
   "queen"  valid-queen-movement?
   "king"   (some-fn* valid-king-movement?
                      valid-castle?)
   "knight" valid-knight-movement?
   "pawn"   (some-fn* valid-pawn-movement?
                      valid-pawn-take?)
   "bishop" valid-bishop-movement?})

(def ALWAYS-VALID-POLICY (constantly true))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: Castling, check?, pawn promotion, en-passant, turn color indicator
(defn valid-movement?
  [{:keys [piece] :as proposed-move}]
  (let [proposed-movement-valid? (get MOVEMENT-POLICY (chess-pieces/type piece))]
    (proposed-movement-valid? proposed-move)))

(defn allowed-action?
  [state new-loc piece monitor]
  (let [proposed-move {:state   state
                       :piece   piece
                       :new-loc new-loc}
        pred          (every-pred proposed-moves/belongs-to-active-player?
                                  valid-movement?)]
    ;;(infof "Checking if %s can be moved from %s to %s" piece old-loc new-loc)
    (pred proposed-move)))

(defn on-drop-handler
  "state-ref is a reference to state (to get the value, must deref)"
  [state-ref new-loc piece monitor]
  ;; Add an additional %s in the infof to print the board state
  ;;(infof "Drop-handler: Dropping piece %s at Coordinate %s on Board" piece new-loc @state)
  (move-piece! {:state   state-ref
                :piece   piece
                :new-loc new-loc}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Check
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn threatened?
  [state target-piece aggressor-piece]
  (valid-movement? {:state   state
                    :piece   aggressor-piece
                    :new-loc (chess-board/where-am-i state target-piece)}))

(defn check?
  [piece state]
  {:pre [(chess-pieces/king? piece)]}
  (->> state
       (chess-board/get-pieces)
       (map (partial threatened? state piece))
       (some identity)
       (boolean)))
