# Nexus Arena - Run Script
# Runs the application

$JAVAFX_LIB = 'C:\javafx-sdk-27.0.1.1\lib'

$CP = (Get-ChildItem -Path $JAVAFX_LIB -Filter '*.jar' | ForEach-Object { $_.FullName }) -join ';'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-javafx\12.3.1\ikonli-javafx-12.3.1.jar'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-core\12.3.1\ikonli-core-12.3.1.jar'
$CP += ';C:\Users\admin\.m2\repository\org\kordamp\ikonli\ikonli-fontawesome5-pack\12.3.1\ikonli-fontawesome5-pack-12.3.1.jar'

if (Test-Path 'lib') {
    $LIB_JARS = Get-ChildItem -Path 'lib' -Filter '*.jar' | ForEach-Object { $_.FullName }
    if ($LIB_JARS) {
        $CP += ';' + ($LIB_JARS -join ';')
    }
}

$SQLSERVER_JDBC = 'C:\sqljdbc_13.6\enu\jars\mssql-jdbc-13.6.0.jre11.jar'
if (Test-Path $SQLSERVER_JDBC) {
    $CP += ';' + $SQLSERVER_JDBC
}

$CP += ';out'

Write-Host 'Starting Nexus Arena...' -ForegroundColor Cyan
$SQLSERVER_AUTH_DIR = 'C:\sqljdbc_13.6\enu\auth\x64'
java --enable-native-access=ALL-UNNAMED "-Djava.library.path=$SQLSERVER_AUTH_DIR" -cp $CP com.esports.manager.EsportsManagerApp
