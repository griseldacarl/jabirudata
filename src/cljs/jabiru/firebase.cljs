(ns jabiru.firebase
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [jabiru.config :as config]
   [cljsjs.firebase]
   [cljs.reader :as reader]
   [cljsjs.csv]
   [cljs-http.client :as http]
   [cljs.core.async :as async :refer [put! chan <! >! timeout close!]]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]))


(defn init-firebase
  []
  (js/firebase.initializeApp
   #js {:apiKey "<your-information>"
        :authDomain "<your-information>"
        :databaseURL "<your-information>"
        :projectId "<your-information>"
        :storageBucket "<your-information>"
        :messagingSenderId "<your-information>"}))
     

                         

(def firebase-csv-data (reagent/atom "")) ;;all of the data from te file


(defn login 
  [email pass]
 (.catch (.then (.signInWithEmailAndPassword (js/firebase.auth) email pass)
          (fn [firebase-user] (re-frame/dispatch [:user-in-database (-> firebase-user .-user .-email)])))
          (fn [error] 
            (js/console.log (-> error .-code)
            (re-frame/dispatch [:user-not-in-database])))))


(defn db-ref [path]
  (.ref (js/firebase.database) (clojure.string/join "/" path)))

(defn storage-ref [path]
  (.child (.ref (js/firebase.storage)) (str path)))

(defn load-csv-file-to-storage
  [path csv-file-string]
  (let [ref (storage-ref path)
        csv-file (js/Blob.  (make-array csv-file-string) (js-obj "type" "text/csv"))]
    (.put ref csv-file)))


(defn get-download-url
  [path]
  (let [ref (storage-ref path)]
    (.getDownloadURL ref)))




(def csv-vec-records (atom []))
(def csv-vec-providers (atom []))
(def csv-vec-examtypes (atom []))
(def csv-providers (atom []))
(def csv-examtypes (atom  []))


(defn load-csv-vec
  [[UserId UserName_last UserName_first UserLastName UserFirstName
    ExamTypeId ExamType ExamsPerformed
    ExamsRequired ExamsQASubmitted ExamsQACompleted
    ExamsQASkipped ExamsBilled ExamsAttend
    ExamsReviewed ExamsUseAll ExamsR ExamsRemaining
    PercentQACompleted PercentBilled PercentUserAll
    PercentTypeAll TN TP FP FN TLS MeetsCriteria ID]]
  (do
    (swap! csv-vec-records conj (hash-map :userid (js/parseInt UserId) :lastname UserLastName :firstname UserFirstName :examtypeid ExamTypeId
                                          :examtype ExamType :examsperformed ExamsPerformed :examrequired ExamsRequired
                                          :examqasubmitted ExamsQASubmitted :examqacompleted ExamsQACompleted :examsqaskipped ExamsQASkipped
                                          :exambilled ExamsBilled :examsattended ExamsAttend :examsreviewed ExamsReviewed
                                          :examsuserall ExamsUseAll :examsremaining ExamsRemaining :percentqacompleted PercentQACompleted
                                          :percentbilled PercentBilled :percentuserall PercentUserAll :percentypeall PercentTypeAll
                                          :tn TN :tp TP :fp FP :fn FN :tls TLS :meetcredentialingcriteria  MeetsCriteria :recordid ID))
    (swap! csv-vec-providers conj (hash-map :firstname UserFirstName
                                            :lastname UserLastName
                                            :userid  UserId
                                            :number_of_technically_limited 0
                                            :number_of_ultrasound_completed 0
                                            :sdots_completed false
                                            :us_advanced_rotation false
                                            :us_rotation_completed false
                                            :us_teaching_completed false))
    (swap! csv-vec-examtypes conj (hash-map :examtype ExamType :examtypeid  ExamTypeId))))




(defn load-csv-lines
  [vec-of-lines]
  (let [line (atom [])]
    (doseq [n (range (count vec-of-lines))]
      (if  (= "" (nth vec-of-lines n))
        (do
          (load-csv-vec @line)
          (reset! line []))
        (swap! line conj (nth vec-of-lines n))))))


