(ns cljs-chess.views
    (:require [clojure.string :refer [includes?]]
              [re-frame.core :refer [subscribe dispatch]]
              [taoensso.timbre :refer-macros [infof]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Landing pages
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn home []
  [:div])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Test pages
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def panels {:home home})

(defn app []
  (let [active-panel @(subscribe [:active-panel])]
    (infof "Active panel %s" active-panel)
    [(get panels active-panel)]))
