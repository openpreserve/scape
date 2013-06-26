//*****************************************************************************
// Contrast Limited Adaptive Histogram Equalization (CLAHE) for OpenCV
//-----------------------------------------------------------------------------
// Original CLAHE implementation by Karel Zuiderveld, karel@cv.ruu.nl
// in "Graphics Gems IV", Academic Press, 1994.
//-----------------------------------------------------------------------------
// Converted to OpenCV format by Toby Breckon, toby.breckon@cranfield.ac.uk
// Copyright (c) 2009 School of Engineering, Cranfield University
// License : LGPL - http://www.gnu.org/licenses/lgpl.html
//-----------------------------------------------------------------------------
// Improved by Shervin Emami on 17th Nov 2010, shervin.emami@gmail.com
// http://www.shervinemami.co.cc/
//*****************************************************************************


#include "opencv/cv.h"       // open cv general include file

#include "CLAHE.h"  // opencv CLAHE include file

#include <stdio.h>
#include <stdlib.h>

using namespace std;

// *****************************************************************************

// type defs. for Graphic Gemms Code - see later

#define BYTE_IMAGE

#ifdef BYTE_IMAGE
typedef unsigned char kz_pixel_t;        /* for 8 bit-per-pixel images */
#define uiNR_OF_GREY (256)
#else
typedef unsigned short kz_pixel_t;       /* for 12 bit-per-pixel images (default) */
# define uiNR_OF_GREY (4096)
#endif

/************* Prototype of Graphic Gemms CLAHE function. *********************/

static int CLAHE_ext(kz_pixel_t* pImage, unsigned int uiXRes, 
		  unsigned int uiYRes, kz_pixel_t Min,
          kz_pixel_t Max, unsigned int uiNrX, unsigned int uiNrY,
          unsigned int uiNrBins, float fCliplimit);

// *****************************************************************************

// General Notes:
//
// The number of "effective" greylevels in the output image is set by bins; selecting
// a small value (eg. 128) speeds up processing and still produce an output image of
// good quality. The output image will have the same minimum and maximum value as the input
// image. A clip limit smaller than 1 (?? is this correct ) results in 
//  standard (non-contrast limited) AHE.
 

// cvAdaptEqualize(src, dst, xdivs, ydivs, bins)
//
// perform adaptive histogram equalization (AHE)
//
// src - pointer to source image (must be single channel 8-bit)
// dst - pointer to destination image (must be single channel 8-bit)
// xdivs - number of cell divisions to use in vertical (x) direction (MIN=2 MAX = 16)
// ydivs - number of cell divisions to use in vertical (y) direction (MIN=2 MAX = 16)
// bins - number of histogram bins to use per division
// range - either of CV_CLAHE_RANGE_INPUT or CV_CLAHE_RANGE_FULL to limit the output
//         pixel range to the min/max range of the input image or the full range of the
//         pixel depth (8-bit in this case)

void cvAdaptEqualize(IplImage *src, IplImage *dst, 
			unsigned int xdivs, unsigned int ydivs, unsigned int bins, int range)
{
	
	// call CLAHE with negative limit (as flag value) to perform AHE (no limits)
	
	cvCLAdaptEqualize(src, dst, xdivs, ydivs, bins, -1.0, range);
}

// cvCLAdaptEqualize(src, dst, xdivs, ydivs, bins, limit)
//
// perform contrast limited adaptive histogram equalization (CLAHE)
//
// src - pointer to source image (must be single channel 8-bit)
// dst - pointer to destination image (must be single channel 8-bit)
// xdivs - number of cell divisions to use in vertical (x) direction (MIN=2 MAX = 16)
// ydivs - number of cell divisions to use in vertical (y) direction (MIN=2 MAX = 16)
// bins - number of histogram bins to use per division (MIN=2 MAX = 256)
// limit - contrast limit for localised changes in contrast 
// (limit >= 0 gives standard AHE without contrast limiting)
// range - either of CV_CLAHE_RANGE_INPUT or CV_CLAHE_RANGE_FULL to limit the output
//         pixel range to the min/max range of the input image or the full range of the
//         pixel depth (8-bit in this case)


