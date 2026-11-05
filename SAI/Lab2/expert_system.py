"""
Модуль продукционной экспертной системы обратного вывода с исчислением предикатов первого порядка.
Вариант №8: Отбор кандидатов на вакантное место.
"""

from typing import List, Dict, Any, Optional, Tuple, Set, Union
import json
import os

class Term:
    def __init__(self, value: str, is_var: bool = False):
        self.value = value
        self.is_var = is_var or value.startswith("?")

    def __repr__(self):
        return self.value

    def __eq__(self, other):
        if isinstance(other, Term):
            return self.value == other.value and self.is_var == other.is_var
        return False

    def __hash__(self):
        return hash((self.value, self.is_var))


class Predicate:
    def __init__(self, name: str, args: List[Union[str, Term]]):
        self.name = name
        self.args: List[Term] = [
            arg if isinstance(arg, Term) else Term(arg)
            for arg in args
        ]

    def substitute(self, bindings: Dict[str, str]) -> 'Predicate':
        new_args = []
        for a in self.args:
            if a.is_var and a.value in bindings:
                new_args.append(Term(bindings[a.value], is_var=False))
            else:
                new_args.append(Term(a.value, is_var=a.is_var))
        return Predicate(self.name, new_args)

    def match(self, ground: 'Predicate') -> Optional[Dict[str, str]]:
        if self.name != ground.name or len(self.args) != len(ground.args):
            return None
        bindings = {}
        for pat_arg, g_arg in zip(self.args, ground.args):
            if pat_arg.is_var:
                if pat_arg.value in bindings:
                    if bindings[pat_arg.value] != g_arg.value:
                        return None
                else:
                    bindings[pat_arg.value] = g_arg.value
            else:
                if pat_arg.value != g_arg.value:
                    return None
        return bindings

    def is_ground(self) -> bool:
        return all(not a.is_var for a in self.args)

    def to_string(self) -> str:
        args_str = ", ".join(a.value for a in self.args)
        return f"{self.name}({args_str})"

    def __repr__(self):
        return self.to_string()

    def __eq__(self, other):
        if isinstance(other, Predicate):
            return self.name == other.name and [a.value for a in self.args] == [a.value for a in other.args]
        return False

    def __hash__(self):
        return hash((self.name, tuple(a.value for a in self.args)))


class Rule:
    def __init__(
        self,
        rule_id: str,
        name: str,
        premises: Dict[str, Any],
        conclusion: Union[Predicate, Dict[str, Any]],
        priority: int = 10,
        author: str = "HR_Expert"
    ):
        self.id = rule_id
        self.name = name
        self.premises = premises  # {'and': [Predicate, ...]} or {'or': [...]}
        if isinstance(conclusion, Predicate):
            self.conclusion = conclusion
        else:
            self.conclusion = Predicate(conclusion['pred'], conclusion['args'])
        self.priority = priority
        self.author = author

    def get_premises_list(self) -> List[Predicate]:
        if 'and' in self.premises:
            return [p if isinstance(p, Predicate) else Predicate(p['pred'], p['args']) for p in self.premises['and']]
        elif 'or' in self.premises:
            return [p if isinstance(p, Predicate) else Predicate(p['pred'], p['args']) for p in self.premises['or']]
        return []

    def __repr__(self):
        prem_type = " AND " if 'and' in self.premises else " OR "
        prems = prem_type.join(str(p) for p in self.get_premises_list())
        return f"[{self.id}] IF {prems} THEN {self.conclusion} (Priority: {self.priority})"


class ProofNode:
    """Узел дерева логического вывода для подсистемы объяснений и визуализации."""
    def __init__(
        self,
        goal: Predicate,
        rule: Optional[Rule] = None,
        success: bool = False,
        children: Optional[List['ProofNode']] = None,
        is_fact: bool = False,
        explanation_step: int = 0,
        failed_condition: Optional[str] = None
    ):
        self.goal = goal
        self.rule = rule
        self.success = success
        self.children: List['ProofNode'] = children or []
        self.is_fact = is_fact
        self.step = explanation_step
        self.failed_condition = failed_condition

    def add_child(self, child: 'ProofNode'):
        self.children.append(child)


