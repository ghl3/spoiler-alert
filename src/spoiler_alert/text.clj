(ns spoiler-alert.text
  (:require [opennlp.nlp :refer :all]
            [opennlp.treebank :refer :all]
            [clojure.set :refer :all]))


(defn gather-words
  "Given a string, return a list of lower-case words with whitespace and
   punctuation removed"
  [s]
  (println "gather-words %s" s)
  (->> s
       (.split #"[^a-zA-Z0-9@']")
       (filter #(not (.isEmpty %)))
       (clojure.string/join " ")))


(def en-tokenizer
  "Return an English language tokenizer"
  (make-tokenizer "nlp/en-token.bin"))


(defn tokenize
  "Tokenize a sentence"
  [s]
  (let [tokens (en-tokenizer (gather-words s))]
    (println (format "%s -> %s" s tokens))
    tokens))


(def detokenize (make-detokenizer "nlp/english-detokenizer.xml"))

(def chunker (make-treebank-chunker "nlp/en-chunker.bin"))

(def pos-tag (make-pos-tagger "nlp/en-pos-maxent.bin"))

(defn chunkize [s]
  "Return a list of maps of pharses and their POS"
  (chunker (pos-tag (tokenize s))))


(defn calculate-frequencies
  "convert list of words to a word-frequency hash"
  [words]
  (reduce (fn [words word] (assoc words word (inc (get words word 0))))
          {}
          words))


(defn combine-two-frequency-maps
  "Merge two word-frequency maps"
  [mapA mapB]
  (reduce (fn [mp [k v]] (assoc mp k (+ v (get mp k 0))))
          mapA
          mapB))


(defn combine-frequency-maps
  "Merge a list of word-frequency maps"
  [maps]
  (reduce combine-two-frequency-maps {} maps))


(defn sort-words-by-frequency
  "Sort a hash of words"
  [words-hash]
  (sort
   (fn [[word1 freq1] [word2 freq2]] (< freq1 freq2))
   words-hash))


(defn wrand
  "Given a vector of values, return an random index
  weighted by the value of that index"
  [slices]
  (let [total (reduce + slices)
        r (rand total)]
    (loop [i 0 isum 0]
      (if (< r (+ (nth slices i) isum))
        i
        (recur (inc i) (+ (nth slices i) isum))))))


(defn random-choice
  "Given a map of keys to values,
  return a random choice over the keys
  preportional to its value"
  [freq-map]
  (let [freq-list (seq freq-map)
        vals (map second freq-list)
        idx (wrand vals)]
    (first (nth freq-list idx))))


;; ({:phrase ["The" "override" "system"], :tag "NP"}
;;  {:phrase ["is" "meant" "to" "deactivate"], :tag "VP"}
;;  {:phrase ["the" "accelerator"], :tag "NP"}
;;  {:phrase ["when"], :tag "ADVP"}
;;  {:phrase ["the" "brake" "pedal"], :tag "NP"}
;;  {:phrase ["is" "pressed"], :tag "VP"})


(defn get-structure
  "Get the structure of a chunk.
  The structure consists of the list
  of phrase tags."
  [chunk]
  (map :tag chunk))


(defn choose-structure
  "Take a list of sentence chunks and
  pick a random sentence structure
  based on those chunks.
  Current implementation is to just pick
  one at random.  Could be more sophisticated"
  [chunks]
  (println "Choosing structure")
  (let [total (count chunks)
        idx (rand total)
        chunk (nth chunks idx)]
    (get-structure chunk)))


(defn get-chunks-by-tag
  "Take a list of chunks group them by
  tag.  Return a map of tag types to phrases"
  [chunk-list]
  (println (format "Get phrase list map from: %s" chunk-list))
  (group-by :tag chunk-list))


(defn random-phrase-from-chunk-list
  "Randomly pick a chunk from a list of chunks
  and return the phrase from that chunk"
  [chunk-list]
  (let [chunk (rand-nth chunk-list)]
    (:phrase chunk)))


(defn create-sentence
  "Create a sentence of the given structure using
  the supplied map of tag types to sentence chunks."
  [structure tag-chunk-map]
  (->> structure
       (map (fn [tag]
              (let [chunk-list (get tag-chunk-map tag)]
                (random-phrase-from-chunk-list chunk-list))))
       flatten
       (clojure.string/join " ")))


(defn capitalize-rt-word
  "If a word begins with rt,
  capitalize the RT"
  [word]
  (if (< 2 (count word))
    word
    (if (= "rt" (clojure.string/lower-case (subs word 0 2)))
      (format "RT%s" (subs word 2))
      word)))


(defn capitalize-rt
  [sentence]
  (let [words (clojure.string/split sentence #"\s")
        cap-words (map capitalize-rt-word words)]
    (clojure.string/join " " cap-words)))


(defn clean-sentence
  "Properly capitalize and otherwise clean a sentence"
  [sentence]
  (->> sentence
       clojure.string/capitalize
       capitalize-rt))


(defn generate-random-sentence
  "Take a list of sentences and generate a new,
  random sentence similar to the supplied list"
  [sentence-list]
  (println "generate-random-sentence")
  (let [chunks (map chunkize sentence-list)
        structure (choose-structure chunks)
        tag-chunk-map (get-chunks-by-tag (flatten chunks))
        raw-sentence (create-sentence structure tag-chunk-map)]
    (println (format "Phrase-list-map %s" tag-chunk-map))
    (clean-sentence raw-sentence)))



;; (def token-similarity-old
;;   "Get the similarity between two tokens"
;;   (fn [tokenA tokenB]
;;     (count (intersection (hash-set tokenA) (hash-set tokenB)))))


;; (def token-similarity
;;   (fn [tokenA tokenB]
;;     (reduce +
;;             (for [a tokenA
;;                   b tokenB]
;;               (do (println (format "Individual token comparison: %s %s" a b))
;;                   (if (= a b) 1 0))))))


;; (def similarity-metric
;;   "Get the similarity between two sentences"
;;   (fn [left right]
;;     (let
;;         [a (tokenize left)
;;          b (tokenize right)]
;;       (do
;;         (println (format "Token lists: %s %s" a b))
;;         (token-similarity a b)))))


;; (def make-similarity-pair
;;   "(token-similarity (tokenize left) (tokenize right))))"
;;   (fn [sentence candidate-list]
;;     (for [cand candidate-list]
;;       (do
;;         (println (format "Sentence: %s Candidate: %s" sentence cand))
;;         (list (token-similarity
;;                (tokenize sentence)
;;                (tokenize cand))
;;               cand)))))


;; (def get-most-similar
;;   "Get the most similar from a list of sentences
;;   to the input sentence"
;;   (fn [sentence candidate-list]
;;     (->> (make-similarity-pair sentence candidate-list)
;;          (sort-by first)
;;          (first)
;;          (last))))
