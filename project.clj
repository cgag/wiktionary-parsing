(defproject wiktionary "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1859"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]

                 [org.flatland/ordered "1.5.1"]
                 [org.blancas/kern "0.7.0"]
                 [fipp "0.4.0"]
                 [ring "1.2.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [io.curtis/boilerpipe-clj "0.2.0"]

                 ;; cljs
                 ;;
                 [org.clojure/google-closure-library-third-party "0.0-2029-2"]
                 [io.curtis/cljs-http "0.0.7-SNAPSHOT"]
                 [shoreleave "0.3.0"]
                 [domina "1.0.1"]
                 [crate "0.2.4"] 
                 [net.drib/strokes "0.5.1"]
                 [com.keminglabs/c2 "0.2.3"]]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :plugins [[lein-cljsbuild "0.3.2"]]

  :cljsbuild {:builds
              [{:source-paths ["src/wiktionary/web/cljs"]
                :compiler {:output-to "resources/public/js/main.js"
                           :optimizations :whitespace
                           :pretty-print true}}]})
