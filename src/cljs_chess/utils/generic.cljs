(ns cljs-chess.utils.generic)

(defn update-when
  [m k f]
  (if (contains? m k)
    (update m k f)
    m))

(defn some-fn*
  "A version of some-fn that accepts multiple arguments in the predicates"
  [& ps]
  (fn [& args]
    (some #(apply % args) ps)))
