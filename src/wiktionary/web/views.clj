(ns wiktionary.web.views
  (:require [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.page :refer [html5]]
            [wiktionary.core :as w]))

(declare conjugation-form)

(defn home []
  (html5
    [:div"fuck"]
    (conjugation-form)))

(defn info-form []
  (form-to [:get "/conjugation-info"]
    (label :verb "Verb: ")
    (text-field :verb)))

(defn word-info [word]
  (let [info (w/definition word)] 
    (html5
      [:div.info info])))
