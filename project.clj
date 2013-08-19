(defproject wiktionary "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.flatland/ordered "1.5.1"]
                 [org.blancas/kern "0.7.0"]
                 [fipp "0.4.0"]
                 [ring "1.2.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [io.curtis/boilerpipe-clj "0.2.0"]
                 [com.keminglabs/c2 "0.2.2"]
                 [prismatic/dommy "0.1.1"]
                 ]
  :plugins [[lein-cljsbuild "0.3.2"]]

  :cljsbuild {:builds
              [{:source-paths ["src/wiktionary/web/cljs"]
                :compiler {:output-to "src/wiktionary/web/resources/public/js/main.js"
                           :optimizations :whitespace
                           :pretty-print true}}]})
