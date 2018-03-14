# clj-opencl-practice
A project for my practice with using OpenCL from clojure.

# Usage
## With clojurecl
```
lein run
```

Default matrix size is 10x10.
If you want to try other size, execute command like this.

```
lein run 1024
```

## Naive
```
lein run -m clj-opencl-practice.naive
```

Default matrix size is 10x10.
If you want to try other size, execute command like this.

```
lein run -m clj-opencl-practice.naive 1024
```

# Times

<table>
<thead>
<tr>
<th rowspan="2">Matrix size</th><th colspan="2">Clojurecl</th><th colspan="2">Naive</th>
</tr>
<tr>
<th>Calculation</th><th>Total</th>
<th>Calculation</th><th>Total</th>
</tr>
</thead>
<tbody align="right">
<tr>
<td>10x10</td>
<td>0.001 sec</td>
<td>0.104 sec</td>
<td>0.018 sec</td>
<td>0.041 sec</td>
</tr>
<tr>
<td>128x128</td>
<td>0.002 sec</td>
<td>0.120 sec</td>
<td>1.209 sec</td>
<td>1.222 sec</td>
</tr>
<tr>
<td>1024x1024</td>
<td>0.624 sec</td>
<td>1.334 sec</td>
<td>452.516 sec</td>
<td>452.528 sec</td>
</tr>
</tbody>
</table>

# License
MIT

# References
- [Interactive GPU Programming - Part 2 - Hello OpenCL](https://dragan.rocks/articles/18/Interactive-GPU-Programming-2-Hello-OpenCL)
- [asukiaaa/c_opencl_practice](https://github.com/asukiaaa/c_opencl_practice)
- [clojurecl/test/clojure/uncomplicate/clojurecl/examples/openclinaction/ch07.clj](https://github.com/uncomplicate/clojurecl/blob/master/test/clojure/uncomplicate/clojurecl/examples/openclinaction/ch07.clj)
