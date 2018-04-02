# ScriptController

Extended API for Java's [Scripting Engine framework](https://docs.oracle.com/javase/8/docs/api/javax/script/package-summary.html) (`javax.script`) with the Nashorn JavaScript engine.


ScriptController provides:

* Automatic detection & reloading of script files
* Global export registry system
* Builder-style API for constructing new controllers and environments

### Usage

The main entry point into the API is via `ScriptController`.

The following is a valid way to obtain an instance.

```java
ScriptController controller = ScriptController.builder().build();
```

However, it's likely that you'll want to customize certain aspects of the controller.

```java
Logger logger = Logger.getLogger("my-logger");
SpecialObject mySpecialObject = new SpecialObject();

EnvironmentSettings environmentSettings = EnvironmentSettings.builder()
        .initScript("init.js")
        .withDefaultPackageImport("java.util")
        .withDefaultTypeImport("me.lucko.test.MyTestClass")
        .withDefaultTypeImport("com.example.RuntimeManager")
        .withBindings((script, accumulator) -> {
            accumulator.put("specialObject", mySpecialObject);
            accumulator.put("bootstrap", this);
        })
        .build();

ScriptController controller = ScriptController.builder()
        .logger(SystemLogger.usingJavaLogger(logger))
        .withDirectory(Paths.get("scripts/"))
        .defaultEnvironmentSettings(environmentSettings)
        .build();
```

These are just a few examples of the available settings. ScriptController is designed with flexibility in mind.

`ScriptController`s "manage" a number of `ScriptEnvironments`, which center around a root scripts directory.

You can define these environments when building the controller, using `.withDirectory(...)`, or after the controller has been constructed.

```java
ScriptController controller = ScriptController.builder().build();
ScriptEnvironment env = controller.setupNewEnvironment(Paths.get("src"), EnvironmentSettings.defaults());
```

`ScriptEnvironment` exposes further instances which make up the overall system.

```java
ExportRegistry exports = env.getExportRegistry();
EnvironmentScriptLoader loader = env.getLoader();
ScriptRegistry scriptRegistry = env.getScriptRegistry();
```

* `ExportRegistry` holds a shared set of "exports". This is effectively a namespace which is shared between scripts.
* `EnvironmentScriptLoader` is responsible for loading/reloading/unloading scripts, and monitoring the source directory for changes.
* `ScriptRegistry` holds all currently loaded scripts.

The library has extensive JavaDocs - all public classes, methods and fields have documentation. More detailed commentary and explanation on the purpose, behaviour and usage of methods and classes can be found there.
