:- dynamic yes/1, no/1.
/* База знаний. */

/* Размещение в резидентной БД информации из утверждений БЗ ЭС */

assert_database:-
        rule(Rule_number,Category,Type_of_breed,Conditions),
        assertz(d_rule(Rule_number,Category,Type_of_breed,Conditions)),fail.

assert_database:-
        cond(Cond_number,Condition),
        assertz(d_cond(Cond_number,Condition)),fail.

assert_database:-!.

/* Условия-характеристики различных пород.*/
   
     cond(1,"короткошерстная порода").
     cond(2,"длинношерстная порода").
     cond(3,"рост меньше 22 дюймов").
     cond(4,"рост больше 30 дюймов").
     cond(5,"свисающий хвост").
     cond(6,"длинные уши").
     cond(7,"хороший характер").
     cond(8,"вес более 100 фунтов").
     
/* Данные о типах породы */

     topic("короткошерстная").
     topic("длинношерстная").

/* Данные о конкретных породах */

     rule(1,"собака","короткошерстная",[1]).
     rule(2,"собака","длинношерстная",[2]).
     rule(3,"короткошерстная","английский бульдог",[3,5,7]).
     rule(4,"короткошерстная","гончая",[3,6,7]).
     rule(5,"короткошерстная","датский дог",[5,6,7,8]).
     rule(6,"короткошерстная","американский фокстерьер",[4,6,7]).
     rule(7,"длинношерстная","коккер-спаниэль",[3,5,6,7]).
     rule(8,"длинношерстная","ирландский сеттер",[4,6]).
     rule(9,"длинношерстная","колли",[4,5,7]).
     rule(10,"длинношерстная","сенбернар",[5,7,8]).
   
/* Система пользовательского интерфейса */

main:-
        assert_database,
        do_expert_job.
     
do_expert_job:-
        do_consulting,
        ask_repeat.
                     
do_consulting:-
        goes(First_keyword),go([],First_keyword),
        clear,!.
     
do_consulting:-
        write("Информация об интересующей Вас породе отсутствует в БЗ."), nl,
        clear.
     
ask_repeat:-
        write(" Повторить консультацию ?"),nl,
        write("1 - да,"),nl,
        write("2 - нет"),nl,
        readloop(Reply),
        end_or_repat(Reply).
         
end_or_repat(1):-
        main.
     
end_or_repat(2):-
        halt.
     
/* Выдача подсказки */
     
info:-
        write("База знаний содержит информацию о типах пород собак: "), nl,
        topic(X), 
        format('\t~w~n',[X]).
     
/* Запрос и получение ответов yes и no от пользователя */
     
ask_question(Breed_cond,Text):-
        format("Вопрос : ~a?~n",[Text]),
        write("1 - да,"),nl,
        write("2 - нет"),nl,
        readloop(Response),
        do_answer(Breed_cond,Response).
     
/* Проверка корректности ввода */
     
readloop(Response):-
        read_string(user_input, "\n", "\r",_,Str),
        atom_number(Str, Response),
        legal_response(Response),!.
     
legal_response(Response):-Response=1.
legal_response(Response):-Response=2.
     
/* Предикаты ЕЯ-интерфейса */

/* Принадлежность элемента списку */
     
member(Head,[Head|_]):-!.
member(Elem,[_|T]):-
        member(Elem,T).
     
/* Подсчет символов в строке до конца строки, либо ближайшего пробела, 
   символа возврата каретки, перевода строки, !,",#,$ */
     
frontchar('', '', ''):-!.
frontchar(S, H, T):-
        sub_string(S, 0, 1, _, H),
        string_length(S, L),
        L1 is L-1,
        sub_string(S, 1, L1, _, T).
     
/* у пустой строки нет подстрок */

frontstr(_, '', '', ''):-!.

/* если позиция = 0, то вся строка помещается во вторую подстроку */

frontstr(0, S, '', S):-!.

/* вычисляем подстроки напрямую */

frontstr(I, S, S1, S2):-
        sub_string(S, 0, I, _, S1),
        string_length(S, L),
        L1 is L-I,
        sub_string(S, I, L1, _, S2).
     
symbol_counter(Str,0):-
        frontchar(Str,' ',_),!.
symbol_counter(Str,0):-
        frontchar(Str,',',_),!.
symbol_counter(Str,0):-
        frontchar(Str,'.',_),!.
symbol_counter(Str,0):-
        frontchar(Str,'!',_),!.
symbol_counter(Str,0):-
        frontchar(Str,'?',_),!.
symbol_counter(Str,0):-
        frontchar(Str,':',_),!.
