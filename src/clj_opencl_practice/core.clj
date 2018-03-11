(ns clj-opencl-practice.core
  (:require [uncomplicate.clojurecl.core :refer :all]
            [uncomplicate.clojurecl.info :refer :all]
            [uncomplicate.commons.core :refer [release]]))

(def first-platform (first (platforms)))
(def first-gpu (first (devices first-platform)))
(def ctx (context [first-gpu]))
(def queue (command-queue ctx first-gpu))
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

(def a-width 10)
(def a-height 10)
(def b-width 10)
(def b-height a-width)
(def r-width b-width)
(def r-height a-height)
(def sizeof-float 4)
(def matrix-a-buffer (cl-buffer ctx (* sizeof-float a-width a-height) :read-only))
(def matrix-b-buffer (cl-buffer ctx (* sizeof-float b-width b-height) :read-only))
(def matrix-result-buffer (cl-buffer ctx (* sizeof-float r-width r-height) :write-only))
(def matrix-a (float-array (range (* a-width a-height))))
(def matrix-b (float-array (for [x (range b-width)
                                 y (range b-height)]
                             (if (not= x y)
                               0
                               (if (= x 1)
                                 2
                                 1)))))
(def matrix-result (float-array (* r-width r-height)))

(defn print-matrix [values w h]
  (doall
   (for [line (partition w values)]
     (prn line))))

(defn -main []
  (let [kernels (build-program! (program-with-source ctx [kernel-source]))
        k (kernel kernels "matrix_dot_matrix")]
    (print-matrix matrix-a a-width a-height)
    (print-matrix matrix-b b-width b-height)
    (enq-write! queue matrix-a-buffer matrix-a)
    (enq-write! queue matrix-b-buffer matrix-b)
    (println "enque")
    (set-arg! k 0 matrix-a-buffer)
    (set-arg! k 1 matrix-b-buffer)
    (set-arg! k 2 matrix-result-buffer)
    (set-arg! k 3 (int-array [a-width]))
    (set-arg! k 4 (int-array [b-width]))
    (println "set-args")
    (enq-nd! queue k (work-size-2d r-width r-height))
    (println "execute")
    (enq-read! queue matrix-result-buffer matrix-result)
    (print-matrix matrix-result r-width r-height)
    (release matrix-a)
    (release matrix-b)
    (release matrix-result)
    (release kernels)
    (release queue)
    (release ctx)))
