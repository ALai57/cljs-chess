(ns cljs-chess.views
  (:require [clojure.string :refer [includes?]]
            [cljs-chess.components.drag-and-drop-board :as dnd-board]
            [cljs-chess.components.chess-piece :as chess-piece]
            [cljs-chess.chess :as chess :refer [STARTING-CHESS-BOARD]]
            [cljs-chess.stories.helper :as helper]
            [re-frame.core :refer [subscribe dispatch]]
            [taoensso.timbre :refer-macros [infof]]
            [reagent.core :as reagent]))

(def state
  (reagent/atom STARTING-CHESS-BOARD))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Landing pages
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn home
  []
  [:div
   [dnd-board/drag-and-drop-board {:rows      8
                                   :cols      8
                                   :tag       "game"
                                   :state     @state
                                   :item-type chess-piece/chess-piece
                                   :can-drop? chess/can-drop?
                                   :on-drop   (partial chess/on-drop-handler state)}]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Test pages
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def panels {:home home})

(defn app []
  (let [active-panel @(subscribe [:active-panel])]
    (infof "Active panel %s" active-panel)
    [(get panels active-panel)]))
