
#include <iostream>
#include <fstream>

#include <boost/archive/binary_oarchive.hpp>
#include <boost/archive/binary_iarchive.hpp>
#include <boost/serialization/split_free.hpp>
#include <boost/serialization/vector.hpp>

#include "opencv/cv.h"

using namespace std;
using namespace cv;


BOOST_SERIALIZATION_SPLIT_FREE(Mat)
namespace boost {
	namespace serialization {

		/*** Mat ***/
		template<class Archive>
		void save(Archive & ar, const Mat& m, const unsigned int version)
		{
			size_t elemSize = m.elemSize(), elemType = m.type();

			ar & m.cols;
			ar & m.rows;
			ar & elemSize;
			ar & elemType; // element type.
			size_t dataSize = m.cols * m.rows * m.elemSize();

			for (size_t dc = 0; dc < dataSize; ++dc) {
				ar & m.data[dc];
			}
		}

		template<class Archive>
		void load(Archive & ar, Mat& m, const unsigned int version)
		{
			int cols, rows;
			size_t elemSize, elemType;

			ar & cols;
			ar & rows;
			ar & elemSize;
			ar & elemType;

			m.create(rows, cols, elemType);
			size_t dataSize = m.cols * m.rows * elemSize;

			for (size_t dc = 0; dc < dataSize; ++dc) {
				ar & m.data[dc];
			}
		}

		template<class Archive>
		void serialize(Archive & ar, KeyPoint & k, const unsigned int version)
		{
			ar & k.pt;
			ar & k.size;
			ar & k.angle;
			ar & k.response;
			ar & k.octave;
			ar & k.class_id;
		}

		template<class Archive>
		void serialize(Archive & ar, Point2f & p, const unsigned int version)
		{
			ar & p.x;
			ar & p.y;
		}

	}
}
