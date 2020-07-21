# Gradle Project Replicator #

This project contains a Gradle plugin and a small command-line app that allows replicating the
structure of a given Gradle project.

This replicates the project's module structure, and for each project replicates the applied plugins,
some of their configurations, and the dependencies (both internal and external).
All the module names are anonymized.

## How to use  ##

See [USAGE.md](docs/USAGE.md)

## todo list ## 

See [TODO.md](TODO.md)

## Disclaimer ##

This is not an officially supported Google product.

## License ##

    Copyright 2020 The Android Open Source Project

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
