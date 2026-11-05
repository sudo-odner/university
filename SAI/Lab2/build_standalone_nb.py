"""Собирает один самодостаточный ноутбук Lab2.ipynb."""
import json
import uuid
from pathlib import Path

import nbformat
from nbformat.v4 import new_notebook, new_markdown_cell, new_code_cell

ROOT = Path(__file__).resolve().parent
engine_src = (ROOT / "expert_system.py").read_text(encoding="utf-8")
rules = json.loads((ROOT / "data" / "rules.json").read_text(encoding="utf-8"))
facts = json.loads((ROOT / "data" / "facts.json").read_text(encoding="utf-8"))

# Убрать зависимость визуализации от внешнего модуля.
viz_src = r'''
def build_full_and_or_graph(engine):
    G = nx.DiGraph()
    for rule in engine.rules:
        r_node = f"[{rule.id}]"
        G.add_node(r_node, type="rule", label=f"{rule.id}\np={rule.priority}")
        c_node = rule.conclusion.to_string()
        G.add_node(c_node, type="fact", label=c_node)
        G.add_edge(r_node, c_node)
        for p in rule.get_premises_list():
            p_node = p.to_string()
            G.add_node(p_node, type="fact", label=p_node)
            G.add_edge(p_node, r_node)
    return G


def plot_and_or_graph(G, title="Граф И/ИЛИ базы знаний (вариант 8)"):
    plt.figure(figsize=(16, 10))
    rule_nodes = [n for n, d in G.nodes(data=True) if d.get("type") == "rule"]
    fact_nodes = [n for n, d in G.nodes(data=True) if d.get("type") == "fact"]
    pos = nx.spring_layout(G, k=0.55, iterations=80, seed=42)
    nx.draw_networkx_nodes(G, pos, nodelist=fact_nodes, node_color="#A0CBE8",
                           node_shape="o", node_size=900, edgecolors="#1F77B4")
    nx.draw_networkx_nodes(G, pos, nodelist=rule_nodes, node_color="#FFBE7D",
                           node_shape="s", node_size=700, edgecolors="#D95F02")
    nx.draw_networkx_edges(G, pos, arrows=True, arrowsize=12, edge_color="#555555")
    labels = {n: G.nodes[n].get("label", n) for n in G.nodes()}
    nx.draw_networkx_labels(G, pos, labels=labels, font_size=6)
    plt.title(title, fontsize=13, fontweight="bold")
    plt.axis("off")
    plt.tight_layout()
    plt.show()


def plot_partial_proof_graph(engine, candidate_name, goal):
    G = nx.DiGraph()
    for step in engine.explanation_steps:
        if step["type"] == "rule":
            r_node = f"[{step['rule_id']}]"
            g_node = step["goal"]
            G.add_node(r_node, type="rule", label=step["rule_id"])
            G.add_node(g_node, type="fact", label=g_node)
            G.add_edge(r_node, g_node)
            for p in step["premises"]:
                G.add_node(p, type="fact", label=p)
                G.add_edge(p, r_node)
        elif step["type"] == "fact":
            G.add_node(step["fact"], type="input_fact", label=step["fact"])
    plt.figure(figsize=(14, 8))
    pos = nx.spring_layout(G, k=0.7, iterations=60, seed=24)
    rule_nodes = [n for n, d in G.nodes(data=True) if d.get("type") == "rule"]
    input_facts = [n for n, d in G.nodes(data=True) if d.get("type") == "input_fact"]
    derived = [n for n, d in G.nodes(data=True) if d.get("type") == "fact" and n not in input_facts]
    if input_facts:
        nx.draw_networkx_nodes(G, pos, nodelist=input_facts, node_color="#86efac",
                               node_shape="o", node_size=1400, edgecolors="#16a34a", label="Факты")
    if derived:
        nx.draw_networkx_nodes(G, pos, nodelist=derived, node_color="#93c5fd",
                               node_shape="o", node_size=1600, edgecolors="#2563eb", label="Вывод")
    if rule_nodes:
        nx.draw_networkx_nodes(G, pos, nodelist=rule_nodes, node_color="#fde047",
                               node_shape="s", node_size=1100, edgecolors="#ca8a04", label="Правила")
    nx.draw_networkx_edges(G, pos, arrows=True, arrowsize=14, edge_color="#334155")
    labels = {n: G.nodes[n].get("label", n) for n in G.nodes()}
    nx.draw_networkx_labels(G, pos, labels=labels, font_size=7)
    plt.title(f"Частичный граф вывода: {candidate_name}\n{goal.to_string()}", fontsize=12)
    plt.legend(scatterpoints=1, loc="upper left")
    plt.axis("off")
    plt.tight_layout()
    plt.show()
'''.strip()

rules_literal = json.dumps(rules, ensure_ascii=False, indent=2)
facts_literal = json.dumps(facts, ensure_ascii=False, indent=2)

cells = []

def md(text):
    cells.append(new_markdown_cell(text.strip() + "\n"))

