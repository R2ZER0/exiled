(ns exiled.core
  (:use [vorce.procedural.simplex :only (noise)]
        [clojure.algo.generic.math-functions :only (log)])
  (:require [clojure.data.json :as json]
            [clojure.string :as s])
  (:import [org.webbitserver WebServer WebServers WebSocketHandler]
           [org.webbitserver.handler StaticFileHandler])
  (:gen-class))
  
(defn- clamp [x]
  (if (> x 1)
    1
    (if (< x 0)
      0
      x)))
  
(defn- logit [t]
  (log (/ t (- 1 t))))

(defn- get-world-height [x y]
  (let [scale 0.1
        x (* x scale)
        y (* y scale)]
    (noise x y)))

(defn- get-world-tile [x y]
  {:x x
   :y y
   :height (get-world-height x y)})
   
(defn- get-world-tiles [tiles]
  (map (fn [tile]
    (get-world-tile (:x tile) (:y tile)))
      tiles))
            
(defn- get-world-area [area]
  (let [ax (:x area)
        ay (:y area)
        aw (:w area)
        ah (:h area)]
    (flatten
      (map (fn [y]
        (map (fn [x]
          (get-world-tile x y))
            (range ax (+ ax aw))))
              (range ay (+ ay ah))))))

; on-X functions generate a reply to the given message
; "gettile" - get a single tile
(defn on-gettile [data]
  (println "gettile" data)
  { :type "gottile"
    :data {:tile (get-world-tile (data :x) (data :y)) }})
   
; "gettiles" - get the given list of tiles
(defn on-gettiles [data]
  (println "gettiles")
  { :type "gottiles"
    :data {:tiles (get-world-tiles (data :tiles))} })

; "getarea" - get an area of tiles at a time
(defn on-getarea [data]
  (println "getarea" data)
  { :type "gottiles"
    :data {:tiles (get-world-area (data :area))} })
    
(def responders
  { "gettile"  on-gettile
    "gettiles" on-gettiles
    "getarea"  on-getarea })
    
(defn default-responder [data]
  (println "unknown message"))

(defn on-message [connection json-message]
  (let [msg (-> json-message json/read-json)
        data  (:data msg)
        mtype (:type msg)]
    (.send connection (json/json-str
                        ((responders mtype default-responder) data)))))

(defn -main
  "Run our web sockets server"
  [& args]
  (println "Starting up!")
  (doto (WebServers/createWebServer 8080)
    (.add "/websocket"
          (proxy [WebSocketHandler] []
            (onOpen [c] (println "opened"))
            (onClose [c] (println "closed"))
            (onMessage [c j] (on-message c j))))
    (.add (StaticFileHandler. "resources/static"))
    (.start)))
