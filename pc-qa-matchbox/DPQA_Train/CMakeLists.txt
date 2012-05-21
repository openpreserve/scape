
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

# include dependencies
find_path(OPENCV_DIR OpenCVConfig.cmake HINTS $ENV{PATH} $ENV{OPENCV_DIR} $ENV{OPENCVDIR} $ENV{OpenCVDIR} $ENV{OPENCV_DIR} $ENV{OpenCV_DIR})
FIND_PACKAGE(OpenCV REQUIRED)

include_directories(${CMAKE_SOURCE_DIR}/DPQAlib)
INCLUDE_DIRECTORIES(${OpenCV_INCLUDE_DIR})
INCLUDE_DIRECTORIES("../libs/tclap/include")

# include sources
FILE (GLOB DPQA_Train_FILES  *.cpp)
set(DPQA_Train_SRC ${DPQA_Train_FILES})

add_executable(train ${DPQA_Train_SRC})

target_link_libraries(train DPQAlib)
target_link_libraries(train ${OpenCV_LIBS})

install(TARGETS train DESTINATION bin)
