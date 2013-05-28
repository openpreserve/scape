
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

include_directories(${CMAKE_SOURCE_DIR}/DPQAlib)

# include sources
FILE (GLOB DPQA_Extract_FILES  *.cpp)
set(DPQA_Extract_SRC ${DPQA_Extract_FILES})

add_executable(extractfeatures ${DPQA_Extract_SRC})

target_link_libraries(extractfeatures DPQAlib)
target_link_libraries(extractfeatures ${Boost_LIBRARIES})
target_link_libraries(extractfeatures ${OpenCV_LIBS})

install(TARGETS extractfeatures DESTINATION bin)
