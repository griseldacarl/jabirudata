(ns jabiru.sessionstorage
  (:require
      [re-frame.core :as re-frame]))

  (defn set-item!
  "Set `key' in browser's sessionStorage to `val`."
  [key val]
  (.setItem (.-sessionStorage js/window) key val))

(defn get-item
  "Returns value of `key' from browser's sessionStorage."
  [key]
  (.getItem (.-sessionStorage js/window) key))

(defn remove-item!
  "Remove the browser's sessionStorage value for the given `key`"
  [key]
  (.removeItem (.-sessionStorage js/window) key))
