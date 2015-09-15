# clojureslim

The [Slim (Simple List Invocation Method) protocol](http://www.fitnesse.org/FitNesse.UserGuide.WritingAcceptanceTests.SliM.SlimProtocol)
 implemented for Clojure.

This allows you to write Clojure slim fixtures for your FitNesse tests.

See [the FitNesse website](http://fitnesse.org/) for more details.

As of 1.0.0, Clojureslim implements nearly all of Slim's features.  There are some unimplemented features such as
map converters and SUT attributes that are not all that useful in the Clojure world.

## Usage

Not fully-featured yet, but satisfies most needs - see the hunt_the_wumpus project for an example.

1 Add clojureslim to your project.clj
 
[![Clojars Project](http://clojars.org/clojureslim/latest-version.svg)](http://clojars.org/clojureslim)

2 Start a FitNesse service inside your project. The working directory of the FitNesse process is important because it
needs 
so it can use lein.  See `hunt_the_wumpus/bin/fitnesse.sh`.  

3 Configure your FitNesse suite.  Add the following to your suite hierarchy.
    
    !define TEST_SYSTEM {slim}
    !define COMMAND_PATTERN {lein run -m clojureslim}       
    
## Writing Fixtures

In Clojureslim, Fixtures are implemented as Namespaces.  For example, say you have the following tables:
  
    !|Import|
    |hunt-the-wumpus.fixtures|
    
    !|Make map|
    |start|end|direction|
    |1|2|E|

The `Import` table is a Slim feature merely adds the namespace prefixes (`hunt-the-wumpus.fixtures`) to a list for later use.

`Make map` is custom fixture that must be implemented, most likely in the src file `hunt_the_wumpus/fixtures/make_map.clj`.

You may define a fn named `new` that will be called to "construct" the fixture.  It must return an `atom` that holds the
state of the fixture.  If you don't define `new`, a default implementation is provided returning `(atom {})`.

Every other "fixture method" should be implemented as an fn in fixture namespace.  Each fn must take at least 1 parameter,
the first of which id the state atom.  In the `make-map` example, the Slim will expect the following fns.

    (defn set-start [state room] ...)
    (defn set-end [state room] ...)
    (defn set-direction [state direction] ...)

You may also add fns for the Slim's optional table methods:

    table
    begin-table
    reset
    end-table
    execute
    
Clojureslim aims to follow Slim convention and provide meaningful error messages. It's fairly easy to figure out what
needs to be done by following the trail of errors Slim provides.

## Development

### Run the specs

    lein spec
         
### Acceptance Tests
         
Slim's SlimSuiteTest has been copied into a local wiki.  Most, but not all of the tests are passing.  The last test summary:
        
    Test Pages: 51 right, 6 wrong, 1 ignored, 5 exceptions     Assertions: 303 right, 23 wrong, 2 ignored, 34 exceptions (27.393 seconds)
    
To run these tests:
    
    bin/fitnesse.sh
    
Open the URL: http://localhost:8082/ClojureSlimSuite.SuiteSlimTests    

## License

Copyright (C) 2011 Colin Jones & Micah Martin

Distributed under the MIT License.
