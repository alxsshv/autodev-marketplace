- Частично настроено окружение в docker-compose.yaml;
- API gateway временно закомментирован в файле docker compose, до соответствующего этапа выполнения работ;
- Работы выполняются без настройки TLS и HTTPS до определённого этапа;
- Порт 8080 на хостовой машине занять другими процессами, для работы используется 8081.

## Документация

- **Консолидация сервисов (MVP: 8-10 сервисов):**docs/architecture/service-consolidation.md
- **Стратегия тестирования:** docs/architecture/testing-strategy.md
- **Eventual Consistency и Saga Pattern:** docs/architecture/eventual-consistency.md
- **Системный обзор (C4 Model):** docs/architecture/system-overview.md
- **Диаграмма развёртывания (Kubernetes):** docs/architecture/deployment-diagram.md
- **Дорожная карта:** docs/architecture/roadmap.md
- **Политика взаимодействия:** docs/architecture/communication-policy.md
- **План отказоустойчивости:** docs/architecture/resilience-plan.md
- **Модель данных:** docs/architecture/data-model.md
- **Glossary:** docs/architecture/glossary.md
- **Диаграмма компонентов:** docs/architecture/component-diagram.md
- **Diagrams:** docs/architecture/sequence-diagrams/
- **Service Catalog:** docs/architecture/service-catalog/
- **API Specification:** docs/architecture/api-specification/
- **ADR:** docs/architecture/architecture-decision-records/