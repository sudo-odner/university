:- dynamic xpositive/2, xnegative/2.

/* Система пользовательского интерфейса */

do_expert_job:-
      do_consulting.

do_consulting:-
      goes,
      clear,!.

do_consulting:-
      write("Информация об интересующей Вас породе отсутствует в БЗ."),nl,
      clear.

ask(X,Y):-
      format("Вопрос : ~a ~a?~n",[X,Y]),
      write("1 - да,"),nl,
      write("2 - нет"),nl,
      readloop(Reply),
      remember(X,Y,Reply).

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
      frontchar(Arg,Char,_),
      not(member(Char,[' ',',','.','!','?',':','\"'])).

/* Модифицированное правило преобразования строки в список
   символов */

convers("",[]):-!.
convers(Str,[Head|Tail]):-
      fronttoken_cyr(Str,Head,Str2),
      del_front_space(Str2,Str1),
      convers(Str1,Tail).

/* Поиск ключевых слов в высказывании пользователя */

keyword_in_sentence([]).

keyword_in_sentence([Head|Tail]):-
      keyword(Head),!,
      keyword_in_sentence(Tail).

keyword_in_sentence([Elem1,Elem2|Tail]):-
      keyword([Elem1,Elem2]),!,
      keyword_in_sentence(Tail).

keyword_in_sentence([_|Tail]):-
      keyword_in_sentence(Tail).

/* Ввод запроса на естественном (русском) языке */

goes:-
      write("Введите Ваш запрос : "), nl,
      read_string(user_input, "\n", "\r", _, Str),
      convers(Str, Mygoal),
      keyword_in_sentence(Mygoal),
      dog_is(Key),!,
      format("Рекомендуемая порода: ~a.~n",[Key]).

/* Механизм вывода экспертного заключения */

positive(X,Y):-
      xpositive(X,Y),!.

positive(X,Y):-
      not(getnegative(X,Y)),!,ask(X,Y).

getpositive(X,Y):-
      xpositive(X,Y),!.

getnegative(X,Y):-
      xnegative(X,Y),!.

negative(X,Y):-
      xnegative(X,Y),!.
negative(X,Y):-
      not(getpositive(X,Y)),!,not(ask(X,Y)).

remember(X,Y,1):-!,
      assertz(xpositive(X,Y)).

remember(X,Y,2):-!,
      assertz(xnegative(X,Y)),fail.

/* Продукционные правила */

     dog_is("английский бульдог"):-
           it_is("короткошерстная"),
           positive("имеет","рост меньше 22 дюймов"),
           positive("имеет","свисающий хвост"),
           positive("имеет","хороший характер"),!.

     dog_is("гончая"):-
           it_is("короткошерстная"),
           positive("имеет","рост меньше 22 дюймов"),
           positive("имеет","длинные уши"),
           positive("имеет","хороший характер"),!.

     dog_is("датский дог"):-
           it_is("короткошерстная"),
           positive("имеет","свисающий хвост"),
           positive("имеет","длинные уши"),
           positive("имеет","хороший характер"),
           positive("имеет","вес более 100 фунтов"),!.

     dog_is("американский фокстерьер"):-
           it_is("короткошерстная"),
           positive("имеет","рост больше 30 дюймов"),
           positive("имеет","длинные уши"),
           positive("имеет","хороший характер"),!.

     dog_is("коккер-спаниэль"):-
           it_is("длинношерстная"),
           positive("имеет","рост меньше 22 дюймов"),
           positive("имеет","свисающий хвост"),
           positive("имеет","длинные уши"),
           positive("имеет","хороший характер"),!.

     dog_is("ирландский сеттер"):-
           it_is("длинношерстная"),
           positive("имеет","рост больше 30 дюймов"),
           positive("имеет","длинные уши"),!.

     dog_is("колли"):-
           it_is("длинношерстная"),
           positive("имеет","рост больше 30 дюймов"),
           positive("имеет","свисающий хвост"),
           positive("имеет","хороший характер"),!.

     dog_is("сенбернар"):-
           it_is("длинношерстная"),
           positive("имеет","свисающий хвост"),
           positive("имеет","хороший характер"),
           positive("имеет","вес более 100 фунтов"),!.

     it_is("короткошерстная"):-
           positive("собака имеет","короткую шерсть"),!.

     it_is("длинношерстная"):-
           positive("собака имеет","длинную шерсть"),!.

/* Ключевые слова */

keyword("короткошерстная"):-
      assertz(xpositive("собака имеет","короткую шерсть")),
      assertz(xnegative("собака имеет","длинную шерсть")),!.

keyword("длинношерстная"):-
      assertz(xpositive("собака имеет","длинную шерсть")),
      assertz(xnegative("собака имеет","короткую шерсть")),!.

keyword(["свисающий","хвост"]):-
      assertz(xpositive("имеет","свисающий хвост")),!.

keyword(["хороший","характер"]):-
      assertz(xpositive("имеет","хороший характер")),!.

/* Уничтожение в базе данных всех ответов yes (да) и no (нет) */

clear:-
      retract(xpositive(_,_)),
      retract(xnegative(_,_)),fail,!.
clear.

%:- do_expert_job.
