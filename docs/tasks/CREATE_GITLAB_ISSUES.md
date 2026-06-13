# Инструкция по созданию GitLab Issues для Auth Service

## Подготовка

Для создания задач в GitLab вам потребуются:
- `GITLAB_TOKEN` - токен доступа с правами developer или更高
- `GITLAB_PROJECT_ID` - ID проекта (autodev-marketplace)
- `GITLAB_API_URL` - URL API GitLab (https://alxsshv.com/)

## Шаги по созданию задач

### Шаг 1: Создать ветку develop (если нет)

```bash
git checkout -b develop
git push -u origin develop
```

### Шаг 2: Создать задачи через GitLab API

Для каждой задачи выполните следующий curl запрос:

```bash
curl --request POST \
  --url "https://alxsshv.com/api/v4/projects/autodev-marketplace/issues" \
  --header "PRIVATE-TOKEN: glpat-s09Obl0w60ak6iB2oYcFcm86MQp1OjUH.01.0w12ad700" \
  --header "Content-Type: application/json" \
  --data '{
    "title": "Исправить сущность UserEntity для соответствия модели данных",
    "description": "# Задача 1: Исправить сущность UserEntity для соответствия модели данных\n\n## Название задачи\nИсправить сущность UserEntity для соответствия модели данных (только аутентификационные данные)\n\n## Описание\nСогласно `docs/architecture/data-model.md`, таблица `auth.users` должна содержать ТОЛЬКО аутентификационные данные:\n- id (первичный ключ)\n- keycloak_user_id (ID пользователя в Keycloak)\n- email (для аутентификации)\n- enabled (активен ли пользователь)\n- created_at (дата создания)\n\n**ТЕКУЩЕЕ СОСТОЯНИЕ:** В сущности User.java есть бизнес-данные (first_name, last_name, phone, role), которые должны быть удалены.\n\n**ПОЧЕМУ:** Согласно архитектуре AutoDev Marketplace, auth-service управляет ТОЛЬКО аутентификационными данными. Все бизнес-данные пользователя хранятся в `platform_service.users`.\n\n## Критерии выполнения\n\n- [ ] Удалены поля: first_name, last_name, phone, role из User.java\n- [ ] Оставлены поля: id, keycloak_user_id, email, enabled, created_at\n- [ ] Класс User.java соответствует миграции `03-06-2026-create-table-users.sql`\n- [ ] Удалены связи с другими сущностями (если есть)\n\n## Ссылки\n- [docs/architecture/data-model.md](../../docs/architecture/data-model.md) - раздел \"Схема: auth\"\n- [docs/architecture/glossary.md](../../docs/architecture/glossary.md) - соглашения по модели данных\n- [services/auth-service/src/main/resources/db/changelog/v1.0.0/03-06-2026-create-table-users.sql](../../services/auth-service/src/main/resources/db/changelog/v1.0.0/03-06-2026-create-table-users.sql)\n\n## Приоритет\nВысокий\n\n## Метки\nbackend, database, entity, auth-service",
    "labels": "backend,database,entity,auth-service",
    "priority": "4",
    "due_date": null,
    "milestone_id": null
  }'
```

### Шаг 3: Создать ветку для задачи

После создания задачи в GitLab, создайте ветку:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/#1-fix-user-entity
```

### Шаг 4: Выполнить задачу

Следуйте инструкциям в файле `docs/tasks/feature/#1-fix-user-entity.md`

### Шаг 5: Создать Pull Request

```bash
git add .
git commit -m "feat: исправлена сущность UserEntity для соответствия модели данных"
git push origin feature/#1-fix-user-entity
```

Создать PR через GitLab UI или CLI:
```bash
glab mr create --title "feat: исправлена сущность UserEntity" --description "#1-fix-user-entity"
```

## Автоматизированный скрипт

Вы можете использовать следующий Python скрипт для автоматического создания задач:

```python
import requests
import json
import os

GITLAB_TOKEN = os.environ.get("GITLAB_TOKEN")
GITLAB_API_URL = os.environ.get("GITLAB_API_URL", "https://alxsshv.com/")
GITLAB_PROJECT_ID = os.environ.get("GITLAB_PROJECT_ID", "autodev-marketplace")

def create_gitlab_issue(title, description, labels, priority=4):
    url = f"{GITLAB_API_URL}/api/v4/projects/{GITLAB_PROJECT_ID}/issues"
    headers = {
        "PRIVATE-TOKEN": GITLAB_TOKEN,
        "Content-Type": "application/json"
    }
    data = {
        "title": title,
        "description": description,
        "labels": labels,
        "priority": priority
    }
    
    response = requests.post(url, headers=headers, json=data)
    response.raise_for_status()
    return response.json()

# Пример использования
# task_1 = create_gitlab_issue(
#     "Исправить сущность UserEntity",
#     "# Описание задачи...",
#     "backend,database,entity,auth-service",
#     4
# )
# print(f"Issue #{task_1['iid']} created: {task_1['web_url']}")
```

## Создание веток через GitLab API

```bash
# Получить last commit ID из develop
curl --header "PRIVATE-TOKEN: $GITLAB_TOKEN" \
  "https://alxsshv.com/api/v4/projects/autodev-marketplace/repository/branches/develop"

# Создать ветку
curl --request POST \
  --header "PRIVATE-TOKEN: $GITLAB_TOKEN" \
  --header "Content-Type: application/json" \
  --url "https://alxsshv.com/api/v4/projects/autodev-marketplace/repository/branches" \
  --data '{
    "branch": "feature/#1-fix-user-entity",
    "ref": "develop"
  }'
```

## Список всех задач для создания

1. #1-fix-user-entity
2. #2-create-role-repository
3. #3-create-auth-dto
4. #4-create-keycloak-service
5. #5-create-token-service
6. #6-create-auth-service
7. #7-create-auth-controller
8. #8-create-user-service
9. #9-create-role-service
10. #10-create-role-controller
11. #11-setup-security
12. #12-write-tests

## Статусы GitLab

После выполнения задачи в PR:
- Пометить задачу как `done` в описании PR
- Указать `Closes #<номер>` в сообщении коммита
- При merge в develop задача автоматически закроется

## Пример сообщения коммита

```
feat: исправлена сущность UserEntity для соответствия модели данных

- Удалены бизнес-данные (first_name, last_name, phone, role)
- Оставлены только аутентификационные данные (id, keycloak_user_id, email, enabled, created_at)
- Класс User.java теперь соответствует миграции 03-06-2026-create-table-users.sql

Closes #1
```

## Тегирование релизов

После выполнения всех задач можно создать тег:

```bash
git tag -a v1.0.0-auth-service -m "Auth Service MVP"
git push origin v1.0.0-auth-service
```
