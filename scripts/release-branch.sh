#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-}"
BASE_BRANCH="${2:-develop}"

if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Версия должна быть в формате X.Y.Z, получено: '$VERSION'" >&2
    exit 1
fi

RELEASE_BRANCH="release/${VERSION}"
TAG="v${VERSION}"

if [[ -n "$(git status --porcelain)" ]]; then
    echo "В рабочем дереве есть незакоммиченные изменения." >&2
    exit 1
fi

git fetch --all --prune
git checkout "$BASE_BRANCH"
git pull --ff-only
git checkout -b "$RELEASE_BRANCH"
git push -u origin "$RELEASE_BRANCH"

cat <<EOF

Релизная ветка создана: $RELEASE_BRANCH
Дальше по GitFlow:
  1. Прогнать регресс: mvn -B test  (и mvn -B test -Pkafka при изменениях в событиях)
  2. Приложить docs/release-checklist.md и Allure-отчёт к релизному объекту
  3. После согласования ЦОИФТ: git tag -a $TAG -m "Release $TAG"
  4. Влить $RELEASE_BRANCH в master и develop, запушить тег: git push origin $TAG
EOF
