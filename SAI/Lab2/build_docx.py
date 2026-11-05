"""
Скрипт создания отчета Lab2.docx по структуре и оформлению Лабораторной №1.
"""

import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
import os

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def set_cell_shading(cell, color_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), color_hex)
    tcPr.append(shd)

def create_lab2_report():
    # Открываем базовый шаблон из Lab1 для сохранения стилей шрифтов и полей
    doc = docx.Document('../Lab1/Lab1.docx')
    
    # Очищаем существующее тело документа
    for p in list(doc.paragraphs):
        p._p.getparent().remove(p._p)
    for t in list(doc.tables):
        t._tbl.getparent().remove(t._tbl)

    # 1. ТИТУЛЬНЫЙ ЛИСТ
    p0 = doc.add_paragraph("Министерство науки и высшего образования Российской Федерации", style='No Spacing')
    p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p1 = doc.add_paragraph("Новгородский государственный университет имени Ярослава Мудрого", style='No Spacing')
    p1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p2 = doc.add_paragraph("Политехнический институт", style='No Spacing')
    p2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p3 = doc.add_paragraph("Кафедра Информационных Технологий и Систем", style='Normal')
    p3.alignment = WD_ALIGN_PARAGRAPH.CENTER

    for _ in range(4):
        doc.add_paragraph("", style='Normal')

    p_title = doc.add_paragraph("Лабораторная работа 2", style='Normal')
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.runs[0].font.size = Pt(16)
    p_title.runs[0].font.bold = True

    p_sub = doc.add_paragraph("Продукционная модель представления знаний и механизмы логического вывода", style='Normal')
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.runs[0].font.size = Pt(14)
    p_sub.runs[0].font.bold = True

    p_disc = doc.add_paragraph("Дисциплина: Системы искусственного интеллекта (ИИ)", style='Normal')
    p_disc.alignment = WD_ALIGN_PARAGRAPH.CENTER

    p_var = doc.add_paragraph("Вариант задания: №8 «Отбор кандидатов на вакантное место» (Обратный вывод)", style='Normal')
    p_var.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_var.runs[0].font.bold = True

    for _ in range(4):
        doc.add_paragraph("", style='Normal')

    p_exec = doc.add_paragraph("Выполнили:", style='No Spacing')
    p_stud = doc.add_paragraph("Студент Ильин Д.А группы № 3093", style='No Spacing')
    p_line1 = doc.add_paragraph("__________________________________", style='No Spacing')
    
    doc.add_paragraph("", style='No Spacing')
    p_check = doc.add_paragraph("Проверил:", style='No Spacing')
    p_doc = doc.add_paragraph("Доцент кафедры ПТИ Михайлов Д.В.", style='No Spacing')
    p_line2 = doc.add_paragraph("__________________________________", style='No Spacing')
    
    doc.add_paragraph("", style='No Spacing')
    doc.add_paragraph("Дата", style='No Spacing')
    doc.add_paragraph("__________________________________", style='No Spacing')

    for _ in range(5):
        doc.add_paragraph("", style='No Spacing')

    p_city = doc.add_paragraph("Великий Новгород", style='No Spacing')
    p_city.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_year = doc.add_paragraph("2026г.", style='No Spacing')
    p_year.alignment = WD_ALIGN_PARAGRAPH.CENTER

    doc.add_page_break()

    # 2. ЦЕЛЬ РАБОТЫ
    h1 = doc.add_paragraph("Цель работы", style='Heading 1')
    p_goal = doc.add_paragraph(
        "Приобретение умений и навыков реализации пополняемой динамической базы знаний продукционного типа, "
        "не включаемой непосредственно в текст программы, с механизмом обратного логического вывода (Backward Chaining), "
        "исчислением предикатов первого порядка, представлением базы знаний графом И/ИЛИ, разрешением конфликтов и "
        "подсистемой объяснения хода рассуждений (вопросы «ПОЧЕМУ?» и «КАК?»).",
        style='Normal'
    )

    # 3. ПОСТАНОВКА ЗАДАЧИ
    h2 = doc.add_paragraph("Постановка задачи и формализация предметной области", style='Heading 1')
    p_task = doc.add_paragraph(
        "В соответствии с вариантом №8 предметной областью экспертной системы является «Отбор кандидатов на вакантное место». "
        "Для четных номеров вариантов регламентирован механизм обратного логического вывода (целеуправляемый поиск).",
        style='Normal'
    )
    
    doc.add_paragraph("Основные формализмы продукционной модели первого порядка:", style='Normal')
    doc.add_paragraph("1. Термы: константы (конкретные значения свойств соискателей) и переменные (?X — целевой кандидат).", style='List Paragraph')
    doc.add_paragraph("2. Предикаты: реляционные отношения вида P(X, Y), связывающие кандидата с его качественными и количественными характеристиками.", style='List Paragraph')
    doc.add_paragraph("3. Продукционные правила: структуры ЕСЛИ A ТО B, где антецедент содержит конъюнкцию (И) либо дизъюнкцию (ИЛИ) предикатов.", style='List Paragraph')
    doc.add_paragraph("4. Иерархия целевых гипотез: принять_senior, принять_middle, испытательный_срок, кадровый_резерв, отказ.", style='List Paragraph')

    # 4. АРХИТЕКТУРА И МЕХАНИЗМ ВЫВОДА
    doc.add_paragraph("Архи��ектура экспертной системы и алгоритм обратного вывода", style='Heading 1')
    doc.add_paragraph(
        "Программный комплекс состоит из следующих ключевых модулей:
"
        "• База правил: хранится в файле data/rules.json (15 продукций с приоритетами от 6 до 45);
"
        "• Рабочая память (БД фактов): динамически загружается из data/facts.json;
"
        "• Модуль динамического пополнения: методы add_rule() и add_fact(), а также сохранение save_rules_to_file() и save_facts_to_file(), позволяющие эксперту и пользователю вносить знания во время сессии без изменения программного кода;
"
        "• Интерпретатор обратного вывода (BackwardChainingEngine): реализует рекурсивный алгоритм унификации целей с антецедентами и консеквентами;
"
        "• Конфликтный блок: ранжирует применимые правила по композитному критерию (наивысший приоритет, затем наибольшая специфичность);
"
        "• Подсистема объяснения: генерирует детальные протоколы на вопросы «ПОЧЕМУ?» (подъем по дереву целей к правилу) и «КАК?» (трассировка вниз к подтвержденным посылкам);
"
        "• Блок выявления ошибок: протоколирует тупиковые пути и идентифицирует невыполнимые условия при неуспешном выводе.",
        style='Normal'
    )

    # 5. ГРАФ И/ИЛИ БАЗЫ ЗНАНИЙ
    doc.add_paragraph("Представление системы продукций графом И/ИЛИ", style='Heading 1')
    doc.add_paragraph(
        "На рисунке 1 представлен полный граф И/ИЛИ спроектированной базы знаний. "
        "Прямоугольные вершины соответствуют продукционным правилам, круглые вершины — фактам и терминальным предикатам.",
        style='Normal'
    )
    
    if os.path.exists("images/and_or_graph.png"):
        doc.add_picture("images/and_or_graph.png", width=Inches(6.2))
        p_cap1 = doc.add_paragraph("Рисунок 1 – Полный граф И/ИЛИ базы знаний «Отбор кандидатов»", style='Normal')
        p_cap1.alignment = WD_ALIGN_PARAGRAPH.CENTER

    # 6. РЕЗУЛЬТАТЫ ЭКСПЕРИМЕНТОВ И ТЕСТИРОВАНИЕ
    doc.add_paragraph("Результаты тестирования и протоколы логического вывода", style='Heading 1')
    doc.add_paragraph(
        "Проведено тестирование работы системы на контрольной выборке соискателей с различными профессиональными профилями.",
        style='Normal'
    )

    table_data = [
        ["Кандидат", "Профиль характеристик", "Целевая гипотеза", "Сработавшее правило", "Результат вывода"],
        ["Иванов", "Опыт > 5 лет, интервью 'отлично', английский B2+, международный опыт", "принять_senior", "R7 (приоритет 30)", "УСПЕХ"],
        ["Петров", "Опыт 3-5 лет, интервью 'хорошо', высокий уровень soft skills", "принять_middle", "R8 (приоритет 25)", "УСПЕХ"],
        ["Сидоров", "Опыт < 3 лет, интервью 'хорошо', средний уровень soft skills", "испытательный_срок", "R9 (приоритет 20)", "УСПЕХ"],
        ["Кузнецов", "Непрофильное образование, отказ на этапе скрининга", "отказ", "R12 (приоритет 45)", "УСПЕХ"]
    ]

    table = doc.add_table(rows=len(table_data), cols=5)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    for r_idx, row in enumerate(table_data):
        for c_idx, val in enumerate(row):
            cell = table.cell(r_idx, c_idx)
            cell.text = val
            cell.paragraphs[0].runs[0].font.size = Pt(9.5)
            set_cell_margins(cell, top=80, bottom=80, left=100, right=100)
            if r_idx == 0:
                cell.paragraphs[0].runs[0].font.bold = True
                set_cell_shading(cell, "E0E0E0")
            else:
                if c_idx == 4:
                    cell.paragraphs[0].runs[0].font.bold = True

    doc.add_paragraph("", style='Normal')
    doc.add_paragraph("Частичный граф успешного логического вывода для кандидата Иванов:", style='Normal')
    if os.path.exists("images/partial_graph_ivanov.png"):
        doc.add_picture("images/partial_graph_ivanov.png", width=Inches(6.0))
        p_cap2 = doc.add_paragraph("Рисунок 2 – Частичный граф вывода для цели рекомендация_кандидату(Иванов, принять_senior)", style='Normal')
        p_cap2.alignment = WD_ALIGN_PARAGRAPH.CENTER

    # 7. ДИАЛОГ С ПОЛЬЗОВАТЕЛЕМ И ОБЪЯСНЕНИЕ
    doc.add_paragraph("Подсистема объяснения: реализация модели MYCIN", style='Heading 1')
    doc.add_paragraph(
        "В процессе консультации пользователь имеет возможность изучить логику рассуждений экспертной системы:\n\n"
        "1. Запрос «КАК получено утверждение рекомендация_кандидату(Иванов, принять_senior)?»:\n"
        "   Ответ ЭС: Утверждение выведено по правилу [R7] ('Рекомендация на должность Senior-разработчик'). "
        "Были подтверждены все посылки: допущен_к_отбору, уровень_hard_skills(высокий), уровень_soft_skills(высокий), "
        "международная_квалификация(подтверждена).\n\n"
        "2. Запрос «ПОЧЕМУ система запрашивает факт 'опыт_работы_лет'?»:\n"
        "   Ответ ЭС: Вопрос необходим для проверки условий правил R2_A и R2_B с целью установления значения "
        "предиката уровень_hard_skills, который является промежуточной целью в цепочке доказательства.",
        style='Normal'
    )

    # 8. АНАЛИЗ ОШИБОЧНЫХ ПРАВИЛ
    doc.add_paragraph("Обнаружение ошибочных правил и обработка неуспешного вывода", style='Heading 1')
    doc.add_paragraph(
        "При проверке невыполнимой для соискателя Сидорова гипотезы 'принять_senior' алгоритм обратного вывода "
        "поочередно исследовал применимые правила R2_A и R2_B. "
        "Система зафиксировала невыполнимое условие опыт_работы_лет(Сидоров, более_5) и опыт_работы_лет(Сидоров, от_3_до_5), "
        "о чем сделана запись в протоколе вывода и возвращен статус НЕУДАЧА, что предотвратило принятие ложного заключения.",
        style='Normal'
    )

    # 9. ВЫВОДЫ
    doc.add_paragraph("Выводы по лабораторной работе", style='Heading 1')
    doc.add_paragraph(
        "1. Разработана и исследована продукционная экспертная система обратного вывода с представлением знаний на языке исчисления предикатов первого порядка.\n"
        "2. Обеспечена динамическая загрузка базы знаний из внешних структурированных файлов (JSON), что гарантирует модифицируемость без изменения исполняемого кода программы.\n"
        "3. Построен граф И/ИЛИ продукционной базы знаний, отображающий взаимосвязи входных фактов, промежуточных предикатов и целевых заключений.\n"
        "4. Реализован механизм разрешения конфликтов на основе приоритетов правил и их специфичности, исключающий недетерминированность логического вывода.\n"
        "5. Разработана подсистема объяснения, функционирующая по стандарту экспертной системы MYCIN (вопросы «ПОЧЕМУ?» и «КАК?»), а также модуль обнаружения невыполнимых условий при неуспешном выводе.",
        style='Normal'
    )

    doc.save("Lab2.docx")
    print("Lab2.docx successfully created!")

if __name__ == "__main__":
    create_lab2_report()
