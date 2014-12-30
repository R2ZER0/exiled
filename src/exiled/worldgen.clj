(ns exiled.worldgen
  (:use [vorce.procedural.simplex :only (noise)]
        [clojure.algo.generic.math-functions :only (log)]))
        
(defn- clamp [x]
  (if (> x 1)
    1
    (if (< x 0)
      0
      x)))  
  
; (((x*2)-1)^3+1)/2
(defn- smooth [x]
  (let [a (- (+ x x) 1)
        b (+ (* a a a) 1)
        y (/ b 2)]
          y))

(defn- get-world-height [x y]
  (smooth (noise x y)))

(defn gen-world-tile [x y]
  {:x x
   :y y
   :height (get-world-height x y)})