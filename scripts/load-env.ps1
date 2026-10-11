$envFile = Join-Path (Get-Location) ".env"
if (-not (Test-Path $envFile)) {
    throw "No existe .env. Copia .env.example a .env y configura las contraseñas locales."
}

foreach ($line in Get-Content $envFile) {
    if ([string]::IsNullOrWhiteSpace($line)) {
        continue
    }
    $separatorIndex = $line.IndexOf('=')
    if ($separatorIndex -le 0) {
        continue
    }
    $name = $line.Substring(0, $separatorIndex).Trim()
    $value = $line.Substring($separatorIndex + 1).Trim()
    [Environment]::SetEnvironmentVariable($name, $value, 'Process')
}

if ($env:POSTGRES_PASSWORD -eq "CAMBIAR_PASSWORD_POSTGRES" -or $env:RABBITMQ_PASSWORD -eq "CAMBIAR_PASSWORD_RABBITMQ") {
    throw "Edita .env y cambia las dos contraseñas de ejemplo antes de iniciar los servicios."
}

$env:DB_USER = $env:POSTGRES_USER
$env:DB_PASSWORD = $env:POSTGRES_PASSWORD
$env:RABBITMQ_HOST = "localhost"
$env:RABBITMQ_PORT = "5672"
