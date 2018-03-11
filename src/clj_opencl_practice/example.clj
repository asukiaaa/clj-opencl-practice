; https://dragan.rocks/articles/18/Interactive-GPU-Programming-2-Hello-OpenCL
(ns clj-opencl-practice.example
  (:require [uncomplicate.clojurecl.core :refer :all]
            [uncomplicate.clojurecl.info :refer :all]))

(def amd-platform (second (platforms)))
(def my-amd-gpu (first (devices amd-platform)))
(def ctx (context [my-amd-gpu]))
(def gpu-array (cl-buffer ctx 1024 :read-write))
(def main-array (float-array (range 256)))
(def queue (command-queue ctx my-amd-gpu))
(enq-write! queue gpu-array main-array)
(def roundtrip-array (float-array 256))
(enq-read! queue gpu-array roundtrip-array)
(def kernel-source
      "__kernel void mul10(__global float *a) {
         int i = get_global_id(0);
         a[i] = a[i] * 10.0f;
       };")

(defn -main []
  (let [hello-program (build-program! (program-with-source ctx [kernel-source]))
        mul10 (kernel hello-program "mul10")
        result (float-array 256)]
    (set-arg! mul10 0 gpu-array)
    (enq-nd! queue mul10 (work-size-1d 256))
    (enq-read! queue gpu-array result)
    (take 12 result)))