class BackwardChainingEngine:
    """Интерпретатор продукционных правил с обратным выводом."""
    def __init__(self, conflict_resolution_strategy: str = "priority_first"):
        self.rules: List[Rule] = []
        self.working_memory: Set[Predicate] = set()
        self.conflict_resolution_strategy = conflict_resolution_strategy
        self.derivation_log: List[str] = []
        self.explanation_steps: List[Dict[str, Any]] = []
        self.step_counter = 0

    def load_rules_from_file(self, filepath: str):
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
            self.rules = []
            for r_data in data['rules']:
                conclusion = Predicate(r_data['conclusion']['pred'], r_data['conclusion']['args'])
                premises = {}
                for k, v in r_data['premises'].items():
                    premises[k] = [Predicate(p['pred'], p['args']) for p in v]
                rule = Rule(
                    rule_id=r_data['id'],
                    name=r_data.get('name', ''),
                    premises=premises,
                    conclusion=conclusion,
                    priority=r_data.get('priority', 10)
                )
                self.rules.append(rule)
        self.derivation_log.append(f"Загружено {len(self.rules)} правил из файла {filepath}.")

    def load_facts_from_file(self, filepath: str, candidate_name: Optional[str] = None):
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
            self.working_memory.clear()
            candidates = data.get('candidates', {})
            if candidate_name and candidate_name in candidates:
                for f_data in candidates[candidate_name]:
                    self.working_memory.add(Predicate(f_data['pred'], f_data['args']))
                self.derivation_log.append(
                    f"Загружено {len(self.working_memory)} фактов для кандидата '{candidate_name}'."
                )
            elif candidate_name:
                raise ValueError(f"Кандидат {candidate_name} не найден в базе фактов.")
            else:
                first_cand = next(iter(candidates))
                for f_data in candidates[first_cand]:
                    self.working_memory.add(Predicate(f_data['pred'], f_data['args']))
                self.derivation_log.append(
                    f"Загружены факты первого кандидата '{first_cand}' ({len(self.working_memory)} шт.)."
                )

    def add_fact(self, fact: Predicate):
        """Добавление нового факта в рабочую память во время работы системы."""
        self.working_memory.add(fact)
        self.derivation_log.append(f"[ПОПОЛНЕНИЕ РП] Добавлен факт: {fact.to_string()}")

    def add_rule(self, rule: Rule):
        """Динамическое добавление нового правила в базу знаний."""
        # Проверка на дубликат по id
        self.rules = [r for r in self.rules if r.id != rule.id]
        self.rules.append(rule)
        self.derivation_log.append(f"[ПОПОЛНЕНИЕ БЗ] Добавлено правило [{rule.id}]: {rule.name}")

    def save_rules_to_file(self, filepath: str):
        """Сохранение пополненной базы правил во внешний файл без изменения исходного кода."""
        rules_data = []
        for r in self.rules:
            prem_dict = {}
            if 'and' in r.premises:
                prem_dict['and'] = [
                    {"pred": p.name, "args": [a.value for a in p.args]}
                    for p in r.get_premises_list()
                ]
            else:
                prem_dict['or'] = [
                    {"pred": p.name, "args": [a.value for a in p.args]}
                    for p in r.get_premises_list()
                ]
            rules_data.append({
                "id": r.id,
                "name": r.name,
                "priority": r.priority,
                "premises": prem_dict,
                "conclusion": {
                    "pred": r.conclusion.name,
                    "args": [a.value for a in r.conclusion.args]
                }
            })
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump({"rules": rules_data}, f, ensure_ascii=False, indent=2)
        self.derivation_log.append(f"[ЭКСПОРТ БЗ] База правил сохранена в файл {filepath}")

    def save_facts_to_file(self, filepath: str, candidate_name: str):
        """Сохранение пополненных фактов рабочей памяти кандидата во внешний файл."""
        data = {}
        if os.path.exists(filepath):
            with open(filepath, 'r', encoding='utf-8') as f:
                try:
                    data = json.load(f)
                except Exception:
                    data = {}
        if "candidates" not in data:
            data["candidates"] = {}

        candidate_facts = []
        for f in self.working_memory:
            candidate_facts.append({
                "pred": f.name,
                "args": [a.value for a in f.args]
            })
        data["candidates"][candidate_name] = candidate_facts

        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        self.derivation_log.append(f"[ЭКСПОРТ РП] Факты кандидата {candidate_name} сохранены в {filepath}")

    def resolve_conflict(self, candidate_rules: List[Rule]) -> List[Rule]:
        """Разрешение конфликта среди применимых правил."""
        if self.conflict_resolution_strategy == "priority_first":
            # Сортировка: максимальный приоритет -> наибольшая специфичность (число посылок) -> исходный порядок
            return sorted(
                candidate_rules,
                key=lambda r: (r.priority, len(r.get_premises_list())),
                reverse=True
            )
        elif self.conflict_resolution_strategy == "specificity_first":
            return sorted(
                candidate_rules,
                key=lambda r: (len(r.get_premises_list()), r.priority),
                reverse=True
            )
        return candidate_rules

    def prove_goal(
        self,
        goal: Predicate,
        call_depth: int = 0,
        visited_goals: Optional[Set[str]] = None
    ) -> Tuple[bool, ProofNode]:
        """
        Рекурсивное доказательство цели методом обратного вывода (Backward Chaining).
        """
        if visited_goals is None:
            visited_goals = set()

        indent = "  " * call_depth
        goal_str = goal.to_string()

        # Защита от зацикливания
        if goal_str in visited_goals:
            self.derivation_log.append(f"{indent}[ЦИКЛ] Повторное обращение к цели {goal_str}, возврат False.")
            return False, ProofNode(goal, success=False, failed_condition="Обнаружен цикл в графе")

        # 1. Проверка рабочей памяти
        for fact in self.working_memory:
            bindings = goal.match(fact)
            if bindings is not None:
                self.step_counter += 1
                self.derivation_log.append(f"{indent}[ФАКТ В БД] Цель {goal_str} подтверждена фактом из РП: {fact}")
                node = ProofNode(goal, success=True, is_fact=True, explanation_step=self.step_counter)
                self.explanation_steps.append({
                    "step": self.step_counter,
                    "type": "fact",
                    "goal": goal_str,
                    "fact": fact.to_string(),
                    "text": f"Утверждение {goal_str} является исходным фактом в базе данных."
                })
                return True, node

        # 2. Поиск правил, заключение которых унифицируется с целью
        matching_rules: List[Tuple[Rule, Dict[str, str]]] = []
        for rule in self.rules:
            bindings = rule.conclusion.match(goal)
            if bindings is not None:
                matching_rules.append((rule, bindings))

        if not matching_rules:
            self.derivation_log.append(f"{indent}[НЕУДАЧА] Для цели {goal_str} нет правил в БЗ и факта в РП.")
            return False, ProofNode(goal, success=False, failed_condition=f"Отсутствует факт/правило для {goal_str}")

        # 3. Разрешение конфликта: сортировка правил
        candidate_rules = [r for r, _ in matching_rules]
        ordered_rules = self.resolve_conflict(candidate_rules)
        rule_bindings = {r.id: b for r, b in matching_rules}

        self.derivation_log.append(
            f"{indent}[КОНФЛИКТНЫЙ НАБОР] Для {goal_str} найдено {len(ordered_rules)} правил: "
            f"{[r.id for r in ordered_rules]} (порядок после разрешения конфликта: {self.conflict_resolution_strategy})"
        )

        visited_goals.add(goal_str)

        # 4. Проверка правил по порядку
        for rule in ordered_rules:
            b = rule_bindings[rule.id]
            self.derivation_log.append(f"{indent}[ПРОБА] Правило {rule.id} (Приоритет {rule.priority}): {rule.name}")
            node = ProofNode(goal, rule=rule, success=False)
            rule_success = True
            failed_cond = None

            is_and = 'and' in rule.premises
            prems = rule.get_premises_list()

            if is_and:
                # Все посылки должны быть доказаны (AND)
                for prem in prems:
                    sub_prem = prem.substitute(b)
                    sub_success, sub_node = self.prove_goal(sub_prem, call_depth + 1, visited_goals.copy())
                    node.add_child(sub_node)
                    if not sub_success:
                        rule_success = False
                        failed_cond = sub_prem.to_string()
                        self.derivation_log.append(
                            f"{indent}  [ОТКЛОНЕНО] Правило {rule.id}: не выполнено условие {failed_cond}"
                        )
                        break
            else:
                # Хотя бы одна посылка (OR)
                or_success = False
                for prem in prems:
                    sub_prem = prem.substitute(b)
                    sub_success, sub_node = self.prove_goal(sub_prem, call_depth + 1, visited_goals.copy())
                    node.add_child(sub_node)
                    if sub_success:
                        or_success = True
                        break
                rule_success = or_success
                if not rule_success:
                    failed_cond = "Ни одно из альтернативных условий OR не выполнено"

            if rule_success:
                self.step_counter += 1
                node.success = True
                node.step = self.step_counter
                self.derivation_log.append(
                    f"{indent}[УСПЕХ] Цель {goal_str} выведена с помощью правила {rule.id}!"
                )
                self.working_memory.add(goal)
                self.explanation_steps.append({
                    "step": self.step_counter,
                    "type": "rule",
                    "goal": goal_str,
                    "rule_id": rule.id,
                    "rule_name": rule.name,
                    "premises": [p.substitute(b).to_string() for p in prems],
                    "text": f"Утверждение {goal_str} доказано по правилу {rule.id} ({rule.name})."
                })
                visited_goals.remove(goal_str)
                return True, node
            else:
                node.failed_condition = failed_cond

        visited_goals.remove(goal_str)
        return False, ProofNode(goal, success=False, failed_condition=f"Не удалось доказать ни одно правило для {goal_str}")

    def explain_why(self, question_premise: str) -> str:
        """
        Подсистема объяснения: ответ на вопрос пользователя ПОЧЕМУ?
        Зачем системе потребовалось выяснять истинность данного факта/условия.
        """
        for r in self.rules:
            for p in r.get_premises_list():
                if p.name in question_premise or question_premise in p.name:
                    return (
                        f"Вопрос о '{question_premise}' необходим для проверки условия правила [{r.id}]: '{r.name}',\n"
                        f"чтобы сделать вывод о заключении: '{r.conclusion.to_string()}'."
                    )
        return f"Условие '{question_premise}' является частью гипотезы текущего логического вывода."

    def explain_how(self, goal_str: str) -> str:
        """
        Подсистема объяснения: ответ на вопрос пользователя КАК N?
        Как именно было доказано конкретное утверждение.
        """
        for item in reversed(self.explanation_steps):
            if goal_str in item['goal']:
                if item['type'] == 'fact':
                    return f"Утверждение '{goal_str}' является исходным фактом в рабочей памяти кандидата."
                elif item['type'] == 'rule':
                    prems_str = "\n  - ".join(item['premises'])
                    return (
                        f"Утверждение '{goal_str}' выведено по правилу [{item['rule_id']}]:\n"
                        f"Название: {item['rule_name']}\n"
                        f"Были подтверждены все посылки:\n  - {prems_str}"
                    )
        return f"Утверждение '{goal_str}' не найдено среди успешно доказанных шагов вывода."
