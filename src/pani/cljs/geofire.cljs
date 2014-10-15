(ns pani.cljs.geofire
  (:require [cljs.core.async :refer (chan put!)]))

(defn root
  "Create and return a Geofire root"
  [url]
  (js/GeoFire. url))

(defn set-key
  "Set the location for the given key"
  [r k lat lng]
  (.set r k #js [lat lng]))

(defn get-key
  "Get location associated with the given key, can accept a callback or returns a channel"
  ([r k]
   (let [c (chan)]
     (.get r k #(put! c %))
     c))
  ([r k f]
   (-> (.get r k)
       (.then f)))
  ([r k f e]
   (-> (.get r k)
       (.then f e))))

(defn remove-key
  "Remove the key"
    ([r k f e]
   (-> (.remove r k)
       (.then f e))))

(defn query
  "Setup a geofire query"
  ([r lat lng radius]
   (let [c (chan)]
     (query r lat lng #(put! c %))
     c))

  ([r lat lng radius f]
   (let [q (.query r (js-obj "center" [lat lng]
                             "radius" radius))]
     (doto q
       (.on "key_entered" (fn [k l d]
                            (f {:type     :entered
                                :key      k
                                :location l
                                :distance d})))
       (.on "key_exited" (fn [k l d]
                           (f {:type     :exited
                               :key      k
                               :location l
                               :distance d})))))))
