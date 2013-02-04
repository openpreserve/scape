<?xml version="1.0" encoding="UTF-8"?>
<tool xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://scape-project.eu/tool tool-1.0_draft.xsd"
    xmlns="http://scape-project.eu/tool" xmlns:xlink="http://www.w3.org/1999/xlink" name="FFMpeg"
    version="0.10" homepage="http://ffmpeg.org/" schemaVersion="1.0">

    <installation>
        <dependency operatingSystemName="Debian">ffmpeg, tomcat6</dependency>
        <dependency operatingSystemName="Other" otherOperatingSystemName="Android" format="Other" otherFormat="apk">ffmpeg, tomcat6</dependency>
        <license type="Other" otherType="Funny license">This software can only be used by nice people</license>
    </installation>

    <otherProperties>
        <property name="cost">0</property>
        <property name="rating">5</property>
    </otherProperties>
    
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
