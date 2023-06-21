# Поисковой движок

Итоговый проект курса Java-разработчик с нуля.

## Технологии

- java
	- spring boot
	- hibernate, jpa
	- junit
- postgresql

## Развертывание
Для создания пользователя и базы данных нужно выполнить скрипт create tables.sql в корне проекта или запустить файл ./docker_db/up docker compose.bat(docker, windows).
Для скачивания некоторых бибилотек нужно добавить\отредактировать файл settings.xml в дерикториях

В Windows он располагается в директории

C:/Users/<Имя вашего пользователя>/.m2

В Linux — в директории

/home/<Имя вашего пользователя>/.m2

В macOs — по адресу

/Users/<Имя вашего пользователя>/.m2


Чтобы он имел следующее содержание:

```sql
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
        <id>skillbox-gitlab</id>
            <configuration>
                <httpHeaders>
                    <property>
                        <name>Private-Token</name
                        <value>wtb5axJDFX9Vm_W1Lexg</value>
                    </property>
                </httpHeaders>
            </configuration>
        </server>
    </servers>
</settings>
```

