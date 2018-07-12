(ns io.hosaka.water.controller
  (:require [manifold.deferred :as d]
            [clojure.tools.logging :as log])
  (:import [com.pi4j.wiringpi Gpio]))

(defn value->bool [v]
  (if (boolean? v)
    v
    (not (= v 0))))

(defn write [pin val]
  (println (format "Wrote value %b to pin %d" val pin))
  (log/info (format "Wrote value %b to pin %d" val pin))
;;  (Gpio/digitalWrite pin val)
  )

(defn toggle
  ([pin val] (toggle pin val nil))
  ([pin val delay]
   (if (or (nil? delay) (< delay 0))
     (write pin val)
     (d/future
       (Thread/sleep delay)
       (write pin val)))))

(defn actuate [{:keys [pin value duration]}]
  (let [b (value->bool value)]
    (if (and (some? duration) (< 0 duration))
      (toggle pin (not b) duration))
    (toggle pin b)))

