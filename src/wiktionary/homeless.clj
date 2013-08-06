(ns wiktionary.homeless
  (:require [clojure.core.reducers :as r]))

(set! *warn-on-reflection* true)

(defn foldv [coll]
  (r/fold (r/monoid into vector) conj coll))

(defmacro dlet [bindings & body]
  `(let [~@(mapcat (fn [[n v]]
                     (if (or (vector? n) (map? n))
                       [n v]
                       [n v '_ `(println (name '~n) " : " ~v)]))
                   (partition 2 bindings))]
     ~@body))
