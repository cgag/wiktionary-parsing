(ns wiktionary.parser
  (:require [blancas.kern.core :refer :all]
            [blancas.kern.lexer.basic :refer :all]
            [clojure.string :as s]))

(def double-braces   "{{}}"   (comp braces braces))
(def double-brackets "[[]]" (comp brackets brackets))

(defn remove-blanks 
  "Remove map-entries who's values are blank strings"
  [m]
  (into {}
        (for [[k v] m]
          (when-not (s/blank? (str v))
            [k v]))))




(def id->lower 
  "parses an identifier and converts it to lowercase"
  (bind [i identifier]
        (return (s/lower-case i))))


(def basic-info
  "Parses all the information before the definition,
  the language, the word itself, and the part of speech."
  (bind [lang id->lower
         word id->lower
         pos  id->lower]
        (return {:lang lang :word word :pos pos})))


(declare template-inner param named-param)
(def template
  "Parse a wiktionary (wikimedia) template.
  {{name-of-template | parmam | namedparam=whatever| ... }}"
  (bind [_ (put-state {:i -1})
         t (double-braces template-inner)]
        (return (apply merge t))))

(def template-inner
  "parse the internals "
  (sep-by (sym \|)
          (<|> named-param param)))

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
 

;; TODO: must be a better way than this if and dropping the \#
(def link 
  "Parse a link within a defintion of either of the forms:
  [[linktext]]
  [[#Language|linktext]]"
  (bind [link-fields (double-brackets (sep-by (sym \|) (field "|]")))]
        (if (= (count link-fields) 2)
          (let [[lang text] link-fields]
            (return {:language (->> lang 
                                    (drop 1) 
                                    (apply str) 
                                    s/lower-case) 
                     :text text}))
          (return {:text (first link-fields)}))))


(def definition
  "Parse the definition of a word which can contain a mixture of words and links"
  (many (<|> (bind [l link]
                   (return {:link l}))
             (bind [word (<+> (lexeme (many (none-of* " \n\t\r"))))]
                   (return {:word word})))))


(def line (many-till any-char (<|> new-line eof)))

(def entry
  "Parse one of the Spanish definition entries"
  (bind [info basic-info
         _ (sym \#)
         defnition  (many (<|> template definition))]
        (return (assoc info :def definition))))
