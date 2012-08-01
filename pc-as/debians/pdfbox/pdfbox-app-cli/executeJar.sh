#!/bin/sh

################################################################################
#                  Copyright 2012 The SCAPE Project Consortium
#
#   This software is copyrighted by the SCAPE Project Consortium. 
#   The SCAPE project is co-funded by the European Union under
#   FP7 ICT-2009.4.1 (Grant Agreement number 270137).
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#                   http://www.apache.org/licenses/LICENSE-2.0              
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   
#   See the License for the specific language governing permissions and
#   limitations under the License.
################################################################################

LINK=`readlink -m $0`
DIR=`dirname $LINK`
java -jar $DIR/pdfbox-app-1.7.1.jar "$@"
