(ns cljs-chess.chess
  (:require [cljs-chess.utils.geometry :as geom]
            [cljs-chess.utils.generic :refer [some-fn* update-when]]
            [taoensso.timbre :refer-macros [infof]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic board setup
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Example of the state contained in a reagent atom
;; {[0 0] {:piece "rook" :owner "black" :id "1"}
;;  [2 4] {:piece "queen" :owner "white" :id "1"}}

(def BLACK-ROOK {:piece "rook" :owner "black"})
(def BLACK-KNIGHT {:piece "knight" :owner "black"})
(def BLACK-BISHOP {:piece "bishop" :owner "black"})
(def BLACK-QUEEN {:piece "queen" :owner "black"})
(def BLACK-KING {:piece "king" :owner "black"})
(def BLACK-PAWN {:piece "pawn" :owner "black"})

(def WHITE-ROOK {:piece "rook" :owner "white"})
(def WHITE-KNIGHT {:piece "knight" :owner "white"})
(def WHITE-BISHOP {:piece "bishop" :owner "white"})
(def WHITE-QUEEN {:piece "queen" :owner "white"})
(def WHITE-KING {:piece "king" :owner "white"})
(def WHITE-PAWN {:piece "pawn" :owner "white"})

(def STARTING-CHESS-BOARD
  {[0 0] (assoc BLACK-ROOK :id "1")
   [0 1] (assoc BLACK-KNIGHT :id "1")
   [0 2] (assoc BLACK-BISHOP :id "1")
   [0 3] (assoc BLACK-KING :id "1")
   [0 4] (assoc BLACK-QUEEN :id "1")
   [0 5] (assoc BLACK-BISHOP :id "2")
   [0 6] (assoc BLACK-KNIGHT :id "2")
   [0 7] (assoc BLACK-ROOK :id "2")

   [1 0] (assoc BLACK-PAWN :id "1")
   [1 1] (assoc BLACK-PAWN :id "2")
   [1 2] (assoc BLACK-PAWN :id "3")
   [1 3] (assoc BLACK-PAWN :id "4")
   [1 4] (assoc BLACK-PAWN :id "5")
   [1 5] (assoc BLACK-PAWN :id "6")
   [1 6] (assoc BLACK-PAWN :id "7")
   [1 7] (assoc BLACK-PAWN :id "8")

   [6 0] (assoc WHITE-PAWN :id "1")
   [6 1] (assoc WHITE-PAWN :id "2")
   [6 2] (assoc WHITE-PAWN :id "3")
   [6 3] (assoc WHITE-PAWN :id "4")
   [6 4] (assoc WHITE-PAWN :id "5")
   [6 5] (assoc WHITE-PAWN :id "6")
   [6 6] (assoc WHITE-PAWN :id "7")
   [6 7] (assoc WHITE-PAWN :id "8")

   [7 0] (assoc WHITE-ROOK :id "1")
   [7 1] (assoc WHITE-KNIGHT :id "1")
   [7 2] (assoc WHITE-BISHOP :id "1")
   [7 3] (assoc WHITE-KING :id "1")
   [7 4] (assoc WHITE-QUEEN :id "1")
   [7 5] (assoc WHITE-BISHOP :id "2")
   [7 6] (assoc WHITE-KNIGHT :id "2")
   [7 7] (assoc WHITE-ROOK :id "2")})

(def TURN-ORDER
  {"black" "white"
   "white" "black"})

(def UP [-1 0])
(def DOWN [1 0])
(def TWO-SQUARES-LEFT  [0 -2])
(def TWO-SQUARES-RIGHT [0  2])

(def PAWN-DIRECTION
  {"black" DOWN
   "white" UP})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def truthy? identity)

;; State
(defn lookup-piece
  [state piece]
  (first (filter (comp (partial = piece)
                       second)
                 state)))

(def where-am-i (comp first lookup-piece))

(defn lookup-loc
  [state loc]
  (get state loc))

(def empty-square?
  (comp nil? lookup-loc))

(defn blockers
  [state pts]
  (reduce (fn [acc pt]
            (if-let [blocker (lookup-loc state pt)]
              (conj acc blocker)
              acc))
          #{}
          pts))

;; Pieces
(defn piece-type
  [piece]
  (:piece piece))

(defn piece-owner
  [piece]
  (:owner piece))

(defn moved?
  [piece]
  (:moved? piece))

(defn first-move?
  [piece]
  (boolean (and piece (not (moved? piece)))))

(defn king?
  [piece]
  (= "king" (piece-type piece)))

(defn next-player
  [x]
  (get TURN-ORDER x))

(defn move-piece!
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (lookup-piece @state piece)]
    (infof "Moving %s from %s to %s" piece old-loc new-loc)
    (swap! state dissoc old-loc)
    (swap! state assoc
           new-loc        (assoc piece :moved? true)
           :active-player (next-player (piece-owner piece)))
    #_(cond
        (promotion? proposed-move) (promote-pawn! proposed-move)
        (castle? proposed-move)    (castle-rook! proposed-move))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Movement policy
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn valid-endpoint?
  [{:keys [state piece new-loc] :as proposed-move}]
  (not= (piece-owner piece)
        (piece-owner (lookup-loc state new-loc))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Jumping fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn valid-jump?
  [valid-geom? {:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (lookup-piece state piece)]
    (and (valid-geom? old-loc new-loc)
         (valid-endpoint? proposed-move))))

(def valid-knight-movement?
  (partial valid-jump? geom/L-movement?))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sliding fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn slide-blocked?
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (lookup-piece state piece)]
    (->> (geom/path-between old-loc new-loc)
         (blockers state)
         (seq)
         (some?))))

