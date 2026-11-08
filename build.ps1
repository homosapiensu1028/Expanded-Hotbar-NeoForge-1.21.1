$ErrorActionPreference = 'Stop'

# Always build from the directory containing this script. This also works when
# the script is launched from a different current directory.
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$GradleVersion = '8.14.3'
$ToolsDir = Join-Path $ProjectRoot '.tools'
$GradleHome = Join-Path $ToolsDir "gradle-$GradleVersion"
$GradleExe = Join-Path $GradleHome 'bin\gradle.bat'
$ZipPath = Join-Path $ToolsDir "gradle-$GradleVersion-bin.zip"

Write-Host '=== Dual Hotbar / NeoForge 1.21.1 build ===' -ForegroundColor Cyan
Write-Host "Project: $ProjectRoot"

# Java prints its version to stderr. Run the probe through cmd.exe so that
# Windows PowerShell 5.1 does not treat that output as a terminating error.
try {
    $JavaCommand = Get-Command java.exe -ErrorAction Stop
} catch {
    throw 'Java 21 was not found. Install a JDK 21 and add its bin directory to PATH.'
}
$JavaOutput = (& $env:ComSpec /d /c 'java -version 2>&1' | Out-String)
$JavaExitCode = $LASTEXITCODE
if ($JavaExitCode -ne 0 -or $JavaOutput -notmatch '(?i)(version\s+"21(?:\.|"))') {
    throw "Java 21 is required. Detected output:`n$JavaOutput"
}
Write-Host 'Java 21 detected.' -ForegroundColor Green

if (-not (Test-Path $GradleExe)) {
    New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null

    if (-not (Test-Path $ZipPath)) {
        $DownloadUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
        Write-Host "Downloading Gradle $GradleVersion..." -ForegroundColor Yellow
        # Windows PowerShell 5.1 may otherwise negotiate an obsolete TLS version.
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            Invoke-WebRequest -Uri $DownloadUrl -OutFile $ZipPath -UseBasicParsing
        } catch {
            if (Test-Path $ZipPath) { Remove-Item -LiteralPath $ZipPath -Force }
            throw "Could not download Gradle. Check the internet connection and try again. Details: $($_.Exception.Message)"
        }
    }

    Write-Host 'Extracting Gradle...' -ForegroundColor Yellow
    try {
        Expand-Archive -LiteralPath $ZipPath -DestinationPath $ToolsDir -Force
    } catch {
        Remove-Item -LiteralPath $ZipPath -Force -ErrorAction SilentlyContinue
        throw "Gradle archive extraction failed. Run the script again to download it again. Details: $($_.Exception.Message)"
    }
}

if (-not (Test-Path $GradleExe)) {
    throw "Gradle executable was not found: $GradleExe"
}

Push-Location $ProjectRoot
try {
    Write-Host "Building with Gradle $GradleVersion..." -ForegroundColor Cyan
    # Avoid PowerShell 5.1's native-stderr error-record behavior while still
    # checking the actual process exit code.
    $PreviousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & $GradleExe --no-daemon --console=plain --stacktrace clean build
    $BuildExitCode = $LASTEXITCODE
    $ErrorActionPreference = $PreviousErrorActionPreference

    if ($BuildExitCode -ne 0) {
        throw "Gradle build failed with exit code $BuildExitCode. Review the first error above."
    }
} finally {
    $ErrorActionPreference = 'Stop'
    Pop-Location
}

$JarPath = Join-Path $ProjectRoot 'build\libs\dualhotbar-1.0.4.jar'
if (-not (Test-Path $JarPath)) {
    throw "Gradle reported success, but the expected JAR was not found: $JarPath"
}

Write-Host ''
Write-Host 'BUILD SUCCEEDED' -ForegroundColor Green
Write-Host "JAR: $JarPath" -ForegroundColor Cyan
