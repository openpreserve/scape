<?xml version="1.0" encoding="UTF-8"?>
<tool xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://scape-project.eu/tool tool-1.1_draft.xsd"
    xmlns="http://scape-project.eu/tool" xmlns:xlink="http://www.w3.org/1999/xlink" name="FFMpeg"
    version="0.10" homepage="http://ffmpeg.org/" schemaVersion="1.1">

    <installation>
        <!-- from attribute @operatingSystemName or @otherOperatingSystemName we can generate a URI like "http://purl.org/DP/components#Debian" -->
        <dependencies operatingSystemName="Debian">

            <!--<packageManagerConfig type="Dpkg">ffmpeg, tomcat6</packageManagerConfig>-->
            <packageManager type="Dpkg">
                <config>ffmpeg, tomcat6</config>
                <source>deb http://scape.keep.pt/apt stable main</source>
            </packageManager>

            <packageManager type="Other" otherType="Maven">
                <config>
                    text is valid here
                    <anyXmlIsAlsoValidHere>
                        <dependency>
                            <groupId>pt.gov.dgarq.roda</groupId>
                            <artifactId>roda-common-utils</artifactId>
                            <version>1.0.0</version>
                        </dependency>
                        <dependency>
                            <groupId>pt.gov.dgarq.roda</groupId>
                            <artifactId>roda-common-data</artifactId>
                            <version>1.0.0</version>
                        </dependency>
                    </anyXmlIsAlsoValidHere>
                </config>
                <source>http://artifactory.keep.pt/keep</source>
            </packageManager>

            <dependency name="ffmpeg">
                <license type="FLOSS" name="LGPL-2.1" uri="http://opensource.org/licenses/LGPL-2.1"/>
            </dependency>
            <dependency name="tomcat6">
                <license type="FLOSS" name="Apache-2.0" uri="http://opensource.org/licenses/Apache-2.0"/>
            </dependency>
        </dependencies>

        <dependencies operatingSystemName="Other" otherOperatingSystemName="Android">
            <packageManager type="Other" otherType="apk">
                <config>ffmpeg4android</config>
            </packageManager>
            <dependency name="ffmpeg4android">
                <license type="FLOSS" name="LGPL-2.1" uri="http://opensource.org/licenses/LGPL-2.1"/>
            </dependency>
        </dependencies>

        <license name="Other" otherName="Funny license">This software can only be used by nice people</license>
    </installation>

    <operations>
        <operation name="video2flv">
            <description>Converts any FFmpeg supported video format to FLV</description>
            <command>ffmpeg -y -i ${input} ${params} ${output}</command>
            <inputs>
                <input name="input" required="true">
                    <format registryName="mimetype">video/*</format>
                    <description>URL reference to input file</description>
                    <defaultValue>http://scape.keep.pt/scape/testdata/video/big_buck_bunny_480p_stereo.ogg</defaultValue>
                </input>
                <parameter name="params" required="true">
                    <format registryName="mimetype">text/plain</format>
                    <description>Additional encoding parameters</description>
                    <defaultValue>-ar 48000</defaultValue>
                </parameter>
            </inputs>

            <outputs>
                <output name="output" required="false">
                    <format registryName="mimetype">video/avi; codecs="theora, vorbis"</format>
                    <description>URL reference to output file</description>
                    <extension>flv</extension>
                </output>
            </outputs>
        </operation>

        <operation name="video2flv_pipe">
            <description>Converts any FFmpeg supported video format to FLV reading from stdin and writting to stdout</description>
            <command>ffmpeg -y -i pipe:0 ${params} -f flv pipe:1</command>
            <inputs>
                <stdin required="true">
                    <format registryName="mimetype">video/*</format>
                    <description>Contents of video stream</description>
                </stdin>
                <parameter name="params" required="true">
                    <format registryName="mimetype">text/plain</format>
                    <description>Additional encoding parameters</description>
                    <defaultValue>-ar 48000</defaultValue>
                </parameter>
            </inputs>

            <outputs>
                <stdout>
                    <format registryName="mimetype">video/avi; codecs="theora, vorbis"</format>
                    <description>Contents of converted video stream</description>
                </stdout>
            </outputs>
        </operation>

        <operation name="video2avi">
            <description>Converts any FFmpeg supported video format to AVI</description>
            <command>ffmpeg -y -i ${input} ${params} ${output}</command>
            <inputs>
                <input name="input" required="true">
                    <format registryName="mimetype">video/*</format>
                    <description>URL reference to input file</description>
                </input>
                <parameter name="params" required="false">
                    <description>Additional encoding parameters</description>
                </parameter>
            </inputs>

            <outputs>
                <output name="output" required="false">
                    <format registryName="mimetype">video/avi; codecs="iv50"</format>
                    <description>URL reference to output file</description>
                    <extension>avi</extension>
                </output>
            </outputs>
        </operation>

        <operation name="digital-preservation-characterisation-video-ffprobe-video2xml">
            <description>Characterises video and outputs information as XML</description>
            <command>ffprobe -show_streams -show_format  -print_format xml="x=1"  -noprivate  -show_versions  -i ${input} > ${output}</command>
            <inputs>
                <input name="input" required="true">
                    <description>Reference to input file</description>
                </input>
                <parameter name="params" required="false">
                    <description>Additional conversion parameters</description>
                </parameter>
            </inputs>
            <outputs>
                <output name="output" required="true">
                    <format registryName="web">http://www.ffmpeg.org/schema/ffprobe.xsd</format>
                    <description>Reference to output file</description>
                </output>
            </outputs>
        </operation>

        <operation name="digital-preservation-qa-audio-xcorrsound-migrationqa">
            <description>Compares two wave files</description>
            <command>migrationQA ${wave1} ${wave2}</command>
            <inputs>
                <input name="wave1" required="true">
                    <description>Reference to input file 1</description>
                </input>
                <input name="wave2" required="true">
                    <description>Reference to input file 2</description>
                </input>
                <parameter name="params" required="false">
                    <description>Additional  parameters</description>
                </parameter>
            </inputs>
            <outputs>
                <stdout>
                    <description>Output will be printed on standard out. Will use return code 0 when the files are deemed identical</description>
                </stdout>
            </outputs>
        </operation>


    </operations>

</tool>
