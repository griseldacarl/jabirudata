(ns jabiru.csv
  (:require
   [jabiru.config :as config]
   [jabiru.firebase :as f]
   [re-frame.core :as re-frame]
   [cljsjs.csv]
   [reagent.core :as r]))

(def csv-data (r/atom "")) ;;all of the data from te file
(def csv-providers (r/atom [])) ;;the total list of providers
(def csv-records (r/atom []))
(def records-data (r/atom []))
(def provider-userids (r/atom #{}))


(def record-keys [:recordid         ;; 27
                  :userid           ;; 0
                  :name             ;; 1
                  :firstname        ;; 3
                  :lastname         ;; 2
                  :examtypeid       ;; 4
                  :examtype         ;; 5
                  :examsperformed   ;; 6
                  :examrequired     ;; 7
                  :examqasubmitted  ;; 8
                  :examqacompleted  ;; 9
                  :examsqaskipped   ;; 10
                  :exambilled       ;; 11
                  :examsattended    ;; 12
                  :examsreviewed    ;; 13
                  :examsuserall     ;; 14
                  :examsremaining   ;; 16
                  :percentqacompleted  ;;17
                  :percentbilled       ;;18
                  :percentuserall      ;;19
                  :percentypeall       ;;20
                  :tn  ;;21
                  :tp  ;;22
                  :fp  ;;23
                  :fn  ;;24
                  :tls ;;25
                  :meetcredentialingcriteria ;;26
])


(def record-pos {:recordid  27
                 :userid  0
                 :name    1
                 :firstname  3
                 :lastname   2
                 :examtypeid  4
                 :examtype    5
                 :examsperformed  6
                 :examrequired  7
                 :examqasubmitted   8
                 :examqacompleted  9
                 :examsqaskipped  10
                 :exambilled   11
                 :examsattended  12
                 :examsreviewed  13
                 :examsuserall   14
                 :examsremaining  16
                 :percentqacompleted 17
                 :percentbilled 18
                 :percentuserall 19
                 :percentypeall  20
                 :tn 21
                 :tp 22
                 :fp 23
                 :fn 24
                 :tls 25
                 :meetcredentialingcriteria 26})

(def provider-keys [:all_threshold_reached
                    :firstname
                    :lastname
                    :number_of_technically_limited
                    :number_of_ultrasound_completed
                    :sdots_completed
                    :us_advanced_rotation
                    :us_rotation_completed
                    :us_teaching_completed
                    :userid])

(defn create-list-of-providers
  []
  (doall (map #(hash-map
                :firstname (aget (clj->js %)  (:firstname record-pos))
                :lastname (aget (clj->js %)  (:lastname record-pos))
                :number_of_technically_limited (aget (clj->js %)  (:tls record-pos))
                :number_of_ultrasound_completed (aget (clj->js %)  (:meetcredentialingcriteria record-pos))
                :sdots_completed false
                :us_advanced_rotation false
                :us_rotation_completed false
                :us_teaching_completed false
                :userid (aget (clj->js %)  (:userid record-pos)))  (rest (js->clj @@csv-data)))))
(defn generate-unique-provider
  [providers]
  (let  [firstname (.-firstname (clj->js (first providers)))
         lastname  (.-lastname (clj->js (first providers)))
         number_of_technically_limited (.-number_of_technically_limited (clj->js (first providers)))
         userid (.-userid (clj->js (first providers)))
         number_of_ultrasound_completed (reduce + (doall (map #(.-number_of_ultrasound_completed (clj->js %)) (js->clj providers))))]
    (hash-map
     :firstname firstname
     :lastname lastname
     :number_of_technically_limited number_of_technically_limited
     :number_of_ultrasound_completed number_of_ultrasound_completed
     :sdots_completed false
     :us_advanced_rotation false
     :us_rotation_completed false
     :us_teaching_completed false
     :userid userid)))

(defn generate-provider-list
  [providers userid]
  (clj->js (filter #(= userid (.-userid (clj->js %))) (js->clj providers))))

(defn consolidate-list-of-providers
  []
  "generate a summary list of providers"
  (let [providers (create-list-of-providers)]
    (doall (map #(swap! provider-userids conj (.-userid (clj->js %))) providers))
    (clj->js (doall (map #(clj->js (generate-unique-provider (generate-provider-list providers %))) @provider-userids)))))

(defn create-list-of-records []
  (doall (map #(hash-map
                :recordid           (aget (clj->js %)  (:recordid record-pos))
                :userid             (aget (clj->js %)  (:userid record-pos))
                :name               (aget (clj->js %)  (:name record-pos))
                :firstname          (aget (clj->js %)  (:firstname record-pos))
                :lastname           (aget (clj->js %)  (:lastname record-pos))
                :examtypeid         (aget (clj->js %)  (:examtypeid record-pos))
                :examtype           (aget (clj->js %)  (:examtype record-pos))
                :examsperformed     (aget (clj->js %)  (:examsperformed record-pos))
                :examrequired       (aget (clj->js %)  (:examrequired record-pos))
                :examqasubmitted    (aget (clj->js %)  (:examqasubmitted record-pos))
                :examqacompleted    (aget (clj->js %)  (:examqacompleted record-pos))
                :examsqaskipped     (aget (clj->js %)  (:examsqaskipped record-pos))
                :exambilled         (aget (clj->js %)  (:exambilled record-pos))
                :examsattended      (aget (clj->js %)  (:examsattended record-pos))
                :examsreviewed      (aget (clj->js %)  (:examsreviewed record-pos))
                :examsuserall       (aget (clj->js %)  (:examsuserall record-pos))
                :examsremaining     (aget (clj->js %)  (:examsremaining record-pos))
                :percentqacompleted (aget (clj->js %)  (:percentqacompleted record-pos))
                :percentbilled      (aget (clj->js %)  (:percentbilled record-pos))
                :percentuserall     (aget (clj->js %)  (:percentuserall record-pos))
                :percentypeall      (aget (clj->js %)  (:percentypeall record-pos))
                :tn                 (aget (clj->js %)  (:tn record-pos))
                :tp                 (aget (clj->js %)  (:tp record-pos))
                :fp                 (aget (clj->js %)  (:fp record-pos))
                :fn                 (aget (clj->js %)  (:fn record-pos))
                :tls                (aget (clj->js %)  (:tls record-pos))
                :meetcredentialingcriteria (aget (clj->js %)  (:meetcredentialingcriteria record-pos)))  (rest (js->clj @@csv-data)))))

(defn create-list-of-examtypes
  []
  (doall (map #(hash-map  :examtype  (aget (clj->js %)  (:examtype record-pos)) :examtypeid         (aget (clj->js %)  (:examtypeid record-pos)))
              (rest (js->clj @@csv-data)))))

(defn consolidate-list-of-examtypes []
  (let [examtypes  (create-list-of-examtypes)
        ids (into #{} (doall (map #(:examtypeid %) examtypes)))]
    (clj->js (doall (for [id ids] (first (doall (filter #(= id (:examtypeid %)) examtypes))))))))

(defn upload
  []
  (let [providers (consolidate-list-of-providers)
        records (create-list-of-records)
        examtypes  (consolidate-list-of-examtypes)]


    (js/console.log "clearing the provider database")
    (f/clear-providers-from-database!)
    
    
    (js/console.log "saving examtypes to the database")
    (re-frame/dispatch [:upload-examtypes examtypes])

    ;;save providers
    (js/console.log "saving providers to database....")
    ;(re-frame/dispatch [:upload-providers providers])
    ;(js/console.log providers)
    (doall (map #(f/save-providers-to-database! "institution/providers" (clj->js %)) (js->clj providers)))
    ;;save records
    (js/console.log "saving records to database")
    (reset! records-data (clj->js records))

    (re-frame/dispatch [:upload-records (clj->js records)])

    (f/load-csv-file-to-storage "qpath.csv" (clj->js @@csv-data))
    ;; The records must be a lazy load

    (js/console.log "done!")
    (set! (.-visibility (.-style (.getElementById js/document "activityIndicator"))) "hidden")))