void cvCLAdaptEqualize(IplImage *src, IplImage *dst, 
			unsigned int xdivs, unsigned int ydivs, unsigned int bins, 
			float limit, int range)
{
	
	double min_val, max_val;
	unsigned char min, max;
	
	// CV_FUNCNAME( "cvCLAdaptEquualize" );
    // __BEGIN__;

	// check the inputs to the function
	
	int type;

	//if ((src == NULL) || (dst == NULL))
        //CV_ERROR( CV_StsUnsupportedFormat, "NULL value passed as image to function" );
	
    	//CV_CALL( type = cvGetElemType( src ));
    	//if( type != CV_8UC1 )
        	//CV_ERROR( CV_StsUnsupportedFormat, "Only 8uC1 images are supported" );
	//CV_CALL( type = cvGetElemType( src ));
    	//if( type != CV_8UC1 )
        	//CV_ERROR( CV_StsUnsupportedFormat, "Only 8uC1 images are supported" );
	
	//if( !CV_ARE_SIZES_EQ( src, dst ))	// Modified by Shervin Emami, 17Nov2010.
	//if (src->width != dst->width || src->height != dst->height)
        	//CV_ERROR( CV_StsUnmatchedSizes, "src and dst images must be equal sizes" );

	//if (((xdivs < 2) || (xdivs > 16)) || ((ydivs < 2) || (ydivs > 16)))
		//CV_ERROR( CV_StsBadFlag, "xdivs and ydivs must in range (MIN=2 MAX = 16)" );

	//if ((bins < 2) || (bins > 256))
		//CV_ERROR( CV_StsBadFlag, "bins must in range (MIN=2 MAX = 256)" );

	// copy src to dst for in-place CLAHE.
	//cvCopy(src, dst);


	// If the dimensions of the image are not a multiple of the xdivs and ydivs, then enlarge the image to be a correct size and then shrink the image.
	// Also make sure the image is aligned to 8 pixels width, so that OpenCV won't add extra padding to the image.
	// Added by Shervin Emami, 17Nov2010.

	int enlarged = 0;
	int origW = dst->width;
	int origH = dst->height;
	IplImage *tmpDst = 0;

	if ((dst->width & (8-1)) || (dst->height & (8-1)) || (dst->width % xdivs) || (dst->height % ydivs)) {
		int newW = ((dst->width + 8-1) & -8);	// Align to 8 pixels, so that widthStep hopefully equals width.
		newW = ((newW + xdivs-1) & -xdivs);		// Also align for CLAHE.
		int newH = ((dst->height + ydivs-1) & -ydivs);
		//std::cout << "w=" << dst->width << ", h=" << dst->height << ". new w = " << newW << ", h = " << newH << std::endl;
		IplImage *enlargedDst = cvCreateImage(cvSize(newW, newH), dst->depth, dst->nChannels);
		cvResize(dst, enlargedDst, CV_INTER_CUBIC);
		//cvReleaseImage(&dst);
		tmpDst = dst;
		dst = enlargedDst;	// Use the enlarged image instead of the original dst image.
		enlarged = 1;	// signal that we need to shrink later!
	}
	// Check if OpenCV adds padding bytes on each row. Added by Shervin Emami, 17Nov2010.
	//if (dst->width != dst->widthStep)
		//CV_ERROR( CV_StsBadFlag, "dst->widthStep should be the same as dst->width. The IplImage has padding, so use a larger image." );


	// check number of xdivs and ydivs is a multiple of image dimensions
	//if (dst->width % xdivs)
		//CV_ERROR( CV_StsBadFlag, "xdiv must be an integer multiple of image width " );
	//if (dst->height % ydivs)
		//CV_ERROR( CV_StsBadFlag, "ydiv must be an integer multiple of image height " );
		
	// get the min and max values of the image
	
	if (range == CV_CLAHE_RANGE_INPUT) {
		cvMinMaxLoc(dst, &min_val, &max_val);
		min = (unsigned char) min_val;
		max = (unsigned char) max_val;
	} else {
		min = 0;
		max = 255;
	}
	// call CLHAHE for in-place CLAHE

	//int rcode = 
	CLAHE_ext((kz_pixel_t*) (dst->imageData), (unsigned int) dst->width, (unsigned int) 
	dst->height, (kz_pixel_t) min, (kz_pixel_t) max, (unsigned int) xdivs, (unsigned int) ydivs,
              (unsigned int) bins, (float) limit);
	//printf("RCODE %i\n", rcode);	


	// If the dst image was enlarged to fit the alignment, then shrink it back now.
	// Added by Shervin Emami, 17Nov2010.
	if (enlarged) {
		//std::cout << "w=" << dst->width << ", h=" << dst->height << ". orig w=" << origW << ", h=" << origH << std::endl;
		cvResize(dst, tmpDst, CV_INTER_CUBIC);	// Shrink the enlarged image back into the original dst image.
		cvReleaseImage(&dst);	// Free the enlarged image.
	}

}
			
