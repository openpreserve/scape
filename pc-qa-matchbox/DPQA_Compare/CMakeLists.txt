
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

# include dependencies
find_path(OPENCV_DIR OpenCVConfig.cmake HINTS $ENV{PATH} $ENV{OPENCV_DIR} $ENV{OPENCVDIR} $ENV{OpenCVDIR} $ENV{OPENCV_DIR} $ENV{OpenCV_DIR})
FIND_PACKAGE(OpenCV REQUIRED)

if(OpenCV_FOUND)
message(STATUS "COMPARE: Opencv found.")
ENDIF(OpenCV_FOUND)

include_directories(${CMAKE_SOURCE_DIR}/DPQAlib)
INCLUDE_DIRECTORIES(${OpenCV_INCLUDE_DIR})
INCLUDE_DIRECTORIES("../libs/tclap/include")

# include sources
FILE (GLOB DPQA_Compare_FILES  *.cpp)
set(DPQA_Compare_SRC ${DPQA_Compare_FILES})

add_executable(compare ${DPQA_Compare_SRC})

target_link_libraries(compare ${OpenCV_LIBS} DPQAlib)

install(TARGETS compare DESTINATION bin)