def code(text):
    cells.append(new_code_cell(text.strip() + "\n"))

md("""
# Лабораторная работа №2 по курсу «Системы искусственного интеллекта»
## Тема: «Продукционная модель представления знаний и механизмы логического вывода»
### Вариант №8: «Отбор кандидатов на вакантное место»

**Цель работы:** реализовать пополняемую динамическую базу знаний продукционного типа, которая не входит в текст интерпретатора. Механизм вывода — обратный (Backward Chaining), язык — исчисление предикатов первого порядка, представление — граф И/ИЛИ, есть разрешение конфликтов и объяснения «ПОЧЕМУ?» и «КАК?».

Ноутбук самодостаточный: интерпретатор, база правил и факты находятся в ячейках ниже. Интерпретатор читает правила и факты из JSON-файлов, которые ноутбук записывает на диск перед запуском. Добавление правила или факта не требует правки кода вывода.
""")

md("""
---
## 1. Импорт библиотек
""")

code("""
import json
import os
from typing import Any, Dict, List, Optional, Set, Tuple, Union

import matplotlib.pyplot as plt
import networkx as nx
import pandas as pd

plt.rcParams["font.sans-serif"] = ["DejaVu Sans", "Arial"]
plt.rcParams["axes.unicode_minus"] = False
print("Библиотеки загружены.")
""")

md("""
---
## 2. Предметная область

Вариант 8 — отбор кандидата на IT-вакансию. Чётный номер варианта задаёт обратный вывод: поиск идёт от целевой гипотезы к фактам.

Признаки кандидата: образование, первичный скрининг, опыт, результат технического интервью, командная работа, коммуникация, английский, международные проекты.

Целевые гипотезы предиката `рекомендация_кандидату`:
1. `принять_senior`
2. `принять_middle`
3. `испытательный_срок`
4. `кадровый_резерв`
5. `отказ`
""")

md("""
---
## 3. Интерпретатор обратного вывода

Код ниже — только машина вывода. Правил предметной области в нём нет. Он загружает их методом `load_rules_from_file` и факты методом `load_facts_from_file`.
""")

code(engine_src)

md("""
---
## 4. Построение графа И/ИЛИ
""")

code(viz_src)

md("""
---
## 5. Внешняя база знаний

Правила и факты записаны как данные JSON и сохраняются в файлы `kb/rules.json` и `kb/facts.json`. Интерпретатор получает их только чтением этих файлов.
""")

code(
    "RULES = "
    + json.dumps(rules, ensure_ascii=False, indent=2)
    + "\n\nFACTS = "
    + json.dumps(facts, ensure_ascii=False, indent=2)
    + """

os.makedirs("kb", exist_ok=True)
with open("kb/rules.json", "w", encoding="utf-8") as f:
    json.dump(RULES, f, ensure_ascii=False, indent=2)
with open("kb/facts.json", "w", encoding="utf-8") as f:
    json.dump(FACTS, f, ensure_ascii=False, indent=2)

print("Записано правил:", len(RULES["rules"]))
print("Кандидаты:", ", ".join(FACTS["candidates"]))
print("Файлы: kb/rules.json, kb/facts.json")
"""
)

md("""
---
## 6. Полный граф И/ИЛИ

Круги — факты и промежуточные предикаты. Квадраты — правила. Стрелка от посылки к правилу и от правила к заключению.
""")

code("""
engine = BackwardChainingEngine()
engine.load_rules_from_file("kb/rules.json")
G_full = build_full_and_or_graph(engine)
print(f"Вершин: {G_full.number_of_nodes()}, дуг: {G_full.number_of_edges()}")
plot_and_or_graph(G_full)
""")

md("""
---
## 7. Обратный вывод для Иванова

Гипотеза: `рекомендация_кандидату(Иванов, принять_senior)`.
""")

code("""
engine_ivanov = BackwardChainingEngine(conflict_resolution_strategy="priority_first")
engine_ivanov.load_rules_from_file("kb/rules.json")
engine_ivanov.load_facts_from_file("kb/facts.json", candidate_name="Иванов")

goal_ivanov = Predicate("рекомендация_кандидату", ["Иванов", "принять_senior"])
success, tree = engine_ivanov.prove_goal(goal_ivanov)
print("Результат:", "УСПЕХ" if success else "НЕУДАЧА")
print()
for line in engine_ivanov.derivation_log:
    print(line)
""")

code("""
plot_partial_proof_graph(engine_ivanov, "Иванов", goal_ivanov)
""")

md("""
---
## 8. Объяснения «КАК?» и «ПОЧЕМУ?»
""")

code("""
print("КАК рекомендация_кандидату(Иванов, принять_senior)?")
print(engine_ivanov.explain_how("рекомендация_кандидату(Иванов, принять_senior)"))
print()
print("КАК уровень_hard_skills(Иванов, высокий)?")
print(engine_ivanov.explain_how("уровень_hard_skills(Иванов, высокий)"))
print()
print("ПОЧЕМУ нужен опыт_работы_лет?")
print(engine_ivanov.explain_why("опыт_работы_лет"))
""")

