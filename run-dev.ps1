$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

# Backend
Start-Process -WorkingDirectory "$root\backend" -FilePath "mvn" -ArgumentList "spring-boot:run"

# Frontend
if (-not (Test-Path "$root\frontend\.env")) { Copy-Item "$root\frontend\.env.example" "$root\frontend\.env" -ErrorAction SilentlyContinue }
Start-Process -WorkingDirectory "$root\frontend" -FilePath "npm" -ArgumentList "install"
Start-Process -WorkingDirectory "$root\frontend" -FilePath "npm" -ArgumentList "run dev"

Write-Host "Backend on http://localhost:8080  |  Frontend on http://localhost:5173"
Write-Host "Press Ctrl+C in terminals to stop."
