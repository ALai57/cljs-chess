(ns cljs-chess.db
    (:require [cljs.spec.alpha :as s]))


;; -- Spec --------------------------------------------------------------------
;; The value in app-db should always match this spec.

(s/def ::active-panel keyword?)

;; -- Default app-db Value  ---------------------------------------------------

(def default-db
  {:active-panel :home})
