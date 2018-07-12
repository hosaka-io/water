(ns io.hosaka.water.handler
  (:require [clojure.string :as str]
            [clojure.tools.logging      :as log]
            [com.stuartsierra.component :as component]
            [io.hosaka.common.rabbitmq   :as rabbitmq]
            [io.hosaka.water.controller :refer [actuate]]
            [manifold.stream :as s]
            [manifold.deferred :as d]))

(defn str->map [v]
  (cond
    (map? v) v
    (string? v) (let [v (str/split v #",")]
                  (case (count v)
                    2 (let [[pin value] v] {:pin (read-string pin) :value (read-string value)})
                    3 (let [[pin value duration] v] {:pin (read-string pin) :value (read-string value) :duration (read-string duration)})
                    nil))
    :else nil))

(defn actuate-handler [{:keys [body response]}]
  (let [v (str->map body)]
    (actuate v)
    (d/success! response true)))


(defrecord Handler [rabbitmq streams]
  component/Lifecycle

  (start [this]
    (do
      (rabbitmq/declare-task-queue rabbitmq "tasks.water_service.pin.actuate")
      (let[actuate-stream (rabbitmq/queue-subscription rabbitmq "tasks.water_service.pin.actuate")]
        (s/consume actuate-handler actuate-stream)
        (assoc this :streams (vector actuate-stream)))))

  (stop [this]
    this))

(defn new-handler []
  (component/using
   (map->Handler {})
   [:rabbitmq]))
