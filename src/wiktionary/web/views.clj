(ns wiktionary.web.views
  (:require [clojure.string :as s ]
            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.page :refer [html5 include-css include-js]]
            [c2.core :refer [unify]]
            [c2.scale :as scale]
            [wiktionary.core :as w]
            [wiktionary.homeless :as homeless]))

;; TODO: probably some of this can be in functions instead
;; of in the macro
(defmacro defview [view-name param-vec & body]
  `(defn ~view-name ~param-vec
     (html5 
       [:head
        [:meta {:charset "utf-8"}]
        [:title "Title"]]
       [:body
        ~@body])))


(defview cljs [] [:div])

(defview home []
  (include-js "js/main.js")
  "whatever man"
  [:script "wiktionary.web.cljs.views.init_home()"])

;[:div.info-form info-form]
;[:hr]
;[:div.frequencies-form frequencies-form])

;; TODO: merge all the conjugations based on infinitive?
(defn display-conjugation-info [conjugation-info]
  [:ul 
   (for [k (keys conjugation-info)]
     [:li (str k ": " (k conjugation-info))])])

(defn display-verb [verb-entry]
  (if-let [conjugation-info (:conjugation verb-entry)] 
    (display-conjugation-info conjugation-info)
    [:li (:definition verb-entry)]))

(defn edn-word-info [word]
  (pr-str (w/definition word)))

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
        data (sort-map freq-map)
        s (scale/linear :domain [0 (apply max (vals data))]
                        :range [0 width])]
    [:div#bars
     (unify data (fn [[label val]]
                   [:div {:style (str "height: " bar-height
                                      "; width: " (/ (s val) 1.0) "px"
                                      "; background-color: blue;")}
                    [:span {:style (str "color: " "white;")} label]]))]))


(defview word-frequencies [text]
  [:div.text 
   (str (w/lemma-frequencies (w/words text)))
   (c2-test (w/lemma-frequencies (w/words text)))])
