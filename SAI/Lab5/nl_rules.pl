% Продукционная ЭС: каждый тип задан отдельным правилом «если — то».
:- use_module(library(readutil)).
cond(1,"заражает исполняемые файлы").
cond(2,"поражает загрузочную область").
cond(3,"использует макросы документов").
cond(4,"распространяется по сети").
cond(5,"распространяется по электронной почте").
cond(6,"шифрует данные").
cond(7,"похищает данные").
cond(8,"скрывает своё присутствие").
cond(9,"изменяет свой код").
cond(10,"распространяется через съёмный носитель").

:- dynamic yes/1, no/1.
clear_answers :- retractall(yes(_)), retractall(no(_)).
remember(Id, yes) :- retractall(no(Id)), (yes(Id) -> true ; assertz(yes(Id))).
remember(Id, no) :- retractall(yes(Id)), (no(Id) -> true ; assertz(no(Id))).
ask(Id) :- cond(Id, Text), format('Программа ~w? (1 = да, 2 = нет): ', [Text]),
    read_line_to_string(user_input, S),
    (S = "1" -> remember(Id, yes) ; S = "2" -> remember(Id, no) ;
     writeln('Введите 1 или 2.'), ask(Id)).
positive(Id) :- yes(Id), !.
positive(Id) :- no(Id), !, fail.
positive(Id) :- ask(Id), yes(Id).
negative(Id) :- no(Id), !.
negative(Id) :- yes(Id), !, fail.
negative(Id) :- ask(Id), no(Id).
consult :- clear_answers,
    (once(diagnose(Name)) -> format('Заключение: ~w~n', [Name]) ;
     writeln('Совпадение не найдено.')),
    clear_answers.
diagnose("Файловый вирус") :-
    positive(1),
    negative(2),
    negative(3),
    negative(4),
    negative(5),
    negative(6),
    negative(7),
    negative(8),
    negative(9),
    negative(10).
diagnose("Полиморфный файловый вирус") :-
    positive(1),
    negative(2),
    negative(3),
    negative(4),
    negative(5),
    negative(6),
    negative(7),
    negative(8),
    positive(9),
    negative(10).
diagnose("Скрытный файловый вирус") :-
    positive(1),
    negative(2),
    negative(3),
    negative(4),
    negative(5),
    negative(6),
    negative(7),
    positive(8),
    negative(9),
    negative(10).
diagnose("Загрузочный вирус") :-
    negative(1),
    positive(2),
    negative(3),
    negative(4),
    negative(5),
    negative(6),
    negative(7),
    negative(8),
    negative(9),
    negative(10).
diagnose("Загрузочный вирус на съёмном носителе") :-
    negative(1),
    positive(2),
    negative(3),
    negative(4),
    negative(5),
    negative(6),
    negative(7),
    negative(8),
    negative(9),
    positive(10).
diagnose("Макровирус") :-
    negative(1),
    negative(2),
    positive(3),
    negative(4),
    negative(5),
    negative(6),
    negative(7),
    negative(8),
    negative(9),
    negative(10).
diagnose("Почтовый макровирус") :-
    negative(1),
    negative(2),
    positive(3),
    negative(4),
    positive(5),
    negative(6),
    negative(7),
    negative(8),
    negative(9),
    negative(10).
diagnose("Сетевой червь") :-
    negative(1),
    negative(2),
    negative(3),
    positive(4),
    negative(5),
    negative(6),
    negative(7),
    negative(8),
    negative(9),
    negative(10).
diagnose("Почтовый червь") :-
    negative(1),
    negative(2),
    negative(3),
    positive(4),
    positive(5),
    negative(6),
    negative(7),
    negative(8),
    negative(9),
    negative(10).
diagnose("Шифровальщик") :-
    negative(1),
    negative(2),
    negative(3),
    negative(4),
    negative(5),
    positive(6),
    negative(7),
    negative(8),
    negative(9),
    negative(10).
diagnose("Шпионская программа") :-
    negative(1),
    negative(2),
    negative(3),
    negative(4),
    negative(5),
    negative(6),
    positive(7),
    negative(8),
    negative(9),
    negative(10).
diagnose("Скрытный троян") :-
    negative(1),
    negative(2),
    negative(3),
    negative(4),
    negative(5),
    negative(6),
    positive(7),
    positive(8),
    negative(9),
    negative(10).

