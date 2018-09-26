(ns jabiru.views
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [jabiru.subs :as subs]
   [jabiru.stats :as stats]
   [jabiru.sessionstorage :as sess]
   [cljs.js :as cljs]
   [cljsjs.csv]
   [cljsjs.chartjs]
   [cljsjs.svgjs]
   [jabiru.csv :as c]
   [clojure.spec.alpha :as s]
   [cljs.core.async :as a]))


(def current-provider-id (r/atom "0"))
;;Refresh

(defn refresh-database
    []
    [:button {:type "button" :class "btn btn-danger"
              :on-click (fn [e]
                          (.preventDefault e)
                          (js/console.log "Refreshing the database!")
                          (re-frame/dispatch [:setup-database]))
              } "Refresh Database"])

;; Reset to Original
(defn reset-database-to-original
  []
  [:button {:type "button" :class "btn btn-danger"
            :on-click (fn [e]
                        (.preventDefault e)
                        (js/console.log "Resetting the database to original!")
                        (re-frame/dispatch [:reset-database]))
            } "Reset Database to Start"])

;; Home Page
;; Provider List view

(defn show-provider-row
  [provider]
  [:tr  {:key (.-userid provider)}
  [:th {:scope "row" } (.-firstname provider)]
  [:td (.-lastname provider)]
  [:td (.-number_of_technically_limited provider)]
  [:td (.-number_of_ultrasound_completed provider)]
  [:td (if (.-sdots_completed provider) "T" "F")]
  [:td (if (.-us_advanced_rotation provider) "T" "F")]
  [:td (if (.-us_rotation_completed provider) "T" "F")]
  [:td (if (.-us_teaching_completed provider) "T" "F")]
  [:td
  [:i {:class "fa fa-check" :aria-hidden "true"}] 
  ]])

