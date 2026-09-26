# Предлагаемые доработки синхронизации

Это инструкция по изменениям, которые обсуждались в ревью. Разделение транзакций,
тайм-ауты и учёт пропущенного финала войны **ещё не внесены в рабочие классы**.
Примеры рассчитаны на текущий проект после переименования `Player` в `PlayerEvent`.

## 1. Состав и события — одна транзакция, война — отдельная

Состав и события связаны: нельзя сохранить уход участника из состава и потерять
соответствующее событие `LEFT`. Поэтому они должны фиксироваться вместе.
Война сохраняется независимо, чтобы ошибка её API не откатывала состав.

Получение JSON выполняется до транзакции. Планировщик вызывает публичные методы
отдельных Spring-сервисов; на самом планировщике `@Transactional` не остаётся.

### MemberService.java

В `src/main/java/com/chapeullah/guccigoblin/member/MemberService.java`:

1. Удалить поле `private final Client client` и его импорт.
2. Изменить сигнатуру на `public MemberSyncResult syncMembers(MembersResponse response)`.
3. Вместо `mapFrom(client.getMembers())` использовать `mapFrom(response)`.
4. Остальной алгоритм и `@Transactional` сохранить. При вызове из нового сервиса
   этот метод присоединится к его транзакции.

### Новый MemberSyncService.java

Создать рядом с `MemberService`:

```java
package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.member.dto.MembersResponse;
import com.chapeullah.guccigoblin.member.service.MemberService;
import com.chapeullah.guccigoblin.player.PlayerEventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSyncService {
    private final MemberService memberService;
    private final PlayerEventService playerEventService;

    @Transactional
    public void sync(MembersResponse response) {
        var result = memberService.syncMembers(response);
        playerEventService.syncEvents(result);
    }
}
```

### WarService.java

В `src/main/java/com/chapeullah/guccigoblin/war/WarService.java`:

1. Удалить поле `private final Client client` и его импорт.
2. Изменить сигнатуру на `public Optional<War> syncWar(WarResponse response)`.
3. Удалить локальную строку `WarResponse response = client.getCurrentWar()`.
4. Сохранить `@Transactional` и остальное сохранение войны.

### Scheduler.java

Заменить содержимое `src/main/java/com/chapeullah/guccigoblin/config/Scheduler.java`:

```java
package com.chapeullah.guccigoblin.config;

import com.chapeullah.guccigoblin.client.Client;
import com.chapeullah.guccigoblin.member.service.MemberSyncService;
import com.chapeullah.guccigoblin.war.WarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {
    private final Client client;
    private final MemberSyncService memberSyncService;
    private final WarService warService;

    @Scheduled(cron = "0 * * * * *")
    public void sync() {
        runStep("Members", () -> memberSyncService.sync(client.getMembers()));
        runStep("War", () -> warService.syncWar(client.getCurrentWar()));
    }

    private void runStep(String name, Runnable action) {
        try {
            action.run();
        } catch (RestClientResponseException e) {
            log.warn("{} sync: API returned HTTP {}", name, e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.warn("{} sync: network error or timeout", name, e);
        } catch (RuntimeException e) {
            log.error("{} sync failed", name, e);
        }
    }
}
```

`client.getMembers()` вычисляется до входа в `MemberSyncService.sync()`;
транзакция начинается при обращении к Spring-прокси сервиса. Для войны порядок такой же.
Перехват ошибок расположен снаружи сервисов, поэтому ошибка сохранения сначала
вызывает откат, а затем попадает в журнал. Второй этап всё равно будет вызван.

Быстрых повторов запросов в этом примере нет: следующую попытку делает планировщик.
Ошибки API нельзя превращать в пустой состав: это создало бы ложные события ухода.

После переноса границ нужно обновить тесты: ошибки состава/событий откатывают их
общую транзакцию; ошибка войны сохраняет уже зафиксированный состав и события.
Тесты отката должны вызывать сервис напрямую, поскольку планировщик теперь
перехватывает исключения. Из `WarServicePersistenceTest` можно убрать `StubClient`
и передавать тестовый `WarResponse` прямо в настоящий `WarService`.

## 2. Тайм-ауты HTTP

В `src/main/java/com/chapeullah/guccigoblin/config/HttpConfig.java` заменить класс:

```java
package com.chapeullah.guccigoblin.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpConfig {
    @Bean
    public RestClient restClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(15));

        return RestClient.builder()
                .baseUrl("https://api.clashofclans.com/v1")
                .requestFactory(factory)
                .build();
    }
}
```

5 секунд на установление нового соединения и 15 секунд для ожидания ответа —
предлагаемые начальные значения, а не требования Clash of Clans API.
Они не являются общим лимитом длительности всей синхронизации, которая ещё
включает второй запрос и работу с БД.

