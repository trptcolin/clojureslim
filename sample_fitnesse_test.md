# Steps to in-development success

* `lein deps`
* `ln -s /path/to/clojureslim /path/to/fitnesse`
  * as peer to FitnesseRoot
* in clojureslim:
  * `lein compile`

# Example test

    !define TEST_SYSTEM {slim}
    !define TEST_RUNNER {clojureslim}
    !define COMMAND_PATTERN {java -cp %p %m}
    !define PATH_SEPARATOR {:}

    !path clojureslim/lib/clojure-1.2.1.jar
    !path clojureslim/lib/fitnesse-20110721.jar
    !path clojureslim/classes

    !|script|CljTest|
    | run |