(defn valid-slide?
  [valid-direction? {:keys [state piece new-loc] :as proposed-move}]
  (true? (let [[old-loc] (lookup-piece state piece)]
           (and (valid-direction? old-loc new-loc)
                (not (slide-blocked? proposed-move))
                (valid-endpoint? proposed-move)))))

(def single-square-move?
  (comp (partial = 1)
        geom/distance))

(def valid-king-movement?
  (partial valid-slide? single-square-move?))

(def valid-bishop-movement?
  (partial valid-slide? geom/diagonal?))

(def valid-rook-movement?
  (partial valid-slide? (some-fn* geom/horizontal?
                                  geom/vertical?)))

(def valid-queen-movement?
  (partial valid-slide? (some-fn* geom/horizontal?
                                  geom/vertical?
                                  geom/diagonal?)))

(defn valid-castle-movement?
  [{:keys [state piece new-loc] :as proposed-move}]
  {:pre [(king? piece)]}
  (let [delta (geom/delta new-loc (where-am-i state piece))]
    (or (= [0  2] delta)
        (= [0 -2] delta))))

(defn lookup-castling-rook
  [{:keys [state piece new-loc] :as proposed-move}]
  {:pre [(king? piece)]}
  #_(infof "%s %s" [(geom/delta new-loc (where-am-i state piece))
                    (piece-owner piece)]
           (lookup-loc state [7 7]))
  (case [(geom/delta new-loc (where-am-i state piece))
         (piece-owner piece)]
    [TWO-SQUARES-LEFT  "white"] (lookup-loc state [7 0])
    [TWO-SQUARES-RIGHT "white"] (lookup-loc state [7 7])
    [TWO-SQUARES-LEFT  "black"] (lookup-loc state [0 0])
    [TWO-SQUARES-RIGHT "black"] (lookup-loc state [0 7])
    nil))

(defn castling-blocked?
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (lookup-piece state piece)]
    (->> (geom/path-between old-loc new-loc)
         (butlast)
         (blockers state)
         (seq)
         (some?))))

(defn castle-states
  [{:keys [state piece new-loc] :as proposed-move}]
  {:pre [(king? piece)]}
  (let [king-loc (where-am-i state piece)
        locs     (geom/path-between king-loc new-loc)]
    (map (fn [loc]
           (-> state
               (dissoc king-loc)
               (assoc loc piece)))
         (concat locs
                 [new-loc
                  king-loc]))))

(defn valid-castle?
  [{:keys [state piece new-loc] :as proposed-move}]
  {:pre [(king? piece)]}
  (let [king piece
        rook (lookup-castling-rook proposed-move)]
    #_(infof "Castling rook %s" rook)
    #_(infof "Valid-castle-movement? %s" (valid-castle-movement? proposed-move))
    #_(infof "First king movement? %s First Rook movement? %s"
             (first-move? king)
             (first-move? rook))
    (and (valid-castle-movement? proposed-move)
         (first-move? king)
         (first-move? rook)
         (not (castling-blocked? proposed-move))
         #_(not-any? check? (castle-states proposed-move)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pawn fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn end-in-enemy-space?
  "Does the move end in a space with an enemy?"
  [{:keys [state piece new-loc] :as proposed-move}]
  (when-let [owner (piece-owner (get state new-loc))]
    (not= (piece-owner piece)
          owner)))

(defn valid-pawn-take?
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (lookup-piece state piece)
        step      (get PAWN-DIRECTION (piece-owner piece))
        d         (map - new-loc old-loc step)]
    (and (or (= [0  1] d)
             (= [0 -1] d))
         (end-in-enemy-space? proposed-move))))

(defn valid-pawn-movement?
  [{:keys [state piece new-loc] :as proposed-move}]
  (true? (let [[old-loc] (lookup-piece state piece)
               step      (get PAWN-DIRECTION (piece-owner piece))]
           (or (and (= new-loc (map + old-loc step))
                    (empty-square? state new-loc))
               (and (= new-loc (map + old-loc step step))
                    (empty-square? state new-loc)
                    (first-move? piece)
                    (not (slide-blocked? proposed-move)))))))

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
;; Turn policy
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn allowed-owner?
  [{:keys [active-player]} piece]
  ;;(infof "Attempting to move: %s Current active player: %s" piece active-player)
  (if active-player
    (= (piece-owner piece) active-player)
    true))

(def turn-policy
  allowed-owner?)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: Castling, pawn promotion, en-passant, check?, turn color indicator
;;
(defn can-drop?
  [state new-loc piece monitor]
  (let [movement-policy (get MOVEMENT-POLICY (piece-type piece))]
    ;;(infof "Checking if %s can be moved from %s to %s" piece old-loc new-loc)
    ;;         Call this "proposed-move"
    (and (turn-policy state piece)
         (movement-policy {:state   state
                           :piece   piece
                           :new-loc new-loc}))))

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
  (can-drop? state (where-am-i state target-piece) aggressor-piece nil))

(defn get-pieces
  [state]
  (map second state))

(defn check?
  [piece state]
  {:pre [(king? piece)]}
  (->> state
       (get-pieces)
       (map (partial threatened? state piece))
       (some truthy?)
       (boolean)))
