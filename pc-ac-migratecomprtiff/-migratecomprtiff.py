# solution.py
# windows prog. Uses Imagemagick and FITS. Very rough code!!!
# not to be used in production!!

import os, sys
from lxml import etree as ET
from subprocess import check_call, CalledProcessError

def convertInPlace(src):
    cmd = r'C:\Program Files\ImageMagick-6.5.7-Q16\convert.exe -compress None "' + src + '" "' + src + '"' #contains path to convert.exe
    try:
        check_call(cmd)
    except Exception as e:
        print e

def getCompressed(path):
    for p, dr, fs in os.walk(path):
        for f in fs:
            if f[-3:] == 'tif':
                cmd = 'fits-0.5.0\\fits.bat -i "' + os.path.join(p, f) + '" -o "D:\\solution\\result.xml"' #
                try:
                    check_call(cmd)
                except Exception as e:
                    print e
                    sys.exit()
                tree = ET.parse(r'D:\solution\result.xml')
                root = tree.getroot()
                comp = root.xpath('//ns:compressionScheme', namespaces={'ns': "http://hul.harvard.edu/ois/xml/ns/fits/fits_output"})[0].text
                if comp == 'T6/Group 4 Fax':
                    convertInPlace(os.path.join(p, f))
                    print 'Converted %s' % os.path.join(p, f)

def main(argv):
    if not len(argv) == 2:
        print 'Usage: python solution.py [path\to\dir\of\images\or\dir\of\dirs]'
        sys.exit()
    getCompressed(argv[1])
    
if __name__ == '__main__':
    main(sys.argv)