`connectTimeout` относится к созданию нового соединения
([Java 21 HttpClient.Builder](https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.Builder.html#connectTimeout(java.time.Duration))).
`setReadTimeout(Duration)` настраивается через фабрику запросов
([Spring JdkClientHttpRequestFactory](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/client/JdkClientHttpRequestFactory.html#setReadTimeout(java.time.Duration))).
Документация проверена 26.09.2026. В первом разделе показано, где перехватывать
сетевые ошибки и ответы с ошибочным HTTP-статусом.

## 3. Пропущенный финальный ответ войны

Нужно различать последнее состояние, полученное из API, и полноту наших данных.
Если последнее полученное состояние — `inWar`, а время окончания прошло,
это ещё не подтверждает, что сохранены все финальные атаки и итоговый счёт.

### Новый WarSnapshotStatus.java

Создать `src/main/java/com/chapeullah/guccigoblin/war/model/WarSnapshotStatus.java`:

```java
package com.chapeullah.guccigoblin.war.model;

public enum WarSnapshotStatus {
    TRACKING,
    COMPLETE,
    FINAL_MISSING
}
```

- `TRACKING`: финальный снимок ещё ожидается.
- `COMPLETE`: получен и сохранён ответ с `state = warEnded`.
- `FINAL_MISSING`: война должна была закончиться, но финального снимка пока нет.
  Если он появится позже, состояние можно исправить на `COMPLETE`.

### War.java

В `src/main/java/com/chapeullah/guccigoblin/war/model/War.java` добавить поля:

```java
@Column(name = "last_synced_at")
private Instant lastSyncedAt;

@Enumerated(EnumType.STRING)
@Column(name = "snapshot_status", nullable = false)
private WarSnapshotStatus snapshotStatus = WarSnapshotStatus.TRACKING;
```

`lastSyncedAt` хранит время последнего успешно сохранённого ответа именно этой войны.
Существующее `state` продолжает хранить состояние из API.
При `notInWar` у старой войны нельзя обновлять `lastSyncedAt`: нового её снимка не было.

### WarRepository.java

Добавить импорты `java.util.List` и `WarSnapshotStatus`, затем метод:

```java
List<War> findAllByClanTagAndSnapshotStatusAndEndsAtBefore(
        String clanTag,
        WarSnapshotStatus snapshotStatus,
        Instant before);
```

Поиск ограничен настроенным кланом, чтобы не затрагивать историю другого клана.

### Новый TimeConfig.java

Создать `src/main/java/com/chapeullah/guccigoblin/config/TimeConfig.java`:

```java
package com.chapeullah.guccigoblin.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
```

Отдельные часы позволяют проверить момент окончания войны без ожидания
реальных минут: в тесте подставляется `Clock.fixed(...)`.

### WarService.java

Этот вариант применяется после разделения транзакций из первого раздела.
Добавить `@Slf4j`, поле `private final Clock clock`, импорты `Clock`, `Duration`,
`WarSnapshotStatus`, `Value` и поле:

```java
@Value("${coc.clanTag}")
private String trackedClanTag;
```

Заменить публичный `syncWar` и добавить метод отметки просроченных войн:

```java
@Transactional
public Optional<War> syncWar(WarResponse response) {
    if (response == null) {
        throw new IllegalStateException("Empty current war response");
    }

    Instant now = clock.instant();
    if ("notInWar".equals(response.state())) {
        markMissingFinalSnapshots(now);
        return Optional.empty();
    }

    War war = saveOrUpdateWar(response);
    saveParticipants(war, response.clan());
    saveParticipants(war, response.opponent());
    saveAttacks(war, response);

    war.setLastSyncedAt(now);
    if ("warEnded".equals(response.state())) {
        war.setSnapshotStatus(WarSnapshotStatus.COMPLETE);
    } else if (war.getSnapshotStatus() != WarSnapshotStatus.COMPLETE) {
        war.setSnapshotStatus(war.getEndsAt().isBefore(now.minus(Duration.ofMinutes(2)))
                ? WarSnapshotStatus.FINAL_MISSING
                : WarSnapshotStatus.TRACKING);
    }

    markMissingFinalSnapshots(now);
    return Optional.of(war);
}

private void markMissingFinalSnapshots(Instant now) {
    var overdue = warRepository.findAllByClanTagAndSnapshotStatusAndEndsAtBefore(
            trackedClanTag,
            WarSnapshotStatus.TRACKING,
            now.minus(Duration.ofMinutes(2)));

    for (War war : overdue) {
        war.setSnapshotStatus(WarSnapshotStatus.FINAL_MISSING);
        log.warn("War {}: final snapshot is missing, ended at {}", war.getId(), war.getEndsAt());
    }
}
```

Двухминутный запас здесь — выбранная задержка перед отметкой неполноты,
а не документированная задержка API. Его можно вынести в настройку.
Загруженные и сохранённые сущности остаются управляемыми внутри транзакции:
изменения полей запишутся при её фиксации. Если сохранение атак упадёт,
откатятся также `lastSyncedAt` и `snapshotStatus`.

Проверка вызывается и перед возвратом из `notInWar`, и после сохранения текущей
войны. Поэтому появление новой войны тоже позволяет отметить пропущенный финал старой.
Во время полного отсутствия успешных ответов API этот метод не выполняется;
устаревание данных в таком случае видно по `lastSyncedAt`.

В последующих отчётах учитывать `snapshotStatus`: `FINAL_MISSING` отображать как
«время войны закончилось, итог не получен». Нельзя показывать старые звёзды и атаки
как подтверждённый финал или считать такое состояние продолжающейся войной.
Эта доработка обнаруживает неполные данные; сама по себе она не восстанавливает
атаки, которых приложение не получило.

### Что проверить тестами после реализации

1. `preparation -> inWar -> warEnded`: одна война, итоговый `COMPLETE`.
2. `inWar -> время после endsAt с запасом -> notInWar`: `FINAL_MISSING`, история сохранена.
3. Появилась новая война: просроченная старая отмечается отдельно.
4. После `FINAL_MISSING` пришёл финальный снимок той же войны: `COMPLETE`, результаты обновлены.
5. `notInWar` до истечения срока не помечает войну просроченной.
6. Ошибка API или откат сохранения не обновляет время последнего успешного снимка.

Работа с миграциями и существующей базой в эту инструкцию не входит.
