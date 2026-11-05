"""
Скрипт сборки Jupyter Notebook: Lab2_Variant8_Recruitment.ipynb
"""

import json
import uuid

cells = []

def add_md(lines):
    cells.append({
        "cell_type": "markdown",
        "id": str(uuid.uuid4())[:8],
        "metadata": {},
        "source": [line + "\n" for line in lines]
    })

def add_code(lines):
    cells.append({
        "cell_type": "code",
        "id": str(uuid.uuid4())[:8],
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [line + "\n" for line in lines]
    })

# 1. Заголовок
add_md([
    "# Лабораторная работа №2 по ��урсу «Системы искусственного интеллекта»",
    "## Тема: «Продукционная модель представления знаний и механизмы логического вывода»",
    "### Вариант №8: «Отбор кандидатов на вакантное место»",
    "",
    "**Цель работы:**",
    "Приобретение умений и навыков реализации пополняемой динамической базы знаний продукционного типа, не включаемой непосредственно в текст программы, с механизмом **обратного логического вывода** (Backward Chaining), исчислением предикатов первого порядка, представлением в виде графа И/ИЛИ, разрешением конфликтов и подсистемой объяснения (вопросы «ПОЧЕМУ?» и «КАК?»)."
])

# 2. Импорт библиотек
add_md([
    "---",
    "## 1. Импорт библиотек и настройка рабочей среды"
])

add_code([
    "import os",
    "import json",
    "from typing import List, Dict, Any, Optional, Tuple, Set, Union",
    "",
    "import pandas as pd",
    "import numpy as np",
    "import matplotlib.pyplot as plt",
    "import networkx as nx",
    "",
    "from expert_system import Term, Predicate, Rule, ProofNode, BackwardChainingEngine",
    "from visualize_graph import build_full_and_or_graph, plot_and_or_graph, plot_partial_proof_graph",
    "",
    "plt.style.use('seaborn-v0_8-whitegrid' if 'seaborn-v0_8-whitegrid' in plt.style.available else 'default')",
    "plt.rcParams['font.size'] = 11",
    "plt.rcParams['figure.figsize'] = (12, 7)",
    "plt.rcParams['font.sans-serif'] = ['DejaVu Sans', 'Arial']",
    "print('Все библиотеки успешно загружены.')"
])

# 3. Теоретическое описание предметной области
add_md([
    "---",
    "## 2. Формализация предметной области: Отбор кандидатов (Вариант №8)",
    "",
    "В рамках варианта №8 решается задача автоматизированного отбора соискателей на IT-позицию на основе многокритериальной оценки:",
    "- **Уровень образования:** профильное высшее или непрофильное;",
    "- **Первичный HR-ск��ининг:** резюме, портфолио, мотивация (да / нет);",
    "- **Опыт работы:** `более_5` лет, `от_3_до_5` лет, `до_3` лет;",
    "- **Техническое собеседование:** `отлично`, `хорошо`, `неудовлетворительно`;",
    "- **Soft Skills:** командная работа и коммуникация (`высокая`, `средняя`, `низкая`);",
    "- **Международная квалификация:** знание английского (B2+) и опыт в распределенных проектах.",
    "",
    "### Иерархия целевых гипотез:",
    "1. `принять_senior`: Senior-разработчик / Тимлид;",
    "2. `принять_middle`: Middle-разработчик;",
    "3. `испытательный_срок`: Junior-разработчик с испытательным сроком;",
    "4. `кадровый_резерв`: сильный специалист при нехватке вторичных навыков;",
    "5. `отказ`: несоответствие базовым критериям или непройденное интервью."
])

# 4. Просмотр внешних файлов базы знаний
add_md([
    "---",
    "## 3. Внешняя динамическая база знаний (JSON)",
    "По требованию лабораторной работы база знаний не зашивается в программный код, а хранится во внешних конфигурационных файлах `data/rules.json` и `data/facts.json`."
])

add_code([
    "with open('data/rules.json', 'r', encoding='utf-8') as f:",
    "    rules_data = json.load(f)",
    "",
    "print(f'Всего продукционных правил в БЗ: {len(rules_data[\"rules\"])}')",
    "print()",
    "for r in rules_data['rules'][:4]:",
    "    print(f'[{r[\"id\"]}] {r[\"name\"]} (Приоритет: {r[\"priority\"]})')",
    "    print(f'  Антецедент: {r[\"premises\"]}')",
    "    print(f'  Консеквент: {r[\"conclusion\"][\"pred\"]}{tuple(r[\"conclusion\"][\"args\"])}')",
    "    print()"
])

add_code([
    "with open('data/facts.json', 'r', encoding='utf-8') as f:",
    "    facts_data = json.load(f)",
    "",
    "print('Зарегистрированные кандидаты в базе данных:')",
    "for cand, facts in facts_data['candidates'].items():",
    "    print(f'  - Кандидат \"{cand}\": {len(facts)} фактов')"
])

