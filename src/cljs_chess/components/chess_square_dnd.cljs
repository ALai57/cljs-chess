(ns cljs-chess.components.chess-square-dnd
  (:require [taoensso.timbre :refer-macros [infof]]
            ["react-dnd" :as rdnd]
            [cljs-chess.components.chess-piece :as chess-piece]))

(defn item-in-drop-zone?
  [monitor]
  (. monitor isOver))

(defn get-state
  [id monitor]
  ;;(js/console.log "Monitor" monitor)
  ;;(js/console.log "Item in drop zone?" (item-in-drop-zone? monitor) id)
  {:in-drop-zone? (if monitor
                    true
                    #_(item-in-drop-zone? monitor)
                    false)})

(defn log-drop-event!
  [item monitor]
  (js/console.log "ITEM!" item)
  (js/console.log "MONITOR!" monitor)
  #_(js/console.log "DROPPED!" (clj->js coords)))

(defn -chess-square-dnd
  "Required because the `useDrag` context uses a Hook and that must be inside a
  Functional component"
  [{:keys [dim id style piece background-color coords on-drop]
    :or   {dim "100px"}
    :as   arg}
   children]
  (let [[{:keys [in-drop-zone?]} drop]
        (rdnd/useDrop
          (fn []
            #js {:accept  "KNIGHT"
                 :drop    (comp on-drop log-drop-event!)
                 :collect (partial get-state id)}))]
    (fn []
      [:div.background-darkgrey.hover-icon.chess-square
       {:ref   drop
        :style (merge {:width            dim
                       :height           dim
                       :float            "left"
                       :background-color background-color}
                 style)
        :id    id}
       children])))

(defn chess-square-dnd
  [{:keys [id piece] :as m}]
  ^{:key id}
  [:f> (partial -chess-square-dnd
         m
         (when piece [:f> (partial chess-piece/chess-piece piece)]))])
