(ns cljs-chess.utils.generic
  (:require [taoensso.timbre :refer-macros [debugf]]))

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

(defmacro with-log
  [f]
  (let [fname (name f)]
    `(fn [& args#]
       (let [result# (apply ~f args#)]
         (~(symbol "debugf") "%s: %s" ~fname result#)
         result#))))
