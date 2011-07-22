# Steps to in-development success

* `lein deps`
* `ln -s /path/to/clojureslim /path/to/fitnesse`
  * as peer to FitnesseRoot

# Example test

    !define TEST_SYSTEM {slim}
    !define TEST_RUNNER {clojure.main clojureslim/script/clojureslim.clj}
    !define COMMAND_PATTERN {java -cp %p %m}
    !define PATH_SEPARATOR {:}

    !path clojureslim/lib/clojure-1.2.1.jar
    !path clojureslim/lib/fitnesse-20110721.jar
    !path clojureslim/src

    !|script|CljTest|
    | run |



