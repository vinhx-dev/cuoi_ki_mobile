# Run once inside the backend folder, after creating .env.
$ErrorActionPreference = 'Stop'
$envPath = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path -LiteralPath $envPath)) { throw 'Create backend .env first.' }

$content = [IO.File]::ReadAllText($envPath)
$pattern = [regex]'(?m)^JWT_SECRET\s*=([^\r\n]*)'
$match = $pattern.Match($content)
$existing = if ($match.Success) { $match.Groups[1].Value.Trim() } else { '' }
if ($existing) {
    if ([Text.Encoding]::UTF8.GetByteCount($existing) -lt 32) {
        throw 'Existing JWT_SECRET is shorter than 32 bytes. Review it before replacing it.'
    }
    Write-Output 'JWT_SECRET already exists. Kept the current signing key.'
    exit 0
}

$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$secret = [Convert]::ToBase64String($bytes)
if ($match.Success) {
    $content = $pattern.Replace($content, ('JWT_SECRET=' + $secret), 1)
} else {
    $content = $content.TrimEnd() + [Environment]::NewLine + 'JWT_SECRET=' + $secret + [Environment]::NewLine
}
[IO.File]::WriteAllText($envPath, $content, (New-Object Text.UTF8Encoding $false))
Write-Output 'Created a local JWT signing key in .env. The key was not printed.'
