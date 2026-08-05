Вот готовый документ для вашего репозитория — сохраните как `COMMITS.md` или добавьте в `CONTRIBUTING.md`:

---

```markdown
# Руководство по Conventional Commits

> Действует для всех участников команды `autodev-marketplace`.
> Нарушение формата не ломает сборку, но лишает вас автоматического
> версионирования и чистого changelog.

---

## 1. Зачем это нужно

Наш CI/CD автоматически определяет номер релиза по истории коммитов:

| Что написано в коммите | Какая версия выйдет | Пример |
|------------------------|---------------------|--------|
| `fix(api-gateway): ...` | **patch** `1.2.3` → `1.2.4` | Исправление бага |
| `feat(service-b): ...` | **minor** `1.2.3` → `1.3.0` | Новая фича |
| `feat(api-gateway)!: ...` или `BREAKING CHANGE` | **major** `1.2.3` → `2.0.0` | Ломающее изменение |

Если коммит не соответствует формату — CI проигнорирует его при расчёте версии,
и релиз получит лишь **patch** bump по умолчанию. Changelog будет пустым.

---

## 2. Общий формат

```
<type>(<scope>): <краткое описание>

[опциональное тело]

[опциональные footer'ы]
```

**Правила:**
- Всё в нижнем регистре (кроме собственных имён, например `OAuth2`)
- Без точки в конце заголовка
- Максимум 72 символа в заголовке
- Пустая строка между заголовком, телом и footer'ами

---

## 3. Типы коммитов

| Тип | Когда использовать | Влияет на версию |
|-----|------------------|------------------|
| `feat` | Новая функциональность | **minor** |
| `fix` | Исправление бага | **patch** |
| `perf` | Оптимизация производительности | **patch** |
| `refactor` | Рефакторинг без изменения поведения | **patch** |
| `revert` | Откат предыдущего коммита | **patch** |
| `docs` | Изменение документации | — |
| `style` | Форматирование, отступы, точки с запятой | — |
| `chore` | Обновление зависимостей, CI, сборка | — |
| `test` | Добавление/исправление тестов | — |
| `ci` | Изменения в GitHub Actions, pipeline | — |

> **Важно:** `docs`, `style`, `chore`, `test`, `ci` **не влияют** на версию.
> Если в релизной ветке только такие коммиты — CI выставит **patch** по умолчанию.

---

## 4. Scope в монорепозитории

Scope **обязателен** и должен совпадать с именем сервиса или модуля.

```
services/api-gateway/    →  scope: api-gateway
services/service-b/      →  scope: service-b
services/shared-lib/     →  scope: shared-lib
```

### Правила для scope

| Ситуация | Правильный scope | Неправильно |
|----------|-----------------|-------------|
| Изменили `services/api-gateway/src/...` | `api-gateway` | `gateway`, `AG`, `api` |
| Изменили `services/shared-lib/src/...` | `shared-lib` | `lib`, `common` |
| Изменения затронули несколько сервисов | Укажите основной или используйте `*` | `all`, `monorepo` |
| Изменения только в корне (gradle, CI) | `ci` или `build` | `root`, `global` |

### Примеры правильных заголовков

```text
feat(api-gateway): add OAuth2 token introspection endpoint
fix(service-b): resolve race condition in payment processor
perf(api-gateway): cache user roles in Redis for 5 minutes
refactor(service-b): extract payment validation to separate service
docs(shared-lib): add JavaDoc for TokenValidator
chore(api-gateway): bump spring-boot from 3.4.0 to 3.4.5
ci: add integration tests for service-b
```

---

## 5. BREAKING CHANGES (major bump)

Есть два способа сообщить о ломающем изменении:

### Способ 1: Восклицательный знак в заголовке

```text
feat(api-gateway)!: drop support for legacy JWT format
fix(service-b)!: change payment webhook response schema
```

### Способ 2: Footer `BREAKING CHANGE`

```text
feat(api-gateway): migrate to new auth protocol

BREAKING CHANGE: the /auth/validate endpoint now requires
`X-API-Version: 2` header. Clients using v1 must update.
```

**Оба способа работают.** Можно использовать одновременно для ясности.

> ⚠️ **Внимание:** `BREAKING CHANGE` в `shared-lib` = пересборка **всех** зависимых
> сервисов при batch-релизе. Обсудите с командой перед мержем.

---

## 6. Тело коммита (опционально, но желательно)

Объясняйте **почему**, а не только **что**.

```text
fix(api-gateway): handle null pointer in token filter

The NPE occurred when Redis was unavailable and fallback
to in-memory cache returned null. Added null-check before
calling cache.get().

Closes: #42
```

Хорошее тело помогает при code review и при генерации release notes.

---

## 7. Footer'ы (опционально)

| Footer | Назначение | Пример |
|--------|-----------|--------|
| `Closes: #123` | Автоматически закрывает Issue | `Closes: #42` |
| `Refs: #456` | Ссылка на связанный Issue без закрытия | `Refs: #55` |
| `Co-authored-by:` | Дополнительные авторы | `Co-authored-by: Ivan Petrov <ivan@example.com>` |
| `BREAKING CHANGE:` | Описание ломающего изменения | см. выше |

---

## 8. Примеры: хорошо и плохо

### ❌ Плохо

```text
update
fixed bug
WIP
some changes
api gateway fix
refactor
fix: теперь работает
feat: добавил крутую штуку
```

**Почему плохо:** нет scope, непонятно что сделано, на русском, нет типа,
или тип не из списка (`update`, `WIP` — не conventional).

### ✅ Хорошо

```text
fix(api-gateway): handle expired refresh tokens correctly

feat(service-b): implement idempotent payment processing

perf(api-gateway): reduce DB calls by caching permissions

refactor(shared-lib): replace custom validator with jakarta-validation

docs: update RELEASE.md with batch release instructions

chore: update Gradle wrapper to 8.14

ci(api-gateway): add OWASP dependency check to pipeline
```

---

## 9. Работа с ветками и коммитами

### Feature-ветки

```bash
# Создаём ветку
git checkout -b feature/api-gateway-oauth2

# Работаем, коммитим часто и понятно
git commit -m "feat(api-gateway): add OAuth2 client configuration"
git commit -m "feat(api-gateway): implement token introspection filter"
git commit -m "test(api-gateway): add unit tests for token filter"
git commit -m "docs(api-gateway): add OAuth2 setup guide"

# Push и PR в develop
git push origin feature/api-gateway-oauth2
```

### Релизные ветки

Когда создаёте `release/api-gateway` — CI проанализирует **все коммиты**
с момента последнего тега `api-gateway/v*`. Поэтому важно, чтобы
каждый значимый коммит в `develop` был правильно оформлен **ещё до мержа**.

---

## 10. Как исправить неправильный коммит

### Если ещё не запушили

```bash
# Исправить последний коммит
git commit --amend -m "fix(api-gateway): handle NPE in token filter"

# Исправить коммит раньше (например, 3 коммита назад)
git rebase -i HEAD~3
# в редакторе замените `pick` на `reword` для нужного коммита
```

### Если уже запушили в develop

**Не переписывайте историю публичных веток!** Просто продолжайте писать
правильно. CI учитывает только коммиты с момента последнего тега —
один неправильный коммит не сломает релиз, просто changelog будет менее
информативным.

---

## 11. Чек-лист перед push

- [ ] Заголовок не длиннее 72 символов
- [ ] Есть тип (`feat`, `fix`, `perf` и т.д.)
- [ ] Есть scope (имя сервиса из `services/`)
- [ ] Нет точки в конце заголовка
- [ ] Если ломающее изменение — добавлен `!` или `BREAKING CHANGE`
- [ ] Тело объясняет **почему**, а не только **что**

---

## 12. Быстрый старт (шпаргалка)

```text
feat(scope): добавил фичу          → minor bump
fix(scope): починил баг            → patch bump
perf(scope): ускорил               → patch bump
refactor(scope): отрефакторил      → patch bump
feat(scope)!: ломающее изменение   → major bump

docs(scope): документация          → не влияет
chore(scope): зависимости          → не влияет
test(scope): тесты                 → не влияет
ci: pipeline                       → не влияет
```

---

*Вопросы? Смотрите полную спецификацию: https://www.conventionalcommits.org/ru/v1.0.0/*
```

---
