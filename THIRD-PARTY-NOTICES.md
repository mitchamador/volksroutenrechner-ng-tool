# Уведомления о сторонних компонентах

Код этого проекта распространяется под лицензией MIT (см. `LICENSE`). Ниже перечислены
сторонние библиотеки, используемые в сборке (в т.ч. включаемые в собранный jar), и их
собственные лицензии. Ни одна из них не накладывает копилефт-требований на код проекта.

## Основные зависимости (всегда в сборке)

| Библиотека | Лицензия | Ссылка |
|---|---|---|
| [Javalin](https://javalin.io/) (и Jetty, используемый им как embedded-сервер) | Apache License 2.0 / Eclipse Public License 2.0 (Jetty — на выбор) | https://github.com/javalin/javalin, https://github.com/jetty/jetty.project |
| [Jackson Databind](https://github.com/FasterXML/jackson-databind) | Apache License 2.0 | https://github.com/FasterXML/jackson-databind |
| [SLF4J](https://www.slf4j.org/) (api + simple) | MIT License | https://www.slf4j.org/license.html |
| [H2 Database](https://h2database.com/) | MPL 2.0 или EPL 1.0 (на выбор) | https://h2database.com/html/license.html |
| [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) | Apache License 2.0 | https://commons.apache.org/proper/commons-cli/ |
| [Apache Commons IO](https://commons.apache.org/proper/commons-io/) | Apache License 2.0 | https://commons.apache.org/proper/commons-io/ |
| [Bootstrap](https://getbootstrap.com/) (через WebJars) | MIT License | https://github.com/twbs/bootstrap/blob/main/LICENSE |
| [Font Awesome Free](https://fontawesome.com/) (через WebJars) | Код — MIT License, шрифты — SIL OFL 1.1, иконки — CC BY 4.0 | https://fontawesome.com/license/free |

Font Awesome Free — единственный компонент с требованием атрибуции (иконки под CC BY 4.0):
при распространении проекта эта атрибуция считается выполненной ссылкой на
https://fontawesome.com в этом файле.

## Опциональные зависимости (только при сборке с профилем `sqlite`)

| Библиотека | Лицензия | Ссылка |
|---|---|---|
| [SQLite JDBC Driver (xerial)](https://github.com/xerial/sqlite-jdbc) | Apache License 2.0 | https://github.com/xerial/sqlite-jdbc/blob/master/LICENSE |

## Про H2 и MPL/EPL отдельно

H2 используется как обычная Maven-зависимость (embedded-режим), без модификации его
исходного кода — только в этом случае MPL 2.0/EPL 1.0 не распространяются на остальной
код проекта ("Larger Work"). Если в какой-то момент исходники H2 будут модифицированы и
распространены в изменённом виде — на изменённые файлы H2 (и только на них) нужно будет
сохранить MPL/EPL.

## Полный список транзитивных зависимостей

Список выше покрывает основные (прямые) зависимости. Полный список транзитивных
зависимостей (Jetty io/http/server/util модули, Kotlin stdlib и т.д.) можно получить
локально:

```bash
mvn dependency:tree
```
