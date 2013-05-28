
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}")

# include dependencies
include_directories(${CMAKE_SOURCE_DIR}/DPQAlib)

# include sources
FILE (GLOB DPQA_Train_FILES  *.cpp)
set(DPQA_Train_SRC ${DPQA_Train_FILES})

add_executable(train ${DPQA_Train_SRC})

target_link_libraries(train DPQAlib)
target_link_libraries(train ${OpenCV_LIBS})
target_link_libraries(train ${Boost_LIBRARIES})

install(TARGETS train DESTINATION bin)