// *****************************************************************************
/*
 * ANSI C code from the article
 * "Contrast Limited Adaptive Histogram Equalization"
 * by Karel Zuiderveld, karel@cv.ruu.nl
 * in "Graphics Gems IV", Academic Press, 1994
 *
 *
 *  These functions implement Contrast Limited Adaptive Histogram Equalization.
 *  The main routine (CLAHE) expects an input image that is stored contiguously in
 *  memory;  the CLAHE output image overwrites the original input image and has the
 *  same minimum and maximum values (which must be provided by the user).
 *  This implementation assumes that the X- and Y image resolutions are an integer
 *  multiple of the X- and Y sizes of the contextual regions. A check on various other
 *  error conditions is performed.
 *
 *  #define the symbol BYTE_IMAGE to make this implementation suitable for
 *  8-bit images. The maximum number of contextual regions can be redefined
 *  by changing uiMAX_REG_X and/or uiMAX_REG_Y; the use of more than 256
 *  contextual regions is not recommended.
 *
 *  The code is ANSI-C and is also C++ compliant.
 *
 *  Author: Karel Zuiderveld, Computer Vision Research Group,
 *           Utrecht, The Netherlands (karel@cv.ruu.nl)
 */

/*

EULA: The Graphics Gems code is copyright-protected. In other words, you cannot 
claim the text of the code as your own and resell it. Using the code is permitted 
in any program, product, or library, non-commercial or commercial. Giving credit 
is not required, though is a nice gesture. The code comes as-is, and if there are 
any flaws or problems with any Gems code, nobody involved with Gems - authors, 
editors, publishers, or webmasters - are to be held responsible. Basically, 
don't be a jerk, and remember that anything free comes with no guarantee. 

- http://tog.acm.org/resources/GraphicsGems/ (August 2009)

*/

/*********************** Local prototypes ************************/
static void ClipHistogram (unsigned long*, unsigned int, unsigned long);
static void MakeHistogram (kz_pixel_t*, unsigned int, unsigned int, unsigned int,
                unsigned long*, unsigned int, kz_pixel_t*);
static void MapHistogram (unsigned long*, kz_pixel_t, kz_pixel_t,
               unsigned int, unsigned long);
static void MakeLut (kz_pixel_t*, kz_pixel_t, kz_pixel_t, unsigned int);
static void Interpolate (kz_pixel_t*, int, unsigned long*, unsigned long*,
        unsigned long*, unsigned long*, unsigned int, unsigned int, kz_pixel_t*);
        
// *****************************************************************************

/**************  Start of actual code **************/
#include <stdlib.h>                      /* To get prototypes of malloc() and free() */

const static unsigned int uiMAX_REG_X = 16;      /* max. # contextual regions in x-direction */
const static unsigned int uiMAX_REG_Y = 16;      /* max. # contextual regions in y-direction */

/************************** main function CLAHE ******************/
static int CLAHE_ext (kz_pixel_t* pImage, unsigned int uiXRes, unsigned int uiYRes,
         kz_pixel_t Min, kz_pixel_t Max, unsigned int uiNrX, unsigned int uiNrY,
              unsigned int uiNrBins, float fCliplimit)
