(ns wiktionary.run-parser
  (:require [clojure.core.reducers :as r]
            [clojure.string :as s]
            [fipp.edn :refer [pprint] :rename {pprint fipp}]
            [wiktionary.parser :as p]))

;;; TODO: learn more about r/monoid and monoids in general
(defn fold-into-vec [coll]
  (r/fold (r/monoid into vector) coll))

(def ^:dynamic input-file "spanish-definitions.tsv")

;(defonce lines (s/split (slurp input-file) #"\n"))

(defn parse-entries [lines] (fold-into-vec (r/map p/parse-line lines)))

(defn n-entries [n] 
  (let [rdr (clojure.java.io/reader input-file)] 
    (doall (map p/parse-line (take n (line-seq rdr))))))

(defn -main [] (spit "parsed-entries" 
                     (->> lines
                          parse-entries)))

(comment (-main))
