(def input-file "spanish-definitions.tsv")
(defonce lines (s/split (slurp input-file) #"\n"))
(defn parse-entries [] (fold-into-vec (r/map p/parse-line lines)))

(defn -main [] (spit "parsed-entries" (parse-entries)))
