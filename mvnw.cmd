@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
@REM Remove trailing backslash to avoid escaping quotes in Java args
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%
set WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPS=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%WRAPPER_PROPS%" (
  echo [ERROR] Missing %WRAPPER_PROPS%
  exit /b 1
)

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper jar...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$props = Get-Content -Raw '%WRAPPER_PROPS%';" ^
    "$line = ($props -split \"`n\" | Where-Object { $_ -match '^wrapperUrl=' } | Select-Object -First 1);" ^
    "$u = $null; if ($line) { $u = ($line -split '=',2)[1].Trim() };" ^
    "if(-not $u){$u='https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar'};" ^
    "New-Item -ItemType Directory -Force -Path '%WRAPPER_DIR%' | Out-Null;" ^
    "Invoke-WebRequest -Uri $u -OutFile '%WRAPPER_JAR%';"
  if errorlevel 1 (
    echo [ERROR] Failed to download Maven Wrapper jar.
    exit /b 1
  )
)

for /f "tokens=2 delims==" %%a in ('findstr /b distributionUrl "%WRAPPER_PROPS%"') do set MAVEN_DIST_URL=%%a

if "%MAVEN_DIST_URL%"=="" (
  echo [ERROR] distributionUrl missing in %WRAPPER_PROPS%
  exit /b 1
)

set MAVEN_OPTS=%MAVEN_OPTS%

"%JAVA_HOME%\bin\java.exe" -version >nul 2>&1
if errorlevel 1 (
  echo [WARN] JAVA_HOME not set. Using 'java' from PATH.
  set JAVA_CMD=java
) else (
  set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
)

%JAVA_CMD% -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*

