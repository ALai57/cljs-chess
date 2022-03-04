(ns cljs-chess.events.core
    (:require [cljs-chess.db :refer [default-db]]
              [re-frame.core :refer [dispatch reg-event-db]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reg-event-db
 :initialize-db
 (fn [_ _]
   default-db))

(defn set-active-panel [db [_ value]]
  (merge db {:active-panel value}))
(reg-event-db
 :set-active-panel
 set-active-panel)
