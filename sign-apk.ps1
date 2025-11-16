# =====================================================================
#  Android Build + Sign Script (MyWH)
#  Smart keystore detection + release/debug support
#  Author: https://github.com/vtstv
# =====================================================================
$ErrorActionPreference = "Stop"

Write-Host "=== MyWH Build & Sign Script ===" -ForegroundColor Cyan

# ------------------------------------------------------------
# PROJECT PATH (current script directory)
# ------------------------------------------------------------
$projectRoot = $PSScriptRoot
Write-Host "[INFO] Project root: $projectRoot"

# ------------------------------------------------------------
# APK PATHS
# ------------------------------------------------------------
$releaseDir  = Join-Path $projectRoot "Release"
$unsignedApk = Join-Path $projectRoot "app\build\outputs\apk\release\app-release-unsigned.apk"
$signedApk   = Join-Path $releaseDir "MyWH-signed.apk"

# Create Release directory if it doesn't exist
if (-not (Test-Path $releaseDir)) {
    New-Item -ItemType Directory -Path $releaseDir -Force | Out-Null
}

if (Test-Path $signedApk) { Remove-Item $signedApk -Force }

# ------------------------------------------------------------
# KEYSTORE CONFIGURATION
# ------------------------------------------------------------

# Release keystore
$releaseAlias = "mywhkey"
$releaseKeystoreLocal = "$projectRoot\mywh.keystore"
$releaseKeystoreHome  = "$env:USERPROFILE\.android\mywh.keystore"

# Debug keystore
$debugAlias = "androiddebugkey"
$debugPassword = "android"
$debugKeystoreLocal = "$projectRoot\debug.keystore"
$debugKeystoreHome  = "$env:USERPROFILE\.android\debug.keystore"

# Selected keystore info
$useKeystorePath = $null
$useAlias = $null
$usePassword = $null

Write-Host "`n=== Checking Keystore ===" -ForegroundColor Cyan

# ------------------------------------------------------------
# KEYSTORE SELECTION LOGIC
# ------------------------------------------------------------

if (Test-Path $releaseKeystoreLocal) {
    Write-Host "[INFO] Using RELEASE keystore (local)" -ForegroundColor Green
    $useKeystorePath = $releaseKeystoreLocal
    $useAlias = $releaseAlias
}
elseif (Test-Path $releaseKeystoreHome) {
    Write-Host "[INFO] Using RELEASE keystore (~/.android)" -ForegroundColor Green
    $useKeystorePath = $releaseKeystoreHome
    $useAlias = $releaseAlias
}
elseif (Test-Path $debugKeystoreLocal) {
    Write-Host "[INFO] Using DEBUG keystore (local)" -ForegroundColor Yellow
    $useKeystorePath = $debugKeystoreLocal
    $useAlias = $debugAlias
    $usePassword = $debugPassword
}
elseif (Test-Path $debugKeystoreHome) {
    Write-Host "[INFO] Using DEBUG keystore (~/.android)" -ForegroundColor Yellow
    $useKeystorePath = $debugKeystoreHome
    $useAlias = $debugAlias
    $usePassword = $debugPassword
}
else {
    Write-Host "[WARN] No keystore found. Creating debug.keystore..." -ForegroundColor Yellow

    keytool -genkey -v `
        -keystore $debugKeystoreLocal `
        -alias $debugAlias `
        -keyalg RSA -keysize 2048 `
        -validity 10000 `
        -storepass android `
        -keypass android `
        -dname "CN=Android Debug,O=Android,C=US" | Out-Null

    $useKeystorePath = $debugKeystoreLocal
    $useAlias = $debugAlias
    $usePassword = $debugPassword
}

# Ask for password if using release keystore
if ($useAlias -eq $releaseAlias -and -not $usePassword) {
    $usePassword = Read-Host "Enter release keystore password"
}

Write-Host "[INFO] Selected keystore: $useKeystorePath"
Write-Host "[INFO] Alias: $useAlias"

# ------------------------------------------------------------
# BUILD PROJECT
# ------------------------------------------------------------
Write-Host "`n=== Building Project ===" -ForegroundColor Cyan

$gradlew = Join-Path $projectRoot "gradlew.bat"

if (-not (Test-Path $gradlew)) {
    Write-Host "[ERROR] gradlew.bat not found!" -ForegroundColor Red
    exit 1
}

# Run Gradle assembleRelease
cmd.exe /c "$gradlew assembleRelease"

# Gradle normally outputs to:
$gradleUnsigned = $unsignedApk

if (-not (Test-Path $gradleUnsigned)) {
    Write-Host "[ERROR] Unsigned APK not found." -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Unsigned APK ready at: $unsignedApk"

# ------------------------------------------------------------
# SIGN APK — smart search for apksigner
# ------------------------------------------------------------
Write-Host "`n=== Signing APK ===" -ForegroundColor Cyan

$possibleApksigner = @(
    "$env:ANDROID_HOME\build-tools\35.0.0\apksigner.bat",
    "$env:ANDROID_HOME\build-tools\34.0.0\apksigner.bat",
    "$env:ANDROID_HOME\build-tools\33.0.2\apksigner.bat",
    "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0\apksigner.bat",
    "$env:LOCALAPPDATA\Android\Sdk\build-tools\34.0.0\apksigner.bat",
    "$env:LOCALAPPDATA\Android\Sdk\build-tools\33.0.2\apksigner.bat"
)

$apksigner = $null
foreach ($p in $possibleApksigner) {
    if (Test-Path $p) {
        $apksigner = $p
        break
    }
}

if (-not $apksigner) {
    Write-Host "[ERROR] apksigner not found!" -ForegroundColor Red
    Write-Host "Install Android SDK Build Tools or set ANDROID_HOME." -ForegroundColor Yellow
    exit 1
}


Write-Host "[INFO] Using apksigner: $apksigner"

# Perform signing
& $apksigner sign `
    --ks "$useKeystorePath" `
    --ks-pass pass:"$usePassword" `
    --key-pass pass:"$usePassword" `
    --out "$signedApk" `
    "$unsignedApk"

Write-Host "[INFO] APK signed successfully!" -ForegroundColor Green

# Verify signature
Write-Host "`n=== Verifying signature ===" -ForegroundColor Cyan
& $apksigner verify "$signedApk"

# ------------------------------------------------------------
# DONE
# ------------------------------------------------------------
Write-Host "`n====================================================="
Write-Host "BUILD COMPLETE!"
Write-Host "Signed APK: $signedApk" -ForegroundColor Green
Write-Host "====================================================="
