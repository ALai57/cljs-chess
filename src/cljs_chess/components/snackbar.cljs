(ns cljs-chess.components.snackbar
  (:require [reagent-mui.components :refer [snackbar alert]]
            [taoensso.timbre :refer-macros [info]]
            [reagent.core :as reagent]))

(defn clickaway?
  [reason]
  (= "clickaway" reason))

(defn handle-close
  [ratom event reason]
  (when (not (clickaway? reason))
    (reset! ratom false)))

(defn basic-snackbar
  [{:keys [message level open? auto-hide-duration]
    :or   {level              "info"
           open?              true
           auto-hide-duration 6000}}]
  (let [showing  (reagent/atom open?)]
    (fn []
      [snackbar {:open               @showing
                 :auto-hide-duration auto-hide-duration
                 :on-close           (partial handle-close showing)}
       [alert {:severity level
               :on-close (partial handle-close showing)}
        message]])))
