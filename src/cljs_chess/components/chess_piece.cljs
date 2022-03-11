(ns cljs-chess.components.chess-piece
  (:require ["react-dnd" :as rdnd]))

(defn drag-active?
  [monitor]
  (. monitor isDragging))

(defn get-image
  [{:keys [piece owner]}]
  (str "images/" owner "-" piece ".svg"))

(defn get-state
  [monitor]
  #_(js/console.log "Getitem: "(. monitor getItem))
  #_(if (drag-active? monitor)
      (js/console.log "DRAGGING")
      (js/console.log "NOT DRAGGING"))
  {:dragging? (drag-active? monitor)})

(defn chess-piece
  [piece]
  (let [[{:keys [dragging?] :as _state} drag] (rdnd/useDrag
                                                (fn []
                                                  #js {:type    "KNIGHT"
                                                       :item    (clj->js piece)
                                                       :collect get-state}))]
    ^{:key (get-image piece)}
    [:img {:src   (get-image piece)
           :ref   drag
           :style {:width  "100%"
                   :height "100%"
                   :opacity (if dragging? 0.5 1)}}]))
