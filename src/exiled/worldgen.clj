(ns exiled.worldgen
  (:use [vorce.procedural.simplex :only (noise)]
        [clojure.algo.generic.math-functions :only (log)]
        [clojure.math.numeric-tower :only (floor)]))
        
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
          
(defn- height2kind [h]
  (cond
    (> h 204) "mountain"
    (> h 40)  "grass"
    :else     "water"))

(defn- get-world-height [x y]
  (let [s 0.05
        x (* x s)
        y (* y s)]
    (-> (noise x y) (* 255) floor int)))

(defn gen-world-tile [x y]
  (let [height (get-world-height x y)
        kind   (height2kind height)]
    { :x x
      :y y
      :height height
      :kind kind }))