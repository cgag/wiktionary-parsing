(ns wiktionary.web.cljs.views
  (:require 
    [clojure.browser.repl :as repl]
    [clojure.string :as s] 
    [cljs.core.async :refer [chan >! <! close!]]
    [c2.scale :as scale]
    [c2.core :refer [unify]]
    [domina.css :refer [sel]]
    [domina.events :as event]
    [shoreleave.browser.history :as h]
    [crate.form :as form]
    [crate.element :refer [link-to]]
    [wiktionary.web.cljs.pages :as p]
    [wiktionary.web.cljs.client :as client])
  (:use [domina :only [append! destroy-children! by-class by-id value text]])
  (:require-macros [crate.def-macros :refer [defpartial]]
                   [cljs.core.async.macros :refer [go]]
                   [wiktionary.web.cljs.macros :refer [forever
                                                       def-js-page]]))

(repl/connect "http://localhost:9000/repl")

(defn keyword->str
  "turn a keyword into a string and keep the namespace"
  [k]
  (subs (str k) 1))

;; Setting the history token causes a navigation event.
;; Rather than placing anything into nav-chan directly from the links, we 
;; instead just set the token and let the navigate-callback handle it.
(defn init-nav-listeners! [nav-chan]
  (letfn [(nav-listener! [selector token]
            (event/listen! (sel selector)
                           :click (fn [e]
                                    (js/alert "click")
                                    (event/prevent-default e)
                                    (let [[event & args] (s/split (keyword->str token) #"/")]
                                      (go (>! nav-chan [(keyword event) args]))))))]
    (nav-listener! "#home-link"    :home)
    (nav-listener! "#about-link"   :about)
    (nav-listener! "#contact-link" :contact)))

;; TODO: Definitely need the callback.  Just use it for back and foward events?
;(comment (h/navigate-callback 
;(fn [m] 
;(let [[event & args] (s/split (keyword->str (:token m)) #"/")]
;(js/alert (str "nav-event: event: " event " args: " args))
;(go (>! nav-chan [(keyword event) args]))))))

;; TODO: pushstate for history
(defn init-word-info [word]
  (go 
    (let [entry (<! (client/word-info word))]
      (destroy-children! (sel ".body-container"))
      (append! (sel ".body-container") (p/word-info-page entry))))) 

(def-js-page init-home "home" (p/home-body)
  (event/listen! (sel ".word-info-form button")
                 :click (fn [e]
                          (let [word (value (by-id "word-info"))] 
                            (h/set-token! h/history (str "word-info/" word)))))
  (event/listen! (sel ".frequencies-form button") 
                 :click (fn [e]
                          (let [text (value (by-id "text"))]
                            (h/set-token! h/history "frequencies")))))

(def-js-page init-about   "about"   (p/about-body))
(def-js-page init-contact "contact" (p/contact-body))

(defn router [nav-chan]
  (go (forever (let [[nav-event args] (<! nav-chan)]
                 (js/alert (str "router: event: " nav-event " args: " args))
                 (condp = nav-event
                   :home    (init-home)
                   :about   (init-about)
                   :contact (init-contact)
                   :word-info   (apply init-word-info args)
                   ;:frequencies (apply init-frequencies args)
                   :else    (init-home))))))

(defn set-initial-state! [nav-chan hist-token]
  (go (>! nav-chan (if (s/blank? hist-token)
                     [:home]
                     (let [[event & args] (s/split hist-token #"/")]
                       (apply vector (keyword event) args))))))

(defn ^:export main []
  (let [nav-chan (chan)] 
    (router nav-chan)
    (set-initial-state! nav-chan (h/get-token h/history))
    (init-nav-listeners! nav-chan)))
