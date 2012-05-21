
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

# include dependencies
find_path(OPENCV_DIR OpenCVConfig.cmake HINTS $ENV{PATH} $ENV{OPENCV_DIR} $ENV{OPENCVDIR} $ENV{OpenCVDIR} $ENV{OPENCV_DIR} $ENV{OpenCV_DIR})
FIND_PACKAGE(OpenCV REQUIRED)

include_directories(${CMAKE_SOURCE_DIR}/DPQAlib)
INCLUDE_DIRECTORIES(${OpenCV_INCLUDE_DIR})
INCLUDE_DIRECTORIES("../libs/tclap/include")

# include sources
FILE (GLOB DPQA_Extract_FILES  *.cpp)
set(DPQA_Extract_SRC ${DPQA_Extract_FILES})

add_executable(extractfeatures ${DPQA_Extract_SRC})

target_link_libraries(extractfeatures DPQAlib)
target_link_libraries(extractfeatures ${OpenCV_LIBS})

install(TARGETS extractfeatures DESTINATION bin)
