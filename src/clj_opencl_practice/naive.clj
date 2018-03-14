(ns clj-opencl-practice.naive
  (:require [clj-time.core :as t]))

(def times (atom []))

(defn record-time [name]
  (reset! times (conj @times [name (t/now)])))

(defn get-millis [prev-time current-time]
  (t/in-millis (t/interval prev-time current-time)))

(defn show-last-time-diff []
  (let [prev-current (take-last 2 @times)
        prev (first prev-current)
        prev-time (last prev)
        current (last prev-current)
        current-time (last current)
        current-name (first current)]
    (prn current-name (get-millis prev-time current-time))))

(defn record-and-show-diff [name]
  (record-time name)
  (show-last-time-diff))

(defn print-matrix [values w h]
  (if (> w 10)
    (prn (take 1 values))
    (doall
     (for [line (partition w values)]
       (prn line)))))

(defn show-times []
  (reduce (fn [prev current]
            (let [prev-time (last prev)
                  current-time (last current)
                  current-name (first current)])
            (prn (first current) (get-millis (last prev) (last current)))
            current)
          @times)
  (prn "Total (without waking up time of clojure)"
       (get-millis (last (first @times))
                   (last (last @times)))))

(record-time "start")

(def args *command-line-args*)
(def arg-matrix-len (when (= 1 (count args)) (Integer/parseInt (first args))))
(def arg-a-width (when (= 3 (count args)) (Integer/parseInt (first args))))
(def arg-a-height (when (= 3 (count args)) (Integer/parseInt (second args))))
(def arg-b-width (when (= 3 (count args)) (Integer/parseInt (nth args 2))))

(def matrix-len (or arg-matrix-len 10))
(def a-width (or arg-a-width matrix-len))
(def a-height (or arg-a-height matrix-len))
(def b-width (or arg-b-width matrix-len))
(def b-height a-width)
(def r-width b-width)
(def r-height a-height)
(def matrix-a (range (* a-width a-height)))
(def matrix-b (for [x (range b-width)
                    y (range b-height)]
                (if (not= x y)
                  0
                  (if (= x 1)
                    2
                    1))))
(print-matrix matrix-a a-width a-height)
(print-matrix matrix-b b-width b-height)
(record-time "prepare data")

(defn mul-matrixes [matrix-a matrix-b a-width b-width r-width r-height]
  (for [y (range r-height)
        x (range r-width)]
    (reduce (fn [sum i]
              (+ sum (* (nth matrix-a (+ (* a-width y) i))
                        (nth matrix-b (+ (* b-width i) x)))))
            0
            (range a-width))))

(defn -main [& args]
  (print-matrix (mul-matrixes matrix-a matrix-b a-width b-width r-width r-height) r-width r-height)
  (record-time "get result")
  (show-times))