/*   pImage - Pointer to the input/output image
 *   uiXRes - Image resolution in the X direction
 *   uiYRes - Image resolution in the Y direction
 *   Min - Minimum greyvalue of input image (also becomes minimum of output image)
 *   Max - Maximum greyvalue of input image (also becomes maximum of output image)
 *   uiNrX - Number of contextial regions in the X direction (min 2, max uiMAX_REG_X)
 *   uiNrY - Number of contextial regions in the Y direction (min 2, max uiMAX_REG_Y)
 *   uiNrBins - Number of greybins for histogram ("dynamic range")
 *   float fCliplimit - Normalized cliplimit (higher values give more contrast)
 * The number of "effective" greylevels in the output image is set by uiNrBins; selecting
 * a small value (eg. 128) speeds up processing and still produce an output image of
 * good quality. The output image will have the same minimum and maximum value as the input
 * image. A clip limit smaller than 1 results in standard (non-contrast limited) AHE.
 */
{
    unsigned int uiX, uiY;                /* counters */
    unsigned int uiXSize, uiYSize, uiSubX, uiSubY; /* size of context. reg. and subimages */
    unsigned int uiXL, uiXR, uiYU, uiYB;  /* auxiliary variables interpolation routine */
    unsigned long ulClipLimit, ulNrPixels;/* clip limit and region pixel count */
    kz_pixel_t* pImPointer;                /* pointer to image */
    kz_pixel_t aLUT[uiNR_OF_GREY];          /* lookup table used for scaling of input image */
    unsigned long* pulHist, *pulMapArray; /* pointer to histogram and mappings*/
    unsigned long* pulLU, *pulLB, *pulRU, *pulRB; /* auxiliary pointers interpolation */

    if (uiNrX > uiMAX_REG_X) return -1;    /* # of regions x-direction too large */
    if (uiNrY > uiMAX_REG_Y) return -2;    /* # of regions y-direction too large */
    if (uiXRes % uiNrX) return -3;        /* x-resolution no multiple of uiNrX */
    if (uiYRes % uiNrY) return -4;        /* y-resolution no multiple of uiNrY #TPB FIX */
    #ifndef BYTE_IMAGE					  /* #TPB FIX */
		if (Max >= uiNR_OF_GREY) return -5;    /* maximum too large */
	#endif
    if (Min >= Max) return -6;            /* minimum equal or larger than maximum */
    if (uiNrX < 2 || uiNrY < 2) return -7;/* at least 4 contextual regions required */
    if (fCliplimit == 1.0) return 0;      /* is OK, immediately returns original image. */
    if (uiNrBins == 0) uiNrBins = 128;    /* default value when not specified */

    pulMapArray=(unsigned long *)malloc(sizeof(unsigned long)*uiNrX*uiNrY*uiNrBins);
    if (pulMapArray == 0) return -8;      /* Not enough memory! (try reducing uiNrBins) */

    uiXSize = uiXRes/uiNrX; uiYSize = uiYRes/uiNrY;  /* Actual size of contextual regions */
    ulNrPixels = (unsigned long)uiXSize * (unsigned long)uiYSize;

    if(fCliplimit > 0.0) {                /* Calculate actual cliplimit  */
       ulClipLimit = (unsigned long) (fCliplimit * (uiXSize * uiYSize) / uiNrBins);
       ulClipLimit = (ulClipLimit < 1UL) ? 1UL : ulClipLimit;
    }
    else ulClipLimit = 1UL<<14;           /* Large value, do not clip (AHE) */
    MakeLut(aLUT, Min, Max, uiNrBins);    /* Make lookup table for mapping of greyvalues */



    /* Calculate greylevel mappings for each contextual region */
    for (uiY = 0, pImPointer = pImage; uiY < uiNrY; uiY++) {

        for (uiX = 0; uiX < uiNrX; uiX++, pImPointer += uiXSize) {

// ****** SEGMENTATION FAULT **********
            pulHist = &pulMapArray[uiNrBins * (uiY * uiNrX + uiX)];

            MakeHistogram(pImPointer,uiXRes,uiXSize,uiYSize,pulHist,uiNrBins,aLUT);


            ClipHistogram(pulHist, uiNrBins, ulClipLimit);

            MapHistogram(pulHist, Min, Max, uiNrBins, ulNrPixels);

        }
        pImPointer += (uiYSize - 1) * uiXRes;             /* skip lines, set pointer */
    }

    /* Interpolate greylevel mappings to get CLAHE image */
    for (pImPointer = pImage, uiY = 0; uiY <= uiNrY; uiY++) {
        if (uiY == 0) {                                   /* special case: top row */
            uiSubY = uiYSize >> 1;  uiYU = 0; uiYB = 0;
        }
        else {
            if (uiY == uiNrY) {                           /* special case: bottom row */
                uiSubY = uiYSize >> 1;  uiYU = uiNrY-1;  uiYB = uiYU;
            }
            else {                                        /* default values */
                uiSubY = uiYSize; uiYU = uiY - 1; uiYB = uiYU + 1;
            }
        }
        for (uiX = 0; uiX <= uiNrX; uiX++) {
            if (uiX == 0) {                               /* special case: left column */
                uiSubX = uiXSize >> 1; uiXL = 0; uiXR = 0;
            }
            else {
                if (uiX == uiNrX) {                       /* special case: right column */
                    uiSubX = uiXSize >> 1;  uiXL = uiNrX - 1; uiXR = uiXL;
                }
                else {                                    /* default values */
                    uiSubX = uiXSize; uiXL = uiX - 1; uiXR = uiXL + 1;
                }
            }

            pulLU = &pulMapArray[uiNrBins * (uiYU * uiNrX + uiXL)];
            pulRU = &pulMapArray[uiNrBins * (uiYU * uiNrX + uiXR)];
            pulLB = &pulMapArray[uiNrBins * (uiYB * uiNrX + uiXL)];
            pulRB = &pulMapArray[uiNrBins * (uiYB * uiNrX + uiXR)];
            Interpolate(pImPointer,uiXRes,pulLU,pulRU,pulLB,pulRB,uiSubX,uiSubY,aLUT);
            pImPointer += uiSubX;                         /* set pointer on next matrix */
        }
        pImPointer += (uiSubY - 1) * uiXRes;
    }


    free(pulMapArray);                                    /* free space for histograms */


    return 0;                                             /* return status OK */
}
void ClipHistogram (unsigned long* pulHistogram, unsigned int
                     uiNrGreylevels, unsigned long ulClipLimit)
