# 构建脚本：直接使用本机已安装的 Minecraft 26.2 客户端与依赖库编译，无需联网。
# 用法：右键“使用 PowerShell 运行”，或在 PowerShell 中执行 .\build.ps1

$ErrorActionPreference = "Stop"

$ProjectDir = $PSScriptRoot
$JdkHome    = "D:\zulu25.38.109-ca-jdk25.0.5.0.3-win_x64"
$McJar      = "D:\PCL\minecraft\versions\26.2\26.2.jar"
$McLibs     = "D:\PCL\minecraft\libraries"

if (-not (Test-Path $JdkHome)) { throw "找不到 JDK 25：$JdkHome" }
if (-not (Test-Path $McJar))  { throw "找不到 Minecraft 26.2 客户端：$McJar" }

$libs = (Get-ChildItem $McLibs -Recurse -File -Filter *.jar | ForEach-Object { $_.FullName }) -join ";"
$classpath = "$McJar;$libs"

$classes = Join-Path $ProjectDir "build\classes"
$stage   = Join-Path $ProjectDir "build\stage"
$libsOut = Join-Path $ProjectDir "build\libs"
$jarOut  = Join-Path $libsOut "emojibox-1.0.0.jar"

Remove-Item $classes, $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $classes, $stage, $libsOut | Out-Null

$sources = Get-ChildItem (Join-Path $ProjectDir "src\main\java"), (Join-Path $ProjectDir "stubs") -Recurse -Filter *.java |
        ForEach-Object { $_.FullName }

Write-Host "编译 $($sources.Count) 个源文件..."
& (Join-Path $JdkHome "bin\javac.exe") -encoding UTF-8 -proc:none -nowarn -d $classes -cp $classpath $sources
if ($LASTEXITCODE -ne 0) { throw "编译失败" }

Copy-Item (Join-Path $classes "com") $stage -Recurse -Force
Copy-Item (Join-Path $ProjectDir "src\main\resources\*") $stage -Recurse -Force

& (Join-Path $JdkHome "bin\jar.exe") --create --file $jarOut -C $stage .
if ($LASTEXITCODE -ne 0) { throw "打包失败" }

Write-Host "完成：$jarOut"
