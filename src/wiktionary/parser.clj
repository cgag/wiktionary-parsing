(ns wiktionary.parser
  (:require [blancas.kern.core :refer :all]
            [blancas.kern.lexer.basic :refer :all]
            [clojure.string :as s]))

(def double-braces   (comp braces braces))
(def double-brackets (comp brackets brackets))

(defn remove-blanks [m]
  (into {}
        (for [[k v] m]
          (when-not (s/blank? (str v))
            [k v]))))

(def param (bind [fld (field "|}")
                  _ (modify-state #(update-in % [:i] inc))
                  u get-state]
               (return {(str (:i u)) (s/trimr fld)})))

(def named-param (bind  [name (field " =}|")
                         _    (sym \=)
                         value    (field "|}")]
                     (return {name (s/trimr value)})))

(def id->lower (bind [i identifier]
                   (return (s/lower-case i))))

;; TODO: make a parser for the text, which can parse either
;; plain-text, potentially in square brakets or not, or templates, or
;; a mixture of the three

(def basic-info
  (bind [lang id->lower
         w    id->lower
         pos  id->lower]
      (return {:lang lang :word w :pos pos})))

(def template-inner
  (bind [inside (sep-by (sym \|)
                        (<|> (<:> named-param) param))]
      (return inside)))

(def template
  (bind [_ (put-state {:i -1})
         t (double-braces template-inner)]
      (return (apply merge t))))

;; TODO: doesn't work at all
(def link (double-brackets (field "]|")))
;(run  (double-brackets (field "|")) "[[#English|adjustable]]")

;; TODO: handle this
(def definition
  ;; [[stuff like this (links?)]] plain text
  ;; [[#Lang|word]] -> translations?
  (bind [def-parts (many (<|> link 
                              identifier))]
      (return (s/join " " def-parts))))


(def line (many-till any-char (<|> new-line eof)))

(def entry
  (bind [info basic-info
         _ (sym \#)
         def  (many (<|> template definition))]
      (return (assoc info :def def))))

;; TODO: Handle double braces for things beyond [[link]], such as
;; [[#English whatever]]