symbol_counter(Str,0):-
        frontchar(Str,'"',_),!.
     
symbol_counter("",0).
symbol_counter(Str,Number):-
        frontchar(Str,_,Rest_of_string),
        symbol_counter(Rest_of_string,Number1),
        plus(Number1,1,Number).
     
/* Выделение подстроки до первого разделителя */
     
fronttoken_cyr(Str,Token,Rest_of_string):-
        symbol_counter(Str,Number),
        frontstr(Number,Str,Token,Rest_of_string).
     
/* Удаление разделителя в начале строки */
     
del_front_space("","").
del_front_space(Arg,Res):-
        frontchar(Arg,Char,Res1),Char=" ",!,
        del_front_space(Res1,Res).
del_front_space(Arg,Res):-
        frontchar(Arg,Char,Res1),Char=",",!,
        del_front_space(Res1,Res).
del_front_space(Arg,Res):-
        frontchar(Arg,Char,Res1),Char=".",!,
        del_front_space(Res1,Res).
del_front_space(Arg,Res):-
        frontchar(Arg,Char,Res1),Char="!",!,
        del_front_space(Res1,Res).
del_front_space(Arg,Res):-
        frontchar(Arg,Char,Res1),Char="?",!,
        del_front_space(Res1,Res).
del_front_space(Arg,Res):-
        frontchar(Arg,Char,Res1),Char=":",!,
        del_front_space(Res1,Res).
del_front_space(Arg,Res):-
        frontchar(Arg,Char,Res1),Char="\"",!,
        del_front_space(Res1,Res).
del_front_space(Arg,Arg):-
        frontchar(Arg,Char,_),not(member(Char,[' ',',','.','!','?',':','\"'])).
     
/* Модифицированное правило преобразования строки в список символов */
     
convers("",[]):-!.
convers(Str,[Head|Tail]):-
        fronttoken_cyr(Str,Head,Str2),
        del_front_space(Str2,Str1),
        convers(Str1,Tail).
     
/* Проверка правильности ключевого слова */
     
keyword(Keyword):-
        rule(_,Keyword,_,_),!.
keyword(Keyword):-
        rule(_,_,Keyword,_),!.
     
/* Поиск первого ключевого слова в высказывании пользователя */
     
first_keyword_in_sentence([Head|_],Head):-
        keyword(Head).
     
first_keyword_in_sentence([_|Tail],First_keyword):-
        first_keyword_in_sentence(Tail,First_keyword).
     
first_keyword_in_sentence([],_):-fail.
     
/* Ввод запроса на естественном (русском) языке */
     
goes(Bag):-
        write("Введите Ваш запрос: "), nl,
        read_string(user_input, "\n", "\r", _, Str),
        convers(Str, Mygoal),
        bagof(First_keyword,
        first_keyword_in_sentence(Mygoal,First_keyword),Bag),!.
     
/* Механизм вывода */
     
/* Начальное правило механизма вывода */
     
go(_,[]):-
        !,fail.
     
go(_,[Mygoal|_]):-
        not(rule(_,Mygoal,_,_)),!,
        format("Рекомендуемая порода: ~a.~n",[Mygoal]).
     
go(History,[Mygoal|_]):-
        rule(Rule_number,Mygoal,Type_of_breed,Conditions),
        check(Rule_number,History,Conditions),
        go([Rule_number|History],[Type_of_breed]).
                   
go(History,[_|Tail]):-
        go(History,Tail).
          
/* Сопоставление входных данных пользователя со списками атрибутов
   отдельных пород собак */

check(Rule_number,History,[Breed_cond|Rest_breed_cond_list]):-
        yes(Breed_cond),!,
        check(Rule_number,History,Rest_breed_cond_list).

check(_,_,[Breed_cond|_]):-
        no(Breed_cond),!,fail.

check(Rule_number,History,[Breed_cond|Rest_breed_cond_list]):-
        cond(Breed_cond,Text),
        ask_question(Breed_cond,Text),
        check(Rule_number,History,Rest_breed_cond_list).

check(_,_,[]).

do_answer(Cond_number,1):-!,
        assertz(yes(Cond_number)).

do_answer(Cond_number,2):-!,
        assertz(no(Cond_number)),fail.
           
/* Исключение данных из базы знаний 
   после завершения цикла "Распознавание-действие" */

erase:-
        retract(_),fail.
erase.

/* Уничтожение в базе данных всех ответов yes (да) и no (нет) */

clear:-
        retract(yes(_)),
        retract(no(_)),
        retract(goes(_,_)),fail,!.
clear.

:- main.