
cmake_minimum_required(VERSION 2.8)

# === Project specific parameters

project(MatchboxTools)

SET(MAJOR_VERSION 1)
SET(MINOR_VERSION 0)
SET(PATCH_VERSION 0)

# === global build parameters

SET(BUILD_SHARED_LIBS ON)
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fpermissive ")

option(withLibs "withLibs" OFF)

# === find and add required ressources

find_path(OPENCV_DIR OpenCVConfig.cmake HINTS $ENV{PATH} $ENV{OPENCV_DIR} $ENV{OPENCVDIR} $ENV{OpenCVDIR} $ENV{OPENCV_DIR} $ENV{OpenCV_DIR})
FIND_PACKAGE(OpenCV REQUIRED)

if(OpenCV_FOUND)
message(STATUS "COMPARE: Opencv found.")
ENDIF(OpenCV_FOUND)

set(Boost_USE_STATIC_LIBS OFF)
set(Boost_USE_MULTITHREADED ON) 
set(Boost_USE_STATIC_RUNTIME OFF)
FIND_PACKAGE(Boost COMPONENTS serialization)

INCLUDE_DIRECTORIES("./libs/tclap/include")
INCLUDE_DIRECTORIES(${OpenCV_INCLUDE_DIR})
INCLUDE_DIRECTORIES(${Boost_INCLUDE_DIRS})

# === CPack packaging parameters

IF(EXISTS "${CMAKE_ROOT}/Modules/CPack.cmake")
INCLUDE(InstallRequiredSystemLibraries)

SET(CPACK_PACKAGE_VERSION_MAJOR    "${MAJOR_VERSION}")
SET(CPACK_PACKAGE_VERSION_MINOR    "${MINOR_VERSION}")
SET(CPACK_PACKAGE_VERSION_PATCH    "${PATCH_VERSION}")

IF (withLibs)

	set(CPACK_PACKAGE_VERSION          "${MAJOR_VERSION}.${MINOR_VERSION}.${CPACK_PACKAGE_VERSION_PATCH}")
	SET(CPACK_PACKAGE_NAME             "${CMAKE_PROJECT_NAME}_${MAJOR_VERSION}.${MINOR_VERSION}.${CPACK_PACKAGE_VERSION_PATCH}_incl_libs")
	SET(CPACK_PACKAGE_FILE_NAME        "${CMAKE_PROJECT_NAME}_${MAJOR_VERSION}.${MINOR_VERSION}.${CPACK_PACKAGE_VERSION_PATCH}_incl_libs")
	SET(CPACK_SOURCE_PACKAGE_FILE_NAME "${CMAKE_PROJECT_NAME}_${MAJOR_VERSION}.${MINOR_VERSION}.${CPACK_PACKAGE_VERSION_PATCH}_incl_libs")
	SET(CPACK_DEBIAN_PACKAGE_DEPENDS   ">=python2.6, python-numpy, python-opencv, libjpeg62")

ELSE (withLibs)

	set(CPACK_PACKAGE_VERSION          "${MAJOR_VERSION}.${MINOR_VERSION}.${CPACK_PACKAGE_VERSION_PATCH}")
	SET(CPACK_PACKAGE_NAME             "${CMAKE_PROJECT_NAME}_${MAJOR_VERSION}.${MINOR_VERSION}.${CPACK_PACKAGE_VERSION_PATCH}")
	SET(CPACK_PACKAGE_FILE_NAME        "${CMAKE_PROJECT_NAME}_${MAJOR_VERSION}.${MINOR_VERSION}.${CPACK_PACKAGE_VERSION_PATCH}")
	SET(CPACK_SOURCE_PACKAGE_FILE_NAME "${CMAKE_PROJECT_NAME}_${MAJOR_VERSION}.${MINOR_VERSION}.${CPACK_PACKAGE_VERSION_PATCH}")
	SET(CPACK_DEBIAN_PACKAGE_DEPENDS   ">=libcv2.4.4, python2.7, python-numpy, python-opencv, libjpeg62")

ENDIF (withLibs)


SET(CPACK_PACKAGE_DESCRIPTION         "Matchbox Tools")
SET(CPACK_PACKAGE_DESCRIPTION_SUMMARY "The SCAPE Matchbox Toolbox is a toolset for detecting duplicate entires in document image collections.")
SET(CPACK_PACKAGE_VENDOR              "Austrian Institute of Technology AIT")
SET(CPACK_PACKAGE_CONTACT             "alexander.schindler@ait.ac.at")


SET(CPACK_DEBIAN_PACKAGE_PRIORITY     "optional")
SET(CPACK_DEBIAN_PACKAGE_SECTION      "tools")
SET(CPACK_DEBIAN_ARCHITECTURE         "i386")

SET(CPACK_COMPONENTS_ALL Libraries ApplicationData)
SET(CPACK_SET_DESTDIR "on")
SET(CPACK_PACKAGING_INSTALL_PREFIX "/tmp")

SET(CPACK_GENERATOR "DEB")

ENDIF(EXISTS "${CMAKE_ROOT}/Modules/CPack.cmake")

# === add additional files to package

INSTALL(FILES ${CMAKE_CURRENT_SOURCE_DIR}/Python/FindDuplicates.py DESTINATION /usr/bin/)
INSTALL(FILES ${CMAKE_CURRENT_SOURCE_DIR}/Python/MatchboxLib.py DESTINATION /usr/bin/)

IF (withLibs)
	FILE(GLOB my_opencv_libs /usr/local/lib/libopencv_*.so.2.4.*)
	IF(my_opencv_libs)
		foreach(_ml ${my_opencv_libs})
			message(STATUS ${_ml})
			INSTALL(FILES ${_ml} DESTINATION /usr/lib/)
		endforeach(_ml)
	ENDIF(my_opencv_libs)

	FILE(GLOB my_opencv_libs /usr/local/lib/libopencv_*.so.2.4)
	IF (my_opencv_libs)
		foreach(_ml ${my_opencv_libs})
			message(STATUS ${_ml})
			INSTALL(FILES ${_ml} DESTINATION /usr/lib/)
		endforeach(_ml)
	ENDIF(my_opencv_libs)
ENDIF (withLibs)

# === add sub-projects for compilation
add_subdirectory(DPQAlib)
add_subdirectory(DPQA_Compare)
add_subdirectory(DPQA_ExtractFeatures)
add_subdirectory(DPQA_Train)

# === declare make install parameters

INCLUDE(CPack)
