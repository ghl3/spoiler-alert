(defproject spoiler-alert "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [org.clojure/data.json "0.2.2"]
                 [twitter-api "0.7.4"]
                 [clojure-opennlp "0.3.1"]
                 [hiccup "1.0.2"]
                 [clabango "0.5"]
                 [com.taoensso/carmine "2.2.0"]
                 [ring-json-params "0.1.0"]
                 [clj-json "0.2.0"]
                 ]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler tweet-speak.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
