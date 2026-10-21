$ErrorActionPreference = 'Stop'
$apiRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$containerName = 'carrinho-test-' + [Guid]::NewGuid().ToString('N').Substring(0, 8)
$containerId = $null
$keys = @('TEST_DATABASE_URL', 'TEST_DATABASE_USERNAME', 'TEST_DATABASE_PASSWORD', 'TEST_DATABASE_DRIVER')
$previous = @{}
foreach ($key in $keys) { $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process') }
try {
    $containerId = docker run --detach --name $containerName --label 'purpose=carrinho-integration-test' --env 'POSTGRES_USER=carrinho_test' --env 'POSTGRES_PASSWORD=carrinho_test_local' --env 'POSTGRES_DB=carrinho_test' --publish '127.0.0.1::5432' postgres:16.3
    if ($LASTEXITCODE -ne 0 -or $containerId -notmatch '^[a-f0-9]{64}$') { throw 'Não foi possível criar o container de teste' }
    $ready = $false
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        docker exec $containerId pg_isready --quiet -U carrinho_test -d carrinho_test
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Milliseconds 500
    }
    if (-not $ready) { throw 'PostgreSQL de teste não ficou pronto' }
    $binding = docker port $containerId '5432/tcp'
    $testPort = ([uri]('http://' + $binding)).Port
    $env:TEST_DATABASE_URL = "jdbc:postgresql://127.0.0.1:$testPort/carrinho_test"
    $env:TEST_DATABASE_USERNAME = 'carrinho_test'
    $env:TEST_DATABASE_PASSWORD = 'carrinho_test_local'
    $env:TEST_DATABASE_DRIVER = 'org.postgresql.Driver'
    & (Join-Path $apiRoot 'mvnw.cmd') -B -f (Join-Path $apiRoot 'pom.xml') test
    if ($LASTEXITCODE -ne 0) { throw 'A suíte apresentou falhas no PostgreSQL de teste' }
} finally {
    foreach ($key in $keys) { [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process') }
    if ($containerId -match '^[a-f0-9]{64}$') { docker rm --force $containerId | Out-Null }
}
