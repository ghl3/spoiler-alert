(ns spoiler-alert.twitter
  (:require [clojure.string]
            [oauth.client :as oauth]
            [twitter.oauth :refer :all]
            [twitter.callbacks :refer :all]
            [twitter.callbacks.handlers :refer :all]
            [twitter.api.restful :refer :all]
            [taoensso.carmine :as car :refer (wcar)])
  (:import
   (twitter.callbacks.protocols SyncSingleCallback)))


;; Redis
(def server1-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))


;; Twitter creds
(def my-creds (make-oauth-creds "rAvKhg7cyjc4XVNLuEA"
                                "jd8Vvu6knavDmj74KNYZJiInaKbYsO4Soum1oTnJ5Nc"
                                "42805000-vFQLTihnbb53pwtxV0iNSXnvyGm7K69WquyYlenM"
                                "bRuHAN42VOxrIzupQHRcXMeV80AGeBBSONRLR98Us"))


(defn twitter-statuses-home-timeline
  "Make a twitter api call:
  Returns a collection of the most recent
  Tweets and retweets posted by the authenticating
  user and the users they follow."
  [user-name]
  (println (format "twitter api - statuses-home-timeline: %s" user-name))
  (try
    (statuses-home-timeline :oauth-creds my-creds
                            :params {:screen-name user-name
                                     :count 10})
    (catch Exception e
      (format "Failed to get tweets for %scaught exception: %s %s "
              user-name
              (.getMessage e)))))


(defn fetch-user-tweets
  "Get the timeline of tweets for a user.
   Make the api call and then pull the tweets
   out of the response"
  [user-name]
  (println (format "fetch-user-tweets %s" user-name))
  (:body (twitter-statuses-home-timeline user-name)))


(defn get-tweet-texts
  "Get a list of tweet bodies
  from a full list of tweet responses"
  [tweets]
  (println tweets)
  (let [bodies (map :text tweets)]
    (println bodies)
    bodies))

(defn ^String substring?
  "True if s contains the substring."
  [substring ^String s]
  (println (format "Substring: %s String: %s" substring s))
  (.contains s substring))


(defn matches-blacklist
  "Return true if the string contains
  any words in the black-list.
  Else return false"
  [string black-list]
  (reduce #(or %1 %2)
          (map (fn [black-word]
                 (substring? black-word string))
               black-list)))


(defn filter-tweets
  "Remove all tweets that contain
  the black-listed words"
  [tweets black-list]
  (let [lower-black-list (map clojure.string/lower-case black-list)]
    (filter (fn [tweet] (not (matches-blacklist
                              (clojure.string/lower-case tweet)
                              black-list)))
            tweets)))



(defn get-filtered-timeline
  "Get a user's filtered timeline"
  [user-name black-list]
  (-> user-name
      fetch-user-tweets
      get-tweet-texts
      (filter-tweets black-list)))
