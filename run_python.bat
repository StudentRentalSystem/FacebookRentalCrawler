@echo off
REM Run script for Python version of Facebook Rental Crawler (Windows)

REM Check if scroll count is provided
if "%1"=="" (
    echo Usage: run_python.bat ^<SCROLL_COUNT^>
    echo Example: run_python.bat 10
    exit /b 1
)

REM Add Python source directory to PYTHONPATH
set PYTHONPATH=%PYTHONPATH%;%cd%\src\python

REM Run the crawler
python -m xyz.jessyu.main %1
