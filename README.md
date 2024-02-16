# <font color='#12E15D'>**WEKA towards machine learning with artificial fish-swarm algorithm and fuzzy systems**</font>

### Artificial Fish Swarm Algorithm (AFSA)

---



The Artificial Fish Algorithm is based on the collective behavior of fish in searching for food, where each fish represents a potential solution to the problem and moves in the search space to find the best possible solution.

AFSA utilize natural-inspired behaviors such as Swarm, Follow, Prey, and Movement to move towards a specific goal.

* ###  Prey Behaviour






Suppose that an AF’s current state is $X_i$. Randomly select a new state $X_j$ in its visual field according to the following equation:

$X_{j} = X_{i} + rand(-1, 1) \times  visual$

*   $X_{i}$: current state of AF
*   $visual$: represents the visual distance of AF

If $f(x_{i}) \lt f(x_{j})$, that is, satisfy the onward condition, then AF’s state at the next iteration is calculated by following equation:

  $X_{i}^{t+1} = X_{i}^{t} + Rand(0, 1) \times Step \times \frac{(X_{j} -X_{i}^{t})}{\parallel (X_{j} -X_{i}^{t})\parallel}$



*   $X_{i}^{t}$: the AF's current state
*   $X_{i}^{t+1}$: the AF’s next state
*   $ {\parallel (X_{j} -X_{i})\parallel}$ represents the Euclidean distance
*   $Step$ represents the distance that AF can move for each step



* ###  Swarm Behaviour

An AF with the current state $X_{i}$ seeks the companion's number in its current neighbourhood where satisfy $d_i,_j < Visual$; and calculate their centre position $X_{centre}$. If $n_{f} \times f (x_{center}) < \delta \times f (x_{i})$, that is, satisfy the onward condition, then the AF’s state at the next iteration is calculated as follows:

$X_{i}^{t+1} = X_{i}^{t} + Rand(0, 1) \times Step \times \frac{(X_{center} -X_{i}^{t})}{\parallel (X_{center} -X_{i}^{t})\parallel}$

Otherwise the AF carries out the prey behaviour.

* ###  Follow Behaviour

an AF with the current state $x_{i}$ will search for a companion, $x_{min}$, that has the lowest optimization function value among all individuals in its current neighborhood ($d_i,_j < Visual$).
if $n_{f}. f (x_{min}) < \delta \times f (x_{i})$, that is, satisfy the onward
condition, then the AF’s state at the next iteration is calculated
as follows:

$X_{i}^{t+1} = X_{i}^{t} + Rand(0, 1) \times Step \times \frac{(X_{min} -X_{i}^{t})}{\parallel (X_{min} -X_{i}^{t})\parallel}$

*   $n_{f}$ represents the companion's number in the AF current neighbourhood
*   $\delta$ is a positive constant of greater than 1, called the crowded degree factor









Otherwise the AF carries out the prey behaviour.



<div align="center">
  <a href="[https://www.researchgate.net/figure/Flow-chart-of-the-artificial-fish-swarm-algorithm_fig1_327637757]">
    <img src="./images/Flow-chart-of-the-artificial-fish-swarm-algorithm.png" alt="Logo" width="850" height="975">
  </a>

<h3 align="center"> AFSA Flowchart</h3>
</div>



### AF-MINER

---




*   Rule Representation

<div > 
    <img src="./images/AF.png"  width="500" height="200">
<h4> Representation of an AF by a vector of 2D values</h4>
</div>


Each AF is interpreted the corresponding classificatin rule:



IF  $A_{1L} \le A_{1} \le A_{1H}$

> AND $A_{2L} \le A_{2} \le A_{2H}$


> ... ...



> AND $A_{2L} \le A_{2} \le A_{2H}$


THEN $class_{j}$



*   Fitness Function

$fitness = \frac{-n}{D} + \frac{TP}{TP + FN} \times \frac{TN}{TN + FP}$



*   $TP$: number of instances covered by the rule that are classified correctly
*   $FP$: number of instances covered by the rule that are wrongly classified
*   $FN$: number of instances not covered by the rule, whose class matches the training target class
*   $TN$: number of instances not covered by the rule, whose class differs from the training target class




