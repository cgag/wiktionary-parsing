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

(declare display-verb)

(defview word-info [word]
  (let [{:keys [lang word non-verbs verbs] :as info} 
        (w/definition word)]
    [:div.info 
     [:h1 lang]
     [:h2 word]
     (when (seq non-verbs)
       [:div [:h3 "Some non-verb part of speech"]
        [:li (for [non-verb-entry non-verbs]
               [:ul (str (:definition non-verb-entry))])]])
     (when (seq verbs) 
       [:div [:h3 "Verb"]
        [:div (for [verb-entry verbs]
                (display-verb verb-entry))]])]))

(declare display-conjugation-info)

(defn display-verb [verb-entry]
  (if-let [conjugation-info (:conjugation verb-entry)] 
    (display-conjugation-info conjugation-info)
    [:li (:definition verb-entry)]))

;; TODO: merge all the conjugations based on infinitive?
(defn display-conjugation-info [conjugation-info]
  [:ul 
   (for [k (keys conjugation-info)]
     [:li (str k ": " (k conjugation-info))])])

(declare words spanish-frequencies)

(defview word-frequencies [text]
  [:div.text (str (spanish-frequencies (words text)))])

(defn words [s]
  (s/split s #"\s+"))

(defn spanish-frequencies [words]
  (let [root-words (into #{} (mapcat w/form-of words))]
    (frequencies root-words)))

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
