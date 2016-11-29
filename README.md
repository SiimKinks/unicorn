Unicorn (WIP)
=====

Very small library for building one-activity view-based applications.
Unicorn is a bit opinionated, but does not try to force any hard architecture design decisions on the developer.

Unicorn is divided into 2 modules:

* `core` Contains all the core logic, such as ViewManager, Navigator, transitions logic, etc
* `impl` _Optional._ Contains opinionated implementations of the contracts provided by the core module. If some implementation details do not fit your needs, you can reference it to create your own implementation.

Installation
-------------

```groovy
compile 'com.siimkinks.unicorn:unicorn-core:0.2.0'

// If you want the implementation
compile 'com.siimkinks.unicorn:unicorn:0.2.0'
```
Usage
-------
See the [sample project](https://github.com/SiimKinks/unicorn/tree/master/sample).

Updates
------------

All updates can be found in the [CHANGELOG](CHANGELOG.md).

Bugs and Feedback
-----------------

**For bugs, questions and discussions please use the [Github Issues](https://github.com/SiimKinks/sqlitemagic/issues).**

License
--------

    Copyright 2016 Siim Kinks

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.