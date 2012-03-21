2012-03-20 - rcastro

Fixed typos in attributes of OperatingSystemDependency type
 - renamed @operatingSytemName to @operatingSystemName
 - renamed @otherOperatingSytemName to @otherOperatingSystemName
 - renamed @operatingSytemVersion" to @operatingSystemVersion

2012-03-20 - rcastro

- assigned namespace "http://scape-project.eu/tool" to the schema 
- added very short documentation to some types
- type OperatingSystem renamed to OperatingSystemDependency
- type OSType renamed to OperatingSystemName

For Instalation type:
- renamed element "os" to "dependency"

For type OperatingSystemDependency
- renamed attribute @type to @operatingSytemName
- renamed attribute @othertype to @otherOperatingSytemName
- added attribute @format (enumeration with possible values "Dpkg", "RPM" and "Other")
- added attribute @otherFormat

For type InOut
 - removed element "datatype"

2012-03-15 - rcastro

- "toolspec" element renamed to "tool" (type renamed from "Toolspec" to "Tool")
- /tool/id removed
- /tool/name element transformed into attribute /tool/@name (nonEmptyString)
- /tool/version element transformed into attribute /tool/@version (nonEmptyString)
- /tool/homepage element transformed into attribute /tool/@homepage (xs:anyURI)

Input type
 - removed "restriction" element

Output type
 - removed elements "prefixFromInput", "autoExtension" and "outFileId"


2012-03-06 - rcastro
- Several elements were transformed into types. There's only one element, toolspec.
- Case has changed elements and types. Types have UpperCamelCase, elements have lowerCamelCase

- /toolspec/@model removed 
- /toolspec/name is optional, but if present cannot be emtpy
- /toolspec/homepage is optional, but if present cannot be emtpy
- /toolspec/version is optional, but if present cannot be emtpy
- /toolspec/services/service/operations moved to /toolspec/operations
- /toolspec/services removed
- /toolspec/deployments removed. Will this cause problems in SOAP webservices?

InOut type
 - element "required" transformed into an attribute "@required" (xs:boolean)
 - element "CliMappping" removed (the attribute "@name" should be used to do the mapping)
 - element "format" was added
 - element "documentation" renamed to "description"

Input type
 - renamed element "Default" to "defaultValue"
 - removed attribute defaultValue/@clireplacement
 - what's the purpose of element "restriction"?

Output type
 - What the purpose of element "prefixFromInput" (xs:string) ?
 - What the purpose of element "autoExtension" (xs:boolean) ?
 - What the purpose of element "outFileId" (xs:boolean) ?

