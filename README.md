Avro Schema Compactor
=====================
A tool for compacting avro schemas into a binary representation.

Why?
----
Avro serializes its schemas as json. This means that:

- Serialized schemas take up way more space than is required
- Consuming the schema requires json parsing
- In the case of single records, the schema is often much larger than the data

Current State
-------------
This is just a POC/WIP project at the moment. 

Currently it only supports "lossy" serialization to be used as the "write-schema" that is stored with the data. It also makes some assumptions about your schema to compact the data further. Eventually various "rules" for compaction and serialization may be available.  

Example:
--------

```java
// Add needed imports
import org.apache.avro.Schema;
import org.avro.compactor.SchemaCompactor;

String schemaLiteral = "{\n" +
    " \"namespace\": \"example.avro\",\n" +
    " \"type\": \"record\",\n" +
    " \"name\": \"Record\",\n" +
    " \"fields\": [\n" +
    "    { \"name\": \"Null\", \"type\": \"null\", \"doc\": \"no value\" },\n" +
    "    { \"name\": \"Boolean\", \"type\": \"boolean\", \"doc\" : \"a binary value\" },\n" +
    "    { \"name\": \"Integer\", \"type\": \"int\", \"doc\": \"32-bit signed integer\" },\n" +
    "    { \"name\": \"Long\", \"type\": \"long\", \"doc\": \"64-bit signed integer\" },\n" +
    "    { \"name\": \"Float\", \"type\": \"long\", \"doc\": \"single precision (32-bit) IEEE 754 floating-point number\" },\n" +
    "    { \"name\": \"Double\", \"type\": \"double\", \"doc\": \"double precision (64-bit) IEEE 754 floating-point number\" },\n" +
    "    { \"name\": \"Bytes\", \"type\": \"bytes\", \"doc\": \"sequence of 8-bit unsigned bytes\" },\n" +
    "    { \"name\": \"String\", \"type\": \"string\", \"doc\" : \"unicode character sequence\" },\n" +
    "    { \"name\": \"Enum\", \"type\": { \"type\": \"enum\", \"name\": \"Foo\", \"symbols\": [\"ALPHA\", \"BETA\", \"DELTA\", \"GAMMA\"] }},\n" +
    "    { \"name\": \"Fixed\", \"type\": { \"type\": \"fixed\", \"name\": \"md5\", \"size\": 16 }},\n" +
    "    { \"name\": \"Array\", \"type\": { \"type\": \"array\", \"items\": \"string\" }},\n" +
    "    { \"name\": \"Map\", \"type\": { \"type\": \"map\", \"values\": \"string\" }},\n" +
    "    { \"name\": \"Union\", \"type\": [\"string\", \"null\"] }\n" +
    " ]\n" +
    "}";

// Show size difference including stripping docs, namespace, etc
Schema schema = new Schema.Parser().parse(schemaLiteral);
System.out.println("Avro Schema: " + schema.toString());
System.out.println("Avro Size: " + schema.toString().getBytes().length);
byte[] bytes = SchemaCompactor.encode(schema);
System.out.println("Encoded Size: " + bytes.length);
Schema decodedSchema = SchemaCompactor.decode(bytes);
System.out.println("Decoded Schema: " + decodedSchema.toString());
// Show size difference just encoding
System.out.println("Dense Avro Size: " + decodedSchema.toString().getBytes().length);
byte[] bytes2 = SchemaCompactor.encode(decodedSchema);
System.out.println("Dense Encoded Size: " + bytes2.length);
```

The output from the above code is:

```java
Avro Schema: {"type":"record","name":"Record","namespace":"example.avro","fields":[{"name":"Null","type":"null","doc":"no value"},{"name":"Boolean","type":"boolean","doc":"a binary value"},{"name":"Integer","type":"int","doc":"32-bit signed integer"},{"name":"Long","type":"long","doc":"64-bit signed integer"},{"name":"Float","type":"long","doc":"single precision (32-bit) IEEE 754 floating-point number"},{"name":"Double","type":"double","doc":"double precision (64-bit) IEEE 754 floating-point number"},{"name":"Bytes","type":"bytes","doc":"sequence of 8-bit unsigned bytes"},{"name":"String","type":"string","doc":"unicode character sequence"},{"name":"Enum","type":{"type":"enum","name":"Foo","symbols":["ALPHA","BETA","DELTA","GAMMA"]}},{"name":"Fixed","type":{"type":"fixed","name":"md5","size":16}},{"name":"Array","type":{"type":"array","items":"string"}},{"name":"Map","type":{"type":"map","values":"string"}},{"name":"Union","type":["string","null"]}]}
Avro Size: 950
Encoded Size: 100
Decoded Schema: {"type":"record","name":"Record","fields":[{"name":"Null","type":"null"},{"name":"Boolean","type":"boolean"},{"name":"Integer","type":"int"},{"name":"Long","type":"long"},{"name":"Float","type":"long"},{"name":"Double","type":"double"},{"name":"Bytes","type":"bytes"},{"name":"String","type":"string"},{"name":"Enum","type":{"type":"enum","name":"Foo","symbols":["ALPHA","BETA","DELTA","GAMMA"]}},{"name":"Fixed","type":{"type":"fixed","name":"md5","size":16}},{"name":"Array","type":{"type":"array","items":"string"}},{"name":"Map","type":{"type":"map","values":"string"}},{"name":"Union","type":["string","null"]}]}
Dense Avro Size: 617
Dense Encoded Size: 100
```

> *Note*: The size can be even smaller by choosing shorter field names

How It Works
------------
Why are Avro schemas "large"?
- The schema is serialized as a json string
- Fields are resolved "by-name"
-- This is great for schema evolution but means each field name is in the schema
-- You can also ensure you schema is smaller by using short field names
-- Other formats use numeric ids to reduce the size
Many fields that are not needed when reading the data are kept in the serialized json. All of the fields below are only used at write time:
-- doc, defaults, order, aliases (record or field), namespace (?)

Why are these compacted Avro schemas smaller?
- Removes fields that are not required at read time
- Serializes in a versioned binary bit packed format
-- Stores types in 4 bits, since avro only has 14 types
-- Stores name/symbol characters in 6 bits, since avro only allows 64 characters ([A-Za-z0-9_])
-- Limits the number of characters, fields, etc to use smaller representations for their size

TODO
----
- Static compactor builder for various versions, rules, etc
- Add examples (compare data sizes: json data, schema + binary data, compacted schema + binary data)
- Support logical types
- Command line tool
- Add feature flags to enabled higher/lower compaction 
- Support write-time/lossless compaction retaining all fields (doc, defaults, etc)
- Unit Tests, Logging, & Javadoc

How To Build:
-------------
>*Note*:
>   This project uses [Gradle](http://www.gradle.org). You must install [Gradle(2.10)](http://www.gradle.org/downloads).
>   If you would rather not install Gradle locally you can use the [Gradle Wrapper](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html) by replacing all references to ```gradle``` with ```gradlew```.

1. Execute ```gradle build```
2. Find the artifact jars in './<sub-project>/build/libs/'

Intellij Project Setup:
-----------------------
1. Execute ```gradle idea```
2. Open project folder in Intellij or open the generated .ipr file

>*Note*:
>   If you have any issues in Intellij a good first troubleshooting step is to execute ```gradle cleanIdea idea```

Eclipse Project Setup:
----------------------
1. Execute ```gradle eclipse```
2. Open the project folder in Eclipse

>*Note*:
>   If you have any issues in Eclipse a good first troubleshooting step is to execute ```gradle cleanEclipse eclipse```










