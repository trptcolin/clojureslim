(ns fitnesse.slim.test.library.library-in-set-up
  (:require [clojureslim.java :as j]))

(j/wrap-java-fixture fitnesse.slim.test.library.LibraryInSetUp)