# Database Reset Script
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESETTING COACHES APP DATABASE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$dbPath = "e:\javalab\Coachsapp\coaches_app.db"

if (Test-Path $dbPath) {
    Write-Host "Deleting old database..." -ForegroundColor Yellow
    Remove-Item $dbPath -Force
    Write-Host "✓ Old database deleted" -ForegroundColor Green
} else {
    Write-Host "No existing database found" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Now run the application to create fresh database with correct schema!" -ForegroundColor Cyan
Write-Host ""
