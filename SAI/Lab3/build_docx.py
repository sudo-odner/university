"""Отчёт Lab3.docx по оформлению лабораторных №1 и №2."""

import os

import docx
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt

from frame_system import FrameBase, RuleChecker, SearchEngine, build_variant8, expected_decisions


def set_cell_margins(cell, top=80, bottom=80, left=80, right=80):
    tc_pr = cell._tc.get_or_add_tcPr()
    tc_mar = OxmlElement("w:tcMar")
    for margin, value in (("top", top), ("bottom", bottom), ("left", left), ("right", right)):
        node = OxmlElement(f"w:{margin}")
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")
        tc_mar.append(node)
    tc_pr.append(tc_mar)


def set_cell_shading(cell, color_hex):
    tc_pr = cell._tc.get_or_add_tcPr()
    shading = OxmlElement("w:shd")
    shading.set(qn("w:val"), "clear")
    shading.set(qn("w:color"), "auto")
    shading.set(qn("w:fill"), color_hex)
    tc_pr.append(shading)


def add_table(doc, rows):
    table = doc.add_table(rows=len(rows), cols=len(rows[0]))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    for r_index, row in enumerate(rows):
        for c_index, value in enumerate(row):
            cell = table.cell(r_index, c_index)
            cell.text = str(value)
            run = cell.paragraphs[0].runs[0]
            run.font.size = Pt(9)
            set_cell_margins(cell)
            if r_index == 0:
                run.font.bold = True
                set_cell_shading(cell, "E0E0E0")
    doc.add_paragraph("", style="Normal")
    return table


def add_code(doc, text):
    for line in text.splitlines():
        paragraph = doc.add_paragraph(style="No Spacing")
        run = paragraph.add_run(line if line else " ")
        run.font.name = "Courier New"
        run.font.size = Pt(8)


def ivanov_trace():
    base = FrameBase()
    build_variant8(base)
    base.trace.clear()
    base.get_slot_value("Иванов", "направление")
    decision = base.call_procedure("Иванов", "рекомендация")
    return decision, base.format_trace()


def search_rows():
    base = FrameBase()
    build_variant8(base)
    base.trace.clear()
    engine = SearchEngine(base)
    queries = [
        "СоискательIT | рекомендация = принять_senior",
        "СоискательIT | рекомендация = принять_middle",
        "СоискательIT | рекомендация = испытательный_срок",
        "СоискательIT | рекомендация = отказ",
        "СоискательIT | опыт_лет >= 5 & международная_квалификация = подтверждена",
        "СоискательIT | направление = разработка ПО & скрининг = да",
        "СоискательIT | hard_skills = высокий & международная_квалификация = не_подтверждена",
    ]
    rows = [["Запрос", "Найдены", "Отклонены"]]
    senior = None
    for text in queries:
        report = engine.search(engine.parse(text))
        if senior is None:
            senior = report
        found = ", ".join(item["фрейм"] for item in report["найдены"]) or "—"
        rejected = ", ".join(item["фрейм"] for item in report["отклонены"]) or "—"
        rows.append([report["запрос"], found, rejected])
    return rows, senior


