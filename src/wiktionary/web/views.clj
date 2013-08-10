(ns wiktionary.web.views
  (:require [clojure.string :as s ]
            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.page :refer [html5]]
            [wiktionary.core :as w]))

(declare info-form frequencies-form)

(defmacro defview [view-name param-vec & body]
  `(defn ~view-name ~param-vec
     (html5 ~@body)))

(defview home []
  [:div.info-form info-form]
  [:hr]
  [:div.frequencies-form frequencies-form])

(defview word-info [word]
  (let [{:keys [lang word non-verbs verbs] :as info} 
        (w/definition word)]
    [:div.info 
     [:h1 lang]
     [:h2 word]
     (when (seq non-verbs)
       [:div [:h3 "Noun"]
        [:li (for [non-verb-entry non-verbs]
               [:ul (str non-verb-entry)])]])
     (when (seq verbs) 
       [:div [:h3 "Verb"]
        [:li (for [verb-entry verbs]
               [:ul (str verb-entry)])]])]))

(defview word-frequencies [text]
  [:div.text (str (frequencies (s/split text #"\s+")))])

(def info-form
  (form-to [:get "/word-info"]
           (label :word "Word: ")
           (text-field :word)
           (submit-button "Submit")))

(def frequencies-form
  (form-to [:post "/frequencies"]
           (label :text "Text: ")
           (text-area :text)
           (submit-button "Submit")))