md("""
---
## 9. Проверка всех кандидатов
""")

code("""
candidates = ["Иванов", "Петров", "Сидоров", "Кузнецов"]
hypotheses = ["принять_senior", "принять_middle", "испытательный_срок", "кадровый_резерв", "отказ"]
rows = []
for name in candidates:
    proven = None
    rule_id = None
    steps = 0
    for hyp in hypotheses:
        eng = BackwardChainingEngine()
        eng.load_rules_from_file("kb/rules.json")
        eng.load_facts_from_file("kb/facts.json", candidate_name=name)
        goal = Predicate("рекомендация_кандидату", [name, hyp])
        ok, node = eng.prove_goal(goal)
        if ok:
            proven = hyp
            rule_id = node.rule.id if node.rule else "факт"
            steps = eng.step_counter
            break
    rows.append({
        "Кандидат": name,
        "Решение": proven,
        "Правило": rule_id,
        "Шагов": steps,
    })
display(pd.DataFrame(rows))
""")

md("""
---
## 10. Неуспешный вывод

Для Сидорова гипотеза `принять_senior` не доказывается. В протоколе видно невыполненное условие.
""")

code("""
engine_fail = BackwardChainingEngine()
engine_fail.load_rules_from_file("kb/rules.json")
engine_fail.load_facts_from_file("kb/facts.json", candidate_name="Сидоров")
false_goal = Predicate("рекомендация_кандидату", ["Сидоров", "принять_senior"])
ok, fail_tree = engine_fail.prove_goal(false_goal)
print("Статус:", ok)
print("Причина:", fail_tree.failed_condition)
print()
for line in engine_fail.derivation_log:
    if "ОТКЛОНЕНО" in line or "НЕУДАЧА" in line or "ПРОБА" in line or "УСПЕХ" in line:
        print(line)
""")

md("""
---
## 11. Пополнение базы без правки интерпретатора

Новое правило и новый факт добавляются во время сеанса и сохраняются обратно в JSON. Код класса `BackwardChainingEngine` при этом не меняется.
""")

code("""
engine_dynamic = BackwardChainingEngine()
engine_dynamic.load_rules_from_file("kb/rules.json")
engine_dynamic.load_facts_from_file("kb/facts.json", candidate_name="Сидоров")
print("Правил до пополнения:", len(engine_dynamic.rules))

new_rule = Rule(
    rule_id="R_ARCHITECT",
    name="Рекомендация на архитектора решений",
    premises={"and": [
        Predicate("допущен_к_отбору", ["?X", "да"]),
        Predicate("опыт_проектирования_систем", ["?X", "да"]),
    ]},
    conclusion=Predicate("рекомендация_кандидату", ["?X", "принять_архитектор"]),
    priority=50,
)
engine_dynamic.add_rule(new_rule)
engine_dynamic.add_fact(Predicate("опыт_проектирования_систем", ["Сидоров", "да"]))
engine_dynamic.save_rules_to_file("kb/rules_extended.json")

new_goal = Predicate("рекомендация_кандидату", ["Сидоров", "принять_архитектор"])
dyn_ok, _ = engine_dynamic.prove_goal(new_goal)
print("Правил после пополнения:", len(engine_dynamic.rules))
print("Новая гипотеза:", "УСПЕХ" if dyn_ok else "НЕУДАЧА")
print()
print(engine_dynamic.explain_how("рекомендация_кандидату(Сидоров, принять_архитектор)"))

fresh = BackwardChainingEngine()
fresh.load_rules_from_file("kb/rules_extended.json")
print()
print("Повторная загрузка из файла, правил:", len(fresh.rules))
print("Новое правило в файле:", any(r.id == "R_ARCHITECT" for r in fresh.rules))
""")

md("""
---
## 12. Выводы

1. База правил и фактов хранится в JSON и загружается интерпретатором. Текст машины вывода не содержит предметных правил.
2. Обратный вывод доказывает целевую гипотезу, спускаясь к фактам рабочей памяти.
3. Если одна цель выводится несколькими правилами, порядок задаёт приоритет и число посылок.
4. Вопросы «КАК?» и «ПОЧЕМУ?» читают протокол доказательства.
5. Новое правило можно добавить и записать в файл. Следующий запуск читает уже пополненную базу.
""")

nb = new_notebook(cells=cells)
nb.metadata["kernelspec"] = {
    "display_name": "Python 3",
    "language": "python",
    "name": "python3",
}
nb.metadata["language_info"] = {"name": "python", "pygments_lexer": "ipython3"}

out = ROOT / "Lab2.ipynb"
# id для новых версий nbformat
for cell in nb.cells:
    cell["id"] = uuid.uuid4().hex[:8]

nbformat.write(nb, out)
print("Wrote", out, "cells:", len(nb.cells))
