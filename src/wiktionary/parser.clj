(ns wiktionary.parser
  (:require [blancas.kern.core :refer :all]
            [blancas.kern.lexer.basic :refer :all]
            [clojure.string :as s]))

(def double-braces   "{{}}"   (comp braces braces))
(def double-brackets "[[]]" (comp brackets brackets))

(def myword
  "Basically any string of non-whitespace"
 (<+> (lexeme (many (none-of* " \n\t\r")))))

(def word->lower 
  "parses an identifier and converts it to lowercase"
  (bind [i myword]
        (return (s/lower-case i))))

(def basic-info
  "Parses all the information before the definition,
  the language, the word itself, and the part of speech."
  (bind [lang word->lower
         word word->lower
         pos  word->lower]
        (return {:lang lang :word word :pos pos})))

(def param
  "Parse a template param (parms are separated by |'s)
  They're given names that are just numbers according to which unamed 
  param they are in the template"
  (bind [fld (field "|}")
         _ (modify-state #(update-in % [:i] inc))
         u get-state]
        (return {(str (:i u)) (s/trimr fld)})))

(def named-param
  "Parse a named param from a template e.g. butts=dongs"
  (<:> (bind  [name (field " =}|")
               _    (sym \=)
               value    (field "|}")]
             (return {name (s/trimr value)}))))

(def template-inner
  "parse the internals "
  (sep-by (sym \|)
          (<|> named-param param)))

(def template
  "Parse a wiktionary (wikimedia) template.
  {{name-of-template | parmam | namedparam=whatever| ... }}"
  (bind [_ (put-state {:i -1})
         t (double-braces template-inner)]
        (return {:template (apply merge t)})))

;; TODO: must be a better way than this if and dropping the \#
(def link 
  "Parse a link within a defintion of either of the forms:
  [[linktext]]
  [[#Language|linktext]]"
  (bind [link-fields (double-brackets (sep-by (sym \|) (field "|]")))]
        (if (= (count link-fields) 2)
          (let [[lang text] link-fields]
            (return {:language (->> lang 
                                    (drop 1) ; Dropping #
                                    (apply str) 
                                    s/lower-case) 
                     :text text}))
          (return {:text (first link-fields)}))))

(def definition
  "Parse the definition of a word which can contain a mixture of words and links"
  (many (<|> (bind [l link]
                   (return {:link l}))
             (bind [word myword]
                   (return {:word word})))))

(def entry
  "Parse one of the Spanish definition entries"
  (bind [info basic-info
         _ (sym \#)
         body (many (<|> template definition))]
        (return (assoc info :body body))))

(defn parse-line [line]
  (value entry line))
