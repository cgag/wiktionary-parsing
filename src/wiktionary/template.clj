(ns wiktionary.template
  (:refer-clojure :exclude [name]))

(defn name [t]
  (get t "0"))

;; TODO: Need to be able to parse templates to get info like: the infinitive of conjugated verbs,
;; masculinity, plural/singular, etc
;; TODO: Dump a list of template names and make sure we know what's available before we start 
;; doing anything with the information.
