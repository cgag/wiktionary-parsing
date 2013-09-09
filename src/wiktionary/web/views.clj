(ns wiktionary.web.views
  (:require [clojure.string :as s ]
            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer [link-to]]
            [c2.core :refer [unify]]
            [c2.scale :as scale]
            [wiktionary.core :as w]
            [wiktionary.homeless :as homeless]))

(def nav
  [:ul
   [:li (link-to {:id "home-link"} "#" "home")]])

;; TODO: probably some of this can be in functions instead
;; of in the macro
(defmacro defview [view-name param-vec & body]
  `(defn ~view-name ~param-vec
     (html5 
       [:head
        [:meta {:charset "utf-8"}]
        [:title "Title"]]
       [:body
        [:div.layout-container
         nav
         [:div.body-container
          ~@body]]
        (include-js "/js/main.js")
        [:script "wiktionary.web.cljs.views.main();"]])))

(defmacro defpartial [name param-vec & body]
  `(defn ~name ~param-vec
     (html ~@body)))

(defview home [] "shiiit")

;; TODO: this and word-freqs are dumb, just override behavior of submitting
;; forms
;(defview word-info [word]
;"in word info view"
;(init-js (str "wiktionary.web.cljs.views.init_word_info(\"" word "\");")))

;; TODO: lmao this doesn't work if text has new lines
;(defview word-frequencies [text]
;"in word-frequencies view"
;(init-js (str "wiktionary.web.cljs.views.init_word_frequencies(\"" text "\");")))

(defn edn-word-info [word]
  (pr-str (w/definition word)))

(defn edn-word-frequencies [text]
  (pr-str (w/lemma-frequencies (w/words text))))

(defn sort-map [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

;(defview c2-test [freq-map]
;(let [width 500, bar-height 20
;data (sort-map freq-map)
;s (scale/linear :domain [0 (apply max (vals data))]
;:range [0 width])]
;[:div#bars
;(unify data (fn [[label val]]
;[:div {:style (str "height: " bar-height
;"; width: " (/ (s val) 1.0) "px"
;"; background-color: blue;")}
;[:span {:style (str "color: " "white;")} label]]))]))


;(defview word-frequencies [text]
;[:div.text 
;(str (w/lemma-frequencies (w/words text)))
;(c2-test (w/lemma-frequencies (w/words text)))])
