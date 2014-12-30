(ns vorce.procedural.simplex)

; Direct translation of
; https://github.com/mikera/clisk/blob/develop/src/main/java/clisk/noise/Simplex.java
; to clojure.
; ...... friday night fun.
; Only supports 2d right now.

(defstruct grad :x :y :z :w)

(defn- g
  ([x y z w]
    (struct grad x y z w))
  ([x y z]
    (g x y z nil))
  ([x y]
    (g x y nil nil)))

(def grad3 [(g 1 1 0) (g -1 1 0)
            (g 1 -1 0) (g -1 -1 0)
            (g 1 0 1) (g -1 0 1)
            (g 1 0 -1) (g -1 0 -1)
            (g 0 1 1) (g 0 -1 1)
            (g 0 1 -1) (g 0 -1 -1)])

(def grad4 [(g 0 1 1 1) (g 0 1 1 -1) (g 0 1 -1 1)
            (g 0 1 -1 -1) (g 0 -1 1 1)
            (g 0 -1 1 -1) (g 0 -1 -1 1)
            (g 0 -1 -1 -1) (g 1 0 1 1)
            (g 1 0 1 -1) (g 1 0 -1 1)
            (g 1 0 -1 -1) (g -1 0 1 1)
            (g -1 0 1 -1) (g -1 0 -1 1)
            (g -1 0 -1 -1) (g 1 1 0 1)
            (g 1 1 0 -1) (g 1 -1 0 1)
            (g 1 -1 0 -1) (g -1 1 0 1)
            (g -1 1 0 -1) (g -1 -1 0 1)
            (g -1 -1 0 -1) (g 1 1 1 0)
            (g 1 1 -1 0) (g 1 -1 1 0)
            (g 1 -1 -1 0) (g -1 1 1 0)
            (g -1 1 -1 0) (g -1 -1 1 0)
            (g -1 -1 -1 0)])

(def p [151, 160, 137, 91, 90, 15, 131, 13, 201, 95,
               96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37,
               240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62,
               94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56,
               87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139,
               48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133,
               230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25,
               63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200,
               196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3,
               64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255,
               82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42,
               223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153,
               101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79,
               113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242,
               193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249,
               14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204,
               176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222,
               114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180])

(def perm
  (for [i (range (* 2 (count p)))]
    (->> (bit-and i 255) (nth p))))

(defn- perm-mod [ps]
  (into [] (for [i ps] (mod i 12))))

; Skewing and unskewing factors for 2, 3, and 4 dimensions
(def f2 (-> (Math/sqrt 3.0) (- 1.0) (* 0.5)))
(def g2 (/ (->> (Math/sqrt 3.0) (- 3.0)) 6.0))
(def f3 (/ 1.0 3.0))
(def g3 (/ 1.0 6.0))
(def f4 (-> (Math/sqrt 5.0) (- 1.0) (/ 4.0)))
(def g4 (/ (->> (Math/sqrt 5.0) (- 5.0)) 20))

(defn- dot
  ([g x y]
    (let [xr (* (:x g) x)
          yr (* (:y g) y)]
      (+ xr yr)))
  ([g x y z]
    (let [zr (* (:z g) z)]
      (-> (dot g x y) (+ zr))))
  ([g x y z w]
    (let [wr (* (:w g) w)]
      (-> (dot g x y z) (+ wr)))))

; call with f2
(defn- hairy-2d [x y f]
  (* (+ x y) f))

; Determine which simplex we are in.
(defn- simplex [xd yd]
  (if (> xd yd)
    [1 0]
    [0 1]))

; Work out the hashed gradient indices of the three simplex corners
(defn- hashed-gradient-indices [i j i1 j1 p mod]
  (let [ii (bit-and i 255)
        jj (bit-and j 255)
        gi0 (->> (nth p jj) (+ ii) (nth mod))
        gi1 (->> (nth p (+ jj j1)) (+ i1 ii) (nth mod))
        gi2 (->> (nth p (+ jj 1)) (+ 1 ii) (nth mod))]
    [gi0 gi1 gi2]))

(defn- contribution [g0 g1 g2 gi0 gi1 gi2 grad3]
  (let [t0 (- 0.5 (* (:x g0) (:x g0)) (* (:y g0) (:y g0)))
        n0 (if (< t0 0)
             0.0
             (* t0 t0 t0 t0 (dot (nth grad3 gi0) (:x g0) (:y g0))))
        t1 (- 0.5 (* (:x g1) (:x g1)) (* (:y g1) (:y g1)))
        n1 (if (< t1 0)
             0.0
             (* t1 t1 t1 t1 (dot (nth grad3 gi1) (:x g1) (:y g1))))
        t2 (- 0.5 (* (:x g2) (:x g2)) (* (:y g2) (:y g2)))
        n2 (if (< t2 0)
             0.0
             (* t2 t2 t2 t2 (dot (nth grad3 gi2) (:x g2) (:y g2))))]
    (* (+ n0 n1 n2) 70.0)))

(defn- noise-2d [x y p mod]
  (let [hair (hairy-2d x y f2)
        i (int (Math/floor (+ x hair)))
        j (int (Math/floor (+ y hair)))
        t (* (+ i j) g2)
        xoffset0 (- x (- i t)) ; The x,y distances from the cell origin
        yoffset0 (- y (- j t))
        offset0 (g xoffset0 yoffset0)
        [i1 j1] (simplex xoffset0 yoffset0)
        xoffset1 (+ (- xoffset0 i1) g2) ; Offsets for middle corner in (x,y) unskewed coords
        yoffset1 (+ (- yoffset0 j1) g2)
        offset1 (g xoffset1 yoffset1)
        xoffset2 (+ (- xoffset0 1.0) (* 2.0 g2)) ; Offsets for last corner in (x,y) unskewed coords
        yoffset2 (+ (- yoffset0 1.0) (* 2.0 g2))
        offset2 (g xoffset2 yoffset2)
        [gi0 gi1 gi2] (hashed-gradient-indices i j i1 j1 p mod)]
    (contribution offset0 offset1 offset2 gi0 gi1 gi2 grad3)))

(defn noise
  ([x y]
    (+ 0.5 (* 0.5 (noise-2d x y perm (perm-mod perm))))))
