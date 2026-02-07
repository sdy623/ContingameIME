cmake_minimum_required(VERSION 3.18)
project(IngameIME_JNI)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(JNI REQUIRED)

add_library(jni SHARED
  city_windmill_ingameime_client_jni_ExternalBaseIME.cpp
)

target_include_directories(jni PRIVATE
  ${JNI_INCLUDE_DIRS}
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME/IngameIME
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME/libtf
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME/libtf/libtf
)

target_compile_definitions(jni PRIVATE UNICODE _UNICODE)

# Keep output next to the resources so it can be picked up by the mod.
set(_jni_out_dir ${CMAKE_CURRENT_LIST_DIR}/../resources/assets/ingameime/natives)
set_target_properties(jni PROPERTIES
  OUTPUT_NAME $<IF:$<EQUAL:${CMAKE_SIZEOF_VOID_P},8>,jni,jni-x86>
  RUNTIME_OUTPUT_DIRECTORY ${_jni_out_dir}
  RUNTIME_OUTPUT_DIRECTORY_DEBUG ${_jni_out_dir}
  RUNTIME_OUTPUT_DIRECTORY_RELEASE ${_jni_out_dir}
  RUNTIME_OUTPUT_DIRECTORY_RELWITHDEBINFO ${_jni_out_dir}
  RUNTIME_OUTPUT_DIRECTORY_MINSIZEREL ${_jni_out_dir}
)