/* This function performs clipping of the histogram and redistribution of bins.
 * The histogram is clipped and the number of excess pixels is counted. Afterwards
 * the excess pixels are equally redistributed across the whole histogram (providing
 * the bin count is smaller than the cliplimit).
 */
{
    unsigned long* pulBinPointer, *pulEndPointer, *pulHisto;
    unsigned long ulNrExcess, ulUpper, ulBinIncr, ulStepSize, i;
	unsigned long ulOldNrExcess;  // #IAC Modification
	
    long lBinExcess;

    ulNrExcess = 0;  pulBinPointer = pulHistogram;
    for (i = 0; i < uiNrGreylevels; i++) { /* calculate total number of excess pixels */
        lBinExcess = (long) pulBinPointer[i] - (long) ulClipLimit;
        if (lBinExcess > 0) ulNrExcess += lBinExcess;     /* excess in current bin */
    };

    /* Second part: clip histogram and redistribute excess pixels in each bin */
    ulBinIncr = ulNrExcess / uiNrGreylevels;              /* average binincrement */
    ulUpper =  ulClipLimit - ulBinIncr;  /* Bins larger than ulUpper set to cliplimit */

    for (i = 0; i < uiNrGreylevels; i++) {
      if (pulHistogram[i] > ulClipLimit) pulHistogram[i] = ulClipLimit; /* clip bin */
      else {
          if (pulHistogram[i] > ulUpper) {              /* high bin count */
              ulNrExcess -= pulHistogram[i] - ulUpper; pulHistogram[i]=ulClipLimit;
          }
          else {                                        /* low bin count */
              ulNrExcess -= ulBinIncr; pulHistogram[i] += ulBinIncr;
          }
       }
    }
	
    // while (ulNrExcess) {   /* Redistribute remaining excess  */
    //    pulEndPointer = &pulHistogram[uiNrGreylevels]; pulHisto = pulHistogram;
	//
    //    while (ulNrExcess && pulHisto < pulEndPointer) {
    //        ulStepSize = uiNrGreylevels / ulNrExcess;
    //        if (ulStepSize < 1) ulStepSize = 1;           /* stepsize at least 1 */
    //        for (pulBinPointer=pulHisto; pulBinPointer < pulEndPointer && ulNrExcess;
    //             pulBinPointer += ulStepSize) {
    //            if (*pulBinPointer < ulClipLimit) {
    //                (*pulBinPointer)++;  ulNrExcess--;    /* reduce excess */
    //            }
    //        }
    //        pulHisto++;           /* restart redistributing on other bin location */
    //    }
    //} 
	
/* ####  
       
       IAC Modification:  
       In the original version of the loop below (commented out above) it was possible for an infinite loop to get  
       created.  If there was more pixels to be redistributed than available space then the  
       while loop would never end.  This problem has been fixed by stopping the loop when all  
       pixels have been redistributed OR when no pixels where redistributed in the previous iteration.  
       This change allows very low clipping levels to be used.  
         
*/   
        
     do {   /* Redistribute remaining excess  */   
         pulEndPointer = &pulHistogram[uiNrGreylevels]; pulHisto = pulHistogram;   
            
         ulOldNrExcess = ulNrExcess;     /* Store number of excess pixels for test later. */   
            
         while (ulNrExcess && pulHisto < pulEndPointer)    
         {   
             ulStepSize = uiNrGreylevels / ulNrExcess;   
             if (ulStepSize < 1)    
                 ulStepSize = 1;       /* stepsize at least 1 */   
             for (pulBinPointer=pulHisto; pulBinPointer < pulEndPointer && ulNrExcess;   
             pulBinPointer += ulStepSize)    
             {   
                 if (*pulBinPointer < ulClipLimit)    
                 {   
                     (*pulBinPointer)++;  ulNrExcess--;    /* reduce excess */   
                 }   
            }   
             pulHisto++;       /* restart redistributing on other bin location */   
         }   
     } while ((ulNrExcess) && (ulNrExcess < ulOldNrExcess));   
     /* Finish loop when we have no more pixels or we can't redistribute any more pixels */   
	
	
}

