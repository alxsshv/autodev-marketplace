# Настройка локальной среды разработки

## Требования

### Основные инструменты
- **Java 17** (OpenJDK или Oracle JDK)
- **Gradle 8.x**
- **Docker 20.x**
- **Docker Compose 2.x**
- **Git 2.30+**
- **IntelliJ IDEA 2023.x** (рекомендуется) или другой современный Java IDE

### Инфраструктура
- **PostgreSQL 15** - основная реляционная база данных
- **Redis 7** - кэширование и хранение сессий
- **Apache Kafka 7.3.2** - асинхронная коммуникация между сервисами
- **Zookeeper 7.3.2** - координация Kafka кластера
- **Elasticsearch 8.13.0** - полнотекстовый поиск
- **MinIO 2023.05.14** - объектное хранилище для файлов
- **Keycloak 21.1.1** - централизованная аутентификация и авторизация
- **Prometheus 2.48.0** - сбор и хранение метрик
- **Grafana 12.4.0** - визуализация метрик и логов
- **Loki 2.9.2** - централизованное логирование
- **Tempo 2.4.0** - трассировка распределённых запросов
- **Alloy 1.12.2** - агент для отправки метрик, логов и трассировок

## Установка и настройка

### Установка Java и Gradle

#### Linux (Ubuntu/Debian)
```bash
# Установка OpenJDK 17
sudo apt update
sudo apt install openjdk-17-jdk

# Проверка версии Java
java -version
javac -version

# Установка Gradle (используем wrapper)
# Gradle wrapper уже включён в проект
./gradlew --version
```

#### macOS
```bash
# Установка OpenJDK 17 через Homebrew
brew install openjdk@17

# Добавление в PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Проверка версии Java
java -version
javac -version

# Gradle wrapper уже включён в проект
./gradlew --version
```

