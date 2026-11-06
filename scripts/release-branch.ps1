param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [string]$BaseBranch = 'develop',
    [string]$ReleaseBranchPrefix = 'release/',
    [switch]$Push,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    throw "Версия должна быть в формате X.Y.Z, получено: $Version"
}

$releaseBranch = "$ReleaseBranchPrefix$Version"
$tag = "v$Version"

function Invoke-Git {
    param([string[]]$Arguments)
    $command = "git " + ($Arguments -join ' ')
    if ($DryRun) {
        Write-Host "[dry-run] $command"
        return
    }
    Write-Host "-> $command"
    & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Команда завершилась с ошибкой: $command"
    }
}

$dirty = git status --porcelain
if ($dirty) {
    throw 'В рабочем дереве есть незакоммиченные изменения. Закоммитьте или спрячьте их (git stash).'
}

Invoke-Git @('fetch', '--all', '--prune')
Invoke-Git @('checkout', $BaseBranch)
Invoke-Git @('pull', '--ff-only')
Invoke-Git @('checkout', '-b', $releaseBranch)
Invoke-Git @('push', '-u', 'origin', $releaseBranch)

Write-Host ''
Write-Host 'Релизная ветка создана. Дальше по GitFlow:'
Write-Host "  1. Прогнать регресс: mvn -B test  (и mvn -B test -Pkafka при изменениях в событиях)"
Write-Host '  2. Приложить к релизному объекту docs/release-checklist.md и Allure-отчёт'
Write-Host "  3. После согласования ЦОИФТ создать тег: git tag -a $tag -m `"Release $tag`""
Write-Host "  4. Влить $releaseBranch в master и develop, затем git push origin $tag и --tags"
Write-Host ''
Write-Host "Тег, который нужно проставить: $tag"

if ($Push) {
    Invoke-Git @('tag', '-a', $tag, '-m', "Release $tag")
    Invoke-Git @('push', 'origin', $tag)
}
