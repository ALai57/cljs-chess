(ns cljs-chess.views
  (:require [clojure.string :refer [includes?]]
            [cljs-chess.components.drag-and-drop-board :as dnd-board]
            [cljs-chess.components.chess-piece :as chess-piece]
            [cljs-chess.chess :as chess :refer [STARTING-CHESS-BOARD]]
            [cljs-chess.stories.helper :as helper]
            [re-frame.core :refer [subscribe dispatch reg-event-db]]
            [taoensso.timbre :refer-macros [infof]]
            [reagent.core :as reagent]))

(def DEFAULT-DB
  {:chess-board nil})

(reg-event-db
 :initialize-db
 (fn [_ _]
   DEFAULT-DB))

(def state
  (reagent/atom STARTING-CHESS-BOARD))

(defn app []
  [:div
   [dnd-board/drag-and-drop-board {:rows      8
                                   :cols      8
                                   :tag       "game"
                                   :state     @state
                                   :item-type chess-piece/chess-piece
                                   :can-drop? chess/allowed-action?
                                   :on-drop   (partial chess/on-drop-handler state)}]])