(defn consoldate-records-and-examtypes
  []
  (let [examtypes (js->clj (rest @csv-vec-examtypes))
        examtypes-list (clj->js (into #{} (doall (map #(.-examtypeid (clj->js %))) examtypes)))]

    (re-frame/dispatch [:upload-records (rest @csv-vec-records)])
    (re-frame/dispatch [:upload-examtypes examtypes-list])))


(defn reload
  [data]
  (do
    (-> data
        (clojure.string/trim)
        (clojure.string/split ",")
        (load-csv-lines))
    (consoldate-records-and-examtypes)))


(defn get-qpath-data
  [path]
  (.then (get-download-url path) (fn [url]
                                   (go (let [response (<! (http/get url {:with-credentials? false}))]
                                         (reload (:body response)))))))



(defn load-from-database!
  [path]
  (let [ref (db-ref [path])
        results (atom [])]
    (.on ref "value" (fn [snapshot] (reset! results (.val snapshot))))
    results))


;; Provider
(defn load-providers-from-database!
  [path]
  (load-from-database! path))

(defn save-providers-to-database!
  [path provider]
  (let [ref (db-ref [path])]
    (.push ref (clj->js provider))))


(defn clear-providers-from-database!
  []
  (let [ref (db-ref ["institution/providers"])]
    (.set ref "")))

;; Users
(defn load-users-from-database!
  [path]
  (load-from-database! path))


(defn save-users-to-database!
  [path user]
  (let [ref (db-ref [path])]
    (.push ref (clj->js user))))


(defn get-userid-for-user-by-provider
  [provider fname lname]
  (do
  ;(js/console.log "userid by provider:" (.-lastname (clj->js provider))  (.-firstname (clj->js provider)) fname lname)
  (if (and       (=
                    (-> (.-firstname (clj->js provider))
                      (clojure.string/lower-case)
                      (clojure.string/trim))
                    (-> fname
                      (clojure.string/lower-case)
                      (clojure.string/trim))
                       ) 
                  (= 
                    (-> (.-lastname (clj->js provider))
                      (clojure.string/lower-case)
                      (clojure.string/trim))
                    (-> lname
                      (clojure.string/lower-case)
                      (clojure.string/trim))
                    ))
    (.-userid (clj->js provider))
    nil 
    )))
 
(defn get-userid
  [fname lname]
  (let [providers (re-frame/subscribe [:provider-items])]
    (first 
          (filter 
               (complement nil?) 
               (doall 
                 (map #(get-userid-for-user-by-provider (clj->js (val %)) fname lname ) (js->clj @@providers)))))))

(defn create-and-save-user-to-database 
  [members]
  (let [members-to-users (js->clj @members)
        memberid 0
        preferredemail 1
        alternateemail 2
        slackusername 3
        firstname 4
        lastname 5
        phonenumber 6
        carrier 7
        role 8
        level 9 
        ]
  (.set (db-ref ["institution/users"]) nil)
  ;;admin user
  (.push (db-ref ["institution/users"]) (clj->js {:userid (.getTime (js/Date.))
                                                  :perferredemail "griseldacarl@gmail.com"
                                                  :alternateemail "griseldacarl@gmail.com"
                                                  :slackusername "@cmitchell"
                                                  :firstname "Carl"
                                                  :lastname "Mitchell"
                                                  :phonenumber "5015549615"
                                                  :carrier "t-mobile"
                                                  :role "faculty"
                                                  :level "director"}))
  ;;all other users; save-users-to-database! 
  (doall (map #(save-users-to-database! "institution/users" (clj->js (hash-map "alternateemail" (nth % alternateemail)
                              "carrier" (nth % carrier)
                              "firstname" (nth % firstname)
                              "lastname" (nth % lastname)
                              "level" (nth % level)
                              "perferredemail" (nth % preferredemail)
                              "phonenumber" (nth % phonenumber)
                              "role" (nth % role)
                              "slackusername" (nth % slackusername)
                              "userid" (let [userid  (get-userid (nth % firstname) (nth % lastname))]
                                            (if-not (nil? userid) userid (.getTime (js/Date.)))) 
                              ))) members-to-users))
  ))

(defn delete-users-from-database!
  [ userkey]
  (let [ref (db-ref [(str "institution/users/" userkey)])]
    (.remove ref )))

(defn clear-users-from-database!
  []
  (let [ref (db-ref ["institution/users"])]
    (.set ref "")))

(defn save-orginal-database!
  []
  (js/console.log "Original database loading...") 
  (.set (db-ref ["institution"]) (clj->js {:providers "providers" :examtypes "examtypes" :records "records" :milestones "milestones" :reports "reports" :users "users" :presets "presets"}))
  (.set (db-ref ["institution/reports"]) (clj->js {:graduatereports "graduatereports" :qualityreports "qualityreports" :individualquality "individualquality"}))
  (.push (db-ref ["institution/providers"]) (clj->js {:firstname "Carl"
                                                      :lastname "Mitchell"
                                                      :number_of_technically_limited 0
                                                      :number_of_ultrasound_completed 0
                                                      :sdots_completed false
                                                      :us_advanced_rotation false
                                                      :us_rotation_completed false
                                                      :us_teaching_completed false
                                                      :userid 1000}))

  (.push (db-ref ["institution/providers"]) (clj->js {:firstname "William"
                                                      :lastname "Mitchell"
                                                      :number_of_technically_limited 0
                                                      :number_of_ultrasound_completed 0
                                                      :sdots_completed false
                                                      :us_advanced_rotation false
                                                      :us_rotation_completed false
                                                      :us_teaching_completed false
                                                      :userid 1001}))

  (.push (db-ref ["institution/reports/graduatereports"]) (clj->js {:graduatereportsid (.getTime (js/Date.))
                                                                    :firstname "Carl"
                                                                    :lastname "Mitchell"
                                                                    :overaltotal 0
                                                                    :soft-tissue-msk-required 0
                                                                    :soft-tissue-msk-completed 0
                                                                    :soft-tissue-msk-limited 0
                                                                    :procedure-required 0
                                                                    :procedure-completed 0
                                                                    :procedure-limited 0
                                                                    :aaa-required 0
                                                                    :aaa-completed 0
                                                                    :aaa-limited 0
                                                                    :biliary-required 0
                                                                    :biliary-completed 0
                                                                    :biliary-limited 0
                                                                    :cardiac-ivc-required 0
                                                                    :cardiac-ivc-completed 0
                                                                    :cardiac-ivc-limited 0
                                                                    :ocular-required 0
                                                                    :ocular-completed 0
                                                                    :ocular-limited 0
                                                                    :trauma-required 0
                                                                    :trauma-completed 0
                                                                    :trauma-limited 0
                                                                    :thoracic-airway-required 0
                                                                    :thoracic-airway-completed 0
                                                                    :thoracic-airway-limited 0
                                                                    :dvt-required 0
                                                                    :dvt-completed 0
                                                                    :dvt-limited 0
                                                                    :iup-required 0
                                                                    :iup-completed 0
                                                                    :iup-limited 0
                                                                    :urinary-required 0
                                                                    :urinary-completed 0
                                                                    :urinary-limited 0}))


  (.set (db-ref ["institution/presets"]) (clj->js [{:presetsid (.getTime (js/Date.)) :userid (.getTime (js/Date.)) :firstname "Carl" :lastname "Mitchell"
                                                    :abscess "abscess" :ocular "ocular" :eFast "eFast" :thoracic "thoracic" :cardiac "cardiac"
                                                    :thoracentesis "thoracentesis" :ultrasoundguidelp "ultrasoundguidelp" :ivc "ivc"
                                                    :aorta "aorta" :vascular "vascular" :gallbladder "gallbladder" :renal "renal" :pelvic "pelvic"
                                                    :testicular "testicular" :soft-tissue-muscularskeletal "soft tissue and muscularskeletal" :dvt "dvt" :total 0}
                                                   {:presetsid (.getTime (js/Date.)) :userid (.getTime (js/Date.)) :firstname "William" :lastname "Mitchell"
                                                    :abscess "abscess" :ocular "ocular" :eFast "eFast" :thoracic "thoracic" :cardiac "cardiac"
                                                    :thoracentesis "thoracentesis" :ultrasoundguidelp "ultrasoundguidelp" :ivc "ivc"
                                                    :aorta "aorta" :vascular "vascular" :gallbladder "gallbladder" :renal "renal" :pelvic "pelvic"
                                                    :testicular "testicular" :soft-tissue-muscularskeletal "soft tissue and muscularskeletal" :dvt "dvt" :total 0}]))

  (.set (db-ref ["institution/milestones"]) (clj->js [{:milestoneid (.getTime (js/Date.)) :userid (.getTime (js/Date.)) :firstname "Carl"    :lastname "Mitchell" :scantotals 100 :milestone 5}
                                                      {:milestoneid (.getTime (js/Date.)) :userid (.getTime (js/Date.)) :firstname "William" :lastname "Mitchell" :scantotals 100 :milestone 5}]))

  (.push (db-ref ["institution/users"]) (clj->js {:userid (.getTime (js/Date.))
                                                  :perferredemail "griseldacarl@gmail.com"
                                                  :alternateemail "griseldacarl@gmail.com"
                                                  :slackusername "@cmitchell"
                                                  :firstname "Carl"
                                                  :lastname "Mitchell"
                                                  :phonenumber "5015549615"
                                                  :carrier "t-mobile"
                                                  :role "faculty"
                                                  :level "director"}))
  )