# 5. Демонстрация графа И/ИЛИ
add_md([
    "---",
    "## 4. Построение и визуализация полного графа И/ИЛИ базы знаний"
])

add_code([
    "engine = BackwardChainingEngine()",
    "engine.load_rules_from_file('data/rules.json')",
    "G_full = build_full_and_or_graph(engine)",
    "plot_and_or_graph(G_full, 'images/and_or_graph.png')",
    "",
    "from IPython.display import Image, display",
    "display(Image('images/and_or_graph.png'))"
])

# 6. Логический вывод для кандидата 1 (Иванов -> Senior)
add_md([
    "---",
    "## 5. Механизм обратного вывода: Кандидат Иванов (Senior)"
])

add_code([
    "engine_ivanov = BackwardChainingEngine(conflict_resolution_strategy='priority_first')",
    "engine_ivanov.load_rules_from_file('data/rules.json')",
    "engine_ivanov.load_facts_from_file('data/facts.json', candidate_name='Иванов')",
    "",
    "goal_ivanov = Predicate('рекомендация_кандидату', ['Иванов', 'принять_senior'])",
    "success, tree = engine_ivanov.prove_goal(goal_ivanov)",
    "",
    "print(f'Результат доказательства гипотезы: {\"УСПЕХ\" if success else \"НЕУДАЧА\"}')",
    "print('\\n--- Протокол обратного вывода на графе ---')",
    "for log_line in engine_ivanov.derivation_log:",
    "    print(log_line)"
])

add_code([
    "plot_partial_proof_graph(engine_ivanov, 'Иванов', goal_ivanov, 'images/partial_graph_ivanov.png')",
    "display(Image('images/partial_graph_ivanov.png'))"
])

# 7. Подсистема объяснений (ПОЧЕМУ? и КАК?)
add_md([
    "---",
    "## 6. Подсистема объяснения: Ответы на вопросы «ПОЧЕМУ?» и «КАК?»"
])

add_code([
    "print('=== ВОПРОС ПОЛЬЗОВАТЕЛЯ: КАК рекомендация_кандидату(Иванов, принять_senior)? ===')",
    "print(engine_ivanov.explain_how('рекомендация_кандидату(Иванов, принять_senior)'))",
    "print()",
    "print('=== ВОПРОС ПОЛЬЗОВАТЕЛЯ: КАК уровень_hard_skills(Иванов, высокий)? ===')",
    "print(engine_ivanov.explain_how('уровень_hard_skills(Иванов, высокий)'))",
    "print()",
    "print('=== ВОПРОС ПОЛЬЗОВАТЕЛЯ: ПОЧЕМУ система запрашивает \"опыт_работы_лет\"? ===')",
    "print(engine_ivanov.explain_why('опыт_работы_лет'))"
])

# 8. Логический вывод для остальных кандидатов
add_md([
    "---",
    "## 7. Комплексное тестирование базы знаний на группе кандидатов"
])

add_code([
    "candidates = ['Иванов', 'Петров', 'Сидоров', 'Кузнецов']",
    "hypotheses = ['принять_senior', 'принять_middle', 'испытательный_срок', 'кадровый_резерв', 'отказ']",
    "",
    "results_table = []",
    "",
    "for c in candidates:",
    "    proven_hyp = None",
    "    applied_rule = None",
    "    steps_count = 0",
    "    for hyp in hypotheses:",
    "        eng = BackwardChainingEngine(conflict_resolution_strategy='priority_first')",
    "        eng.load_rules_from_file('data/rules.json')",
    "        eng.load_facts_from_file('data/facts.json', candidate_name=c)",
    "        goal = Predicate('рекомендация_кандидату', [c, hyp])",
    "        succ, node = eng.prove_goal(goal)",
    "        if succ:",
    "            proven_hyp = hyp",
    "            applied_rule = node.rule.id if node.rule else 'БД'",
    "            steps_count = eng.step_counter",
    "            break",
    "            ",
    "    results_table.append({",
    "        'Кандидат': c,",
    "        'Решение ЭС': proven_hyp,",
    "        'Ключевое правило': applied_rule,",
    "        'Число шагов доказательства': steps_count",
    "    })",
    "",
    "df_res = pd.DataFrame(results_table)",
    "display(df_res)"
])

# 9. Анализ ошибочных правил и неуспешного вывода
add_md([
    "---",
    "## 8. Обнаружение ошибочных правил и обработка неуспешного вывода",
    "",
    "В соответствии с требованием п. 2(д, е) методички система выявляет невыполнимые условия в правилах при проверке заведомо ложной гипотезы."
])

add_code([
    "engine_fail = BackwardChainingEngine()",
    "engine_fail.load_rules_from_file('data/rules.json')",
    "engine_fail.load_facts_from_file('data/facts.json', candidate_name='Сидоров')",
    "",
    "# Проверяем невыполнимую гипотезу: Сидоров не подходит под Senior",
    "false_goal = Predicate('рекомендация_кандидату', ['Сидоров', 'принять_senior'])",
    "succ, fail_tree = engine_fail.prove_goal(false_goal)",
    "",
    "print(f'Статус вывода: {succ}')",
    "print(f'Невыполнимое условие: {fail_tree.failed_condition}')",
    "print('\\nФрагмент протокола поиска с фиксацией неуспешных правил:')",
    "for log in engine_fail.derivation_log[-6:]:",
    "    print(log)"
])