% Машинные эксперименты без ввода с клавиатуры.
test_case("Файловый вирус",[1]).
test_case("Полиморфный файловый вирус",[1,9]).
test_case("Скрытный файловый вирус",[1,8]).
test_case("Загрузочный вирус",[2]).
test_case("Загрузочный вирус на съёмном носителе",[2,10]).
test_case("Макровирус",[3]).
test_case("Почтовый макровирус",[3,5]).
test_case("Сетевой червь",[4]).
test_case("Почтовый червь",[4,5]).
test_case("Шифровальщик",[6]).
test_case("Шпионская программа",[7]).
test_case("Скрытный троян",[7,8]).
load_answers(RequiredYes) :-
    forall(cond(Id, _),
           (memberchk(Id, RequiredYes) -> remember(Id, yes) ; remember(Id, no))).
check_case(Name, RequiredYes) :-
    setup_call_cleanup(
        (clear_answers, load_answers(RequiredYes)),
        (findall(Result, diagnose(Result), Results),
         (Results == [Name] -> format('OK: ~w~n', [Name]) ;
          format('Ошибка: ~w -> ~w~n', [Name, Results]), fail)),
        clear_answers).
check_unknown :-
    setup_call_cleanup(
        (clear_answers, load_answers([1,2])),
        (findall(Result, diagnose(Result), Results),
         (Results == [] -> writeln('OK: неизвестный профиль отклонён') ; fail)),
        clear_answers).
run_tests :- forall(test_case(Name, RequiredYes),
                    check_case(Name, RequiredYes)),
             check_unknown,
             writeln('Все 13 проверок пройдены.').


% Лабораторная работа № 5: русский интерфейс и построчный текстовый редактор.
% Запуск: swipl -q -s nl_logic.pl -g main -t halt (или nl_rules.pl).

% В редакторе каждая строка текста сохраняется. Команды начинаются с двоеточия.
main :- writeln('Введите запрос на русском языке. Команды редактора: :готово, :показать, :очистить, :удалить N, :заменить N текст, :отмена.'),
        editor([], Text),
        ( Text == "" -> writeln('Запрос отменён.')
        ; process_query(Text) ).

editor(Lines, Text) :-
    write('> '), flush_output,
    read_line_to_string(user_input, Line),
    ( Line == end_of_file -> join_lines(Lines, Text)
    ; Line == ":готово" -> join_lines(Lines, Text)
    ; Line == ":отмена" -> Text = ""
    ; Line == ":показать" -> show_lines(Lines), editor(Lines, Text)
    ; Line == ":очистить" -> editor([], Text)
    ; sub_string(Line, 0, _, _, ":удалить ") ->
        ( command_number(Line, ":удалить ", N), delete_line(Lines, N, New)
        -> editor(New, Text)
        ; writeln('Укажите номер существующей строки.'), editor(Lines, Text) )
    ; sub_string(Line, 0, _, _, ":заменить ") ->
        ( replacement(Line, N, Value), replace_line(Lines, N, Value, New)
        -> editor(New, Text)
        ; writeln('Формат: :заменить N новый текст'), editor(Lines, Text) )
    ; append(Lines, [Line], New), editor(New, Text) ).

join_lines(Lines, Text) :- atomics_to_string(Lines, ' ', Text).
show_lines(Lines) :- show_lines(Lines, 1).
show_lines([], _).
show_lines([Line|Rest], N) :- format('~d: ~s~n', [N, Line]), Next is N + 1, show_lines(Rest, Next).
command_number(Line, Prefix, N) :-
    string_length(Prefix, Length), sub_string(Line, Length, _, 0, Tail),
    number_string(N, Tail), integer(N), N > 0.
replacement(Line, N, Value) :-
    sub_string(Line, 10, _, 0, Tail),
    split_string(Tail, ' ', ' ', [Number|Words]), Words \= [],
    number_string(N, Number), integer(N), N > 0,
    atomics_to_string(Words, ' ', Value).
delete_line(Lines, N, New) :- nth1(N, Lines, _, New).
replace_line(Lines, N, Value, New) :-
    nth1(N, Lines, _, Rest), nth1(N, New, Value, Rest).

% Unicode-aware lowercasing and tokenization. Punctuation and line breaks separate words.
tokens(Text, Words) :-
    string_lower(Text, Lower),
    split_string(Lower, " \t\n\r.,!?;:()[]{}\"'«»/\\-", " \t\n\r.,!?;:()[]{}\"'«»/\\-", Words).
