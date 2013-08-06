(ns wiktionary.run-parser
  (:require [clojure.core.reducers :as r]
            [clojure.string :as s]
            [fipp.edn :refer [pprint]]
            [wiktionary.parser :as p]
            [wiktionary.homeless :as homeless]))

(set! *warn-on-reflection* true)

;;; TODO: learn more about r/monoid and monoids in general

(def ^:dynamic *input-file* "spanish-definitions.tsv")

(defonce lines (s/split (slurp *input-file*) #"\n"))

(defn parse-entries [lines] (homeless/foldv (r/map p/parse-line lines)))

(defn n-entries [n] 
  (let [rdr (clojure.java.io/reader *input-file*)] 
    (doall (map p/parse-line (take n (line-seq rdr))))))

;; Takes 12 minutes
(defn -main [] (time
                 (spit "parsed-entries" 
                       (with-out-str 
                         (->> lines
                              parse-entries
                              (interpose "DIVIDER")
                              (map pprint)
                              doall)))))

(comment (-main))
