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
