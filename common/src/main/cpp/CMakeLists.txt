cmake_minimum_required(VERSION 3.18)
project(IngameIME_JNI)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(JNI REQUIRED)

add_library(jni SHARED
  city_sdy623_kitsuneime_client_jni_ExternalBaseIME.cpp
  ../../../../3rd/IngameIME_Win32/src/IngameIMEImpl.cpp
  ../../../../3rd/IngameIME_Win32/src/TfInputContextImpl.cpp
  ../../../../3rd/IngameIME_Win32/src/TfCompositionHandler.cpp
  ../../../../3rd/IngameIME_Win32/src/TfContextOwner.cpp
  ../../../../3rd/IngameIME_Win32/src/TfInputModeHandler.cpp
  ../../../../3rd/IngameIME_Win32/src/TfFunction.cpp
  ../../../../3rd/IngameIME_Win32/src/ComBSTR.cpp
  ../../../../3rd/IngameIME_Win32/src/ComException.cpp
  ../../../../3rd/IngameIME_Win32/src/ComObjectBase.cpp
  ../../../../3rd/IngameIME_Win32/src/InputContextImpl.cpp
  ../../../../3rd/IngameIME_Win32/src/ImmInputContextImpl.cpp
)

target_include_directories(jni PRIVATE
  ${JNI_INCLUDE_DIRS}
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME/IngameIME
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME/libtf
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME/libtf/libtf
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME-Common/include
  ${CMAKE_CURRENT_LIST_DIR}/../../../../3rd/IngameIME_Win32/include
)

target_compile_definitions(jni PRIVATE UNICODE _UNICODE)

# Link against COM/TSF related libraries
if (WIN32)
  # Link common COM/TSF-related system libraries.
  # Older/newer SDKs sometimes omit an explicit msctf.lib import library; in most cases linking
  # against ole32/oleaut32/uuid is sufficient because TSF uses COM interfaces.
  target_link_libraries(jni PRIVATE ole32 oleaut32 uuid)

  # If you still get undefined references for TSF symbols, try adding msctf.lib explicitly:
  #   target_link_libraries(jni PRIVATE "${ProgramFiles(x86)}/Windows Kits/10/Lib/10.0.26100.0/um/${CMAKE_VS_PLATFORM_NAME}/msctf.lib")
  # or set MSCTF_LIB to the full path to msctf.lib and add it to the target.
endif()

# Keep output next to the resources so it can be picked up by the mod.
set(_jni_out_dir ${CMAKE_CURRENT_LIST_DIR}/../resources/assets/kitsuneime/natives)
set_target_properties(jni PROPERTIES
  OUTPUT_NAME $<IF:$<EQUAL:${CMAKE_SIZEOF_VOID_P},8>,jni,jni-x86>
  RUNTIME_OUTPUT_DIRECTORY ${_jni_out_dir}
  RUNTIME_OUTPUT_DIRECTORY_DEBUG ${_jni_out_dir}
  RUNTIME_OUTPUT_DIRECTORY_RELEASE ${_jni_out_dir}
  RUNTIME_OUTPUT_DIRECTORY_RELWITHDEBINFO ${_jni_out_dir}
  RUNTIME_OUTPUT_DIRECTORY_MINSIZEREL ${_jni_out_dir}
)
