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
        <mdRef label="external" mdType="PlanningProperties" locType="URL" xlink:href="planning.props"/>
        <mdWrap label="internal xml">
            <xmlData>
                <anyxml>
                    <anyelem></anyelem>
                </anyxml>
            </xmlData>
        </mdWrap>
        <mdWrap label="internal bin">
            <binData>U0NBUEUNCj09PT09DQoNClRoaXMgaXMgdGhlIGdpdCByZXBvc2l0b3J5IGZvciB0aGUgU0NBUEUg
                cHJvamVjdC4gSXQgaXMgYSBwbGFjZSB0byBleHBlcmltZW50IHRvZ2V0aGVyIC0gbWF0dXJlIA0K
                YXBwbGljYXRpb25zIHNob3VsZCBiZSBtb3ZlZCB0byBkZWRpY2F0ZWQgcmVwb3NpdG9yaWVzLg0K
                DQpJZiB5b3Ugc3RhcnQgYSBuZXcgcHJvamVjdCBvciBtYWtlIGNoYW5nZXMsIHBsZWFzZSBzZW5k
                IGEgbWFpbCB0byB0aGUgdGVjaGllIG1haWxpbmcgbGlzdDoNCg0KKiBodHRwOi8vbGlzdC5zY2Fw
                ZS1wcm9qZWN0LmV1L2NnaS1iaW4vbWFpbG1hbi9saXN0aW5mby90ZWNoaWUNCg0K</binData>
        </mdWrap>
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
                <input name="params" required="true">
                    <format registryName="mimetype">text/plain</format>
                    <description>Additional encoding parameters</description>
                    <defaultValue>-ar 48000</defaultValue>
                </input>
            </inputs>

            <outputs>
                <output name="output" required="false">
                    <format registryName="mimetype">video/avi; codecs="theora, vorbis"</format>
                    <description>URL reference to output file</description>
                    <extension>flv</extension>
                </output>
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
                <input name="params" required="false">
                    <description>Additional encoding parameters</description>
                </input>
            </inputs>

            <outputs>
                <output name="output" required="false">
                    <format registryName="mimetype">video/avi; codecs="iv50"</format>
                    <description>URL reference to output file</description>
                    <extension>avi</extension>
                </output>
            </outputs>
        </operation>
    </operations>

</tool>
