cmake_minimum_required(VERSION 3.5)

project(id3FaceSdkCppSamples)

# Macros
macro(postBuildCopyDirectory target dst_dir)
    set(dirs_list ${ARGN})
    foreach (src_dir ${dirs_list})
        add_custom_command(
                TARGET ${target} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy_directory
                ${src_dir}
                ${dst_dir})
    endforeach ()
endmacro()
# End macros

set(ID3FACE_SDK_ROOT_DIR ${CMAKE_CURRENT_LIST_DIR}/../sdk/)

include_directories(${ID3FACE_SDK_ROOT_DIR}include)

if (LINUX_BUILD)
    set(LINK_DIR ${ID3FACE_SDK_ROOT_DIR}bin/linux/x64)
elseif (WINDOWS_BUILD)
    set(LINK_DIR ${ID3FACE_SDK_ROOT_DIR}bin/windows/x64)
endif ()
# If building for another target (Linux x64 GPU, Raspberry Pi 4, etc) add an option here and fill in the correct path to the binaries

link_directories(${LINK_DIR})

# Compile Face Recognition Sample CLI
add_executable(id3FaceRecognitionSampleCLI id3FaceRecognitionSampleCLI.cpp)
target_link_libraries(id3FaceRecognitionSampleCLI id3Face)
set(TO_COYPY_FR ${LINK_DIR})
postBuildCopyDirectory(id3FaceRecognitionSampleCLI $<TARGET_FILE_DIR:id3FaceRecognitionSampleCLI> ${TO_COYPY_FR})

# Compile Portrait Processor Sample CLI
add_executable(id3PortraitProcessorSampleCLI id3PortraitProcessorSampleCLI.cpp)
target_link_libraries(id3PortraitProcessorSampleCLI id3Face)
set(TO_COPY_PP ${LINK_DIR})
postBuildCopyDirectory(id3PortraitProcessorSampleCLI $<TARGET_FILE_DIR:id3PortraitProcessorSampleCLI> ${TO_COPY_PP})
