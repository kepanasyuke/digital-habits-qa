# GitFlow в этом проекте

Схема ветвления, принятая в команде (и то, как её проверяют на собеседовании).

| Ветка | Назначение | Куда вливается |
| --- | --- | --- |
| `main` | прод, только релизные слияния и теги | — |
| `develop` | интеграционная ветка, сюда идут все фичи | `release/*` |
| `feature/<KEY>-<описание>` | задача | `develop` через merge request |
| `release/<X.Y.Z>` | стабилизация релиза, только фиксы | `main` и `develop` |
| `hotfix/<X.Y.Z>` | срочный фикс прода | `main` и `develop` |

## Порядок релиза

```bash
git checkout develop && git pull --ff-only
pwsh scripts/release-branch.ps1 -Version 1.2.0     # или bash scripts/release-branch.sh 1.2.0
mvn -B test                                        # регресс
mvn -B test -Pkafka                                # проверки событий, если менялась интеграция
# согласование ЦОИФТ по docs/release-checklist.md
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin v1.2.0
git checkout main && git merge --no-ff release/1.2.0 && git push
git checkout develop && git merge --no-ff release/1.2.0 && git push
git branch -d release/1.2.0
```

## Что важно помнить

- Тег ставится только на `main` после слияния релизной ветки, иначе артефакт в проде не совпадёт с тегом.
- В `release/*` не добавляют новый функционал — только фиксы, найденные на регрессе.
- Каждое слияние в `main` сопровождается релизным объектом: ветка, тег, артефакт, Allure-отчёт, чек-лист ЦОИФТ.
