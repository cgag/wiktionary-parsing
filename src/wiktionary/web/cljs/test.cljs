(ns wiktionary.web.cljs.test
  (:require [dommy.core :as d])
  (:use-macros [dommy.macros :only [node sel1]]))

(def body-node 
  (node
    [:div.fuck
     (for [i (range 10)]
       [:p (str "p: " i)])]))

(d/append! (sel1 :body) body-node)
