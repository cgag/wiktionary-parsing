(ns wiktionary.web.cljs.views
  (:require 
    [clojure.browser.repl :as repl]
    [clojure.string :as s]
    [c2.scale :as scale]
    [domina :as d]
    [domina.css :refer [sel]]
    [crate.core :as crate]
    [crate.form :as form])
  (:use [c2.core :only [unify]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(repl/connect "http://localhost:9000/repl")

;; TODO: merge all the conjugations based on infinitive?
(defn display-conjugation-info [conjugation-info]
  [:ul 
   (for [k (keys conjugation-info)]
     [:li (str k ": " (k conjugation-info))])])  

(defn display-verb [verb-entry]
  (if-let [conjugation-info (:conjugation verb-entry)] 
    (display-conjugation-info conjugation-info)
    [:li (:definition verb-entry)]))

(def info-form
  (form/form-to [:get "/word-info"]
                (form/label :word "Word: ")
                (form/text-field :word)
                (form/submit-button "Submit")))

(def frequencies-form
  (form/form-to [:post "/frequencies"]
                (form/label :text "Text: ")
                (form/text-area :text)
                (form/submit-button "Submit")))

(defpartial home []
  [:div.home
   [:div.info-form info-form]
   [:hr]
   [:div.frequencies-form frequencies-form]])

(defpartial word-info [word]
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

(defn sort-map [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

(defpartial c2-test [freq-map]
  (let [width 500, bar-height 20
        data (sort-map freq-map)
        s (scale/linear :domain [0 (apply max (vals data))]
                        :range [0 width])]
    [:div#bars
     (unify data (fn [[label val]]
                   [:div {:style (str "height: " bar-height
                                      "; width: " (/ (s val) 1.0) "px"
                                      "; background-color: blue;")}
                    [:span {:style (str "color: " "white;")} label]]))]))


(defn ^:export init-home []
  (d/append! (sel "body") (home)))

;; TODO: Where we're at: this basically works but the way we handle conjugations
;; and such is maybe kind of wack.  If "fue" appears 10 times, then "ser" and "ir"
;; both get credit for appearing 10 times. Perhaps we should just
;; merge them: "ser, ir: 10"
;; -- fucking "para" is dominating everything by being a form of parir and parar.

;; TODO: handle not having any valid words.  Handle the whole "valid words" thing in
;; a cleaner way.
;(deftemplate word-frequencies [text]
;[:div.text 
;(str (lemma-frequencies (words text)))
;(c2-test (lemma-frequencies (words text)))])