contains_stem(Words, Stem) :- member(Word, Words), sub_string(Word, 0, _, _, Stem), !.
all_stems(_, []).
all_stems(Words, [Stem|Rest]) :- contains_stem(Words, Stem), all_stems(Words, Rest).

% Команда может находиться в любой позиции, регистр и окончания слов игнорируются.
action(Words, describe) :- (contains_stem(Words, "опиш") ; contains_stem(Words, "расскаж") ; contains_stem(Words, "сведен")), !.
action(Words, verify) :- (contains_stem(Words, "проверь") ; contains_stem(Words, "провер") ; contains_stem(Words, "подтверд")), !.
action(Words, classify) :- (contains_stem(Words, "определ") ; contains_stem(Words, "найд") ; contains_stem(Words, "распозна") ; contains_stem(Words, "классифиц")), !.

% Словарь ключей предметной области. Более конкретное имя имеет приоритет.
object_stems("Файловый вирус", ["файлов", "вирус"]).
object_stems("Полиморфный файловый вирус", ["полиморф", "файлов", "вирус"]).
object_stems("Скрытный файловый вирус", ["скрыт", "файлов", "вирус"]).
object_stems("Загрузочный вирус", ["загруз", "вирус"]).
object_stems("Загрузочный вирус на съёмном носителе", ["загруз", "съём", "вирус"]).
object_stems("Макровирус", ["макровирус"]).
object_stems("Почтовый макровирус", ["почтов", "макровирус"]).
object_stems("Сетевой червь", ["сетев", "черв"]).
object_stems("Почтовый червь", ["почтов", "черв"]).
object_stems("Шифровальщик", ["шифровальщ"]).
object_stems("Шпионская программа", ["шпион"]).
object_stems("Скрытный троян", ["скрыт", "троян"]).

object_match(Words, Name, Score) :-
    object_stems(Name, Stems), all_stems(Words, Stems), length(Stems, Score).
select_object(Words, Name) :-
    findall(Score-Candidate, object_match(Words, Candidate, Score), Matches),
    Matches \= [], keysort(Matches, Sorted), reverse(Sorted, [_-Name|_]).

process_query(Text) :-
    tokens(Text, Words),
    ( action(Words, Action) -> dispatch(Action, Words)
    ; writeln('Не удалось определить действие. Используйте слова «опиши», «проверь» или «определи».') ).

dispatch(describe, Words) :-
    ( select_object(Words, Name) -> describe_object(Name)
    ; writeln('Тип программы не найден в запросе.') ).
dispatch(verify, Words) :-
    ( select_object(Words, Name) -> verify_object(Name)
    ; writeln('Укажите тип программы, который нужно проверить.') ).
dispatch(classify, _) :- consult.

% test_case/2 — каталог профилей из лабораторной работы № 4.
describe_object(Name) :-
    test_case(Name, Yes),
    format('Тип: ~s~nХарактерные признаки:~n', [Name]),
    forall(member(Id, Yes), (cond(Id, Feature), format('- Программа ~s.~n', [Feature]))).
verify_object(Expected) :-
    clear_answers,
    ( once(diagnose(Actual)) ->
        ( Actual == Expected -> format('Подтверждено: ~s.~n', [Expected])
        ; format('Признаки соответствуют типу «~s», а не «~s».~n', [Actual, Expected]) )
    ; writeln('Ни один тип не соответствует ответам.') ),
    clear_answers.

% Проверки разбора свободных русских фраз и редактора без интерактивного ввода.
run_nl_tests :-
    tokens("Расскажи о ПОЧТОВОМ макровирусе!", W1),
    action(W1, describe), select_object(W1, "Почтовый макровирус"),
    tokens("Проверь скрытный файловый вирус", W2),
    action(W2, verify), select_object(W2, "Скрытный файловый вирус"),
    tokens("Определи тип программы", W3), action(W3, classify),
    \+ action(["неизвестное", "слово"], _),
    join_lines(["Опиши", "макровирус"], "Опиши макровирус"),
    replace_line(["первая", "ошибка"], 2, "вторая", ["первая", "вторая"]),
    delete_line(["первая", "вторая"], 1, ["вторая"]),
    writeln('Проверки русского интерфейса пройдены.').
run_all_tests :- run_tests, run_nl_tests.
