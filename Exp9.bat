@echo off
cls
cd /d "C:\Users\TANISI\OneDrive\Desktop\SVU-Project"
javac -cp .;JMapViewer.jar;json-20230227.jar *.java
java -cp .;JMapViewer.jar;json-20230227.jar WeatherApp
pause
