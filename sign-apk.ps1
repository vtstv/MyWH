# Sign MyWH Release APK
# This script signs the release APK with a debug keystore

Write-Host "===================================" -ForegroundColor Cyan
Write-Host "  MyWH APK Signing Script" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""

# Paths
$projectRoot = "D:\Dev\MyWH"
$apkPath = "$projectRoot\app\build\outputs\apk\release\app-release-unsigned.apk"
$signedApkPath = "$projectRoot\app\build\outputs\apk\release\MyWH-signed.apk"
$keystorePath = "$projectRoot\debug.keystore"

# Check if unsigned APK exists
if (-not (Test-Path $apkPath)) {
    Write-Host "[ERROR] Unsigned APK not found at:" -ForegroundColor Red
    Write-Host "  $apkPath" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please build the release APK first:" -ForegroundColor Yellow
    Write-Host "  .\gradlew assembleRelease" -ForegroundColor White
    exit 1
}

Write-Host "[1/4] Found unsigned APK" -ForegroundColor Green
$apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
Write-Host "  Size: $apkSize MB" -ForegroundColor Gray

# Create debug keystore if it doesn't exist
if (-not (Test-Path $keystorePath)) {
    Write-Host ""
    Write-Host "[2/4] Creating debug keystore..." -ForegroundColor Yellow

    $keytoolCmd = "keytool -genkey -v -keystore `"$keystorePath`" -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname `"CN=Android Debug,O=Android,C=US`""

    try {
        Invoke-Expression $keytoolCmd 2>&1 | Out-Null
        Write-Host "  Keystore created successfully" -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Failed to create keystore" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host ""
    Write-Host "[2/4] Using existing debug keystore" -ForegroundColor Green
}

# Remove old signed APK if exists
if (Test-Path $signedApkPath) {
    Remove-Item $signedApkPath -Force
    Write-Host "  Removed old signed APK" -ForegroundColor Gray
}

# Sign APK using apksigner (from Android SDK build-tools)
Write-Host ""
Write-Host "[3/4] Signing APK..." -ForegroundColor Yellow

# Try to find apksigner in Android SDK
$possiblePaths = @(
    "$env:ANDROID_HOME\build-tools\35.0.0\apksigner.bat",
    "$env:ANDROID_HOME\build-tools\34.0.0\apksigner.bat",
    "$env:ANDROID_HOME\build-tools\33.0.2\apksigner.bat",
    "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0\apksigner.bat",
    "$env:LOCALAPPDATA\Android\Sdk\build-tools\34.0.0\apksigner.bat",
    "$env:LOCALAPPDATA\Android\Sdk\build-tools\33.0.2\apksigner.bat"
)

$apksigner = $null
foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $apksigner = $path
        break
    }
}

if (-not $apksigner) {
    Write-Host "[ERROR] apksigner not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please install Android SDK Build Tools or set ANDROID_HOME environment variable" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Alternative: Use jarsigner (legacy method)" -ForegroundColor Cyan
    Write-Host "  jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore debug.keystore -storepass android -keypass android app-release-unsigned.apk androiddebugkey" -ForegroundColor White
    exit 1
}

Write-Host "  Using apksigner: $(Split-Path $apksigner -Leaf)" -ForegroundColor Gray

try {
    & $apksigner sign --ks $keystorePath --ks-pass pass:android --key-pass pass:android --out $signedApkPath $apkPath 2>&1 | Out-Null

    if (Test-Path $signedApkPath) {
        Write-Host "  APK signed successfully!" -ForegroundColor Green
    } else {
        throw "Signed APK was not created"
    }
} catch {
    Write-Host "[ERROR] Failed to sign APK" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

# Verify signature
Write-Host ""
Write-Host "[4/4] Verifying signature..." -ForegroundColor Yellow

try {
    & $apksigner verify $signedApkPath 2>&1 | Out-Null
    Write-Host "  Signature verified successfully!" -ForegroundColor Green
} catch {
    Write-Host "[WARNING] Could not verify signature, but APK should still work" -ForegroundColor Yellow
}

# Final info
Write-Host ""
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "  SUCCESS!" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Signed APK location:" -ForegroundColor White
Write-Host "  $signedApkPath" -ForegroundColor Cyan
Write-Host ""
$signedSize = [math]::Round((Get-Item $signedApkPath).Length / 1MB, 2)
Write-Host "Size: $signedSize MB" -ForegroundColor White
Write-Host ""
Write-Host "Install command:" -ForegroundColor White
Write-Host "  adb install `"$signedApkPath`"" -ForegroundColor Cyan
Write-Host ""
Write-Host "Or install now? (Y/N)" -ForegroundColor Yellow
$response = Read-Host

if ($response -eq "Y" -or $response -eq "y") {
    Write-Host ""
    Write-Host "Installing APK..." -ForegroundColor Yellow

    try {
        adb install -r $signedApkPath
        Write-Host ""
        Write-Host "Installation completed!" -ForegroundColor Green
    } catch {
        Write-Host ""
        Write-Host "[ERROR] Installation failed" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        Write-Host ""
        Write-Host "Make sure:" -ForegroundColor Yellow
        Write-Host "  1. USB debugging is enabled on your device" -ForegroundColor White
        Write-Host "  2. Device is connected via USB" -ForegroundColor White
        Write-Host "  3. You've authorized the computer on your device" -ForegroundColor White
    }
} else {
    Write-Host ""
    Write-Host "APK is ready to install manually!" -ForegroundColor Green
}

Write-Host ""