def create_report():
    doc = docx.Document("../Lab1/Lab1.docx")
    for paragraph in list(doc.paragraphs):
        paragraph._p.getparent().remove(paragraph._p)
    for table in list(doc.tables):
        table._tbl.getparent().remove(table._tbl)

    for text, style in (
        ("Министерство науки и высшего образования Российской Федерации", "No Spacing"),
        ("Новгородский государственный университет имени Ярослава Мудрого", "No Spacing"),
        ("Политехнический институт", "No Spacing"),
    ):
        paragraph = doc.add_paragraph(text, style=style)
        paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    department = doc.add_paragraph("Кафедра Информационных Технологий и Систем", style="Normal")
    department.alignment = WD_ALIGN_PARAGRAPH.CENTER

    for _ in range(4):
        doc.add_paragraph("", style="Normal")

    title = doc.add_paragraph("Лабораторная работа 3", style="Normal")
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    title.runs[0].font.size = Pt(16)
    title.runs[0].font.bold = True

    subtitle = doc.add_paragraph(
        "Поисковая система на основе фреймовой модели представления знаний",
        style="Normal",
    )
    subtitle.alignment = WD_ALIGN_PARAGRAPH.CENTER
    subtitle.runs[0].font.size = Pt(14)
    subtitle.runs[0].font.bold = True

    discipline = doc.add_paragraph("Дисциплина: Системы искусственного интеллекта (ИИ)", style="Normal")
    discipline.alignment = WD_ALIGN_PARAGRAPH.CENTER
    variant = doc.add_paragraph(
        "Вариант задания: №8 «Отбор кандидатов на вакантное место»",
        style="Normal",
    )
    variant.alignment = WD_ALIGN_PARAGRAPH.CENTER
    variant.runs[0].font.bold = True

    for _ in range(4):
        doc.add_paragraph("", style="Normal")

    doc.add_paragraph("Выполнили:", style="No Spacing")
    doc.add_paragraph("Студент Ильин Д.А группы № 3093", style="No Spacing")
    doc.add_paragraph("__________________________________", style="No Spacing")
    doc.add_paragraph("", style="No Spacing")
    doc.add_paragraph("Проверил:", style="No Spacing")
    doc.add_paragraph("Доцент кафедры ПТИ Михайлов Д.В.", style="No Spacing")
    doc.add_paragraph("__________________________________", style="No Spacing")
    doc.add_paragraph("", style="No Spacing")
    doc.add_paragraph("Дата", style="No Spacing")
    doc.add_paragraph("__________________________________", style="No Spacing")
    for _ in range(5):
        doc.add_paragraph("", style="No Spacing")
    city = doc.add_paragraph("Великий Новгород", style="No Spacing")
    city.alignment = WD_ALIGN_PARAGRAPH.CENTER
    year = doc.add_paragraph("2026г.", style="No Spacing")
    year.alignment = WD_ALIGN_PARAGRAPH.CENTER
    doc.add_page_break()

    doc.add_paragraph("Цель работы", style="Heading 1")
    doc.add_paragraph(
        "Изучить фреймовую модель представления знаний и реализовать поисковую систему, "
        "в которой вывод управляется демонами, служебными процедурами и наследованием. "
        "Предметная область совпадает с вариантом №8 предыдущей лабораторной работы: "
        "отбор кандидатов на вакантное место.",
        style="Normal",
    )

    doc.add_paragraph("Модель представления знаний", style="Heading 1")
    doc.add_paragraph(
        "Знания хранятся во фреймах двух видов. Прототип описывает класс объектов, "
        "экземпляр — конкретного кандидата или вакансию. Иерархия строится по степени абстракции "
        "и задаётся связью AKO: Сущность → Кандидат → СоискательIT → экземпляры "
        "Иванов, Петров, Сидоров и Кузнецов. Отдельно от кандидата наследует ветка Вакансия.",
        style="Normal",
    )
    doc.add_paragraph(
        "Слот объединяет декларативную и процедурную части. У него есть имя, указатель наследования, "
        "тип данных, значение, демон и присоединенная процедура. Указатель S передаёт значение предка, "
        "U запрещает наследование, R передаёт только ограничение диапазона или домена. "
        "Типы данных: string, integer, real, boolean, frame, list и enum. "
        "Слот «рассматриваемая_вакансия» имеет тип frame и хранит имя другого фрейма.",
        style="Normal",
    )
    doc.add_paragraph(
        "Исходные факты кандидата (образование, скрининг, опыт, результат интервью, "
        "командная работа, коммуникация, английский и международный опыт) записаны в экземплярах. "
        "Навыки и международная квалификация не вводятся вручную: их вычисляют демоны if-needed. "
        "Рекомендация записывается служебной процедурой. Направление «разработка ПО» задано "
        "только у прототипа СоискательIT и читается экземплярами по наследованию.",
        style="Normal",
    )
    if os.path.exists("images/hierarchy.png"):
        doc.add_picture("images/hierarchy.png", width=Inches(6.2))
        caption = doc.add_paragraph(
            "Рисунок 1 – Иерархия фреймов предметной области",
            style="Normal",
        )
        caption.alignment = WD_ALIGN_PARAGRAPH.CENTER

    doc.add_paragraph("Механизм управления выводом", style="Heading 1")
    doc.add_paragraph(
        "Исполнительный механизм читает слот в одном порядке. Сначала берётся собственное значение. "
        "Если его нет и указатель равен S, ищется факт или значение по умолчанию ближайшего предка. "
        "Если значение по-прежнему пусто и к слоту присоединён демон if-needed, демон вычисляет его. "
        "Указатели U и R этот поиск по предкам не выполняют. При любой записи проверяются тип и ограничения, "
        "после успешной записи запускается демон if-added.",
        style="Normal",
    )
    doc.add_paragraph(
        "В предметной области три демона if-needed повторяют правила лабораторной №2. "
        "Hard skills определяются опытом, оценкой интервью и системным мышлением. "
        "Soft skills определяются командной работой и коммуникацией. "
        "Международная квалификация подтверждается при английском не ниже B2 и наличии международного опыта. "
        "Служебная процедура «сформировать_рекомендацию» читает эти слоты и записывает одну из рекомендаций: "
        "принять_senior, принять_middle, испытательный_срок, кадровый_резерв или отказ. "
        "Демон if-added рекомендации добавляет строку в журнал и ставит статус «рассмотрен».",
        style="Normal",
    )
    doc.add_paragraph(
        "Каждый шаг пишется в протокол с именем механизма. По протоколу можно отделить "
        "наследование, работу демона и запуск служебной процедуры.",
        style="Normal",
    )

    doc.add_paragraph("Тесты функций преобразования знаний", style="Heading 1")
    doc.add_paragraph(
        "Системные функции проверены на рабочей базе и на временных фреймах, которые после теста удаляются. "
        "Редактор сразу отклоняет повтор имени, неизвестный тип, неверный указатель наследования, "
        "незарегистрированную функцию, значение вне домена, нарушение диапазона и цикл AKO. "
        "На чистой базе модуль проверки правил замечаний не выдаёт. "
        "Если рекомендации Кузнецова принудительно присвоить «принять_senior», модуль находит противоречие "
        "со скринингом, интервью и образованием. Если значению по умолчанию опыта поставить −3, "
        "модуль сообщает о нарушении ограничения предка 0…50.",
        style="Normal",
    )
    add_table(doc, [
        ["Функция", "Контрольный пример", "Результат"],
        ["Создание и удаление фрейма", "Временный потомок «Сущность»", "После удаления фрейм не определён"],
        ["Тип фрейма", "Кандидат и Иванов", "prototype и instance"],
        ["Имена слотов", "Экземпляр Иванов", "Слоты предков и собственный слот «направление»"],
        ["Наследование типа", "Повторное копирование Кандидат → СоискательIT", "Новых слотов нет, структура уже перенесена"],
        ["Список единичной глубины", "Потомки «Вакансия»", "Три экземпляра вакансий"],
        ["Параметр слота", "min и max у «опыт_лет»", "0 и 50, ограничение видно у потомка"],
        ["Проверка типа", "integer 4 и enum «школа»", "Число принято, значение вне домена отклонено"],
        ["Регистрация функции", "сформировать_рекомендацию", "Функция найдена, неизвестное имя отклонено"],
        ["Общая часть списков", "Иванов, Петров, Сидоров и Сидоров, Кузнецов, Иванов", "Иванов, Сидоров"],
        ["Наследование S", "Новый экземпляр без фактов", "Направление «разработка ПО», мышление «среднее», статус «в_очереди»"],
        ["Указатель U", "ФИО нового экземпляра", "Пусто, чужое ФИО не копируется"],
        ["Указатель R", "Опыт нового экземпляра и запись −1 Иванову", "Значение не наследуется, −1 отклоняется"],
        ["Демон if-needed", "Иванов.hard_skills", "высокий"],
        ["Служебная процедура", "Петров", "принять_middle"],
        ["Демон if-added", "Журнал Петрова", "Строка об установке рекомендации"],
        ["Запись и чтение файла", "kb/frames.json, повторный вывод Петрова", "принять_middle, ссылка на вакансию сохранена"],
    ])

    doc.add_paragraph("Результаты вывода по кандидатам", style="Heading 1")
    decisions = expected_decisions()
    add_table(doc, [
        ["Кандидат", "Основание", "Рекомендация"],
        ["Иванов", "Опыт 8 лет, интервью «отлично», высокие soft skills, английский B2 и международный опыт", decisions["Иванов"]],
        ["Петров", "Опыт 4 года, интервью «хорошо», высокие soft skills", decisions["Петров"]],
        ["Сидоров", "Опыт 2 года, интервью «хорошо», средние soft skills", decisions["Сидоров"]],
        ["Кузнецов", "Непрофильное образование, скрининг не пройден, интервью неудовлетворительное", decisions["Кузнецов"]],
    ])

    decision, trace = ivanov_trace()
    doc.add_paragraph(
        f"Ниже приведён полный протокол кандидата Иванов. Итоговая рекомендация — {decision}. "
        "Шаг 1 получает направление наследованием. Далее служебная процедура читает собственные факты, "
        "три демона if-needed записывают навыки, процедура записывает рекомендацию, "
        "а демон if-added обновляет журнал и статус.",
        style="Normal",
    )
    add_code(doc, trace)

    doc.add_paragraph("Механизм поиска по запросу", style="Heading 1")
    doc.add_paragraph(
        "Запрос задаёт класс и набор условий, соединённых союзом «И». "
        "Редактор запроса проверяет, что класс есть в базе и что каждое условие разобрано. "
        "Поиск перебирает экземпляры класса с учётом подклассов. Для каждого экземпляра "
        "запускается присоединенная процедура слота «рекомендация». Она инициирует демоны и наследование. "
        "После этого фактическое значение каждого слота сравнивается с условием. "
        "Операции: =, !=, >, >=, <, <= и «содержит». В выдачу попадает экземпляр, у которого выполнены все условия. "
        "Протокол согласования сохраняется и для принятых, и для отклонённых кандидатов.",
        style="Normal",
    )
    doc.add_paragraph("Тестовый набор поисковой системы", style="Heading 1")
    rows, senior = search_rows()
    add_table(doc, rows)
    doc.add_paragraph(
        "Запрос «рекомендация = принять_senior» оставляет Иванова. "
        "У Петрова процедура даёт принять_middle, у Сидорова — испытательный срок, у Кузнецова — отказ, "
        "поэтому условие равенства для них ложно. Запрос с опытом не меньше 5 лет и подтверждённой "
        "международной квалификацией также находит только Иванова: опыт сравнивается как число, "
        "а квалификация появляется из демона if-needed. Запрос по направлению использует значение, "
        "унаследованное от СоискательIT, и отсекает Кузнецова по скринингу. "
        "Запрос на высокие hard skills при неподтверждённой квалификации никого не возвращает: "
        "у Иванова квалификация подтверждена, у остальных hard skills не высокие.",
        style="Normal",
    )
    ivanov = senior["найдены"][0]
    doc.add_paragraph(
        "Фрагмент согласования Иванова по первому запросу. Процедура запускается внутри поиска, "
        "демон вычисляет навыки, условие сравнивает уже выведенную рекомендацию.",
        style="Normal",
    )
    for cond in ivanov["условия"]:
        mark = "выполнено" if cond["выполнено"] else "не выполнено"
        doc.add_paragraph(
            f"Условие {cond['слот']} {cond['операция']} {cond['ожидание']}: "
            f"факт «{cond['факт']}», {mark}.",
            style="Normal",
        )
    add_code(doc, senior_trace(ivanov))

    doc.add_paragraph("Выводы", style="Heading 1")
    doc.add_paragraph(
        "1. Для варианта №8 построена фреймовая база с иерархией прототипов и экземпляров. "
        "Слот хранит и декларативные признаки, и присоединенные процедуры.\n"
        "2. Вывод управляется тремя механизмами: наследование передаёт направление и значения по умолчанию, "
        "демоны вычисляют навыки в момент чтения и ведут журнал при записи, служебная процедура формирует рекомендацию.\n"
        "3. Редактор фреймов отклоняет ошибки отдельного поля, а модуль проверки правил обнаруживает "
        "противоречия между слотами и нарушение унаследованного ограничения.\n"
        "4. Поисковая система запускает тот же вывод и согласует запрос с экземплярами. "
        "Контрольные запросы воспроизводят решения лабораторной №2: Иванов — принять_senior, "
        "Петров — принять_middle, Сидоров — испытательный_срок, Кузнецов — отказ.",
        style="Normal",
    )

    doc.save("Lab3.docx")
    print("Lab3.docx saved")


def senior_trace(item):
    lines = []
    for index, step in enumerate(item["протокол"], 1):
        lines.append(f"{index:02d}. {step.format()}")
    return "\n".join(lines)


if __name__ == "__main__":
    create_report()
