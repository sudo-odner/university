% Логическая ЭС: знания хранятся в фактах object/4 и cond/2.
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
object(file_basic,"Файловый вирус",[1],[2,3,4,5,6,7,8,9,10]).
object(file_polymorphic,"Полиморфный файловый вирус",[1,9],[2,3,4,5,6,7,8,10]).
object(file_stealth,"Скрытный файловый вирус",[1,8],[2,3,4,5,6,7,9,10]).
object(boot_basic,"Загрузочный вирус",[2],[1,3,4,5,6,7,8,9,10]).
object(boot_removable,"Загрузочный вирус на съёмном носителе",[2,10],[1,3,4,5,6,7,8,9]).
object(macro_basic,"Макровирус",[3],[1,2,4,5,6,7,8,9,10]).
object(macro_mail,"Почтовый макровирус",[3,5],[1,2,4,6,7,8,9,10]).
object(worm_network,"Сетевой червь",[4],[1,2,3,5,6,7,8,9,10]).
object(worm_mail,"Почтовый червь",[4,5],[1,2,3,6,7,8,9,10]).
object(ransomware,"Шифровальщик",[6],[1,2,3,4,5,7,8,9,10]).
object(spyware,"Шпионская программа",[7],[1,2,3,4,5,6,8,9,10]).
object(trojan_stealth,"Скрытный троян",[7,8],[1,2,3,4,5,6,9,10]).

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

check_yes([]).
check_yes([Id|Rest]) :- positive(Id), check_yes(Rest).
check_no([]).
check_no([Id|Rest]) :- negative(Id), check_no(Rest).
diagnose(Name) :- object(_, Name, RequiredYes, RequiredNo),
    check_yes(RequiredYes), check_no(RequiredNo).

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
