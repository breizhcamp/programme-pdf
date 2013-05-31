@echo off
@setlocal enabledelayedexpansion


for /f %%i in ("%CD%\%0") do (
    set SCRIPT_PATH=%%~dpi
)

for /f %%i in ("%SCRIPT_PATH%\..") do (
    set PROJECT_PATH=%%~fi
)

set BEES_APP_ID=%1
set MAIN_CLASS=fr.ybonnel.breizhcamppdf.BreizhcampPdf

if [%BEES_APP_ID%] == [] (
    echo "Use : deploybees <APP_ID>"
    goto end
)

set ARTIFACT_ID=breizhcamp-pdf


for /f %%i in ('unzip -Z -1 %PROJECT_PATH%\target\%ARTIFACT_ID%-cloudbees.zip ^| grep jar') do (
    if [!BEES_CLASSPATH!] == [] (
        set BEES_CLASSPATH=%%i
    ) else (
        set BEES_CLASSPATH=!BEES_CLASSPATH!:%%i
    )
)

bees app:deploy -a %BEES_APP_ID% -t java -R java_version=1.7 -R class=%MAIN_CLASS% -R classpath=%BEES_CLASSPATH% %PROJECT_PATH%\target\%ARTIFACT_ID%-cloudbees.zip

:end
@endlocal