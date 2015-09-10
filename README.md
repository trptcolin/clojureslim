# clojureslim

The [Slim (Simple List Invocation Method) protocol](http://www.fitnesse.org/FitNesse.UserGuide.WritingAcceptanceTests.SliM.SlimProtocol)
 implemented for Clojure.

This allows you to write Clojure slim fixtures for your FitNesse tests.

See [the FitNesse website](http://fitnesse.org/) for more details.

## Usage

Not fully-featured yet, but satisfies most needs - see the hunt_the_wumpus project for an example.

1 Add clojureslim to your project.clj
 
[![Clojars Project](http://clojars.org/clojureslim/latest-version.svg)](http://clojars.org/clojureslim)

2 Start a FitNesse service inside your project. The working directory of the FitNesse process is important because it
needs 
so it can use lein.  See `hunt_the_wumpus/bin/fitnesse.sh`.  

3 Configure your FitNesse suite.  Add the following to your suite hierarchy.
    
    !define TEST_SYSTEM {slim}
    !define COMMAND_PATTERN {lein run -m clojureslim -v}       
    
## TODO

* Proper documentation and explicit examples with Fitnesse tables and
corresponding fixtures.
* Hash table -> map conversion
* smarter function lookup - default to the same namespace as the fixture
generator
* get the rest of the Slim acceptance tests passing
* unit test coverage for statement-executor ns

## License

Copyright (C) 2011 Colin Jones & Micah Martin

Distributed under the MIT License.
