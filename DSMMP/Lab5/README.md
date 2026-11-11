# Лабораторная работа № 5 — учёт личных финансов

Откройте `finance-lab` в Android Studio и запустите конфигурацию `app`. Для сборки: `./gradlew :app:assembleDebug :app:testDebugUnitTest`.

Денежные суммы хранятся целым числом копеек. Лимит относится к календарному месяцу, предупреждение возникает при достижении 80 %, превышение — выше 100 %. Порог хранится вместе с лимитом. При удалении счёта удаляются его операции; при удалении категории — её операции и лимит.

```mermaid
erDiagram
    ACCOUNTS ||--o{ OPERATIONS : contains
    CATEGORIES ||--o{ OPERATIONS : classifies
    CATEGORIES ||--o| BUDGETS : limits
    ACCOUNTS { long id PK
        string name }
    CATEGORIES { long id PK
        string name }
    OPERATIONS { long id PK
        long accountId FK
        long categoryId FK
        long amount
        long createdAt
        string note }
    BUDGETS { long id PK
        long categoryId FK
        long amount
        int warningPercent }
```

WorkManager планирует напоминания с приблизительным временем выполнения, которое зависит от ограничений Android. Повторяющийся платёж напоминает каждые 30 дней. Разрешение на уведомления необязательно для работы каталога.
