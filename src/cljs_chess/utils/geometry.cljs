(ns cljs-chess.utils.geometry)

(defn abs
  [x]
  (if (pos? x) x (- x)))

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

(defn path-between
  [old-loc new-loc]
  (let [step (partial map + (direction old-loc new-loc))
        n    (dec (distance old-loc new-loc))]
    (->> (step old-loc)
         (iterate step)
         (take n)))
  #_(println "UNIT DIRECTION" (direction old-loc new-loc)))

(defn L-movement?
  [old-loc new-loc]
  (= [1 2]
     (sort (map (comp abs -) old-loc new-loc))))
