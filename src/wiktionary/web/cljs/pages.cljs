(ns wiktionary.web.cljs.pages
  (:require 
    [crate.core :as crate]
    [crate.element :refer [link-to]]
    [crate.form :as form])
  (:require-macros [crate.def-macros :refer [defpartial]]
                   [cljs.core.async.macros :refer [go]]))

(def info-form
  [:div.word-info-form
   (form/form-to [:get "/word-info"]
                 (form/label :word "Word: ")
                 (form/text-field {:id "word-info"} :word)
                 [:button {:type "button"} "Submit"])])

(def frequencies-form
  [:div.frequencies-form
   (form/form-to [:post "/frequencies"]
                 (form/label :text "Text: ")
                 (form/text-area :text)
                 [:button {:type "button"} "Submit"])])

;; TODO: merge all the conjugations based on infinitive?
(defn display-conjugation-info [conjugation-info]
  [:ul 
   (for [k (keys conjugation-info)]
     [:li (str k ": " (k conjugation-info))])])  

(defn display-verb [verb-entry]
  (if-let [conjugation-info (:conjugation verb-entry)] 
    (display-conjugation-info conjugation-info)
    [:li (:definition verb-entry)])) 


(defpartial home []
  [:div.home
   [:div.info-form info-form]
   [:hr]
   [:div.frequencies-form frequencies-form]])

(defpartial about []
  [:div.about
   [:p "Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz."]])

(defpartial contact []
  [:div.contact
   (link-to "#" "test is the contact page")])

(defpartial render-frequencies [freq-map]
  [:div.frequencies
   (str freq-map)])

(defpartial render-word-info [entry]
  (let [{:keys [lang word non-verbs verbs] :as info} entry]
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

(defpartial word-info [word-entry]
  [:div.container
   (render-word-info word-entry)] ) 

(defpartial freqs [freq-map]
  [:div.container
   (render-frequencies freq-map)])
