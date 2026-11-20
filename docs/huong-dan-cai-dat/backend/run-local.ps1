# Run from any folder. Only this process and its Maven child receive the values.
$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    $envPath = Join-Path $PSScriptRoot '.env'
    if (-not (Test-Path -LiteralPath $envPath)) { throw 'Create backend .env first.' }
    $lineNumber = 0
    foreach ($line in [IO.File]::ReadAllLines($envPath)) {
        $lineNumber++
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) { continue }
        $pair = $trimmed -split '=', 2
        if ($pair.Count -ne 2 -or $pair[0].Trim() -notmatch '^[A-Za-z_][A-Za-z0-9_]*$') {
            throw "Invalid .env format at line $lineNumber."
        }
        $name = $pair[0].Trim()
        $value = $pair[1].Trim()
        if ($value.Length -ge 2 -and (
            ($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'")))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
    if (-not $env:DB_PASSWORD) { throw 'Fill in DB_PASSWORD in .env.' }
    if (-not $env:MAIL_USERNAME -or -not $env:MAIL_PASSWORD) { throw 'Fill in Gmail SMTP credentials in .env.' }
    $env:MAIL_PASSWORD = $env:MAIL_PASSWORD -replace '\s', ''
    if ($env:MAIL_PASSWORD.Length -ne 16) { throw 'Use the 16-character Gmail App Password.' }
    if ([string]::IsNullOrWhiteSpace($env:MAIL_FROM)) {
        $env:MAIL_FROM = $env:MAIL_USERNAME
    }

    if ([Text.Encoding]::UTF8.GetByteCount([string]$env:JWT_SECRET) -lt 32) {
        throw 'JWT_SECRET must be at least 32 bytes. Run New-JwtSecret.ps1 first.'
    }
    if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        throw 'Set JAVA_HOME to your JDK 25 installation in this terminal first.'
    }
    & (Join-Path $env:JAVA_HOME 'bin\java.exe') -version
    if ($LASTEXITCODE -ne 0) { throw 'Could not run Java.' }
    & (Join-Path $PSScriptRoot 'mvnw.cmd') spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'Backend failed. Read the error above.' }
} finally { Pop-Location }
