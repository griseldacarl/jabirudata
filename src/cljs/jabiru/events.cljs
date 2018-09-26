(ns jabiru.events
  (:require
   [re-frame.core :as re-frame]
   [jabiru.db :as db]
   [jabiru.firebase :as f]
   [jabiru.views :as v]
   [jabiru.csv :as c]
   [jabiru.sessionstorage :as s]))
   

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
;;--------------------------------------------------------
(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

;;loader-on
(re-frame/reg-event-fx
 :loader-on
 (fn [cofx [_ signal]]
   {:db (assoc (:db cofx) :loaderOn signal)}))

;;------------------------------------------------------------

;;Go here
;;login
(re-frame/reg-event-fx
 :login
 (fn [cofx [_ login-data]]
   {:db  (:db cofx) 
    :login-correct-screen login-data 
    }))

 (re-frame/reg-fx
  :login-correct-screen
  (fn [{:keys [email pass]}]
   (f/login email pass)))


(re-frame/reg-event-fx
 :add-user-to-data
 (fn [cofx [_ user]]
   {:db  (:db cofx) 
    :add-user user }))


 (re-frame/reg-fx
  :add-user
  (fn [user]
  (f/save-users-to-database! "institution/users" user)))  

(re-frame/reg-event-fx
 :delete-user-from-data
 (fn [cofx [_ userkey]]
   {:db  (:db cofx) 
    :delete-user userkey }))

 (re-frame/reg-fx
  :delete-user
  (fn [userkey]
  (f/delete-users-from-database! userkey)))  


(re-frame/reg-event-fx
 :user-not-in-database
 (fn [cofx [_ ]]
   {:db  (:db cofx) 
    :go-to-login-screen 2 
    }))

 (re-frame/reg-fx
  :go-to-login-screen
  (fn []
  (reset! v/screen-state 2)))


(re-frame/reg-event-fx
 :user-in-database
 (fn [cofx [_ email]]
   {:db  (:db cofx) 
    :go-correct-screen email 
    }))


 (re-frame/reg-fx
  :go-correct-screen
  (fn [email]
   (let [users (re-frame/subscribe [:user-items])]
    (doall (for [user (js->clj @@users)]
        (let [user-email (.-perferredemail (aget (clj->js user) 1))
              alternate-email (.-alternateemail (aget (clj->js user) 1)) 
              firstname (.-firstname (aget (clj->js user) 1)) 
              lastname (.-lastname (aget (clj->js user) 1)) 
              level (.-level (aget (clj->js user) 1)) 
              phonenumber (.-phonenumber (aget (clj->js user) 1)) 
              role (.-role (aget (clj->js user) 1)) 
              slack (.-slackusername (aget (clj->js user) 1)) 
              userid (.-userid (aget (clj->js user) 1)) 
              ]
           (if (= email user-email)
                (do
                  (prn "Logging you in...")
                  ; 0 - Administrative page
                  ; 1 - resident page
                  (if (= level "director") (reset! v/screen-state 0) (reset! v/screen-state 1)) 
                  (re-frame/dispatch [:user-login {:userid userid
                                                    :alterateemail alternate-email
                                                    :email user-email
                                                    :firstname firstname
                                                    :lastname lastname
                                                    :level level
                                                    :role role
                                                    :slack slack 
                                                    }])                    
                 (js/console.log "Found a user!")) 
                 (js/console.log "There is no user for this login account!"))))))))     
        
 
(re-frame/reg-event-fx
 :user-refresh
 (fn [cofx [_ user]]
   {:db (assoc (:db cofx) :user user) }))

(re-frame/reg-event-fx
 :user-login
 (fn [cofx [_ user]]
   {:db (assoc (:db cofx) :user user)
    :save-user-session user}))
 
(re-frame/reg-fx
 :save-user-session 
 (fn [user]
   (s/set-item! :user user)))

(re-frame/reg-event-fx
 :user-logout
 (fn [cofx [_ ]]
   {:db  (:db cofx) 
    :remove-user-session "success"}))

(re-frame/reg-fx
 :remove-user-session 
 (fn []
   (s/remove-item! :user)))
;;------------------------------------------------------------------
(re-frame/reg-event-fx
  :upload-csv-members-to-user-database
  (fn [cofx [_ members]]
    {:upload-users-database members}))
(re-frame/reg-fx
  :upload-users-database
  (fn [members]
    (f/create-and-save-user-to-database members)))
;;-------------------------------------------------------------------
;;upload-csv-to-app-state
(re-frame/reg-event-fx
 :upload-csv-to-app-state
 (fn [cofx [_]]
   {:upload-from-csv {:log "success"}}))

(re-frame/reg-fx
 :upload-from-csv
 (fn []
   (c/upload)))

;;upload-examtypes
(re-frame/reg-event-fx
 :upload-examtypes
 (fn [cofx [_ examtypes]]
   {:db (assoc (:db cofx) :examtypes examtypes)}))

;;upload-providers
(re-frame/reg-event-fx
 :upload-providers
 (fn [cofx [_ providers]]
   {:db (assoc (:db cofx) :providers providers)}))

;;upload-records
(re-frame/reg-event-fx
 :upload-records
 (fn [cofx [_ records]]
   {:db (:db cofx) :upload-records-to-list records}))

(re-frame/reg-fx
 :upload-records-to-list
 (fn [records]
   (reset! c/records-data (clj->js records))))
;;---------------------------------------------------------------------
;;:add-record 
(re-frame/reg-event-fx
 :add-record
 (fn [cofx [_ record]]
   {:db (assoc (:db cofx) :records record)}))

;;:add-provider 
(re-frame/reg-event-fx
 :add-provider
 (fn [cofx [_ provider]]
   {:db (assoc (:db cofx) :providers provider)}))

;;----------------------------------------------------------------------
;;reset-database
(re-frame/reg-event-fx
 :reset-database
 (fn [cofx [_]]
   {:firebase-reset-realtime-database {:log "successful database reset"}}))

(re-frame/reg-fx
 :firebase-reset-realtime-database
 (fn [request]
   (f/save-orginal-database!)))

;;------------------------------------------------------------------------
;;load-from-cloud-storage
(re-frame/reg-event-fx
 :load-from-cloud-storage
 (fn [cofx [_]]
   {:load-from-storage {:log "successful log from stoage"}}))

(re-frame/reg-fx
 :load-from-storage
 (fn [request]
   (f/get-qpath-data "qpath.csv")))

;;setup-database
(re-frame/reg-event-fx
 :setup-database
 [(re-frame/inject-cofx :firebase-store-users "institution/users")
  (re-frame/inject-cofx :firebase-store-providers "institution/providers")]
 (fn [cofx [_]]
   {:db (assoc (:db cofx) :users (:firebase-store-users cofx)
               :providers (:firebase-store-providers cofx))}))


(re-frame/reg-cofx
 :firebase-store-users
 (fn [coeffects firebase-storage-users-path]
   (assoc coeffects
          :firebase-store-users
          (f/load-users-from-database! firebase-storage-users-path))))


(re-frame/reg-cofx
 :firebase-store-providers
 (fn [coeffects firebase-storage-users-path]
   (assoc coeffects
          :firebase-store-providers
          (f/load-providers-from-database! firebase-storage-users-path))))

;;---------------------------------------------------------------------------
(re-frame/reg-event-fx
 :show-overall-residency
 (fn [cofx [_]]
   {:generate-overall-residency-chart "success"}))

(re-frame/reg-fx
 :generate-overall-residency-chart
 (fn [examtype]
   (v/show-overall-residency-chart examtype)))
