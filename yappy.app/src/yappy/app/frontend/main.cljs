(ns ^:figwheel-hooks yappy.app.frontend.main
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [yappy.app.frontend.events :as events]
   [yappy.app.frontend.views :as views]
   [yappy.app.frontend.config :as config]))

(js/console.log "Hello, world")

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

;; This is called once
(defonce init
  (do
    (re-frame/dispatch-sync [::events/initialize-db])
    (dev-setup)
    (mount-root)

    true))

;; This is called every time you make a code change
(defn ^:after-load reload []
  (mount-root))
