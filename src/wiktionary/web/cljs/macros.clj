(ns wiktionary.web.cljs.macros)

;; NOTE: Have to manually qualitfy stuff since you can't require clojurescript stuff in clojure.
(defmacro defview [view-name param-vec & body]
  `(crate.def-macros/defpartial ~view-name ~param-vec
     [:html 
      [:head
       [:meta {:charset "utf-8"}]
       [:title "Title"]]
      [:body
       ~@body]]))

(defmacro forever [& body]
  `(while true
     ~@body))

(defmacro def-js-page [page-name path body-partial & js-forms]
  `(defn ~page-name []
     (shoreleave.browser.history/set-token!
       shoreleave.browser.history/history ~path)
     (domina/destroy-children! (domina.css/sel ".body-container"))
     (domina/append! (domina.css/sel ".body-container") (~body-partial))
     ~@js-forms))
