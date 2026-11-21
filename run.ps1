# Nexus Arena - Run Script
# Runs the application

$JAVAFX_LIB = 'C:\javafx-sdk-27.0.1.1\lib'

$CP = (Get-ChildItem -Path $JAVAFX_LIB -Filter '*.jar' | ForEach-Object { $_.FullName }) -join ';'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-javafx\12.3.1\ikonli-javafx-12.3.1.jar'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-core\12.3.1\ikonli-core-12.3.1.jar'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-fontawesome5-pack\12.3.1\ikonli-fontawesome5-pack-12.3.1.jar'
$CP += ';out'

Write-Host 'Starting Nexus Arena...' -ForegroundColor Cyan
java --enable-native-access=ALL-UNNAMED -cp $CP com.esports.manager.EsportsManagerApp
