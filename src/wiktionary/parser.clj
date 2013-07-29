(ns wiktionary.parser
  (:require [blancas.kern.core :refer :all]
            [blancas.kern.lexer.basic :refer :all]
            [clojure.string :as s]
            [wiktionary.template :as t]))

(declare entry)

(defn parse-line [line]
  (let [state (parse entry line)]
    (if (:ok state)
      (:value state)
      (throw (Exception. (str "error on: " line))))))

;;;;;;;;;;;;;;

(def double-braces   "{{}}" (comp braces braces)) 
(def double-brackets "[[]]" (comp brackets brackets))

(def myword
  "Basically any string of non-whitespace" 
  (letfn [(drop-surrounding-single-quotes [s]
            (if-not (and (< 1 (count s))
                         (= (subs s 0 2) "''"))
              s
              (loop [s s]
                (if (and (< 1 (count s))
                         (= \' (first s))
                         (= \' (last  s)))
                  (recur (subs s 1 (- (count s) 1)))
                  s))))]
    (bind [s (<+> (lexeme (many (none-of* " \n\t\r"))))]
      (return (drop-surrounding-single-quotes s)))))

(def word->lower 
  "parses an identifier and converts it to lowercase"
  (bind [i myword]
        (return (s/lower-case i))))

(declare template)

(def basic-info
  "Parses all the information before the definition,
  the language, the word itself, and the part of speech."
  (bind [lang (field* "\t") 
         _ (sym \tab) 
         word (field* "\t") 
         _ (sym \tab) 
         pos (<|> template (field* "\t"))]
        (return {:lang (s/lower-case lang) :word (s/lower-case word) :pos (if (map? pos) 
                                                                            (t/name (:template pos))
                                                                            (s/lower-case pos))})))

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
  (<:> (bind  [name  (field " =}|")
               _     (sym \=)
               value (field "|}")]
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

(def link 
  "Parse a link within a defintion of either of the forms:
  [[linktext]]
  [[#Language|linktext]]
  [[word#language]] (maybe??)
  [[word#language|linktext]]"
  (double-brackets
    (bind [link-fields (sep-by (sym \|) (field "|]"))]
          (if (= (count link-fields) 2)
            (let [[lang text] link-fields]
              (return {:target lang 
                       :text   text}))
            (return {:text (first link-fields)})))))

(def definition
  "Parse the definition of a word which can contain a mixture of words and links"
  (many (<|> (<:> (bind [l link]
                        (return {:link l})))
             (bind [word myword]
                   (return {:word word})))))

(def entry
  "Parse one of the Spanish definition entries"
  (bind [info basic-info
         _ (field "#")
         body (many (<|> template definition))]
        (return (assoc info :body body))))


(comment (def test-entry "Spanish\ta otra cosa, mariposa\tPhrase\t# {{idiomatic|lang=es}} Let's change the subject, shall we?$"))
