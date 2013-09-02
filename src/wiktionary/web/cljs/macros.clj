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
