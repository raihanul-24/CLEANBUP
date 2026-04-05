# CleanBUP

This is a Java Swing DSA project that simulates bin fill levels across floors and uses 0/1 Knapsack to select bins for collection.

## Algorithms Used

- 0/1 Knapsack dynamic programming to select bins per floor under a fixed capacity.
- Priority-based floor ordering (sorting by computed priority score).
- Threshold filtering to consider only bins above the fill-level limit.


## To run the program, compile and execute:

```bash
javac -d . CleanBUPWithTrees.java
java cleanbupwithtrees.CleanBUPWithTrees
```

## Credits
This project was successfully delivered by a development team including [Raihanul Hasan](https://github.com/raihanul-24), who contributed to the overall system architecture and collaborated on core implementation.
