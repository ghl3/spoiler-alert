(ns spoiler-alert.handler
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer :all]
            [hiccup.core :as h]
            [spoiler-alert.views :refer :all]
            [spoiler-alert.twitter :refer :all]
            [clabango.parser :refer [render-file]]
            [ring.middleware.json-params :refer :all]
            [clj-json.core :as json]
            ))


(defn json-response
  "Return a json resonse from a given data"
  [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})


(defn get-timeline
  "Fetch a user's tweets using the twitter api,
  put them in the DB, and render a response"
  [id black-list-string]
  (println (format "get-timeline-and-render-response %s %s" id black-list-string))
  (let
      [black-list (clojure.string/split black-list-string #"\s+")
       tweets (get-filtered-timeline id black-list)
       num-tweets (count tweets)]
    (if (= num-tweets 0)
      (do
        (println (format "Didn't find any fucking tweets for twitter id %s" id))
        [])
      tweets)))


(defn get-timeline-and-render-response
  "Get the tweets and render in the body
  of a json response"
  [id blacklist]
  (json-response (get-timeline id blacklist)))


(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/rest/timeline" [id blackList] (get-timeline-and-render-response id blackList))
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))
