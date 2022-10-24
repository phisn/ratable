@echo off

if "%~1" neq "_start_" (
    start "az-functions-service" func start --java --cors *
    cd ../..
    sbt dev
    cmd /c "%~f0" _start_ %*
    taskkill /FI "WindowTitle eq az-functions-service*" /T /F
)
shift /1
