(ns wiktionary.web.views
  (:require [clojure.string :as s ]
            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.page :refer [html5]]
            [c2.core :refer [unify]]
            [c2.scale :as scale]
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

(defn sort-map [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

(defview c2-test [freq-map]
  (let [width 500, bar-height 20
        data freq-map
        s (scale/linear :domain [0 (apply max (vals data))]
                        :range [0 width])]
    [:div#bars
     (unify data (fn [[label val]]
                   [:div {:style (str "height: " bar-height
                                  "; width: " (/ (s val) 1.0) "px"
                                  "; background-color: blue;")}
                    [:span {:style (str "color: " "white;")} label]]))]))

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

;; TODO: Where we're at: this basically works but the way we handle conjugations
;; and such is maybe kind of wack.  If "fue" appears 10 times, then "ser" and "ir"
;; both get credit for appearing 10 times. Perhaps we should just
;; merge them: "ser, ir: 10"
(defview word-frequencies [text]
  [:div.text 
   (str (spanish-frequencies (words text)))
   (c2-test (sort-map (:valid-words (spanish-frequencies (words text)))))])

;; TODO: how to handle parens?
(defn words [s]
  (let [words (s/split s #"\s+|[,.|:;@#$%^&*+()]")]
    (remove s/blank?
            (for [word words] 
              (-> word
                  s/trim
                  s/lower-case)))))

(defn spanish-word? [word]
  (let [{:keys [verbs non-verbs]} (w/definition word)]
    (boolean (or (seq verbs)
                 (seq non-verbs)))))

(defn spanish-frequencies [words]
  (let [spanish-words (filter spanish-word? words)
        non-spanish-words (remove spanish-word? words)
        forms (mapcat w/form-of spanish-words)]
    {:valid-words   (frequencies forms)
     :invalid-words (frequencies non-spanish-words)
     :all-word      words}))

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
