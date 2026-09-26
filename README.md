# gucci-goblin

Фоновое приложение на Java 21 и Spring Boot 4.0.2. Раз в минуту получает состав клана
и текущую войну из Clash of Clans API, сохраняет данные и историю входов/выходов.
HTTP-сервер и интерактивная командная оболочка не используются.

## Локальный запуск

Нужны JDK 21 или новее, доступный PostgreSQL с созданной базой и токен Clash of Clans API.
Устанавливать Maven отдельно не обязательно: Wrapper использует Maven 3.9.14.
При первом запуске Wrapper скачивает Maven и необходимые зависимости.

В PowerShell, из корня проекта:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/gucci"
$env:SPRING_DATASOURCE_USERNAME = "gucci"
$env:SPRING_DATASOURCE_PASSWORD = "YOUR_DATABASE_PASSWORD"
$env:COC_APITOKEN = "YOUR_API_TOKEN"
$env:COC_CLANTAG = "#YOUR_CLAN_TAG"

.\mvnw.cmd spring-boot:run
```

`COC_APITOKEN` и `COC_CLANTAG` переопределяют существующие свойства `coc.apiToken`
и `coc.clanTag`. Имена переменных приведены для текущего формата конфигурации.
В IDE задайте те же переменные окружения в конфигурации запуска `Application`.

Синхронизация выполняется на нулевой секунде каждой минуты. Отдельного запроса
при старте нет: после запуска нужно дождаться ближайшей границы минуты.
Текущая конфигурация Hibernate использует `ddl-auto: update`.

## Сборка и тесты

```powershell
.\mvnw.cmd verify
```

Для Linux/macOS используйте `./mvnw verify`.
Если Maven уже установлен, можно выполнить `mvn verify`.
Готовое приложение: `target/gucci-goblin-1.2.0.jar`.

```powershell
java -jar target/gucci-goblin-1.2.0.jar
```

Тесты используют H2 в памяти, настоящий JPA и фиктивные ответы API.
PostgreSQL, контейнеры и действующий токен для тестов не нужны.
JSON в `src/test/resources/fixtures` содержит вымышленные данные.

## Данные и синхронизация

- `member`: текущий состав, накопленные донаты и даты наблюдаемых изменений.
- `player`: неизменяемые события `PlayerEvent` с типами `JOINED` и `LEFT`.
  Существующее имя таблицы `players` сохранено.
- `war`: войны, участники обеих сторон и атаки.
- `config/Scheduler`: запуск синхронизации каждую минуту.

Повышение ТХ обновляет `lastTownHallUpgrade`, но не считается активностью
и само по себе не меняет `lastActivity`.

Сейчас один вызов планировщика объединяет состав, события и войну общей
транзакцией. Ошибка любого этапа откатывает весь цикл.
Война обновляется только по доступному ответу API: пропущенный финальный
ответ автоматически не восстанавливается.

## Логи

Логи выводятся в консоль и в `logs/gucci.log`.
Каталог можно переопределить переменной окружения `LOG_DIR`.

Архивы создаются ежедневно и при достижении 10 MB, сжимаются в gzip.
Хранятся архивы за последние 30 дней с общим ограничением 250 MB;
активный файл учитывается отдельно. Очистка архивов также выполняется при старте.
Каталог `logs/` исключён из Git.

Полезные ссылки: [Maven Wrapper](https://maven.apache.org/tools/wrapper/),
[настройки через переменные окружения Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html).
