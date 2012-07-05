project(TestProject)

cmake_minimum_required(VERSION 2.8)

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fpermissive")

add_subdirectory(DPQAlib)
add_subdirectory(DPQA_Compare)
add_subdirectory(DPQA_ExtractFeatures)
add_subdirectory(DPQA_Train)
