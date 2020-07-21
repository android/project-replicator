# How to use the Structure Replicator plugin #

The Replicator plugin provides a task that generates the structure of your project. To use
it you need to first build it, then run the task on your project with the plugin applied.

## Publish the plugin ##

The first step is to build and publish the Gradle plugin:

`./gradlew publishLocal`

This will put the plugin and its dependencies in your `maven local` repository, generally
located under `~./m2/repository`

## Applying the plugin ##

The plugin must be applied to the projects.

- Copy the content of `$ROOT/initscript/init.gradle` to the top of your root project's `build.gradle`. Only the
 `plugins {}` block needs to be before it
- If you do not want to access `jCenter` edit the snippet to use your own repository. 

## Run the task to extract the structure ##

There is a single task to call. It is registered in the root project:

`./gradlew getStructure`

This creates a file in the build folder of the root project: `build/project-structure.json`.
A mapping file is also created next to the structure plugin should you need to analyze the structure. This file
contains the original names from the project and is not used by the generator.


### Generating the test project from the structure ###

To build the generator, use the following command:

`./gradlew generator:installDist`

To launch the generator:

`generator/build/install/generator/bin/generator  --structure /path/to/project-structure.json --destination /path/to/empty/folder`