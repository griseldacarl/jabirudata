(ns jabiru.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))


(re-frame/reg-sub
 ::loaderOn
 (fn [db]
   (:loaderOn db)))


(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
  :examtype-items
  (fn [db _]
    (:examtypes db)))

(re-frame/reg-sub
  :user-items
  (fn [db _]
    (:users db)))

(re-frame/reg-sub
  :provider-items
    (fn [db _]
      (:providers db)))

(re-frame/reg-sub
  :record-items
    (fn [db _]
      (:records db)))


(re-frame/reg-sub
  :user-current
  (fn [db _]
    (:user db)))
