REM Must be called from powershell
mkdir .\build\
cd .\build\
cmake -G "Visual Studio 15 2017 Win64" -DWINDOWS_BUILD=ON ..
cmake --build . --config Release
REM Executable(s) are in the folder Release\