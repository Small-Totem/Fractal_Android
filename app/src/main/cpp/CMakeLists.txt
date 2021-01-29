cmake_minimum_required(VERSION 3.10.2)

project("fractal")

add_library(fractal
             SHARED
             fractal.c )

find_library(log-lib
             log )

target_link_libraries(fractal
                    ${log-lib} )

