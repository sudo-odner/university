:- dynamic xpositive/2, xnegative/2.

/* Система пользовательского интерфейса */

do_expert_job:-
      do_consulting.

do_consulting:-
      dog_is(X),!,
      format("Рекомендуемая порода: ~a.~n",[X]),
      clear.

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


/* Уничтожение в базе данных всех ответов yes (да) и no (нет) */

   clear:-retract(xpositive(_,_)),retract(xnegative(_,_)),fail,!.
   clear.

%:- do_expert_job.
