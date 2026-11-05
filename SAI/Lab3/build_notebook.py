"""Собирает самодостаточный ноутбук Lab3.ipynb."""

from pathlib import Path

import nbformat
from nbformat.v4 import new_code_cell, new_markdown_cell, new_notebook

ROOT = Path(__file__).resolve().parent
ENGINE = (ROOT / "frame_system.py").read_text(encoding="utf-8")


def md(text: str):
    return new_markdown_cell(text.strip() + "\n")


def code(text: str):
    return new_code_cell(text.strip() + "\n")


cells = [
    md("""
# Лабораторная работа №3
## Поисковая система на основе фреймовой модели представления знаний

**Дисциплина:** системы искусственного интеллекта  
**Вариант №8:** отбор кандидатов на вакантное место  
**Студент:** Ильин Д.А., группа 3093

Ноутбук самодостаточен: язык представления знаний, база фреймов, проверка правил и поиск реализованы в ячейках ниже и не требуют отдельных модулей.
"""),
    md("""
## 1. Цель

Рассмотреть представление знаний фреймами и три способа управления выводом:

1. демон (присоединённая процедура, которая срабатывает при обращении к слоту или при его изменении);
2. служебная процедура, которую явно запускает исполнительный механизм;
3. наследование значений и ограничений по иерархии «абстрактное — конкретное».

На этой модели строится база знаний предметной области и поисковая система.
"""),
    md("""
## 2. Язык представления знаний

Фрейм — именованная структура. Прототип описывает класс, экземпляр хранит факты о конкретном объекте. Иерархия задаётся указателем родителя (связь AKO): более частный фрейм стоит ниже более абстрактного.

Слот содержит:

| Поле | Назначение |
| --- | --- |
| Имя фрейма и имя слота | адрес знания |
| Указатель наследования | `S` (same) — наследовать значение, `U` (unique) — не наследовать, `R` (range) — наследовать только ограничение |
| Тип данных | string, integer, real, boolean, frame, list, enum |
| Значение | факт, значение по умолчанию или результат вывода |
| Демон | `if-needed`, `if-added`, `if-removed` |
| Присоединённая процедура | служебная процедура слота |

Декларативные знания — иерархия, типы, домены и факты. Процедурные знания — демоны и процедура формирования рекомендации. Редактор фреймов отклоняет простые ошибки сразу (повтор имени, чужой тип, неверный указатель наследования, значение вне домена). Модуль проверки правил ищет то, что нельзя увидеть по одному полю: циклы, если они попали в базу при загрузке, нарушение ограничений предка и противоречия между слотами.
"""),
    md("""
## 3. Управление выводом

Чтение слота идёт в фиксированном порядке.

1. Если у фрейма есть собственное значение, оно и берётся.
2. При указателе `S` значение ищется у предков: сначала записанный факт, затем значение по умолчанию ближайшего предка.
3. Если значения всё ещё нет и у слота есть демон `if-needed`, демон вычисляет его и записывает в слот.
4. Иначе используется значение по умолчанию определения слота. Указатель `U` запрещает шаг 2, указатель `R` тоже не копирует значение, но при записи проверяет `min`, `max` и домен предка.

Запись значения проверяет тип и запускает демон `if-added`. Служебная процедура вызывается отдельно: для варианта 8 это `сформировать_рекомендацию`. Она читает исходные слоты, тем самым запускает демоны навыков, и записывает рекомендацию. Демон `if-added` этой записи ведёт журнал и меняет статус рассмотрения.

Каждый шаг попадает в протокол. По протоколу видно, какое знание получено наследованием, какое — демоном, а какое — служебной процедурой.
"""),
    md("""
## 4. Реализация фреймовой системы

В ячейке собраны системные функции из задания.

- Фреймы: создание, удаление, получение, тип, список слотов, наследование типа, связь подкласса, иерархия.
- Слоты: создание, удаление, чтение описания, чтение значения, чтение и установка необязательного параметра, запись значения.
- Вызов присоединённой процедуры.
- Проверки типа, существования фрейма и регистрации функции.
- Список потомков одного уровня, запись базы в файл, общая часть двух списков.

Дальше идут модуль проверки правил и поисковый механизм.
"""),
    code(ENGINE + """

print("Классы загружены: FrameBase, RuleChecker, SearchEngine")
print("Типы данных:", ", ".join(DATA_TYPES))
print("Указатели наследования:", ", ".join(INHERITANCE))
"""),
    md("""
## 5. Предметная область варианта 8

Иерархия абстракции:

`Сущность` → `Кандидат` → `СоискательIT` → экземпляры Иванов, Петров, Сидоров, Кузнецов.

Рядом находится ветка `Вакансия` с тремя экземплярами. Экземпляр ссылается на вакансию слотом типа `frame`.

Демоны `if-needed` считают hard skills, soft skills и международную квалификацию по тем же качественным правилам, что и продукционная модель лабораторной №2. Служебная процедура собирает из них рекомендацию: `принять_senior`, `принять_middle`, `испытательный_срок`, `кадровый_резерв` или `отказ`. Слот `направление` задан у класса `СоискательIT` и должен доставаться экземплярам наследованием, а не копированием факта.
"""),
    code("""
from pathlib import Path

fs = FrameBase()
build_variant8(fs)
fs.trace.clear()

print(fs.hierarchy_tree())
print()
print(f"Фреймов в базе: {len(fs.frames)}")
print(f"Прямые потомки «Кандидат»: {fs.list_depth_one('Кандидат')}")
print(f"Прямые потомки «СоискательIT»: {fs.list_depth_one('СоискательIT')}")
"""),
    code("""
import pandas as pd

rows = []
for slot_name in fs.get_slot_names("СоискательIT"):
    view = fs.get_slot("СоискательIT", slot_name)
    rows.append({
        "слот": slot_name,
        "тип": view["data_type"],
        "наследование": view["inheritance"],
        "определён в": view["defined_on"],
        "if-needed": view["if_needed"] or "—",
        "if-added": view["if_added"] or "—",
        "процедура": view["procedure"] or "—",
        "значение класса": "—" if view["value"] is None else view["value"],
    })

pd.DataFrame(rows)
"""),
    md("""
## 6. Иерархия фреймов

Прототипы и экземпляры показаны разными цветами. Дуга направлена от общего фрейма к частному: по ней работает наследование.
"""),
    code("""
from pathlib import Path

import matplotlib.pyplot as plt
import networkx as nx

plt.rcParams["font.family"] = "DejaVu Sans"

children = {name: [] for name in fs.frames}
for parent, child in fs.hierarchy():
    children[parent].append(child)

positions = {}

def place(node, depth, left, right):
    positions[node] = ((left + right) / 2, -depth)
    kids = sorted(children[node])
    if not kids:
        return
    width = (right - left) / len(kids)
    for index, kid in enumerate(kids):
        place(kid, depth + 1, left + index * width, left + (index + 1) * width)

place("Сущность", 0, 0, 1)

graph = nx.DiGraph()
graph.add_nodes_from(fs.frames)
graph.add_edges_from(fs.hierarchy())
prototypes = [name for name, frame in fs.frames.items() if frame.frame_type == "prototype"]
instances = [name for name, frame in fs.frames.items() if frame.frame_type == "instance"]

plt.figure(figsize=(14, 8))
nx.draw_networkx_nodes(graph, positions, nodelist=prototypes, node_color="#A0CBE8",
                       node_shape="s", node_size=2600, edgecolors="#1F4E79")
nx.draw_networkx_nodes(graph, positions, nodelist=instances, node_color="#B7E1CD",
                       node_shape="o", node_size=2300, edgecolors="#1B7F4E")
nx.draw_networkx_edges(graph, positions, arrows=True, arrowsize=16, edge_color="#4A4A4A",
                       width=1.4, min_target_margin=18)
nx.draw_networkx_labels(graph, positions, font_size=8, font_family="DejaVu Sans")
plt.title("Иерархия фреймов: отбор кандидатов, вариант 8")
plt.axis("off")
plt.tight_layout()

Path("images").mkdir(exist_ok=True)
plt.savefig("images/hierarchy.png", dpi=160, bbox_inches="tight")
plt.show()
"""),
    md("""
## 7. Тесты системных функций

Проверяются функции управления фреймами и слотами, вызов процедуры, проверки и вспомогательные операции. Отдельный экземпляр «Новичок» нужен, чтобы увидеть наследование, не затирая факты четырёх кандидатов. После теста он удаляется.
"""),
    code("""
checks = []

def check(name, call, predicate, detail):
    try:
        value = call()
        ok = predicate(value)
        shown = detail(value) if ok else f"не сошлось: {value!r}"
    except Exception as error:
        ok = False
        shown = str(error)
    checks.append({"функция": name, "результат": shown, "статус": "успех" if ok else "ошибка"})

def create_and_delete():
    fs.create_frame("Временный", parent="Сущность")
    fs.destroy_frame("Временный")
    return fs.frame_defined("Временный")

check("создание и удаление фрейма",
      create_and_delete,
      lambda value: value is False,
      lambda value: "фрейм создан и удалён, frame_defined = False")

check("получение типа фрейма",
      lambda: (fs.get_frame_type("Кандидат"), fs.get_frame_type("Иванов")),
      lambda value: value == ("prototype", "instance"),
      lambda value: "Кандидат — prototype, Иванов — instance")

check("список слотов с учётом предков",
      lambda: fs.get_slot_names("Иванов"),
      lambda value: "hard_skills" in value and "направление" in value and "фио" in value,
      lambda value: f"{len(value)} слотов, есть фио, направление и hard_skills")

check("наследование типа (копия структуры)",
      lambda: fs.inherit_frame_type("СоискательIT", "Кандидат"),
      lambda value: value == [],
      lambda value: "повторный вызов ничего не копирует: структура уже перенесена")

check("список единичной глубины",
      lambda: fs.list_depth_one("Вакансия"),
      lambda value: value == ["ВакансияJunior", "ВакансияMiddle", "ВакансияSenior"],
      lambda value: ", ".join(value))

check("необязательный параметр слота",
      lambda: (fs.get_slot_facet("Кандидат", "опыт_лет", "min"), fs.get_slot_facet("Кандидат", "опыт_лет", "max")),
      lambda value: value == (0, 50),
      lambda value: "опыт_лет: min = 0, max = 50")

check("установка необязательного параметра",
      lambda: (fs.set_slot_facet("СоискательIT", "направление", "comment", "наследуется экземплярами"),
               fs.get_slot_facet("Иванов", "направление", "comment")),
      lambda value: value[1] == "наследуется экземплярами",
      lambda value: "комментарий класса виден у экземпляра")

check("проверка типа данных",
      lambda: (fs.check_data("integer", 4, {"min": 0, "max": 50})[0],
               fs.check_data("enum", "школа", {"domain": ["профильное", "непрофильное"]})[0]),
      lambda value: value == (True, False),
      lambda value: "число 4 принято, значение «школа» отклонено")

check("регистрация функции",
      lambda: fs.function_registered("сформировать_рекомендацию") and not fs.function_registered("нет_такой"),
      lambda value: value is True,
      lambda value: "служебная процедура зарегистрирована")

check("общая часть списков",
      lambda: fs.list_intersection(["Иванов", "Петров", "Сидоров"], ["Сидоров", "Кузнецов", "Иванов"]),
      lambda value: value == ["Иванов", "Сидоров"],
      lambda value: "Иванов, Сидоров")

fs.create_frame("Новичок", parent="СоискательIT", frame_type="instance")
check("наследование S",
      lambda: (fs.get_slot_value("Новичок", "направление"), fs.get_slot_value("Новичок", "системное_мышление"),
               fs.get_slot_value("Новичок", "статус_рассмотрения")),
      lambda value: value == ("разработка ПО", "среднее", "в_очереди"),
      lambda value: "направление, системное мышление и статус взяты у предков")
check("указатель U не тянет чужое значение",
      lambda: fs.get_slot_value("Новичок", "фио"),
      lambda value: value is None,
      lambda value: "ФИО новичка пусто и не равно ФИО другого кандидата")
check("указатель R не копирует значение",
      lambda: fs.get_slot_value("Новичок", "опыт_лет"),
      lambda value: value is None,
      lambda value: "опыт не наследуется, ограничение 0..50 проверяется при записи")
fs.destroy_frame("Новичок")

check("демон if-needed",
      lambda: fs.get_slot_value("Иванов", "hard_skills"),
      lambda value: value == "высокий",
      lambda value: "Иванов.hard_skills = высокий")
check("служебная процедура",
      lambda: fs.call_procedure("Петров", "рекомендация"),
      lambda value: value == "принять_middle",
      lambda value: "Петров → принять_middle")
check("демон if-added",
      lambda: fs.get_slot_value("Петров", "журнал"),
      lambda value: value == ["Рекомендация установлена: принять_middle"],
      lambda value: "в журнал Петрова добавлена строка рекомендации")

# Остальных кандидатов тоже проводим через процедуру, чтобы таблица решений была полной.
for person, expected in expected_decisions().items():
    if fs.frames[person].values.get("рекомендация") != expected:
        fs.call_procedure(person, "рекомендация")

check("согласованность четырёх кандидатов",
      lambda: {name: fs.get_slot_value(name, "рекомендация") for name in expected_decisions()},
      lambda value: value == expected_decisions(),
      lambda value: ", ".join(f"{name}: {decision}" for name, decision in value.items()))

check("получение фрейма",
      lambda: fs.get_frame("ВакансияSenior").values["название"],
      lambda value: value == "Senior-разработчик",
      lambda value: "ВакансияSenior.название прочитано")

ok_count = sum(item["статус"] == "успех" for item in checks)
print(f"Пройдено проверок: {ok_count} из {len(checks)}")
pd.DataFrame(checks)
"""),
    md("""
## 8. Редактор фреймов и модуль проверки правил

Редактор останавливает ошибку в момент операции. Модуль проверки смотрит уже собранную базу: противоречие между скринингом и рекомендацией, а также значение по умолчанию, которое выходит за ограничение предка. После демонстрации база возвращается в согласованное состояние.
"""),
    code("""
editor_cases = []

def expect_error(title, action):
    try:
        action()
    except KnowledgeError as error:
        editor_cases.append({"проверка": title, "как поймана": "редактор", "сообщение": str(error)})
        return
    raise AssertionError(title)

expect_error("повтор имени фрейма", lambda: fs.create_frame("Иванов"))
expect_error("неизвестный тип данных", lambda: fs.create_slot("Кандидат", "временный_слот", "date"))
expect_error("неверный указатель наследования", lambda: fs.create_slot("Кандидат", "временный_слот", "string", "Z"))
expect_error("функция не зарегистрирована", lambda: fs.create_slot("Кандидат", "временный_слот", "string", if_needed="нет_такой"))
expect_error("значение вне домена", lambda: fs.set_slot_value("Иванов", "образование", "школа"))
expect_error("нарушение диапазона R", lambda: fs.set_slot_value("Иванов", "опыт_лет", -1))
expect_error("родитель не определён", lambda: fs.create_frame("БезРодителя", parent="НетТакого"))

fs.create_frame("ЦиклА", parent="Сущность")
fs.create_frame("ЦиклБ", parent="ЦиклА")
expect_error("цикл наследования", lambda: fs.set_subclass("ЦиклА", "ЦиклБ"))
fs.destroy_frame("ЦиклБ")
fs.destroy_frame("ЦиклА")

print("Чистая база, число замечаний:", len(RuleChecker(fs).check()))

saved = fs.frames["Кузнецов"].values["рекомендация"]
fs.frames["Кузнецов"].values["рекомендация"] = "принять_senior"
contradictions = RuleChecker(fs).check()
fs.frames["Кузнецов"].values["рекомендация"] = saved

fs.frames["СоискательIT"].slot_defs["опыт_лет"].default = -3
range_issues = RuleChecker(fs).check()
fs.frames["СоискательIT"].slot_defs["опыт_лет"].default = None
assert RuleChecker(fs).check() == []

found = [{"проверка": item["вид"], "как поймана": "модуль проверки правил", "сообщение": f"{item['где']}: {item['описание']}"}
         for item in contradictions + range_issues]
pd.DataFrame(editor_cases + found)
"""),
    md("""
## 9. Протоколы вывода по кандидатам

Для каждого соискателя заново строится база, чтобы протокол показывал полный путь, а не чтение уже записанного результата. В трассе должны встретиться собственный факт, демон `if-needed`, служебная процедура и демон `if-added`. Наследование видно отдельно: направление и статус по умолчанию не вводились у экземпляра.
"""),
    code("""
def fresh_base():
    base = FrameBase()
    build_variant8(base)
    base.trace.clear()
    return base

summary = []
protocols = {}
for person, expected in expected_decisions().items():
    base = fresh_base()
    direction = base.get_slot_value(person, "направление")
    decision = base.call_procedure(person, "рекомендация")
    assert decision == expected
    protocols[person] = base.format_trace()
    mechanisms = sorted({step.mechanism for step in base.trace})
    summary.append({
        "кандидат": person,
        "направление (наследование)": direction,
        "hard skills": base.get_slot_value(person, "hard_skills"),
        "soft skills": base.get_slot_value(person, "soft_skills"),
        "международная квалификация": base.get_slot_value(person, "международная_квалификация"),
        "рекомендация": decision,
        "механизмы в протоколе": ", ".join(mechanisms),
    })

pd.DataFrame(summary)
"""),
    code("""
for person, protocol in protocols.items():
    print("=" * 78)
    print(person)
    print("=" * 78)
    print(protocol)
    print()
"""),
    md("""
## 10. Поисковая система

Запрос состоит из имени класса и условий, разделённых знаком `&` или союзом `И`. Перед сравнением исполнительный механизм запускает служебную процедуру экземпляра. Процедура тянет демоны, а чтение `направление` и других незаполненных слотов проходит через наследование. Экземпляр попадает в выдачу только если выполнены все условия. Для каждого кандидата сохраняется протокол согласования.
"""),
    code("""
search_base = fresh_base()
engine = SearchEngine(search_base)

queries = [
    "СоискательIT | рекомендация = принять_senior",
    "СоискательIT | рекомендация = принять_middle",
    "СоискательIT | рекомендация = испытательный_срок",
    "СоискательIT | рекомендация = отказ",
    "СоискательIT | опыт_лет >= 5 & международная_квалификация = подтверждена",
    "СоискательIT | направление = разработка ПО & скрининг = да",
    "СоискательIT | hard_skills = высокий & международная_квалификация = не_подтверждена",
]

search_rows = []
reports = []
for text in queries:
    query = engine.parse(text)
    report = engine.search(query)
    reports.append(report)
    found = ", ".join(item["фрейм"] for item in report["найдены"]) or "—"
    rejected = ", ".join(item["фрейм"] for item in report["отклонены"]) or "—"
    search_rows.append({"запрос": report["запрос"], "найдены": found, "отклонены": rejected})

pd.DataFrame(search_rows)
"""),
    code("""
senior = reports[0]
print(senior["запрос"])
print()
for bucket, title in (("найдены", "Согласовано"), ("отклонены", "Отклонено")):
    for item in senior[bucket]:
        print("-" * 78)
        print(title, ":", item["фрейм"])
        for cond in item["условия"]:
            mark = "да" if cond["выполнено"] else "нет"
            print(f"  условие {cond['слот']} {cond['операция']} {cond['ожидание']}: факт {cond['факт']!r} → {mark}")
        print(search_base.format_trace(item["протокол"]))
        print()

bad_query = False
try:
    engine.parse("НеизвестныйКласс | фио = Иванов")
except KnowledgeError as error:
    bad_query = True
    print("Редактор запроса отклонил неизвестный класс:", error)
assert bad_query
"""),
    md("""
## 11. Запись базы в файл и повторная загрузка

Процедуры при загрузке регистрируются заново: в файле хранятся декларативные знания и имена демонов, а не текст функций. После загрузки вывод Петрова повторяется и даёт ту же рекомендацию.
"""),
    code("""
Path("kb").mkdir(exist_ok=True)
fs.save_to_file("kb/frames.json")

restored = FrameBase()
register_domain_functions(restored)
restored.load_from_file("kb/frames.json")
restored.trace.clear()
decision = restored.call_procedure("Петров", "рекомендация")
assert decision == "принять_middle"
assert restored.get_slot_value("Иванов", "рассматриваемая_вакансия") == "ВакансияSenior"
assert restored.frame_defined("Сидоров")
print("Файл kb/frames.json прочитан")
print("Петров после загрузки:", decision)
print("Ссылка Иванов →", restored.get_slot_value("Иванов", "рассматриваемая_вакансия"))
print("Наследование сохранилось:", restored.get_slot_value("Сидоров", "направление"))
"""),
    md("""
## 12. Выводы

1. Знания варианта 8 описаны фреймами с иерархией прототипов и экземпляров. В одном слоте сочетаются декларативная часть (тип, домен, указатель наследования, факт) и процедурная (демон или служебная процедура).
2. Вывод управляется тремя механизмами. Наследование передаёт классу и экземпляру направление, статус и значение по умолчанию системного мышления. Демоны `if-needed` вычисляют навыки в момент чтения. Служебная процедура собирает итоговую рекомендацию, а демон `if-added` фиксирует её в журнале.
3. Редактор не даёт записать заведомо неверное поле. Модуль проверки правил находит противоречие между фактами кандидата и рекомендацией и нарушение диапазона, унаследованного от предка.
4. Поиск принимает запрос к классу `СоискательIT`, запускает вывод и оставляет только согласованные экземпляры. Контрольные запросы возвращают Иванова на senior, Петрова на middle, Сидорова на испытательный срок и Кузнецова в отказ. Это совпадает с решениями лабораторной №2.
"""),
]

notebook = new_notebook(
    cells=cells,
    metadata={
        "kernelspec": {"display_name": "Python 3", "language": "python", "name": "python3"},
        "language_info": {"name": "python", "pygments_lexer": "ipython3"},
    },
)
path = ROOT / "Lab3.ipynb"
nbformat.write(notebook, path)
print(f"Wrote {path} with {len(cells)} cells")