void MakeHistogram (kz_pixel_t* pImage, unsigned int uiXRes,
                unsigned int uiSizeX, unsigned int uiSizeY,
                unsigned long* pulHistogram,
                unsigned int uiNrGreylevels, kz_pixel_t* pLookupTable)
/* This function classifies the greylevels present in the array image into
 * a greylevel histogram. The pLookupTable specifies the relationship
 * between the greyvalue of the pixel (typically between 0 and 4095) and
 * the corresponding bin in the histogram (usually containing only 128 bins).
 */
{
    kz_pixel_t* pImagePointer;
    unsigned int i;

    for (i = 0; i < uiNrGreylevels; i++) pulHistogram[i] = 0L; /* clear histogram */

    for (i = 0; i < uiSizeY; i++) {
        pImagePointer = &pImage[uiSizeX];
        while (pImage < pImagePointer) pulHistogram[pLookupTable[*pImage++]]++;
        pImagePointer += uiXRes;
        pImage = &pImagePointer[-uiSizeX];
    }
}

void MapHistogram (unsigned long* pulHistogram, kz_pixel_t Min, kz_pixel_t Max,
               unsigned int uiNrGreylevels, unsigned long ulNrOfPixels)
/* This function calculates the equalized lookup table (mapping) by
 * cumulating the input histogram. Note: lookup table is rescaled in range [Min..Max].
 */
{
    unsigned int i;  unsigned long ulSum = 0;
    const float fScale = ((float)(Max - Min)) / ulNrOfPixels;
    const unsigned long ulMin = (unsigned long) Min;

    for (i = 0; i < uiNrGreylevels; i++) {
        ulSum += pulHistogram[i]; pulHistogram[i]=(unsigned long)(ulMin+ulSum*fScale);
        if (pulHistogram[i] > Max) pulHistogram[i] = Max;
    }
}

