(ns clj-opencl-practice.core
  (:require [clj-time.core :as t]
            [uncomplicate.clojurecl.core :refer :all]
            [uncomplicate.commons.core :refer [release]]))

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

(def first-platform (first (platforms)))
(def first-gpu (first (devices first-platform)))
(def ctx (context [first-gpu]))
(def queue (command-queue ctx first-gpu))

(record-time "loaded gpu info")

(def kernel-source "
  __kernel void matrix_dot_matrix(
    __global const float* A,
    __global const float* B,
    __global float* Result,
    const int wA,
    const int wB
  ) {
    const int x = get_global_id(0);
    const int y = get_global_id(1);
    float value = 0;
    for (int i = 0; i < wA; ++i) {
      int index_a = y * wA + i;
      int index_b = i * wB + x;
      float elementA = A[index_a];
      float elementB = B[index_b];
      if (elementA != 0.0 && elementB != 0.0) {
        value = value + elementA * elementB;
      }
    }
    Result[y * wB + x] = value;
  }")
(def kernels (build-program! (program-with-source ctx [kernel-source])))
(record-time "set kernel source")
(def mul-matrixes-k (kernel kernels "matrix_dot_matrix"))
(record-time "load kernel")

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
(def matrix-a (float-array (range (* a-width a-height))))
(def matrix-b (float-array (for [x (range b-width)
                                 y (range b-height)]
                             (if (not= x y)
                               0
                               (if (= x 1)
                                 2
                                 1)))))
(def matrix-result (float-array (* r-width r-height)))
(print-matrix matrix-a a-width a-height)
(print-matrix matrix-b b-width b-height)
(def sizeof-float 4)
(def matrix-a-buffer (cl-buffer ctx (* sizeof-float a-width a-height) :read-only))
(def matrix-b-buffer (cl-buffer ctx (* sizeof-float b-width b-height) :read-only))
(def matrix-result-buffer (cl-buffer ctx (* sizeof-float r-width r-height) :write-only))
(enq-write! queue matrix-a-buffer matrix-a)
(enq-write! queue matrix-b-buffer matrix-b)
(record-time "hold buffers")

(defn release-resources []
  (release matrix-a)
  (release matrix-b)
  (release matrix-result)
  (release kernels)
  (release queue)
  (release ctx))

(defn -main [& args]
  (set-arg! mul-matrixes-k 0 matrix-a-buffer)
  (set-arg! mul-matrixes-k 1 matrix-b-buffer)
  (set-arg! mul-matrixes-k 2 matrix-result-buffer)
  (set-arg! mul-matrixes-k 3 (int-array [a-width]))
  (set-arg! mul-matrixes-k 4 (int-array [b-width]))
  (record-time "set args")
  (enq-nd! queue mul-matrixes-k (work-size-2d r-width r-height))
  (enq-read! queue matrix-result-buffer matrix-result)
  (print-matrix matrix-result r-width r-height)
  (record-time "load result")
  (release-resources)
  (record-time "releas resources")
  (show-times))
