# SysML v2 Reference Generator

Generates the following artifacts for the [SysML v2 API & Services](https://github.com/Systems-Modeling/SysML-v2-API-Services) reference implementation:

- [Java interfaces](src/main/kotlin/com/twingineer/sysml/ref/gen/GenerateInterfaces.kt)
- [JPA (Hibernate) entity classes](src/main/kotlin/com/twingineer/sysml/ref/gen/GenerateImpl.kt)
- [JSON Schema](src/main/kotlin/com/twingineer/sysml/ref/gen/GenerateJsonSchema.kt) - also supplies [SysML v2 Pilot Implementation](https://github.com/Systems-Modeling/SysML-v2-Pilot-Implementation/tree/master/org.omg.sysml/json-schema)
- [JSON-LD contexts](src/main/kotlin/com/twingineer/sysml/ref/gen/GenerateJsonLd.kt)

Each of these have a command-line interface (CLI), e.g. [`InterfaceGeneratorCli`](src/main/kotlin/com/twingineer/sysml/ref/gen/GenerateInterfaces.kt#InterfaceGeneratorCli) and Gradle task(s), e.g. [`generateInterfaces`](build.gradle.kts).

The typical generation is aggregated in [`generate.sh`](generate.sh) and executed as follows.

```shell
./generate.sh /path/to/SysML.mof /path/to/SysML-v2-API-Services
```

Notes:

- You will likely want to [`clean.sh` from SysML-v2-API-Services](https://github.com/Systems-Modeling/SysML-v2-API-Services/blob/master/clean.sh) first so that files that are no longer a result of the generation are removed.
- The [`SysML.json`](https://github.com/Systems-Modeling/SysML-v2-Pilot-Implementation/blob/master/org.omg.sysml/json-schema/SysML.json) artifact can be found at [`/path/to/SysML-v2-API-Services/conf/json/schema/metamodel/schemas.json`](https://github.com/Systems-Modeling/SysML-v2-API-Services/blob/master/conf/json/schema/metamodel/schemas.json).

A second script [`generate-kerml.sh`](generate-kerml.sh) is used to generate the [`KerML.json`](https://github.com/Systems-Modeling/SysML-v2-Pilot-Implementation/blob/master/org.omg.sysml/json-schema/KerML.json) artifact and can be found at [`build/gen/jsonschema-kerml/schemas.json`](build/gen/jsonschema-kerml/schemas.json). It is executed as follows.

```shell
./generate-kerml.sh /path/to/KerML.mof
```