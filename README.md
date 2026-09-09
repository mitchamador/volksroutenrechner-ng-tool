# volksroutenrechner-ng-tool

Веб-инструмент для работы с данными маршрутного компьютера: журнал поездок (trip C/A/B,
разгоны) хранится в базе и доступен через браузер, плюс отдельные утилиты для работы
с EEPROM — импорт/экспорт журнала и конвертация дампа EEPROM микроконтроллера в C-код
прошивки.

## Возможности

- **Журнал** — таблицы Trip C / Trip A / Trip B / Accel с фильтром по периоду,
  постраничной навигацией (10/25/50/100 записей) и удалением записей.
- **Импорт** бинарного EEPROM-дампа журнала (`.bin`) в базу — с дедупликацией
  (повторный импорт того же файла не создаёт дублей).
- **Экспорт** журнала обратно в `.bin` или в C-исходник (`__EEDATA(...)`), с выбором
  размера (768 / 2048, см. `JournalSize`).
- **Конвертация EEPROM микроконтроллера** (`.hex`/`.eep`) в C-код — отдельная функция,
  не связанная с журналом, с автоопределением типа контроллера (pic16f876a / pic16f193x /
  pic18f252 / atmega328p) или явным выбором.
- Хранилище — **H2** (по умолчанию, чистая Java, работает "из коробки" на любой
  платформе) или **SQLite** (опционально, см. ниже).

## Требования для сборки

- JDK 11+
- Maven (нужен доступ к Maven Central — зависимости не вендорятся)

## Сборка

```bash
mvn clean package
```

По умолчанию собирается jar на **H2** — без SQLite-зависимости вообще (самый компактный
вариант). Результат — `target/volksroutenrechner-ng-tool-1.0-SNAPSHOT.jar`, готовый к запуску
(`java -jar ...`, main-class уже прописан).

### Опционально: поддержка SQLite

SQLite не входит в сборку по умолчанию — она собирается ~24 МБ нативных библиотек под
все платформы разом. Чтобы вернуть поддержку SQLite, используется профиль:

```bash
mvn clean package -P sqlite                  # все платформы (портативно, тяжело)
mvn clean package -P sqlite,windows-x64      # только Windows/x86_64 (легко)
mvn clean package -P sqlite,linux-x64        # только Linux/x86_64
mvn clean package -P sqlite,mac-x64          # только macOS/x86_64
mvn clean package -P sqlite,mac-arm64        # только macOS/arm64 (Apple Silicon)
```

Без указания платформенного профиля вместе с `sqlite` в jar попадут нативные библиотеки
под все поддерживаемые платформы (портативно, но тяжело — эти самые ~24 МБ).

## Запуск

```bash
java -jar target/volksroutenrechner-ng-tool-1.0-SNAPSHOT.jar [опции]
```

| Опция | По умолчанию | Описание |
|---|---|---|
| `-d`, `--db <path>` | `./journal` | путь к файлу БД (без расширения — его добавляет сам движок) |
| `-p`, `--port <port>` | `7000` | порт |
| `--host <host>` | `localhost` | адрес для прослушивания |
| `-r`, `--root-path <path>` | (нет) | префикс пути, если инструмент работает не в корне сайта — например, за reverse proxy (`/tool`) |
| `-e`, `--engine <sqlite\|h2>` | `h2` | движок БД (`sqlite` доступен только если jar собран с профилем `sqlite`) |
| `-h`, `--help` | | справка |

Примеры:

```bash
# по умолчанию: H2, localhost:7000, без префикса пути
java -jar volksroutenrechner-ng-tool.jar

# другой порт и файл БД
java -jar volksroutenrechner-ng-tool.jar --db /var/data/journal --port 8181

# за reverse proxy на поддиректории
java -jar volksroutenrechner-ng-tool.jar --host 0.0.0.0 --port 8181 --root-path /tool
# -> открывать по http://<хост>:8181/tool/ (обязательно со слэшем в конце,
#    иначе относительные ссылки на статику/API резолвятся браузером неверно)

# SQLite вместо H2 (если jar собран с профилем sqlite)
java -jar volksroutenrechner-ng-tool.jar --engine sqlite --db /var/data/journal.db
```

## Веб-интерфейс

Две вкладки верхнего уровня:

- **Журнал** — период/пагинация/импорт EEPROM журнала (`.bin`) сверху, под ними —
  вложенные вкладки Trip C / Trip A / Trip B / Accel с таблицами записей.
- **EEPROM** — две независимые формы:
  - экспорт журнала (bin — скачивается файлом; C source — показывается текстом
    в блоке с кнопкой "Копировать", как обычно и используется — скопировать в прошивку);
  - конвертация EEPROM микроконтроллера в C (загрузка `.hex`/`.eep`, выбор контроллера,
    результат — тоже текстовый блок с копированием).

## REST API

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/api/trips/{type}?from=&to=&page=&pageSize=` | список записей trip `C`/`A`/`B`; ответ: `{since, records, total, page, pageSize}` |
| `DELETE` | `/api/trips/{type}/{id}` | удалить запись |
| `PUT` | `/api/trips/{type}/{id}` | изменить запись (тело — JSON `TripRecord`) |
| `GET` | `/api/accel?from=&to=&page=&pageSize=` | список разгонов; ответ: `{records, total, page, pageSize}` |
| `DELETE` | `/api/accel/{id}` | удалить запись |
| `PUT` | `/api/accel/{id}` | изменить запись (тело — JSON `AccelRecord`) |
| `POST` | `/api/import` | импорт журнала; `multipart/form-data`, поле `file`; ответ: `{imported, skipped}` |
| `GET` | `/api/export?format=bin\|c&size=768\|2048` | экспорт журнала (bin или C-исходник) |
| `POST` | `/api/mcu/convert` | конвертация EEPROM МК; `multipart/form-data`, поля `file`, `type` (`auto`/`pic16f876a`/`pic16f193x`/`pic18f252`/`atmega328p`), `format` (`text` — чистый текст, иначе JSON `{code}`) |

Пример через `curl`:

```bash
curl -F "file=@dump.hex" -F "type=auto" -F "format=text" \
  http://localhost:7000/api/mcu/convert
```

`from`/`to`/`time`/`startTime`/`since` везде — epoch millis.

## Особенности и ограничения

- Поле `since` (заголовок "since" в бинарном журнале — дата следующей ещё не
  завершённой записи) хранится в БД отдельно от самих записей и обновляется при
  импорте только если новое значение новее уже сохранённого — старые/повторные
  импорты не могут откатить его назад.
- При `--root-path` открывать инструмент нужно **со слэшем на конце**
  (`http://host:port/tool/`, а не `.../tool`) — иначе относительные ссылки на
  статику и API в браузере срезают последний сегмент пути.
- Java 11+, зависимости — Javalin (веб-сервер), Jackson (JSON), H2/SQLite (БД),
  Bootstrap + Font Awesome через WebJars (фронтенд), commons-cli (аргументы
  командной строки).

## Лицензия

MIT — см. [`LICENSE`](LICENSE). Лицензии сторонних зависимостей — см.
[`THIRD-PARTY-NOTICES.md`](THIRD-PARTY-NOTICES.md).

