# clojureslim

The Slim (Simple List Invocation Method) protocol implented for Clojure.

This allows you to write Clojure fixtures for your Fitnesse tests.

See [the Fitnesse website](http://fitnesse.org/) for more details.

## Usage

Not fully-featured yet, but satisfies most needs - see the examples directory
for a number of example fixtures.

## TODO

* Proper documentation and explicit examples with Fitnesse tables and
corresponding fixtures.
* Hash table -> map conversion
* smarter function lookup - default to the same namespace as the fixture
generator
* get the rest of the Slim acceptance tests passing
* unit test coverage for statement-executor ns

## License

Copyright (C) 2011 Colin Jones

Distributed under the MIT License.
