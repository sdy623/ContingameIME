param(
  [ValidateSet("build_jni", "build_all")]
  [string]$Task = "build_jni",
  [ValidateSet("Debug", "Release", "RelWithDebInfo", "MinSizeRel")]
  [string]$Config = "Release",
  [string]$Generator = "",
  [switch]$SkipX86,
  [switch]$SkipX64
)

$ErrorActionPreference = "Stop"
$repoRoot = $PSScriptRoot

function Assert-Command {
  param([string]$Name)
  if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
    throw "Required command not found in PATH: $Name"
  }
}

function Invoke-CMakeBuild {
  param([string]$Arch)

  $srcDir = Join-Path $repoRoot "common/src/main/cpp"
  $buildDir = Join-Path $repoRoot "build/jni/$Arch"

  $cmakeArgs = @("-S", $srcDir, "-B", $buildDir)
  if ($Generator -ne "") {
    $cmakeArgs += @("-G", $Generator)
  }

  if ($Arch -eq "x64") {
    $cmakeArgs += @("-A", "x64")
  } elseif ($Arch -eq "x86") {
    $cmakeArgs += @("-A", "Win32")
  } else {
    throw "Unsupported architecture: $Arch"
  }

  Write-Host "Configuring JNI ($Arch)..."
  cmake @cmakeArgs

  Write-Host "Building JNI ($Arch) - $Config..."
  cmake --build $buildDir --config $Config --target jni
}

function Build-Jni {
  Assert-Command cmake

  if (-not $SkipX64) {
    Invoke-CMakeBuild -Arch "x64"
  }
  if (-not $SkipX86) {
    Invoke-CMakeBuild -Arch "x86"
  }
}

function Build-All {
  Build-Jni

  $gradlew = Join-Path $repoRoot "gradlew.bat"
  if (-not (Test-Path $gradlew)) {
    throw "gradlew.bat not found in repo root."
  }

  Write-Host "Running Gradle build..."
  & $gradlew build
}

switch ($Task) {
  "build_jni" { Build-Jni }
  "build_all" { Build-All }
  default { throw "Unknown task: $Task" }
}