#### Windows
1. Скачайте и установите OpenJDK 17 с [Adoptium](https://adoptium.net/)
2. Добавьте переменную окружения `JAVA_HOME` с путём к установленному JDK
3. Добавьте `%JAVA_HOME%\bin` в переменную `PATH`
4. Проверьте установку:
```cmd
java -version
javac -version
```
5. Gradle wrapper уже включён в проект

### Установка Docker и Docker Compose

#### Linux
```bash
# Установка Docker
sudo apt update
sudo apt install apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io

# Добавление пользователя в группу docker
sudo usermod -aG docker $USER

# Установка Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Проверка установки
docker --version
docker-compose --version
```

#### macOS
1. Скачайте и установите Docker Desktop с [официального сайта](https://www.docker.com/products/docker-desktop/)
2. Docker Compose включён в Docker Desktop
3. Проверьте установку:
```bash
docker --version
docker-compose --version
```

#### Windows
1. Скачайте и установите Docker Desktop с [официального сайта](https://www.docker.com/products/docker-desktop/)
2. Docker Compose включён в Docker Desktop
3. Проверьте установку:
```cmd
docker --version
docker-compose --version
```

## Настройка инфраструктуры

### Запуск инфраструктуры через Docker Compose

Инфраструктура для разработки описана в файле `docker-compose.yaml` в корне проекта.

```bash
# Клонирование репозитория
git clone http://192.168.0.162/autodev-marketplace.git
cd autodev-marketplace

# Запуск инфраструктуры
docker-compose up -d

# Проверка состояния контейнеров
docker-compose ps
```

### Доступ к сервисам

| Сервис | URL | Docker | Host | Доступ |
|--------|-----|--------|------|--------|
| API Gateway | http://localhost:8081 | 8080 | 8081 | Все |
| Keycloak | http://localhost:8090 | 8080 | 8090 | Администратор |
| Kafka UI | http://localhost:8082 | 8080 | 8082 | Все |
| Prometheus | http://localhost:9091 | 9090 | 9091 | Мониторинг |
| Grafana | http://localhost:3000 | 3000 | 3000 | Мониторинг |
| Loki | http://localhost:3100 | 3100 | 3100 | Логирование |
| Tempo | http://localhost:3200 | 3200 | 3200 | Трассировка |
| Alloy | http://localhost:9080 | 9080 | 9080 | Агент мониторинга |
| Alloy (OTLP gRPC) | - | 4317 | 4317 | Агент мониторинга |
| Alloy (OTLP HTTP) | - | 4318 | 4318 | Агент мониторинга |
| MinIO | http://localhost:9000 | 9000 | 9000 | Администратор |
| MinIO Console | http://localhost:9001 | 9001 | 9001 | Администратор |
| MinIO Metrics | - | 9090 | 9095 | Мониторинг |
| PostgreSQL | - | 5432 | 5435 | Подключение |
| Redis | - | 6379 | 6379 | Подключение |
| Elasticsearch | http://localhost:9200 | 9200 | 9200 | Подключение |
| Consul | http://localhost:8500 | 8500 | 8500 | Администратор |
| Consul DNS | - | 8600/udp | 8600/udp | DNS |
| Keycloak DB | - | 5432 | 5435 | Подключение |
| User Service | - | 8080 | 8083 (план) | Все |
| Catalog Service | - | 8080 | 8084 (план) | Все |
| Pricing & Inventory Service | - | 8080 | 8085 (план) | Все |
| Search Service | - | 8080 | 8086 (план) | Все |
| Order Service | - | 8080 | 8087 (план) | Все |
| Notification Service | - | 8080 | 8088 (план) | Все |
| Review Service | - | 8080 | 8089 (план) | Все |
| Recommendation Service | - | 8080 | 8091 (план) | Все |
| Admin Service | - | 8080 | 8092 (план) | Администратор |
| Analytics Service | - | 8080 | 8093 (план) | Все |
| keycloak-database-exporter | - | 9187 | 9188 | Мониторинг |
| redis-exporter | - | 9121 | 9122 | Мониторинг |
| elasticsearch-exporter | - | 9114 | 9114 | Мониторинг |

### Настройка Keycloak

1. Перейдите на http://localhost:8081
2. Войдите с учётными данными администратора:
   - Username: `admin`
   - Password: `admin`
3. Создайте новый realm `autodev`
4. В настройках realm:
   - Установите `Access Token Lifespan` в 12 часов
   - Включите `SSL Required` в `none` для разработки
5. Создайте клиент `autodev-frontend` с настройками:
   - Access Type: `public`
   - Valid Redirect URIs: `http://localhost:3000/*`
   - Web Origins: `+`
6. Создайте клиент `api-gateway` с настройками:
   - Access Type: `bearer-only`
7. Создайте клиент `service-client` с настройками:
   - Access Type: `confidential`
   - Service Accounts Enabled: `ON`
8. Создайте роли: `BUYER`, `SELLER`, `MODERATOR`, `ADMIN`
9. Назначьте роль `ADMIN` сервисному аккаунту клиента `service-client`

## Настройка IDE

### IntelliJ IDEA

1. Откройте проект через `File -> Open`
2. Выберите корневую папку проекта
3. IntelliJ автоматически распознает Gradle проект
4. Дождитесь синхронизации зависимостей
5. Настройте SDK:
   - File -> Project Structure -> Project
   - Установите Project SDK: `17`
   - Установите Project language level: `17`
6. Настройте Gradle:
   - File -> Settings -> Build -> Build Tools -> Gradle
   - Установите Gradle JVM: `17`

### Рекомендуемые плагины
- **Lombok** - для работы с аннотациями Lombok
- **Spring Boot** - для подсказок Spring Boot
- **Maven Helper** - для анализа зависимостей
- **Rainbow Brackets** - для удобства чтения кода
- **SonarLint** - для анализа качества кода
- **GitToolBox** - для улучшения работы с Git

## Запуск сервисов

### Запуск через Gradle

Каждый сервис можно запустить отдельно через Gradle wrapper.

```bash
# Запуск API Gateway
./gradlew :services:api-gateway:bootRun

# Запуск User Service (когда будет создан)
# ./gradlew :services:user-service:bootRun

# Запуск Catalog Service (когда будет создан)
# ./gradlew :services:catalog-service:bootRun
```

### Запуск через IDE

1. Найдите главный класс приложения (с аннотацией `@SpringBootApplication`)
2. Нажмите на зелёную стрелку рядом с методом `main` и выберите "Run"
3. Приложение запустится на указанном в `application.yml` порту

## Переменные окружения

Для локальной разработки можно использовать файл `.env` в корне проекта.

```env
# База данных
DB_URL=jdbc:postgresql://localhost:5432/autodev
DB_USERNAME=autodev
DB_PASSWORD=autodev

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/autodev

# JWT
JWT_SECRET=mySecretKey
JWT_EXPIRATION=86400

# MinIO
MINIO_URL=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=autodev

# Elasticsearch
ELASTICSEARCH_HOST=localhost
ELASTICSEARCH_PORT=9200
```

## Проверка установки

```bash
# Проверка Java
java -version
# Ожидаемый результат: openjdk version "17.x.x"

# Проверка Gradle
./gradlew --version
# Ожидаемый результат: Gradle 8.x

# Проверка Docker
docker --version
# Ожидаемый результат: Docker version 20.x

# Проверка Docker Compose
docker-compose --version
# Ожидаемый результат: docker-compose version 2.x

# Проверка запуска инфраструктуры
docker-compose ps
# Ожидаемый результат: все сервисы в статусе "Up"

# Проверка API Gateway
curl -v http://localhost:8081/actuator/health
# Ожидаемый результат: HTTP/1.1 200 OK, {"status":"UP"}
```

## Устранение неполадок

### Проблемы с Docker
- **Ошибка: Cannot connect to the Docker daemon**
  - Решение: Убедитесь, что Docker запущен и ваш пользователь в группе docker
  ```bash
  sudo systemctl start docker
  sudo usermod -aG docker $USER
  # Перезагрузите сессию
  ```

- **Ошибка: Port already allocated**
  - Решение: Остановите процессы, использующие порты
  ```bash
  sudo lsof -i :8080
  sudo kill -9 <PID>
  ```

### Проблемы с Keycloak
- **Ошибка: Invalid parameter: redirect_uri**
  - Решение: Проверьте настройки клиента в Keycloak, Valid Redirect URIs должны включать ваш frontend URL

- **Ошибка: Login failed**
  - Решение: Проверьте учётные данные, по умолчанию admin/admin

### Проблемы с базой данных
- **Ошибка: Connection refused**
  - Решение: Убедитесь, что PostgreSQL запущен через docker-compose
  ```bash
  docker-compose ps | grep postgres
  docker-compose logs postgres
  ```

### Проблемы с зависимостями
- **Ошибка: Could not resolve dependencies**
  - Решение: Проверьте подключение к интернету, попробуйте очистить кеш Gradle
  ```bash
  ./gradlew --refresh-dependencies
  ```