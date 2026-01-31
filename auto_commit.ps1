param (
    [string]$Message = ""
)

$ErrorActionPreference = "Stop"

# Get current date and time
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# Determine commit message
if ([string]::IsNullOrWhiteSpace($Message)) {
    $commitMessage = "Auto commit: $timestamp"
} else {
    $commitMessage = "$Message ($timestamp)"
}

Write-Host "Checking for changes..."
$status = git status --porcelain

if ($null -eq $status) {
    Write-Host "No changes to commit."
} else {
    Write-Host "Changes found. Staging files..."
    git add .
    
    Write-Host "Committing changes with message: '$commitMessage'"
    git commit -m "$commitMessage"
    
    Write-Host "Pushing to remote..."
    git push
    
    Write-Host "Done!"
}
