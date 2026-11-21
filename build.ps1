# Nexus Arena - Build Script
# Compile Java sources and copy resources to output directory

$JAVAFX_LIB = 'C:\javafx-sdk-27.0.1.1\lib'

$CP = (Get-ChildItem -Path $JAVAFX_LIB -Filter '*.jar' | ForEach-Object { $_.FullName }) -join ';'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-javafx\12.3.1\ikonli-javafx-12.3.1.jar'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-core\12.3.1\ikonli-core-12.3.1.jar'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-fontawesome5-pack\12.3.1\ikonli-fontawesome5-pack-12.3.1.jar'

Write-Host 'Compiling Java sources...' -ForegroundColor Cyan
$FILES = Get-ChildItem -Path 'src\main\java' -Filter '*.java' -Recurse | ForEach-Object { $_.FullName }
javac -d out -cp $CP -encoding UTF-8 @FILES
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Compilation failed!' -ForegroundColor Red
    exit 1
}

Write-Host 'Copying resources...' -ForegroundColor Cyan
Copy-Item -Path 'src\main\resources\*' -Destination 'out\' -Recurse -Force

Write-Host 'Build completed successfully!' -ForegroundColor Green
Write-Host ''
Write-Host 'To run: .\run.cmd'
