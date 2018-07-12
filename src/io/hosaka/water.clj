(ns io.hosaka.water
  (:require [com.stuartsierra.component  :as component]
            [manifold.deferred           :as d]
            [config.core                 :refer [env]]
            [io.hosaka.water.handler     :refer [new-handler]]
            [io.hosaka.common.rabbitmq   :refer [new-rabbitmq]])
  (:import [com.pi4j.wiringpi Gpio])
  (:gen-class))

(defn init-system [env]
  (component/system-map
   :rabbitmq (new-rabbitmq env)
   :handler (new-handler)
   ))

(defonce system (atom {}))

(defn -main [& args]
  (let [semaphore (d/deferred)]
    ;;    (Gpio/wiringPiSetupSys)

    (reset! system (init-system env))

    (swap! system component/start)

    (deref semaphore)

    (component/stop @system)

    (shutdown-agents)))
