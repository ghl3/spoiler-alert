(ns spoiler-alert.views
  (:use [hiccup core page])
  (:require [clabango.parser :refer [render-file]]))


(defn index-page []
  "Render the index page"
  (render-file "templates/index.html" {:greeting "Hey!"}))


(defn tweet-page [params]
  "Render a list of tweets using the tweet template"
  (render-file "templates/tweets.html" params))
