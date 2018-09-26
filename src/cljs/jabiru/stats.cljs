(ns jabiru.stats
  (:require
   [reagent.core :as r]
   [jabiru.csv :as c]
   [re-frame.core :as re-frame]))



(defn total-tls-by-examtype
  [examtype-name]
  (let [records c/records-data;(re-frame/subscribe [:record-items])
        record-list (filter #(= examtype-name (.-examtype  (clj->js %))) (js->clj @records))]
    (reduce + (map #(.-tls (aget (clj->js %) 1)) record-list))))


(defn total-tls-by-examtype-and-by-provider
  [examtype-name provider-lastname]
  (let [records c/records-data;(re-frame/subscribe [:record-items])
        record-list (filter #(and (= provider-lastname (.-lastname  (clj->js %)))
                                  (= examtype-name (.-examtype  (clj->js %)))) (js->clj @records))]
    (reduce + (map #(.-tls (clj->js %)) record-list))))

(defn total-by-examtype
  [examtype-name]
  (let [records c/records-data; (re-frame/subscribe [:record-items])
        record-list (filter #(= examtype-name (.-examtype  (clj->js %))) (js->clj @records))]
    (reduce + (map #(.-meetcredentialingcriteria  (clj->js %)) record-list))))

(defn total-by-examtype-and-by-provider
  [examtype-name provider-lastname]
  (let [records c/records-data;(re-frame/subscribe [:record-items])
        record-list (filter #(and (= provider-lastname (.-lastname (clj->js %)))
                                  (= examtype-name (.-examtype  (clj->js %))))
                            (js->clj @records))]
    (reduce + (map #(.-meetcredentialingcriteria  (clj->js %)) record-list))))











