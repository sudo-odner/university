package com.example.lab3.domain.model

enum class Category(
    val title: String,
    val iconName: String
) {
    SUPERMARKETS("Супермаркеты", "shopping_cart"),
    RESTAURANTS("Рестораны и кафе", "restaurant"),
    TRANSPORT("Транспорт и авто", "directions_car"),
    HEALTH("Здоровье и аптеки", "medical_services"),
    ENTERTAINMENT("Развлечения и отдых", "sports_esports"),
    HOUSING("Коммунальные услуги", "home"),
    SALARY("Зарплата и аванс", "payments"),
    INVESTMENTS("Инвестиции", "trending_up"),
    OTHER("Прочее", "more_horiz")
}