void MakeLut (kz_pixel_t * pLUT, kz_pixel_t Min, kz_pixel_t Max, unsigned int uiNrBins)
/* To speed up histogram clipping, the input image [Min,Max] is scaled down to
 * [0,uiNrBins-1]. This function calculates the LUT.
 */
{
    int i;
    const kz_pixel_t BinSize = (kz_pixel_t) (1 + (Max - Min) / uiNrBins);

    for (i = Min; i <= Max; i++)  pLUT[i] = (i - Min) / BinSize;
}

void Interpolate (kz_pixel_t * pImage, int uiXRes, unsigned long * pulMapLU,
     unsigned long * pulMapRU, unsigned long * pulMapLB,  unsigned long * pulMapRB,
     unsigned int uiXSize, unsigned int uiYSize, kz_pixel_t * pLUT)
/* pImage      - pointer to input/output image
 * uiXRes      - resolution of image in x-direction
 * pulMap*     - mappings of greylevels from histograms
 * uiXSize     - uiXSize of image submatrix
 * uiYSize     - uiYSize of image submatrix
 * pLUT        - lookup table containing mapping greyvalues to bins
 * This function calculates the new greylevel assignments of pixels within a submatrix
 * of the image with size uiXSize and uiYSize. This is done by a bilinear interpolation
 * between four different mappings in order to eliminate boundary artifacts.
 * It uses a division; since division is often an expensive operation, I added code to
 * perform a logical shift instead when feasible.
 */
{
    const unsigned int uiIncr = uiXRes-uiXSize; /* Pointer increment after processing row */
    kz_pixel_t GreyValue; unsigned int uiNum = uiXSize*uiYSize; /* Normalization factor */

    unsigned int uiXCoef, uiYCoef, uiXInvCoef, uiYInvCoef, uiShift = 0;

    if (uiNum & (uiNum - 1))   /* If uiNum is not a power of two, use division */
    for (uiYCoef = 0, uiYInvCoef = uiYSize; uiYCoef < uiYSize;
         uiYCoef++, uiYInvCoef--,pImage+=uiIncr) {
        for (uiXCoef = 0, uiXInvCoef = uiXSize; uiXCoef < uiXSize;
             uiXCoef++, uiXInvCoef--) {
            GreyValue = pLUT[*pImage];             /* get histogram bin value */
            *pImage++ = (kz_pixel_t ) ((uiYInvCoef * (uiXInvCoef*pulMapLU[GreyValue]
                                      + uiXCoef * pulMapRU[GreyValue])
                                + uiYCoef * (uiXInvCoef * pulMapLB[GreyValue]
                                      + uiXCoef * pulMapRB[GreyValue])) / uiNum);
        }
    }
    else {                         /* avoid the division and use a right shift instead */
        while (uiNum >>= 1) uiShift++;             /* Calculate 2log of uiNum */
        for (uiYCoef = 0, uiYInvCoef = uiYSize; uiYCoef < uiYSize;
             uiYCoef++, uiYInvCoef--,pImage+=uiIncr) {
             for (uiXCoef = 0, uiXInvCoef = uiXSize; uiXCoef < uiXSize;
               uiXCoef++, uiXInvCoef--) {
               GreyValue = pLUT[*pImage];         /* get histogram bin value */
               *pImage++ = (kz_pixel_t)((uiYInvCoef* (uiXInvCoef * pulMapLU[GreyValue]
                                      + uiXCoef * pulMapRU[GreyValue])
                                + uiYCoef * (uiXInvCoef * pulMapLB[GreyValue]
                                      + uiXCoef * pulMapRB[GreyValue])) >> uiShift);
            }
        }
    }
}
// *****************************************************************************
