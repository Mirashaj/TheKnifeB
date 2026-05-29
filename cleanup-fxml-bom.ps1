#!/usr/bin/env powershell
# Remove UTF-8 BOM from all FXML files
# Run this script whenever you edit FXML files before building

param(
    [string]$path = "src\main\resources\theknife\views"
)

$count = 0
Get-ChildItem "$path\*.fxml" | ForEach-Object {
    $content = [System.IO.File]::ReadAllText($_.FullName)
    $cleanBytes = [System.Text.Encoding]::UTF8.GetBytes($content)
    [System.IO.File]::WriteAllBytes($_.FullName, $cleanBytes)
    $count++
    Write-Host "Cleaned: $($_.Name)"
}

Write-Host "`nTotal files cleaned: $count"