(defn show-list-of-providers-view
  []
(let [providers (re-frame/subscribe [:provider-items])]  
 [:table {:class "table table-striped"}
 [:thead
  [:tr
   [:th {:scope "col"} "FN"]
   [:th {:scope "col"} "LN"]
   [:th {:scope "col"} "LIM"]
   [:th {:scope "col"} "#"]
   [:th {:scope "col"} "THRES"]
   [:th {:scope "col"} "ADV"]
   [:th {:scope "col"} "ROT"]
   [:th {:scope "col"} "TEACH"]
   [:th {:scope "col"} "Controls"]

   ]]
 [:tbody
  (map #(show-provider-row  (aget (clj->js %) 1)) (js->clj @@providers))
 ]]

  ))
;; Clear Provider view
(defn clear-provider-vew
  []
  (set! (.. js/document (getElementById "providerFirstName") -value) "")
  (set! (.. js/document (getElementById "providerLastName") -value) "")
  (set! (.. js/document (getElementById "providerTechnicallyLimited") -value) "")
  (set! (.. js/document (getElementById "providerUltrasoundCompleted") -value) "")
  (set! (.. js/document (getElementById "gridThresholdYes") -checked) false)
  (set! (.. js/document (getElementById "gridThresholdNo") -checked) true)
  (set! (.. js/document (getElementById "gridSDOTSYes") -checked) false)
  (set! (.. js/document (getElementById "gridSDOTSNo") -checked) true)
  (set! (.. js/document (getElementById "gridAdvancedRotationYes") -checked) false)
  (set! (.. js/document (getElementById "gridAdvancedRotationNo") -checked) true)
  (set! (.. js/document (getElementById "gridInternRotationYes") -checked) false)
  (set! (.. js/document (getElementById "gridInternRotationNo") -checked) true)
  (set! (.. js/document (getElementById "gridTeachingYes") -checked) false)
  (set! (.. js/document (getElementById "gridTeachingNo") -checked) true))

;; Provider View
(defn show-provider-view
  []
  [:form  {:id "providerForm"}
(comment  [:div {:class "form-group row"}  ;;For now, I will not allow users to add providers
   [:label {:for "providerFirstName", :class "col-sm-2 col-form-label"} "First Name"]
   [:div {:class "col-sm-10"}
    [:input {:type "text", :class "form-control", :id "providerFirstName", :placeholder "First Name"}]]]
  [:div {:class "form-group row"}
   [:label {:for "providerLastName", :class "col-sm-2 col-form-label"} "Last Name"]
   [:div {:class "col-sm-10"}
    [:input {:type "text", :class "form-control", :id "providerLastName", :placeholder "Last Name"}]]]

  [:fieldset {:class "form-group"}
   [:div {:class "row"}
    [:legend {:class "col-form-label col-sm-2 pt-0"} "Threshold"]
    [:div {:class "col-sm-10"}
     [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridThreshold", :id "gridThresholdYes", :value "1"  }]
      [:label {:class "form-check-label", :for "gridThresholdYes"} "Yes"]]
     [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridThreshold", :id "gridThresholdNo", :value "0" }]
      [:label {:class "form-check-label", :for "gridThresholdNo"} "No"]]]]]

  [:fieldset {:class "form-group"}
  [:div {:class "row"}
    [:legend {:class "col-form-label col-sm-2 pt-0"} "SDOTS"]
    [:div {:class "col-sm-10"}
    [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridSDOTS", :id "gridSDOTSYes", :value "1"  }]
      [:label {:class "form-check-label", :for "gridSDOTSYes"} "Yes"]]
    [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridSDOTS", :id "gridSDOTSNo", :value "0" }]
      [:label {:class "form-check-label", :for "gridSDOTSNo"} "No"]]]]]

  [:fieldset {:class "form-group"}
  [:div {:class "row"}
    [:legend {:class "col-form-label col-sm-2 pt-0"} "Advanced Rotation"]
    [:div {:class "col-sm-10"}
    [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridAdvancedRotation", :id "gridAdvancedRotationYes", :value "1"  }]
      [:label {:class "form-check-label", :for "gridAdvancedRotationYes"} "Yes"]]
    [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridAdvancedRotation", :id "gridAdvancedRotationNo", :value "0"}]
      [:label {:class "form-check-label", :for "gridAdvancedRotationNo"} "No"]]]]]

  [:fieldset {:class "form-group"}
  [:div {:class "row"}
    [:legend {:class "col-form-label col-sm-2 pt-0"} "Intern Rotation"]
    [:div {:class "col-sm-10"}
    [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridInternRotation", :id "gridInternRotationYes", :value "1"  }]
      [:label {:class "form-check-label", :for "gridInternRotationYes"} "Yes"]]
    [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridInternRotation", :id "gridInternRotationNo", :value "0" }]
      [:label {:class "form-check-label", :for "gridInternRotationNo"} "No"]]]]]
  [:fieldset {:class "form-group"}
  [:div {:class "row"}
    [:legend {:class "col-form-label col-sm-2 pt-0"} "Teaching"]
    [:div {:class "col-sm-10"}
    [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridTeaching", :id "gridTeachingYes", :value "1" }]
      [:label {:class "form-check-label", :for "gridTeachingYes"} "Yes"]]
    [:div {:class "form-check"}
      [:input {:class "form-check-input", :type "radio", :name "gridTeaching", :id "gridTeachingNo", :value "0" }]
      [:label {:class "form-check-label", :for "gridTeachingNo"} "No"]]]]]

    [:div {:class "form-row"}
      [:div {:class "form-group col-md-6"}
       [:label {:for "providerTechnicallyLimited"} "Technically Limited"]
       [:input {:type "text", :class "form-control", :id "providerTechnicallyLimited", :placeholder "# of Technically Limited"}]]
      [:div {:class "form-group col-md-6"}
       [:label {:for "providerUltrasoundCompleted"} "Ultrasound Completed"]
       [:input {:type "text", :class "form-control", :id "providerUltrasoundCompleted", :placeholder "# of Ultrasound Completed"}]]]
  [:div {:class "form-group row"}
   [:div {:class "col-sm-10"}
    [:button {:type "submit"  :class "btn btn-primary"
              :on-click (fn [e]
              (.preventDefault e)
              (def string-number #"^[0-9]*$")
              (def threshold-val (if (.-checked (.getElementById js/document "gridThresholdYes"))
                (.-value (.getElementById js/document "gridThresholdYes"))
                (.-value (.getElementById js/document "gridThresholdNo"))))
              (def sdots-val (if (.-checked (.getElementById js/document "gridSDOTSYes"))
                (.-value (.getElementById js/document "gridSDOTSYes"))
                (.-value (.getElementById js/document "gridSDOTSNo"))))
              (def adv-rot-val (if (.-checked (.getElementById js/document "gridAdvancedRotationYes"))
                (.-value (.getElementById js/document "gridAdvancedRotationYes"))
                (.-value (.getElementById js/document "gridAdvancedRotationNo"))))
              (def intern-rotation-val (if (.-checked (.getElementById js/document "gridInternRotationYes"))
                  (.-value (.getElementById js/document "gridInternRotationYes"))
                  (.-value (.getElementById js/document "gridInternRotationNo"))))
              (def teaching-val (if (.-checked (.getElementById js/document "gridTeachingYes"))
                (.-value (.getElementById js/document "gridTeachingYes"))
                (.-value (.getElementById js/document "gridTeachingNo"))))


              (s/def ::first-name (s/and string? #(not-empty %)))
              (s/def ::last-name (s/and string? #(not-empty %)))
              (s/def ::tech-limited (s/and string? #(not-empty %) #(re-matches string-number %)))
              (s/def ::ultrasound-completed (s/and string? #(not-empty %) #(re-matches string-number %)))

              (s/def ::provider (s/keys :req [::first-name ::last-name]))
              (if (s/valid? ::provider {
                                        ::first-name (.-value (.getElementById js/document "providerFirstName"))
                                        ::last-name (.-value (.getElementById js/document "providerLastName"))
                                        ::tech-limited (.-value (.getElementById js/document "providerTechnicallyLimited"))
                                        ::ultrasound-completed (.-value (.getElementById js/document "providerTechnicallyLimited"))
                                        })
                                        (re-frame/dispatch [:add-provider {
                                                               :userid  (.getTime (js/Date.))
                                                               :firstname (.-value (.getElementById js/document "providerFirstName"))
                                                               :lastname (.-value (.getElementById js/document "providerLastName"))
                                                               :number_of_technically_limited (.-value (.getElementById js/document "providerTechnicallyLimited"))
                                                               :number_of_ultrasound_completed (.-value (.getElementById js/document "providerTechnicallyLimited"))
                                                               :all_threshold_reached threshold-val
                                                               :sdots_completed sdots-val
                                                               :us_advanced_rotation adv-rot-val
                                                               :us_rotation_completed intern-rotation-val
                                                               :us_teaching_completed teaching-val
                                                               }])
                                        (js/console.log "Invalid"))
              (clear-provider-vew)
                                        )} "Add"]]] )
 
    (show-list-of-providers-view)
   ])

;; ExamTypes View

(defn show-examtypes-view
  []
  (let [examtypes (re-frame/subscribe [:examtype-items])]

        [:ul {:class "list-group"}
      
      (for [examtype @examtypes]
         [:li {:class "list-group-item" :key (.-examtypeid examtype) :id (.-examtypeid examtype)}  (.-examtype examtype)] )
     ]))

;;Users view
(defn show-users-view
  []
  (let [users (re-frame/subscribe [:user-items])]
    [:ul {:class "list-group"}
     (for [user (js->clj @@users)]
         [:li {:class "list-group-item" :key (.-userid (aget (clj->js user) 1)) :id (.-userid (aget (clj->js user) 1))}
           [:div {:class "card" :style {:width "100%"}}
             [:div {:class "card-body"}
               [:h5 {:class "card-title"} (str (.-firstname (aget (clj->js user) 1)) " "  (.-lastname (aget (clj->js user) 1)))]
               [:p {:class "card-text"} (.-perferredemail (aget (clj->js user) 1))]
               [:ul {:class "list-group list-group-flush"}
                [:li {:class "list-group-item bg-info"} (.-level (aget (clj->js user) 1))]
                [:li {:class "list-group-item bg-white"} (.-role (aget (clj->js user) 1))]
                [:li {:class "list-group-item bg-info"} (.-slackusername (aget (clj->js user) 1))]]
              [:input {:type "hidden" :id (str "card"(.-userid (aget (clj->js user) 1))) :value (.-userid (aget (clj->js user) 1))}] 
              [:i {:class "fa fa-trash fa-4x", :aria-hidden "true" 
                   :on-click (fn [e]
                              (.preventDefault e) 
                              (re-frame/dispatch [:delete-user-from-data (aget (clj->js user) 0)]))}]]


           ]])
     ]))


;;Setup View

(defn show-setup-view
  []
   [:div {:class "container"}
          [:div {:class "row"}
             [:div {:class "col"} (refresh-database) ]
             [:div {:class "col"} [:div {:class "alert alert-danger" :role "alert"} "To refresh the database"]]
             ]
             [:div {:class "row"}
             [:div {:class "col"} (reset-database-to-original) ]
             [:div {:class "col"} [:div {:class "alert alert-danger" :role "alert"} "To reset the database to original template form"]]

             ]])



(defn show-provider-option
  [provider]
  [:option {:key (.-userid provider)  :value (.-userid provider) } (str (.-firstname provider) " "(.-lastname provider))])
;;Settings View

(def uploaded-csv-file-members (r/atom ""))
(def member-csv-data (r/atom ""))

(defn debug-print-csv
  []
  (prn @uploaded-csv-file-members))

(defn show-settings-view
  []
  [:div {:class "accordion" :id "accordionExample"}
    [:div {:class "card"}
      [:div {:class "card-header" :id "headingOne"}
        [:h5 {:class "mb-0"}
          [:button {:class "btn btn-link" :type "button" :data-toggle "collapse" :data-target "#collapseOne" :aria-expanded "true" :aria-controls "collapseOne"
                    :on-click (fn [e] (.preventDefault e) )}
          "Examtypes"
          ]
        ]
       ]

   [:div {:id "collapseOne" :class "collapse" :aria-labelledby "headingOne" :data-parent "#accordionExample"}
      [:div {:class "card-body"}(show-examtypes-view)]]]

   [:div {:class "card"}
      [:div {:class "card-header" :id "headingTwo"}
        [:h5 {:class "mb-0"}
          [:button {:class "btn btn-link" :type "button" :data-toggle "collapse" :data-target "#collapseTwo" :aria-expanded "true" :aria-controls "collapseTwo"}
          "Users "
          ]
        ]
       ]

    
    [:div {:id "collapseTwo" :class "collapse show" :aria-labelledby "headingTwo" :data-parent "#accordionExample"}
      [:button {:type "button", :class "btn btn-primary", :data-toggle "modal", :data-target "#userModal"} "Add User"]
      [:button {:type "button", :class "btn btn-outline-primary" :on-click (fn [e]  (.click (.getElementById js/document "dataUploadMembersFile")))} "Upload"] 
      [:form [:div {:class "form-group"} [:input {:type "file", :class "form-control", :id "dataUploadMembersFile" 
                     :on-change (fn [e]
                        (let [file (first (array-seq (.. e -target -files)))
                              file-reader (js/FileReader.)]
                             (set! (.-onload file-reader)
                                 (fn [e] 
                                   (reset! uploaded-csv-file-members (-> e .-target .-result))
                                   (reset! member-csv-data (.. js/CSV (parse @uploaded-csv-file-members)))
                                   (re-frame/dispatch [:upload-csv-members-to-user-database member-csv-data])                                  
                                   ))
                                     (.readAsText file-reader file)
                                     (prn (-> e .-target .-error )))
                        )}]]]


    [:div {:class "modal fade", :id "userModal", :tabIndex "-1", :role "dialog", :aria-labelledby "userModalLabel", :aria-hidden "true"}
       [:div {:class "modal-dialog", :role "document"}
       [:div {:class "modal-content"}
       [:div {:class "modal-header"}
        [:h5 {:class "modal-title", :id "userModalLabel"} "Add User"]
        [:button {:type "button", :id "closeUserModal" :class "close", :data-dismiss "modal", :aria-label "Close"}
       [:span {:aria-hidden "true"} "×"]]]
       [:div {:class "modal-body"} 
        
       (let [
        providers (re-frame/subscribe [:provider-items])
        sorted-providers (sort-by #(.-lastname (aget (clj->js %) 1)) (js->clj @@providers))
        ]
        [:form  
          [:div {:class "form-row"}
            [:div {:class "form-group col-md-6"}
              [:label {:for "userEmail"} "Email"]
              [:input {:type "email", :class "form-control", :id "userEmail", :placeholder "Email"}]]
            [:div {:class "form-group col-md-6"}
              [:label {:for "userProviderSelect"} "Provider"]
              [:select {:id "userProviderSelect", :class "form-control"}
                
                
              (map #(show-provider-option (aget (clj->js %) 1)) sorted-providers)
                
                ]]]
            [:div {:class "form-group"}
              [:label {:for "userAlternativeEmail"} "Alternative Email"]
              [:input {:type "text", :class "form-control", :id "userAlternativeEmail", :placeholder "Email"}]]
            [:div {:class "form-group"}
              [:label {:for "userSlackUsername"} "Slack"]
              [:input {:type "text", :class "form-control", :id "userSlackUsername", :placeholder "@slackusername"}]]
            [:div {:class "form-row"}
              [:div {:class "form-group col-md-4"}
                [:label {:for "userPhoneNumber"} "Phone #"]
                [:input {:type "text", :class "form-control", :id "userPhoneNumber" :placeholder "5015559555"}]]
              [:div {:class "form-group col-md-4"}
               [:label {:for "userLevel"} "Level"]
               [:select {:id "userLevel", :class "form-control"}
                [:option  "director"]
                [:option  "facutly"]
                [:option  "alumin"]
                [:option  "senior"]
                [:option  "junior"]
                [:option  "intern"]
                [:option  "visiting"]
                ]]
             [:div {:class "form-group col-md-4"}
              [:label {:for "userRole"} "Role"]
              [:select {:id "userRole", :class "form-control"}
                [:option  "attending"]
                [:option  "coordinator"]
                [:option  "resident"]
                [:option  "student"]
                [:option  "app"]
                ]]]]
                                    ) 
        
        ]
       [:div {:class "modal-footer"}
        [:button {:type "button", :class "btn btn-secondary", :data-dismiss "modal"} "Close"]
        [:button {:type "button", :class "btn btn-primary"
                  :on-click (fn [e] 
                             (let [user-email (.getElementById js/document "userEmail")
                                   user-provider-select (.getElementById js/document "userProviderSelect")
                                   user-alternative-email (.getElementById js/document "userAlternativeEmail")
                                   user-slack (.getElementById js/document "userSlackUsername")
                                   user-phone-number (.getElementById js/document "userPhoneNumber")
                                   user-level (.getElementById js/document "userLevel")
                                   user-role (.getElementById js/document "userRole")
                                   user-provider-selected-value (atom 0)]
                                  
                              
                                   (def email-re  #"^\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,3}$")
                                   (def phone-number-re #"^\s*(?:\+?(\d{1,3}))?[-. (]*(\d{3})[-. )]*(\d{3})[-. ]*(\d{4})(?: *x(\d+))?\s*$") 
                                   (s/def ::preferred-email (s/and string? #(not-empty %)#(re-matches email-re %)))
                                   (s/def ::alternative-email (s/and string? #(not-empty %)#(re-matches email-re %)))
                                   (s/def ::slack-username (s/and string? #(not-empty %)))
                                   (s/def ::phone-number (s/and string? #(not-empty %)))
                                   (s/def ::user (s/keys :req [::preferred-email ]))
                                   (if (s/valid? ::user {::preferred-email   (.-value user-email)
                                                         ::slack-username    (.-value user-slack)
                                                         })
                                             
                                     
                                   (do  
                                   ;(js/console.log (.-value user-email))
                                   (def selected-user (loop [n 0]
                                           (if (and (.-selected (aget (.-options user-provider-select) n)) (<= n (-> user-provider-select .-options .-length)))
                                               (hash-map :value (.-value (aget (.-options user-provider-select) n))
                                                         :text  (.-text (aget (.-options user-provider-select) n))) 
                                               (recur (inc n)))))
                                   

                                    ;the dispach go here
                                    ;create a user and send to database at this point 
                                    (def fullname (:text selected-user))
                                    (def name-array (clojure.string/split fullname #" "))


                                    (re-frame/dispatch [:add-user-to-data
                                                        {
                                                         :userid (:value selected-user) 
                                                         :alternateemail (.-value user-alternative-email) 
                                                         :perferredemail (.-value user-email)
                                                         :firstname (first name-array)
                                                         :lastname (second name-array)
                                                         :phonenumber (.-value user-phone-number)
                                                         :level (.-value user-level) 
                                                         :role (.-value user-role) 
                                                         :slackusername (.-value user-slack) 
                                                    }]) 

                                   )
                                   (js/console.log "invalid"))
                                  
                                (.click (.getElementById js/document "closeUserModal") )

                              

                               ))} "Save changes"]]]]]

      [:div {:class "card-body"}(show-users-view)]
]]
   ])

(defn show-record-row-by-provider [record]
  (str "<tr  key='" (.-recordid record) "' style='color:black'>"
       "<th scope='row'>" (.-name record) "</th>"
       "<td>" (.-examtype record) "</td>"
       "<td>" (.-tls record) "</td>"
       "<td>" (.-meetcredentialingcriteria record) "</td>"
       "<td><i class= 'fa fa-check' aria-hidden= 'true' ></i></td></tr>"))

(defn show-record-by-provider-view
  [records]
   (let [data (vec records)
        head (str "<table class='table'><thead class='thead-dark'><tr><th scope='col'>Name</th><th scope='col'> Type</th><th scope='col'> TL</th><th scope='col'> #</th><th scope='col'>Action</th></tr></thead>")
      table-body (map #(show-record-row-by-provider (clj->js %)) records)]
    (str head "<tbody>" table-body "</tbody></table>")))

(defn show-record-view
  [provider]
  [:div {:class "card" :key (.-userid provider)}
  [:div {:class "card-header", :id (str "heading"(.-userid provider))}
   [:h5 {:class "mb-0"}
    [:button {:class "btn btn-link collapsed", :data-toggle "collapse" :id (str "buttonid"(.-userid provider) ) :value (.-userid provider) :data-target (str "#collapse"(.-userid provider)), :aria-expanded "false", :aria-controls (str "collapse"(.-userid provider)) 
              :on-click 
                (fn [e]  (.preventDefault e) 
                   (let [records c/records-data;(re-frame/subscribe [:record-items])
                         record-list (filter #(= (js/parseInt (-> e .-target .-value))(.-userid (clj->js %))) (js->clj @records))]
                         (set! (.-innerHTML (.getElementById js/document (str "card"(-> e .-target .-value))))  (show-record-by-provider-view  record-list))))
         
                   } (str (.-firstname provider) " " (.-lastname provider))]]]
  [:div {:id (str "collapse"(.-userid provider)), :class "collapse", :aria-labelledby (str "heading"(.-userid provider)), :data-parent "#accordion"}
   [:div {:class "card-body" :style {:color "#FFFFFF"} :id (str "card"(.-userid provider))}
    
   
     ]]])


(defn show-data-all-record
  []
  (let [
        providers (re-frame/subscribe [:provider-items])
        sorted-providers (sort-by #(.-lastname (aget (clj->js %) 1)) (js->clj @@providers))
        ]
   [:div {:id "accordion"}
    
    (doall (map #(show-record-view (aget (clj->js %)1)) sorted-providers))
    ]))

;;Data Entry View

(defn clear-data-entry-view
  []
  (set! (.. js/document (getElementById "dataEntryMeetCredentialCriteria") -value) "")
  (set! (.. js/document (getElementById "dataEntryTechnicallyLimited") -value) "")
  )


(defn show-data-entry-view
  []
  [:form

 [:div {:class "form-group"}
  [:label {:for "dataEntryProvider"} "Providers"]
  [:select {:class "form-control", :id "dataEntryProvider"}
   (let  [providers (re-frame/subscribe [:provider-items])
          sorted-providers  (sort-by #(.-lastname (aget (clj->js %)1)) (js->clj @@providers))
     ]
        
      (map #(show-provider-option (aget (clj->js %) 1)) sorted-providers)
     )]]

 [:div {:class "form-group"}
  [:label {:for "dataEntryExamtypes"} "Examtypes"]
  [:select {:class "form-control", :id "dataEntryExamtypes"}
 (let [examtypes (re-frame/subscribe [:examtype-items])]
       (for [examtype @examtypes]
         (do 
         [:option {:value (.-examtypeid examtype) :key (.-examtypeid examtype) :id (.-examtypeid examtype)}  (.-examtype examtype)]))
       
   )]]

 [:div {:class "form-group"}
  [:label {:for "dataEntryMeetCredentialCriteria"} "Number of studies that meet criteria"]
   [:input {:type "text", :class "form-control", :id "dataEntryMeetCredentialCriteria", :aria-describedby "dataEntryMeetCredentialCriteriaHelp", :placeholder "Number that meets criteria"}]
   [:small {:id "dataEntryMeetCredentialCriterialHelp" :class "form-text text-muted"} "This is a count of the quality studies performed by the provider."]]

  [:div {:class "form-group"}
   [:label {:for "dataEntryTechnicallyLimited"} "Number of technically limited studies"]
   [:input {:type "text", :class "form-control", :id "dataEntryTechnicallyLimited", :aria-describedby "dataEntryTechnicallyLimitedHelp", :placeholder "Number of technically limited studies"}]
   [:small {:id "dataEntryTechnicallyLimitedHelp" :class "form-text text-muted"} "This is a count of the number of studies that are technically limited."]]

   [:button {:type "submit", :class "btn btn-primary"

             :on-click (fn
                         [e]
                           (.preventDefault e)
                             
                         
                           (s/def ::meet-credential-criteria (s/and string? #(not-empty %)))
                           (s/def ::technically-limited (s/and string? #(not-empty %)))
                           (s/def ::name  (s/and string? #(not-empty %)))
                           (s/def ::first-name  (s/and string? #(not-empty %)))
                           (s/def ::last-name  (s/and string? #(not-empty %)))
                           (s/def ::exam-type  (s/and string? #(not-empty %)))
                           
                           (s/def ::record (s/keys :req [::meet-credential-criteria ::technically-limited]))
                          
                            
                         (let [
                               providers (re-frame/subscribe [:provider-items])
                               examtypes (re-frame/subscribe [:examtype-items])
                               userid (.-value (.getElementById js/document "dataEntryProvider"))
                               examtypeid (.-value (.getElementById js/document "dataEntryExamtypes"))
                               curr-provider  (aget  (clj->js (first (doall (filter #(= userid (str (.-userid (aget (clj->js %) 1)))) (js->clj @providers))))) 1)
                               examtype  (clj->js (first (doall (filter #(= examtypeid (str (.-id (clj->js %))))  (js->clj @@examtypes)))))
                               examtype-name (.-name examtype)
                               ]
                           
                            (if (s/valid? ::record {
                                            ::name (str (.-firstname curr-provider)  (.-lastname curr-provider))
                                            ::first-name (.-firstname curr-provider)
                                            ::last-name (.-lastname curr-provider)
                                            ::examtype examtype-name
                                            ::technically-limited (.-value (.getElementById js/document "dataEntryTechnicallyLimited"))
                                            ::meet-credential-criteria (.-value (.getElementById js/document "dataEntryMeetCredentialCriteria"))                   

                                            })
                                            (re-frame/dispatch [:add-record {
                                                          :recordid (.getTime (js/Date.))
                                                          :userid (.-userid curr-provider)
                                                          :name (str (.-firstname curr-provider) " " (.-lastname curr-provider))
                                                          :firstname (.-firstname curr-provider)
                                                          :lastname (.-lastname curr-provider)
                                                          :examtypeid examtypeid
                                                          :examtype examtype-name
                                                          :examsperformed 0
                                                          :examrequired 0
                                                          :examqasubmitted 0
                                                          :examqacompleted 0
                                                          :examsqaskipped 0
                                                          :exambilled 0
                                                          :examsattended 0
                                                          :examsreviewed 0
                                                          :examsuserall 0
                                                          :examsremaining 0
                                                          :percentqacompleted 0
                                                          :percentbilled 0
                                                          :percentuserall 0
                                                          :percentypeall 0
                                                          :tn 0
                                                          :tp 0
                                                          :fp 0
                                                          :fn 0
                                                          :tls (js/parseInt (.-value (.getElementById js/document "dataEntryTechnicallyLimited")))
                                                          :meetcredentialingcriteria (js/parseInt (.-value (.getElementById js/document "dataEntryMeetCredentialCriteria")) )                     
                                            }])
                                            (js/console.log "This is invalid")))
                          
                           (clear-data-entry-view)



                         )} "Submit"]])


(def uploaded-csv-file (r/atom ""))
(def uploaded-csv-data (r/atom ""))
(defn uploaded-cvs-file-into-db
  []
    (reset! uploaded-csv-data (.. js/CSV (parse @uploaded-csv-file)))
    (reset! c/csv-data uploaded-csv-data)
    (re-frame/dispatch [:upload-csv-to-app-state])
  )


;;Data Upload
(defn show-data-upload-view
  []
  [:form 
     [:div {:class "custom-control custom-checkbox my-1 mr-sm-2"}
       [:input {:type "checkbox", :class "custom-control-input", :id "customControlInlineReplaceExisting"}]
       [:label {:class "custom-control-label", :for "customControlInlineReplaceExisting"} "Upload and replace existing"]]
        [:div {:class "input-group mb-3"}
           [:div {:class "input-group-prepend"}
            [:span {:class "input-group-text"} "Upload"]]
            [:div {:class "custom-file"}
            [:input {:type "file", :class "custom-file-input", :id "dataUploadFile" 
                     :on-change (fn [e]
                        (let [file (first (array-seq (.. e -target -files)))
                              file-reader (js/FileReader.)]
                             (set! (.-onload file-reader)
                                 (fn [e] 
                                   (reset! uploaded-csv-file (-> e .-target .-result))))
                                     (.readAsText file-reader file)
                                     (set! (.. js/document (getElementById "fileNameToUpload") -innerHTML) (.-name file))
                                    
                                     ) 
                                  )}]
            [:label {:class "custom-file-label", :for "dataUploadFile"} "Choose file"]]]
   [:p {:id "fileNameToUpload"} ""]
      [:button {:type "submit", :class "btn btn-primary my-1"   :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
                                                                :on-click (fn [e] (.preventDefault e) (uploaded-cvs-file-into-db)) 
                } "Submit"]]
  )

;;Data view


(defn show-data-view
  []
  [:div [:nav
  [:div {:class "nav nav-tabs", :id "nav-tab", :role "tablist"}
   [:a {:class "nav-item nav-link active", :id "nav-record-tab", :data-toggle "tab", :href "#nav-record", :role "tab", :aria-controls "nav-record", :aria-selected "true"} "Records"]
 ;  [:a {:class "nav-item nav-link", :id "nav-data-entry-tab", :data-toggle "tab", :href "#nav-data-entry", :role "tab", :aria-controls "nav-data-entry", :aria-selected "false"} "Data Entry"]
   [:a {:class "nav-item nav-link", :id "nav-data-upload-tab", :data-toggle "tab", :href "#nav-data-upload", :role "tab", :aria-controls "nav-data-upload", :aria-selected "false"} "Data Upload"]
   ]]
 [:div {:class "tab-content", :id "nav-tabContent"}
  [:div {:class "tab-pane fade show active", :id "nav-record", :role "tabpanel", :aria-labelledby "nav-record-tab"} (show-data-all-record)]
;  [:div {:class "tab-pane fade", :id "nav-data-entry", :role "tabpanel", :aria-labelledby "nav-data-entry-tab"} (show-data-entry-view )]
  [:div {:class "tab-pane fade", :id "nav-data-upload", :role "tabpanel", :aria-labelledby "nav-data-upload-tab"} (show-data-upload-view)]
  ]])

(defn show-home-report-component
  [id]
    (let [providerid id
        records c/records-data;(re-frame/subscribe [:record-items])
        record-list (filter #(= (js/parseInt providerid) (.-userid  (clj->js %))) (js->clj @records))
        labels-list (atom [])
        data-complete-list (atom [])
        svg (.. js/document (getElementById "personalReport") (getElementsByTagName "svg"))]
        
        (if  (= 0 (.-length svg)) 
             (js/console.log "No Elements") 
             (do 
                (js/console.log "Got it")
                (-> svg
                    (.item 0)
                    (.remove))  
                 ))

        (doall (map #(do
                  (swap! labels-list conj (.-examtype  (clj->js %)))
                  (swap! data-complete-list conj (.-meetcredentialingcriteria  (clj->js %)))
                    
                    ) record-list))
      
      
      (let [draw (js/SVG. "personalReport")
               [w h] [400 1300]
               tiles [
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "Aorta" :x 50 :y 50 :fill "#ec7063" :text  {:value "1" :cx 70 :cy 70 }}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "Appy" :x 150 :y 50 :fill "#9b59b6" :text  {:value "2" :cx 170 :cy 70 }}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "GB" :x 250 :y 50 :fill "#2980b9" :text  {:value "3" :cx 270 :cy 70}}
                      
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "Post Void" :x 50 :y 150 :fill "#17a589" :text  {:value "4" :cx 70 :cy 170}}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "DVT" :x 150 :y 150 :fill "#27ae60" :text  {:value "5" :cx 170 :cy 170}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "EFAST" :x 250 :y 150 :fill "#d4ac0d" :text  {:value "6" :cx 270 :cy 170}}

                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "FAST" :x 50 :y 250 :fill "#f39c12" :text  {:value "7" :cx 70 :cy 270}}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "TAUS" :x 150 :y 250 :fill "#ba4a00" :text  {:value "8" :cx 170 :cy 270}}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "TVUS" :x 250 :y 250 :fill "#839192" :text  {:value "9" :cx 270 :cy 270}} 
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "IVC" :x 50 :y 350 :fill "#283747" :text  {:value "10" :cx 70 :cy 370}}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "ABD" :x 150 :y 350 :fill "#145a32" :text  {:value "11" :cx 170 :cy 370}}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "ECHO" :x 250 :y 350 :fill "#fa8072" :text  {:value "12" :cx 270 :cy 370}}
                     
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "MSK" :x 50 :y 450 :fill "#283747" :text  {:value "13" :cx 70 :cy 470}}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "TAUS" :x 150 :y 450 :fill "#145a32" :text  {:value "14" :cx 170 :cy 470}}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "TVUS" :x 250 :y 450 :fill "#fa8072" :text  {:value "15" :cx 270 :cy 470}}
                     
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "OCULAR" :x 50 :y 550 :fill "#283747" :text  {:value "16" :cx 70 :cy 570}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "ART" :x 150 :y 550 :fill "#145a32" :text  {:value "17" :cx 170 :cy 570}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "Artho" :x 250 :y 550 :fill "#fa8072" :text  {:value "18" :cx 270 :cy 570}}
                      
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "CL" :x 50 :y 650 :fill "#283747" :text  {:value "19" :cx 70 :cy 670}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "FB" :x 150 :y 650 :fill "#145a32" :text  {:value "20" :cx 170 :cy 670}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "LP" :x 250 :y 650 :fill "#fa8072" :text  {:value "21" :cx 270 :cy 670}}
                      
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "NB" :x 50 :y 750 :fill "#283747" :text  {:value "22" :cx 70 :cy 770}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "Para" :x 150 :y 750 :fill "#145a32" :text  {:value "23" :cx 170 :cy 770}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "Thoraco" :x 250 :y 750 :fill "#fa8072" :text  {:value "24" :cx 270 :cy 770}}
                      
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "Peri" :x 50 :y 850 :fill "#283747" :text  {:value "25" :cx 70 :cy 870}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "PIV" :x 150 :y 850 :fill "#145a32" :text  {:value "26" :cx 170 :cy 870}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "PTA" :x 250 :y 850 :fill "#fa8072" :text  {:value "27" :cx 270 :cy 870}}
                      
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "Abscess" :x 50 :y 950 :fill "#283747" :text  {:value "28" :cx 70 :cy 970}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "Supra" :x 150 :y 950 :fill "#145a32" :text  {:value "29" :cx 170 :cy 970}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "Thora" :x 250 :y 950 :fill "#fa8072" :text  {:value "30" :cx 270 :cy 970}}
                      
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "RENAL" :x 50 :y 1050 :fill "#283747" :text  {:value "31" :cx 70 :cy 1070}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "SCROTUM" :x 150 :y 1050 :fill "#145a32" :text  {:value "32" :cx 170 :cy 1070}}
                      {:diameter 50 :base-fill "#ffff00" :score 0 :label "SHOCK" :x 250 :y 1050 :fill "#fa8072" :text  {:value "33" :cx 270 :cy 1070}}
                     
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "ST" :x 50 :y 1150 :fill "#283747" :text  {:value "31" :cx 70 :cy 1170}}
                      {:diameter 50 :base-fill "#ff0000" :score 0 :label "THOARC" :x 150 :y 1150 :fill "#145a32" :text  {:value "32" :cx 170 :cy 1170}}

                      ]]
           
           (.size draw w h)
           
;; row 1 ---------------------------------------------------------    
;; (js/console.log @data-complete-list)
(if-not (empty? @data-complete-list)
(doseq [n (range 35)]
  (-> draw
    (.circle (get-in (assoc-in tiles [n :score] (* 2 (js/parseInt (nth @data-complete-list n)))) [n :score]))
    (.fill (get-in tiles [n :fill]))
    (.move (- (get-in tiles [n :x])  (- (js/parseInt (nth @data-complete-list n)) (/ (get-in tiles [n :diameter]) 2))) 
           (- (get-in tiles [n :y])  (- (js/parseInt (nth @data-complete-list n)) (/ (get-in tiles [n :diameter]) 2)))))

 
 (-> draw 
    (.circle (get-in tiles [n :diameter]))
    (.fill (get-in tiles [n :base-fill]))
    (.move (get-in tiles [n :x]) (get-in tiles [n :y])))

  (-> draw
    (.text (get-in (assoc-in tiles [n :text :value] (nth @data-complete-list n)) [n :text :value]))
    (.move (get-in tiles [n :text :cx]) (get-in tiles [n :text :cy])))
  
 (-> draw 
   ; (.text (nth @labels-list n))
    (.text (get-in tiles [n :label]))
    (.font #js { "family" "Helvetica" "size" "12px" "anchor" "middle" "style" "bold" })
    (.fill "blue")
    (.move (+ (get-in tiles [n :x]) 0) (+ 50 (get-in tiles [n :y])))))
  (do (-> draw 
    (.text "Select a")  
    (.font #js { "family" "Helvetica" "size" "70px" "anchor" "middle" "style" "bold" })
    (.fill "red")
    (.move 250 50))
  (-> draw
    (.text "Provider!")
    (.font #js {"family" "Peoni Pro" "size" "50px" "anchor" "middle" "style" "italic"})
    (.fill "blue")
    (.move 300 150))))
)))     
         
              

(defn home-report-component
[id]
  (r/create-class
   {:component-did-mount #(show-home-report-component id)
    :display-name        "home-report-component"
    :reagent-render      (fn []
                           [:div {:id "personalReport"} 
                                                   ] )}))

  
;;Quality Report
(defn  show-quality-report-chart [id]
  (let [providerid id
        records c/records-data;(re-frame/subscribe [:record-items])
        record-list (filter #(= (js/parseInt providerid) (.-userid  (clj->js %))) (js->clj @records))
        labels-list (atom [])
        data-complete-list (atom [])
        data-tls-list (atom [])]

    (doall (map #(do
                   (swap! labels-list conj (.-examtype  (clj->js %) ))
                   (swap! data-complete-list conj (.-meetcredentialingcriteria  (clj->js %)))
                   (swap! data-tls-list conj (.-tls  (clj->js %)))) record-list))

     (let [context (.getContext (.getElementById js/document "qualityReportChart") "2d")

          chart-data {:type "line"

                      :data {:labels (clj->js @labels-list)
                             :datasets [
                                        {:data (clj->js @data-tls-list)
                                         :label "Limited Studies"
                                         :type "line"
                                         :backgroundColor "#0000FF"}
                                        
                                        {:data (clj->js @data-complete-list)
                                         :label (if (empty? record-list) "Resident" (str (.-firstname  (clj->js (first record-list))) " " (.-lastname  (clj->js (first record-list)))))
                                         :backgroundColor "#F08080"
                                         }
                                        ]}
                      :options {:events ["click"]}}]
      (.clearRect context 0 0 700 380)
      (js/Chart. context (clj->js chart-data)))))
      

(defn quality-report-chartjs-component
  [id]
  (r/create-class
   {:component-did-mount #(show-quality-report-chart id)
    :display-name        "quality-report-chartjs-component"
    :reagent-render      (fn []
                           [:canvas {:id "qualityReportChart" :width "700" :height "380"}])}))

;;Overall Statistics

(defn show-overall-chart [id]
  (let [providerid id
        records c/records-data ;(re-frame/subscribe [:record-items])
        record-list (filter #(= (js/parseInt providerid) (.-userid  (clj->js %))) (js->clj @records))
        labels-list (atom [])
        data-list (atom [])]

    (doall (map #(do (swap! labels-list conj (.-examtype (clj->js %))) (swap! data-list conj (.-meetcredentialingcriteria (clj->js %)))) record-list))
   
  ;  (if-not (empty? record-list) (prn (clj->js (first record-list))))

    (let [context (.getContext (.getElementById js/document  "overallChart") "2d")

          chart-data {:type "bar"

                      :data {:labels (clj->js @labels-list)
                             :datasets [
                                         {:data (clj->js @data-list)
                                         :label (if (empty? record-list) "Resident" (str (.-firstname  (clj->js (first record-list))) " " (.-lastname  (clj->js (first record-list)))))
                                         :backgroundColor "#F08080"}
                                          {:data [25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25 25]
                                           :label "Finish Line"
                                           :backgroundColor "#0000FF"
                                           :type "line"
                                           :fill false
                                           }
                                        ]}
                      :options {:events ["click"]}}]

      (js/Chart. context (clj->js chart-data)))))

(defn overall-chartjs-component
  [id]
  (r/create-class
   {
    :component-did-mount #(show-overall-chart id)
    :display-name        "overall-chartjs-component"
    :reagent-render      (fn []
                            [:canvas {:id "overallChart" :width "700" :height "380"}]
                           )}))


(defn show-overall-residency-chart [examtype]
  (let [
        providers (re-frame/subscribe [:provider-items])
        labels-list  (doall (map #(str (.-firstname (aget (clj->js %)1 )) " " (.-lastname  (aget (clj->js %)1)))  (js->clj @@providers)))                    
        labels-lastname-list  (doall (map #(str (.-lastname  (aget (clj->js %) 1)))  (js->clj @@providers)))
        data-list (doall (map #(stats/total-by-examtype-and-by-provider examtype (clj->js %)) labels-lastname-list))]

      
    
    (let [context (.getContext (.getElementById js/document  (str "overallResidencyChart" examtype)) "2d")

          chart-data {:type "bar"

                      :data {:labels (clj->js labels-list)
                             :datasets [{:data (clj->js data-list)
                                         :label (str "Overall " examtype)
                                         :backgroundColor "#F08080"}
                                        ]}
                      :options {:events ["click"]}}]
      (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "hidden")
      
      (js/Chart. context (clj->js chart-data)))))


(defn show-overall-residency-chart-by-examtype
[examtype-name]
    (r/create-class
     {:component-did-mount #(show-overall-residency-chart examtype-name)
      :display-name        (str "overall-residency-chartjs-component" examtype-name)
      :reagent-render      (fn []
                             [:canvas {:id (str "overallResidencyChart" examtype-name) :width "700" :height "380"}])}))

(defn show-quality-residency-chart [examtype]
  (let [
        providers (re-frame/subscribe [:provider-items])
        labels-list  (doall (map #(str (.-firstname (aget (clj->js %)1 )) " " (.-lastname  (aget (clj->js %)1)))  (js->clj @@providers)))                    
        labels-lastname-list  (doall (map #(str (.-lastname  (aget (clj->js %) 1)))  (js->clj @@providers)))
        data-list (doall (map #(stats/total-by-examtype-and-by-provider examtype (clj->js %)) labels-lastname-list))
        tls-list (doall (map #(stats/total-tls-by-examtype-and-by-provider examtype (clj->js %)) labels-lastname-list))
        ]

    
    (let [context (.getContext (.getElementById js/document  (str "qualityResidencyChart" examtype)) "2d")

          chart-data {:type "line"

                      :data {:labels (clj->js labels-list)
                             :datasets [
                                       
                                        {:data (clj->js tls-list)
                                         :label (str "Quality " examtype)
                                         :backgroundColor "#0000FF"}
                                       
                                        {:data (clj->js data-list)
                                         :label (str "Quality " examtype)
                                         :backgroundColor "#F08080"}
                                        
                                        ]}
                      :options {:events ["click"]}}]
      (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "hidden")
      
      (js/Chart. context (clj->js chart-data)))))


(defn show-quality-residency-chart-by-examtype
[examtype-name]
    (r/create-class
     {:component-did-mount #(show-quality-residency-chart examtype-name)
      :display-name        (str "quality-residency-chartjs-component" examtype-name)
      :reagent-render      (fn []
                             [:canvas {:id (str "qualityResidencyChart" examtype-name) :width "700" :height "380"}])}))

(defn show-overall-residency
[]
  [:div
          [show-overall-residency-chart-by-examtype "POCUS_MSK"]
           [show-overall-residency-chart-by-examtype "POCUS_OB_TAUS"]
           [show-overall-residency-chart-by-examtype "POCUS_OB_TVUS"]
           [show-overall-residency-chart-by-examtype "POCUS_Ocular"]
           [show-overall-residency-chart-by-examtype "POCUS_Renal"]
           [show-overall-residency-chart-by-examtype "POCUS_Thoracic"]
           [show-overall-residency-chart-by-examtype "POCUS_Abdominal_Aorta"]
           [show-overall-residency-chart-by-examtype "POCUS_Biliary"]
           [show-overall-residency-chart-by-examtype "POCUS_EFAST"]
           [show-overall-residency-chart-by-examtype "POCUS_FAST"]
           [show-overall-residency-chart-by-examtype "POCUS_Limited_Echocardiography"]])

(defn list-overall-residency-component
  []
  [:div {:class "container"}
    [:div {:class "row"}
     [:div {:class "col"} 
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/Gray410_6x5.png" :alt "MSK"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "MSK"]
        [:p {:class "card-text"} "Reports the overall results of MSK point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_MSK"]
                                          (.getElementById js/document "residencyApplicationChart" ))) 
            
              } "Show Report"]]]]
      ]
     [:div {:class "col"} 
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/Gray589_6x5.png" :alt "TAUS"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "TAUS"]
        [:p {:class "card-text"} "Reports the overall results of TAUS point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_OB_TAUS"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
      ]
     [:div {:class "col"} 
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/Gray589_6x5.png" :alt "TVUS"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "TVUS"]
        [:p {:class "card-text"} "Reports the overall results of TVUS point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_OB_TVUS"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
      ]
     ]
    [:div {:class "row"}
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/Gray1205.png" :alt "Ocular"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Ocular"]
        [:p {:class "card-text"} "Reports the overall results of Ocular point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_Ocular"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      ]
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image390_6x5.gif" :alt "Ocular"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Thoracic"]
        [:p {:class "card-text"} "Reports the overall results of Thoracic point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_Thoracic"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      ]
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image501_6x5.gif" :alt "Cardiac"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Cardiac"]
        [:p {:class "card-text"} "Reports the overall results of Cardiac point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_Limited_Echocardiography"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
      
      ]]
   [:div {:class "row"}
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image531.jpg" :alt "Renal"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Renal"]
        [:p {:class "card-text"} "Reports the overall results of Renal point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_Renal"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      
      ]
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image1121_6x5.gif" :alt "Aorta"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Aorta"]
        [:p {:class "card-text"} "Reports the overall results of Aorta point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_Abdominal_Aorta"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      ]
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image1111_6x5.gif" :alt "Biliary"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Biliary"]
        [:p {:class "card-text"} "Reports the overall results of Biliary point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary":data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_Biliary"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      
      
      ]]
   [:div {:class "row"}
    [:div {:class "col"} 
     
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image1219_6x5.jpg" :alt "EFAST"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "EFAST"]
        [:p {:class "card-text"} "Reports the overall results of EFAST point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_EFAST"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
     
     ]
    [:div {:class "col"} 
    
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image1219_6x5.jpg" :alt "FAST"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "FAST"]
        [:p {:class "card-text"} "Reports the overall results of FAST point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_FAST"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
     
     ]
    [:div {:class "col"} 
     
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image545_6x5.gif" :alt "DVT"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "DVT"]
        [:p {:class "card-text"} "Reports the overall results of DVT point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyOverallModal"

              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-overall-residency-chart-by-examtype "POCUS_DVT"]
                                          (.getElementById js/document "residencyApplicationChart" )))
             
             } "Show Report"]]]]
     
     
     ]]])

;;Quality Reports for the residency --------------------------------

(defn list-quality-residency-component
  []
  [:div {:class "container"}
    [:div {:class "row"}
     [:div {:class "col"} 
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/Gray410_6x5.png"  :alt "MSK"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "MSK"]
        [:p {:class "card-text"} "Reports the overall results of MSK point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary"  :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_MSK"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
            
              } "Show Report"]]]]
      ]
     [:div {:class "col"} 
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/Gray589_6x5.png"  :alt "TAUS"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "TAUS"]
        [:p {:class "card-text"} "Reports the overall results of TAUS point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_OB_TAUS"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
      ]
     [:div {:class "col"} 
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/Gray589_6x5.png" :alt "TVUS"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "TVUS"]
        [:p {:class "card-text"} "Reports the overall results of TVUS point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_OB_TVUS"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
      ]
     ]
    [:div {:class "row"}
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/Gray1205.png" :alt "Ocular"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Ocular"]
        [:p {:class "card-text"} "Reports the overall results of Ocular point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_Ocular"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      ]
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image390_6x5.gif" :alt "Ocular"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Thoracic"]
        [:p {:class "card-text"} "Reports the overall results of Thoracic point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_Thoracic"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      ]
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image501_6x5.gif" :alt "Cardiac"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Cardiac"]
        [:p {:class "card-text"} "Reports the overall results of Cardiac point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_Limited_Echocardiography"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
      
      ]]
   [:div {:class "row"}
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image531.jpg" :alt "Renal"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Renal"]
        [:p {:class "card-text"} "Reports the overall results of Renal point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_Renal"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      
      ]
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image1121_6x5.gif" :alt "Aorta"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Aorta"]
        [:p {:class "card-text"} "Reports the overall results of Aorta point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary":data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_Abdominal_Aorta"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      ]
     [:div {:class "col"} 
      
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image1111_6x5.gif" :alt "Biliary"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "Biliary"]
        [:p {:class "card-text"} "Reports the overall results of Biliary point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary":data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_Biliary"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
      
      
      
      
      ]]
   [:div {:class "row"}
    [:div {:class "col"} 
     
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image1219_6x5.jpg" :alt "EFAST"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "EFAST"]
        [:p {:class "card-text"} "Reports the overall results of EFAST point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_EFAST"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
     
     ]
    [:div {:class "col"} 
    
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image1219_6x5.jpg" :alt "FAST"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "FAST"]
        [:p {:class "card-text"} "Reports the overall results of FAST point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_FAST"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
     
     ]
    [:div {:class "col"} 
     
      [:div 
      [:div {:class "card", :style {:width "100%" :height "100%"}}
       [:img {:class "card-img-top", :src "images/image545_6x5.gif" :alt "DVT"}]
       [:div {:class "card-body"}
        [:h5 {:class "card-title"} "DVT"]
        [:p {:class "card-text"} "Reports the overall results of DVT point-of-care ultrasound"]
        [:a {:href "#", :class "btn btn-primary" :data-toggle "modal" :data-target "#residencyQualityModal"
              :on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible")) 
              :on-click (fn [e] (r/render 
                                          [show-quality-residency-chart-by-examtype "POCUS_DVT"]
                                          (.getElementById js/document "residencyQualityApplicationChart" )))
             
             } "Show Report"]]]]
     
     
     ]]])







;;Statistics Residency Panel
(defn show-residency-statistics-view
[]
[:div [:nav
       [:div {:class "nav nav-tabs", :id "nav-tab", :role "tablist"  }
         ; [:a {:class "nav-item nav-link active ", :id "nav-residency-home-tab", :data-toggle "tab", :href "#nav-residency-home", :role "tab", :aria-controls "nav-residency-home", :aria-selected "false"} "Home"]       
         [:a {:class "nav-item nav-link active ", :id "nav-residency-overall-tab", :data-toggle "tab", :href "#nav-residency-overall", :role "tab", :aria-controls "nav-residency-overall", :aria-selected "true"
              ;:on-mouse-down (fn [e]  (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "visible"))
              ;:on-click (fn [e] (r/render  [show-overall-residency] (.getElementById js/document "nav-residency-overall" )))
             } "Overall"]
        [:a {:class "nav-item nav-link ", :id "nav-residency-quality-report-tab", :data-toggle "tab", :href "#nav-residency-quality-report", :role "tab", :aria-controls "nav-residency-quality-report", :aria-selected "false"} "Quality Report"]
        ]]
 [:div {:class "tab-content", :id "nav-tabContent"}
  ; [:div {:class "tab-pane fade show active", :id "nav-residency-home", :role "tabpanel", :aria-labelledby "nav-residency-home-tab"} "Home" ]
   [:div {:class "tab-pane fade show active", :id "nav-residency-overall", :role "tabpanel", :aria-labelledby "nav-residency-overall-tab"} [list-overall-residency-component]
   
  [:div {:class "modal fade", :id "residencyOverallModal", :tabIndex "-1", :role "dialog", :aria-labelledby "residencyOverallModalLabel", :aria-hidden "true"}
   [:div {:class "modal-dialog modal-lg", :role "document"}
   [:div {:class "modal-content"}
   [:div {:class "modal-header"}
    [:h5 {:class "modal-title", :id "residencyOverallModalLabel"} "Report"]
    [:button {:type "button", :class "close", :data-dismiss "modal", :aria-label "Close"}
    [:span {:aria-hidden "true"} "×"]]]
   [:div {:class "modal-body"} 
    [:div {:id "residencyApplicationChart"}]]
   [:div {:class "modal-footer"}
    [:button {:type "button", :class "btn btn-secondary", :data-dismiss "modal"} "Close"]
    ]]]]]
  [:div {:class "tab-pane fade ", :id "nav-residency-quality-report", :role "tabpanel", :aria-labelledby "nav-residency-quality-report-tab"} [list-quality-residency-component]  
 
   
  [:div {:class "modal fade", :id "residencyQualityModal", :tabIndex "-1", :role "dialog", :aria-labelledby "residencyQualityModalLabel", :aria-hidden "true"}
   [:div {:class "modal-dialog modal-lg", :role "document"}
   [:div {:class "modal-content"}
   [:div {:class "modal-header"}
    [:h5 {:class "modal-title", :id "residencyQualityModalLabel"} "Report"]
    [:button {:type "button", :class "close", :data-dismiss "modal", :aria-label "Close"}
    [:span {:aria-hidden "true"} "×"]]]
   [:div {:class "modal-body"} 
    [:div {:id "residencyQualityApplicationChart"}]]
   [:div {:class "modal-footer"}
    [:button {:type "button", :class "btn btn-secondary", :data-dismiss "modal"} "Close"]
    ]]]] 
 
 ]
  ]])
  
;;Statistics Panel
(defn show-statistics-view
  []
[:div [:nav
       [:div {:class "nav  nav-tabs", :id "nav-tab", :role "tablist" }
        [:a {:class "nav-item nav-link active", :id "nav-home-tab", :data-toggle "tab", :href "#nav-home", :role "tab", :aria-controls "nav-home", :aria-selected "true"} "Home"]
        [:a {:class "nav-item nav-link", :id "nav-overall-tab", :data-toggle "tab", :href "#nav-overall", :role "tab", :aria-controls "nav-overall", :aria-selected "true"} "Overall"]
        [:a {:class "nav-item nav-link ", :id "nav-quality-report-tab", :data-toggle "tab", :href "#nav-quality-report", :role "tab", :aria-controls "nav-quality-report", :aria-selected "false"} "Quality Report"]
        ;[:a {:class "nav-item nav-link", :id "nav-letter-tab", :data-toggle "tab", :href "#nav-letter", :role "tab", :aria-controls "nav-letter", :aria-selected "false"} "Letter"]
        ]]
 [:div {:class "tab-content", :id "nav-tabContent"}
  [:div {:class "tab-pane fade show active", :id "nav-home", :role "tabpanel", :aria-labelledby "nav-home-tab"}  [home-report-component @current-provider-id]]
  [:div {:class "tab-pane fade", :id "nav-overall", :role "tabpanel", :aria-labelledby "nav-overall-tab"} [overall-chartjs-component @current-provider-id]]
  [:div {:class "tab-pane fade ", :id "nav-quality-report", :role "tabpanel", :aria-labelledby "nav-quality-report-tab"} [quality-report-chartjs-component @current-provider-id]]
  ;[:div {:class "tab-pane fade", :id "nav-letter", :role "tabpanel", :aria-labelledby "nav-letter-tab"} "Letter"]
  ]])


(defn show-provider-list-to-select
  [provider]
  [:a {:key (.-userid provider) :id (.-userid provider) :class "dropdown-item", :data-provider (.-userid provider)  :href "#" 
        :on-click (fn [e] (.preventDefault e)
                   (js/console.log (-> e .-target .-id))    
                   (reset! current-provider-id (-> e .-target .-id)) 
                   (set! (.. js/document (getElementById "selectedPersonForStatistics") -innerHTML) (-> e .-target .-innerHTML)))
               } (str (.-firstname provider) " "(.-lastname provider))])


(def screen-state (r/atom 0))

;;Indiviual screen

(defn personal-panel
  [id]
[:div  
                    (let [current-user (re-frame/subscribe [:user-current])]
                     [:div 
                      [:div {:class "jumbotron jumbotron-fluid"}
                      [:div {:class "container"}
                      [:h1 {:class "display-4"} (str (:firstname @current-user)" "(:lastname @current-user) )]
                      [:p {:class "lead"} 
                       "Please review each graph carefully. The first graph/chart is a reference.
                       You must have more than 25 scans in each area except for procedure which is only 10 scans.
                       The last graph shows you a quick look at your quality metrics."]
                       [:hr {:class "my-4"}]
                       [:p "Please do not refresh the page."]
                       [:a {:class "btn btn-primary btn-lg", :href "#", :role "button" 
                            :on-click (fn [e]
                                        (.preventDefault e)
                                        (re-frame/dispatch [:user-logout])  
                                       )} "Logout"]]] 
                     [home-report-component id ]
                     [overall-chartjs-component id ]
                     [quality-report-chartjs-component id]])
                     ])


;; Home Panel
(defn home-panel []
  (let [providers (re-frame/subscribe [:provider-items] )
        sorted-providers (sort-by #(.-lastname (aget (clj->js %) 1)) (js->clj @@providers))
        ]
      
       (re-frame/dispatch [:setup-database]) ;this loads all the information from database

              
     (if-not (nil? (sess/get-item :user))
        (let [user (sess/get-item :user)
           current-user (cljs.reader/read-string user)]
           (re-frame/dispatch[:user-refresh current-user])
           (if (= (:level current-user) "director") 
             (reset! screen-state 0) ;this is for the director page
            (do 
              (reset! current-provider-id (:userid current-user))         
              (reset! screen-state 1)));this is for the resident page
           )(reset! screen-state 2)); this is for the login page

(cond
(= @screen-state 0)[:div { :id "adminScreen"}  
[:nav {:class "navbar navbar-expand-sm navbar-dark bg-info justify-content-between" }
 [:a {:class "navbar-brand", :href "#"} "Jabiru"]
 [:button {:class "navbar-toggler", :type "button", :data-toggle "collapse", :data-target "#navbarNavAltMarkup", :aria-controls "navbarNavAltMarkup", :aria-expanded "false", :aria-label "Toggle navigation"}
  [:span {:class "navbar-toggler-icon"}]]
 [:div {:class "collapse navbar-collapse", :id "navbarNavAltMarkup"}
  [:div {:class "navbar-nav"}
   [:form {:class "form-inline my-2 my-lg-0"}
       [:li {:class "nav-item dropdown"}
          [:a {:class "nav-link dropdown-toggle", :href "#", :id "navbarDropdown", :role "button", :data-toggle "dropdown", :aria-haspopup "true", :aria-expanded "false"}
            [:i {:class "fa fa-users", :aria-hidden "true"}] " Select a Providers"
           ]
          [:div {:class "dropdown-menu", :aria-labelledby "navbarDropdown"} 
           (map #(show-provider-list-to-select (aget (clj->js %) 1)) sorted-providers)
           ]]
       [:div {:id "selectedPersonForStatistics" :class "text-warning clearfix" :data-value 0 :style {:padding-right "10px" :padding-left "10px"}} " No one Selected "]
      ] 
       [:a {:class "btn btn-primary "  :href "#" :role "button"
                        :on-click (fn [e] 
                                    (.preventDefault e)
                                    (js/console.log "Loading report ..... for " @current-provider-id)
                                   (show-home-report-component @current-provider-id ) 
                                   (show-overall-chart @current-provider-id)
                                   (show-quality-report-chart @current-provider-id)
                                    )} "Go"]
    [:button {:class "btn btn-danger my-2 my-sm-0", :type "submit"
             :on-click (fn [e]
                                        (.preventDefault e)
                                        (re-frame/dispatch [:user-logout])  
                                       ) 
              
              } "Logout"]
   ]]]
    [:div {:class "row"}
      [:div {:class "col-3 bg-white"}
       [:div {:class "nav bg-while flex-column nav-pills" :id "v-pills-tab" :role "tablist" :aria-orientation "vertical"}
         [:a {:class "nav-link  " :id "v-pills-statistics-tab" :data-toggle "pill" :href "#v-pills-statistics" :role "tab" :aria-controls "v-pills-statistics" :aria-selected "true"}[:i {:class "fa fa-users" :aria-hidden "true"}] " Personal"]
         [:a {:class "nav-link active" :id "v-pills-resident-tab" :data-toggle "pill" :href "#v-pills-resident" :role "tab" :aria-controls "v-pills-resident" :aria-selected "true"}  [:i {:class "fa fa-hospital-o" :aria-hidden "true"}] " Residency"] 
         [:a {:class "nav-link " :id "v-pills-data-tab" :data-toggle "pill" :href "#v-pills-data" :role "tab" :aria-controls "v-pills-data" :aria-selected "true"}[:i {:class "fa fa-database" :aria-hidden "true"}]  " Data"]
        ; [:a {:class "nav-link" :id "v-pills-calendar-tab" :data-toggle "pill" :href "#v-pills-calendar" :role "tab" :aria-controls "v-pills-calendar" :aria-selected "true"} "Calendar"]
         ;[:a {:class "nav-link" :id "v-pills-education-tab" :data-toggle "pill" :href "#v-pills-education" :role "tab" :aria-controls "v-pills-education" :aria-selected "true"} "Education"]
         [:a {:class "nav-link" :id "v-pills-providers-tab" :data-toggle "pill" :href "#v-pills-providers" :role "tab" :aria-controls "v-pills-providers" :aria-selected "true"}[:i {:class "fa fa-user-md" :aria-hidden "true"}] " Providers"]
         [:a {:class "nav-link " :id "v-pills-settings-tab" :data-toggle "pill" :href "#v-pills-settings" :role "tab" :aria-controls "v-pills-settings" :aria-selected "true"}[:i {:class "fa fa-cogs" :aria-hidden "true"}] " Settings"]
         [:a {:class "nav-link" :id "v-pills-setup-tab" :data-toggle "pill" :href "#v-pills-setup" :role "tab" :aria-controls "v-pills-setup" :aria-selected "true"}[:i {:class "fa fa-wrench" :aria-hidden "true"}] " Setup"]]]
      [:div {:class "col-9" :style {:background-color "#fffff0"}}
       [:div {:class "tab-content" :id "nav-tabContent"}
         [:div {:class "tab-pane fade " :id "v-pills-statistics" :role "tabpanel" :aria-labelledby "v-pills-statistics-tab"} (show-statistics-view)  
          ]
          [:div {:class "tab-pane fade show active" :id "v-pills-resident" :role "tabpanel" :aria-labelledby "v-pills-resident-tab"} (show-residency-statistics-view) 
           ]
          [:div {:class "tab-pane fade" :id "v-pills-data" :role "tabpanel" :aria-labelledby "v-pills-data-tab"} (show-data-view)
           ]
          ;[:div {:class "tab-pane fade" :id "v-pills-calendar" :role "tabpanel" :aria-labelledby "v-pills-calendar-tab"} "Calendar"]
          ;[:div {:class "tab-pane fade" :id "v-pills-education" :role "tabpanel" :aria-labelledby "v-pills-education-tab"} "Education"]
          [:div {:class "tab-pane fade " :id "v-pills-providers" :role "tabpanel" :aria-labelledby "v-pills-providers-tab"} (show-provider-view)
          ]
         [:div {:class "tab-pane fade " :id "v-pills-settings" :role "tabpanel" :aria-labelledby "v-pills-settings-tab"} (show-settings-view)
          ]
         [:div {:class "tab-pane fade" :id "v-pills-setup" :role "tabpanel" :aria-labelledby "v-pills-setup-tab"} (show-setup-view)]
        ]
         
           [:div {:id "activityIndicator" :style {:visibility "hidden"}} [:i {:class "fa fa-spinner fa-spin fa-3x fa-fw" :aria-hidden "true"}]] 
       ]]]
(= @screen-state 1) 
[:div {:id "residentScreen"} [personal-panel @current-provider-id]]
:else [:div {:id "loginScreen" } 
       [:div {:class "jumbotron jumbotron-fluid"}
        [:div {:class "container"}
          [:h1 {:class "display-4"} "Jabiru"]
          [:p {:class "lead"} "This is a tool used to visualize Q-path data."]]]
       [:form {:style {:margin-right "150px" :margin-left "150px"}} 
        [:div {:class "form-group"}
          [:label {:for "txtInputEmail"} "Email address"]
          [:input {:type "email", :class "form-control", :id "txtInputEmail", :aria-describedby "emailHelp", :placeholder "Enter email"}]
          [:small {:id "emailHelp", :class "form-text text-muted"} "Please supply the email that you were given."]]
        [:div {:class "form-group"}
          [:label {:for "exampleInputPassword1"} "Password"]
          [:input {:type "password", :class "form-control", :id "txtInputPassword", :placeholder "Password"}]]
        [:button {:type "submit", :class "btn btn-primary"
                  :on-click (fn [e]
                              (let [ email (.-value (.getElementById  js/document "txtInputEmail"))
                                     pass  (.-value (.getElementById  js/document "txtInputPassword"))] 
                                (.preventDefault e)
                               
                                (re-frame/dispatch [:login {:email email :pass pass}])

                                ))} "Submit"]]])
    
))

;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