# 9.1 Динамическое пополнение базы знаний и рабочей памяти
add_md([
    "---",
    "## 9. Пополнение динамической базы знаний без изменения текста программы",
    "",
    "В соответствии с главной целью лабораторной работы («приобретение навыков реализации пополняемой динамической базы знаний, не включаемой непосредственно в текст программы»):",
    "1. Эксперт или инженер по знаниям может добавить новое правило через метод `add_rule()` или файл JSON;",
    "2. Пользователь или HR-менеджер может ввести новый факт через метод `add_fact()` или обновить карточку кандидата;",
    "3. Пополненная база может быть сохранена во внешний файл `save_rules_to_file()` без перезапуска или модификации кода интерпретатора."
])

add_code([
    "print('=== ДЕМОНСТРАЦИЯ ДИНАМИЧЕСКОГО ПОПОЛНЕНИЯ БАЗЫ ЗНАНИЙ ===')",
    "engine_dynamic = BackwardChainingEngine()",
    "engine_dynamic.load_rules_from_file('data/rules.json')",
    "engine_dynamic.load_facts_from_file('data/facts.json', candidate_name='Сидоров')",
    "",
    "print(f'Исходное количество правил: {len(engine_dynamic.rules)}')",
    "print(f'Исходное количество фактов для Сидорова: {len(engine_dynamic.working_memory)}')",
    "",
    "# Эксперт вводит новое правило (например, для роли Архитектора решений)",
    "new_rule = Rule(",
    "    rule_id='R_ARCHITECT',",
    "    name='Рекомендация на архитектора решений (Solution Architect)',",
    "    premises={'and': [",
    "        Predicate('допущен_к_отбору', ['?X', 'да']),",
    "        Predicate('опыт_проектирования_систем', ['?X', 'да'])",
    "    ]},",
    "    conclusion=Predicate('рекомендация_кандидату', ['?X', 'принять_архитектор']),",
    "    priority=50",
    ")",
    "engine_dynamic.add_rule(new_rule)",
    "",
    "# Пользователь сообщает новый факт, полученный на доп. собеседовании",
    "engine_dynamic.add_fact(Predicate('опыт_проектирования_систем', ['Сидоров', 'да']))",
    "",
    "print(f'Правил после пополнения: {len(engine_dynamic.rules)}')",
    "print(f'Фактов после пополнения: {len(engine_dynamic.working_memory)}')",
    "",
    "# Запуск вывода по новому правилу",
    "new_goal = Predicate('рекомендация_кандидату', ['Сидоров', 'принять_архитектор'])",
    "dyn_success, dyn_tree = engine_dynamic.prove_goal(new_goal)",
    "print(f'Результат логического вывода с новой продукцией: {\"УСПЕХ\" if dyn_success else \"НЕУДАЧА\"}')",
    "print()",
    "print('Объяснение решения по новой продукции:')",
    "print(engine_dynamic.explain_how('рекомендация_кандидату(Сидоров, принять_архитектор)'))"
])

# 10. Выводы
add_md([
    "---",
    "## 9. Выводы по лабораторной работе",
    "",
    "1. **Динамическая база знаний:** Создана масштабируемая архитектура экспертной системы, в которой продукционные правила и факты хранятся во внешних файлах (`rules.json`, `facts.json`), что исключает жесткую завязку на исходный код и позволяет динамически пополнять базу знаний новыми правилами.",
    "2. **Обратный логический вывод (Backward Chaining):** Реализован интерпретатор, работающий от целевых гипотез к фактам рабочей памяти. ��анный подход оптимален для задач отбора и классификации с ограниченным числом возможных исходов.",
    "3. **Разрешение конфликтов:** Использован механизм многокритериального приоритетного упорядочивания конфликтного набора правил (учет статического приоритета и специфичности правила).",
    "4. **Объяснения и протоколирование:** Реализована модель объяснений MYCIN, позволяющая пользователю получать развернутые ответы на вопросы «КАК?» (трассировка вниз к посылкам) и «ПОЧЕМУ?» (подъем вверх к текущей цели), а также строить частичные графы успешного и неуспешного вывода."
])

notebook = {
    "cells": cells,
    "metadata": {
        "kernelspec": {
            "display_name": "Python 3",
            "language": "python",
            "name": "python3"
        },
        "language_info": {
            "name": "python",
            "version": "3.12"
        }
    },
    "nbformat": 4,
    "nbformat_minor": 5
}

with open("Lab2_Variant8_Recruitment.ipynb", "w", encoding="utf-8") as f:
    json.dump(notebook, f, indent=2, ensure_ascii=False)

print("Lab2_Variant8_Recruitment.ipynb successfully created!")
