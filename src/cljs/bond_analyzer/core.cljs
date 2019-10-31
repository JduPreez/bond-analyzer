(ns bond-analyzer.core
  (:require-macros [secretary.core :refer [defroute]])
  (:import [goog History]
           [goog.history EventType])
  (:require [bond-analyzer.bonds :as bonds]
            [bond-analyzer.bond :as bond]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as reagent]
            [secretary.core :as secretary]))

(def app-state$ (reagent/atom {}))
(def page-params$ (reagent/atom {}))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn set-current-page!
  [page]
  (swap! app-state$ assoc :page page))

(defn get-current-page
  []
  (@app-state$ :page))

(defn set-page-params!
  [page params]
  (swap! page-params$ assoc page params))

(defn get-page-param
  [page param]
  ((@page-params$ page) param))

(defn app-routes []
  (js/console.log "app-routes")
  (secretary/set-config! :prefix "#")

  (defroute "/" []
    (set-current-page! :bonds))

  (defroute "/bonds" []
    (set-current-page! :bonds))
  
  (defroute "/bonds/new" []
    (set-current-page! :bond)
    (set-page-params! :bond {:id nil}))

  (defroute "/bonds/:id" [id]
    (set-current-page! :bond)
    (set-page-params! :bond {:id id}))
  
  (hook-browser-navigation!))

(defmulti current-page #(get-current-page))
(defmethod current-page :bonds []
  [bonds/view])
(defmethod current-page :bond []
  [#(bond/view (get-page-param :bond :id))])
(defmethod current-page :default []
  [bonds/view])

(defn init! []
  (app-routes)
  (reagent/render-component [current-page]
                            (.getElementById js/document "content")))

(init!)