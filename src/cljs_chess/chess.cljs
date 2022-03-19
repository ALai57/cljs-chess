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
  {[0 0] (assoc BLACK-ROOK :id "1" :first-move? true)
   [0 1] (assoc BLACK-KNIGHT :id "1")
   [0 2] (assoc BLACK-BISHOP :id "1")
   [0 3] (assoc BLACK-KING :id "1" :first-move? true)
   [0 4] (assoc BLACK-QUEEN :id "1")
   [0 5] (assoc BLACK-BISHOP :id "2")
   [0 6] (assoc BLACK-KNIGHT :id "2")
   [0 7] (assoc BLACK-ROOK :id "2" :first-move? true)

   [1 0] (assoc BLACK-PAWN :id "1" :first-move? true)
   [1 1] (assoc BLACK-PAWN :id "2" :first-move? true)
   [1 2] (assoc BLACK-PAWN :id "3" :first-move? true)
   [1 3] (assoc BLACK-PAWN :id "4" :first-move? true)
   [1 4] (assoc BLACK-PAWN :id "5" :first-move? true)
   [1 5] (assoc BLACK-PAWN :id "6" :first-move? true)
   [1 6] (assoc BLACK-PAWN :id "7" :first-move? true)
   [1 7] (assoc BLACK-PAWN :id "8" :first-move? true)

   [6 0] (assoc WHITE-PAWN :id "1" :first-move? true)
   [6 1] (assoc WHITE-PAWN :id "2" :first-move? true)
   [6 2] (assoc WHITE-PAWN :id "3" :first-move? true)
   [6 3] (assoc WHITE-PAWN :id "4" :first-move? true)
   [6 4] (assoc WHITE-PAWN :id "5" :first-move? true)
   [6 5] (assoc WHITE-PAWN :id "6" :first-move? true)
   [6 6] (assoc WHITE-PAWN :id "7" :first-move? true)
   [6 7] (assoc WHITE-PAWN :id "8" :first-move? true)

   [7 0] (assoc WHITE-ROOK :id "1" :first-move? true)
   [7 1] (assoc WHITE-KNIGHT :id "1")
   [7 2] (assoc WHITE-BISHOP :id "1")
   [7 3] (assoc WHITE-KING :id "1" :first-move? true)
   [7 4] (assoc WHITE-QUEEN :id "1")
   [7 5] (assoc WHITE-BISHOP :id "2")
   [7 6] (assoc WHITE-KNIGHT :id "2")
   [7 7] (assoc WHITE-ROOK :id "2" :first-move? true)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn lookup-piece
  [state piece]
  (first (filter (comp (partial = piece)
                       second)
                 state)))

(defn lookup-loc
  [state loc]
  (get state loc))

(defn first-move?
  [piece]
  (get piece :first-move?))

(defn piece-type
  [piece]
  (:piece piece))

(defn piece-owner
  [piece]
  (:owner piece))

(defn move-piece!
  [state piece old-loc new-loc]
  (infof "Moving %s from %s to %s" piece old-loc new-loc)
  (swap! state dissoc old-loc)
  (swap! state assoc new-loc (update-when piece :first-move? (constantly false))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Movement policy
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def UP [-1 0])
(def DOWN [1 0])

(def PAWN-DIRECTION
  {"black" DOWN
   "white" UP})

(defn valid-endpoint?
  [state item old-loc new-loc]
  (not= (piece-owner item)
        (piece-owner (get state new-loc))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Jumping fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn valid-jump?
  [valid-geom? state item old-loc new-loc]
  (and (valid-geom? old-loc new-loc)
       (valid-endpoint? state item old-loc new-loc)))

(defn L-movement?
  [old-loc new-loc]
  (= [1 2]
     (sort (map (comp geom/abs -) old-loc new-loc))))

(def valid-knight-movement?
  (partial valid-jump? L-movement?))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sliding fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn blockers
  [state pts]
  (reduce (fn [acc pt]
            (if-let [blocker (lookup-loc state pt)]
              (conj acc blocker)
              acc))
          #{}
          pts))

(defn slide-blocked?
  [state item old-loc new-loc]
  (->> (geom/path-between old-loc new-loc)
       (blockers state)
       (seq)))

(defn valid-slide?
  [valid-direction? state item old-loc new-loc]
  (and (valid-direction? old-loc new-loc)
       (not (slide-blocked? state item old-loc new-loc))
       (valid-endpoint? state item old-loc new-loc)))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pawn fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn end-in-enemy-space?
  "Does the move end in a space with an enemy?"
  [state item old-loc new-loc]
  (when-let [owner (piece-owner (get state new-loc))]
    (not= (piece-owner item)
          owner)))

(defn empty-square?
  [state loc]
  (nil? (lookup-loc state loc)))

(defn valid-pawn-take?
  [state item old-loc new-loc]
  (let [step (get PAWN-DIRECTION (piece-owner item))
        d    (map - new-loc old-loc step)]
    (and (or (= [0  1] d)
             (= [0 -1] d))
         (end-in-enemy-space? state item old-loc new-loc))))

(defn valid-pawn-movement?
  [state item old-loc new-loc]
  (let [step (get PAWN-DIRECTION (piece-owner item))]
    (or (and (= new-loc (map + old-loc step))
             (empty-square? state new-loc))
        (and (= new-loc (map + old-loc step step))
             (empty-square? state new-loc)
             (first-move? item)
             (not (slide-blocked? state item old-loc new-loc))))))

(def MOVEMENT-POLICY
  {"rook"   valid-rook-movement?
   "queen"  valid-queen-movement?
   "king"   valid-king-movement?
   "knight" valid-knight-movement?
   "pawn"   (some-fn* valid-pawn-movement?
                      valid-pawn-take?)
   "bishop" valid-bishop-movement?})

(def ALWAYS-VALID-POLICY (constantly true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: Extract these to more generic location
;; TODO: Set up testing for logic
(defn can-drop?
  [state new-loc item monitor]
  (let [[old-loc] (lookup-piece state item)
        policy    (get MOVEMENT-POLICY (piece-type item))]
    ;;(infof "Checking if %s can be moved from %s to %s" item old-loc new-loc)
    (policy state item old-loc new-loc)))

(defn on-drop-handler
  [state new-loc item monitor]
  (let [[old-loc] (lookup-piece @state item)]
    ;; Add an additional %s in the infof to print the board state
    ;;(infof "Drop-handler: Dropping Item %s at Coordinate %s on Board" item new-loc @state)
    (move-piece! state item old-loc new-loc)))
