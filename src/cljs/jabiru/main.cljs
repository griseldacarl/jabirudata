(ns jabiru.main
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [jabiru.events :as events]
   [jabiru.routes :as routes]
   [jabiru.views :as views]
   [jabiru.config :as config]
   [jabiru.stats :as stats]
   [cljs-http.client :as http]
   [jabiru.firebase :as firebase]
   [cljsjs.firebase]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))


(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (firebase/init-firebase)
  (re-frame/dispatch [:load-from-cloud-storage]) ;this loads all the information from storage
  (re-frame/dispatch [:setup-database]) ;;this primary all of the variables
  (mount-root))
