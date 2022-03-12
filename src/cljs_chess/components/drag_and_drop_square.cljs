(ns cljs-chess.components.drag-and-drop-square
  (:require ["react-dnd" :as rdnd]
            [cljs-chess.components.chess-piece :as chess-piece]
            [taoensso.timbre :refer-macros [infof]]))

(defn log-drop-event!
  [item monitor]
  #_(js/console.log "Drop event triggered with:\n\nITEM\n" item "\n\nMONITOR\n" monitor))

(defn -dnd-square
  "Required because the `useDrag` context uses a Hook and that must be inside a
  Functional component"
  [{:keys [dim id style background-color coords on-drop can-drop? accept]
    :or   {dim "100px"}
    :as   arg}
   children]
  (let [[_ drop] (rdnd/useDrop
                   (fn []
                     #js {:accept  accept
                          :canDrop (fn [item monitor]
                                     (let [item (js->clj item :keywordize-keys true)]
                                       (can-drop? item monitor)))
                          :drop    (fn [item monitor]
                                     (let [item (js->clj item :keywordize-keys true)]
                                       (log-drop-event! item monitor)
                                       (on-drop item monitor)))}))]
    (fn []
      [:div.background-darkgrey.hover-icon.drag-and-drop-square
       {:ref   drop
        :style (merge {:width            dim
                       :height           dim
                       :float            "left"
                       :background-color background-color}
                 style)
        :id    id}
       children])))

(defn drag-and-drop-square
  [{:keys [id piece] :as m}]
  ^{:key id}
  [:f> (partial -dnd-square m piece)])
