(ns wiktionary.web.cljs.views
  (:require 
    [clojure.browser.repl :as repl]
    [clojure.string :as s] 
    [cljs.core.async :refer [chan >! <! close!]]
    [cljs.reader :refer [read-string]]
    [cljs-http.client :as http]
    [c2.scale :as scale]
    [c2.core :refer [unify]]
    [domina.css :refer [sel]]
    [domina.events :as event]
    [shoreleave.browser.history :as h]
    [crate.core :as crate]
    [crate.form :as form]
    [crate.element :refer [link-to]])
  (:use [domina :only [append! destroy-children! by-class by-id value text]])
  (:require-macros [crate.def-macros :refer [defpartial]]
                   [cljs.core.async.macros :refer [go]]
                   [wiktionary.web.cljs.macros :refer [defview 
                                                       forever
                                                       def-js-page]]))

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

(declare layout)

(defpartial home-body []
  [:div.home
   [:div.info-form info-form]
   [:hr]
   [:div.frequencies-form frequencies-form]])

(defpartial about-body []
  [:div.about
   [:p "Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz. 
       Some language learning jazz. Some language learning jazz."]])

(defpartial contact-body []
  [:div.contact
   (link-to "#" "this is the contact page")])

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

(defpartial word-info-page [word-entry]
  [:div.container
   (render-word-info word-entry)] )

(defn word-info [word]
  (go 
    (read-string 
      (:body (<! (http/get "/edn/word-info" {:query-params {:word word}}))))))

(defn lemma-frequencies [text]
  (go
    (read-string
      (:body (<! (http/post "/edn/frequencies" {:query-params {:text text}}))))))

(defn init-nav-listeners! [nav-chan]
  (letfn [(nav-listener! [selector event]
            (event/listen! (sel selector)
                           :click (fn [e]
                                    (event/prevent-default e)
                                    (go (>! nav-chan event)))))]
    (nav-listener! "#home-link"    :home)
    (nav-listener! "#about-link"   :about)
    (nav-listener! "#contact-link" :contact)
    (h/navigate-callback (fn [m] 
                           (go (>! nav-chan (:token m)))))))

;(defn ^:export init-word-frequencies [text]
;(go
;(let [freqs (<! (lemma-frequencies text))]
;(append! (sel ".body-container") (str "<div>" freqs "</div>"))
;(append! (sel ".body-container") (render-frequencies freqs)))))

;; TODO: pushstate for history
(defn init-word-info [word]
  (go 
    (let [entry (<! (word-info word))]
      (destroy-children! (sel ".body-container"))
      (append! (sel ".body-container") (word-info-page entry))))) 

(def-js-page init-home "home" (home-body)
  (event/listen! (sel ".word-info-form button")
                 :click (fn [e]
                          (init-word-info (value (by-id "word-info"))))))

(def-js-page init-about "about" (about-body))
(def-js-page init-contact "contact" (contact-body))

(defn router [nav-chan]
  (go (forever (let [nav-event (<! nav-chan)]
                 (condp = nav-event
                   :home    (init-home)
                   :about   (init-about)
                   :contact (init-contact)
                   :else    (init-home))))))

(defn set-initial-state! [nav-chan hist-token]
  (go (>! nav-chan (if (s/blank? hist-token)
                     :home
                     (keyword hist-token)))))

(defn ^:export main []
  (let [nav-chan (chan)] 
    (router nav-chan)
    (set-initial-state! nav-chan (h/get-token h/history))
    (init-nav-listeners! nav-chan)))
