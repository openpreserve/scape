Nanite - a tiny droid
=====================

This small amount of code wraps up the DROID engine in a simpler wrapper that is better suited 
to running from the CLI or as an Hadoop map-reduce JAR. Critically, this means being able to 
identify byte arrays or input streams without assuming they are file-backed (as Droid does).

This code required that you have compiled the Droid code and installed it in your local Maven 
repository. Contact the us via http://www.openplanetsfoundation.org/contact if you have 
problems.

Build it using:

<code>
  mvn package
</code>

Run it using:

<code>
  java -jar target/pc-cc-nanite-0.0.1-SNAPSHOT-jar-with-dependencies.jar {input file} 
</code>

Example output:

<code>
  opf:pc-cc-nanite andy$ java -jar target/pc-cc-nanite-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/Downloads/168-777-1-PB.pdf 
  2011-10-13 11:11:14,209  WARN Signature [id:293] will always scan up to maximum bytes.  Matches formats:  [Name:Internet Message Format] [PUID:fmt/278]  
  2011-10-13 11:11:14,257  WARN Signature [id:305] will always scan up to maximum bytes.  Matches formats:  [Name:WARC] [PUID:fmt/289]  
  MATCHING: fmt/18, Acrobat PDF 1.4 - Portable Document Format 1.4
  Content-Type: application/pdf; version=1.4
</code>

Note use of extended MIME types to act as interoperable identifiers.

I've also found that the -with-dependencies jar can be deployed as part of an Hadoop job and it works fine.

TODO
----

* Add an InputstreamIdentificationRequest class.
* Make ByteArray and Inputstream Identification Request classes spool out to tmp files 
so the contained-id can use getSourceFile and open up the data.
* Add in the Container-level identification engine (only does bytestream ID at present).
* Make the slow, auto-updating, Spring-based SignatureManager start-up optional.
* Consider modifying the Droid source code so Zip and other container-opening algorithms 
can operate directly on streams instead of spooling to tmp. Note that this all overlaps
somewhat with JHOVE2.
* Consider stripping log4j config out of /droid-core-interfaces/src/main/resources/log4j.properties because this overrides local config when assembling one jar.
* Consider cleaning up Droid and Planets dependencies, e.g. multiple Spring versions. Perhaps no longer an issue as dependency on planets-suite:core-utils has been dropped.
* Consider cleaning up the SubmissionGateway from droid-results so that the logic that calls the container identification engine and combines the results is put together while keeping it separate from all the threading and other UI-oriented stuff.

