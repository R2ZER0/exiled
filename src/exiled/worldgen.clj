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
          
(defn- height2kind [height]
  (cond
    (> n 204) "mountain"
    (> n 40)  "grass"
    :else     "water"))

(defn- get-world-height [x y]
  (* 255
    (let [s 0.05]
      (noise (* x s) (* y s)))))

(defn gen-world-tile [x y]
  (let [height (get-world-height x y)
        kind   (height2kind height)]
    { :x x
      :y y
      :height height
      :kind kind }))