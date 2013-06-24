
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

# include dependencies
include_directories(${CMAKE_SOURCE_DIR}/DPQAlib)

# include sources
FILE (GLOB DPQA_Compare_FILES  *.cpp)
set(DPQA_Compare_SRC ${DPQA_Compare_FILES})

add_executable(compare ${DPQA_Compare_SRC})

target_link_libraries(compare ${OpenCV_LIBS} DPQAlib)
target_link_libraries(compare ${OpenCV_LIBS})
target_link_libraries(compare ${Boost_LIBRARIES})

install(TARGETS compare DESTINATION bin)
