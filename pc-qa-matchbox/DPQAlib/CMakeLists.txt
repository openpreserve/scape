
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

set(OpenCV_DIR "/usr/local/share/OpenCV/")

find_path(OPENCV_DIR OpenCVConfig.cmake HINTS $ENV{PATH} $ENV{OPENCV_DIR} $ENV{OPENCVDIR} $ENV{OpenCVDIR} $ENV{OPENCV_DIR} $ENV{OpenCV_DIR})

FIND_PACKAGE(OpenCV REQUIRED)

if(OpenCV_FOUND)
message(STATUS ${OpenCV_LIBRARIES})
ENDIF(OpenCV_FOUND)

INCLUDE_DIRECTORIES(${OpenCV_INCLUDE_DIR})
INCLUDE_DIRECTORIES("../libs/tclap/include")

FILE (GLOB DPQAlib_FILES  *.cpp)

set(DPQAlibSrcs ${DPQAlib_FILES})

add_library(DPQAlib SHARED ${DPQAlibSrcs})
target_link_libraries(DPQAlib ${OpenCV_LIBRARIES})

install(TARGETS DPQAlib DESTINATION lib)
