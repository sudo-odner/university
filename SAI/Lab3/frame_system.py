"""
Фреймовая модель представления знаний и управление выводом.

Базовая единица — фрейм со слотами. У слота есть имя, указатель наследования
(S/U/R), тип данных, значение, демон и присоединенная процедура.
Вывод выполняется тремя способами: демон, служебная процедура, наследование.
"""

import copy
import json
from dataclasses import dataclass, field
from typing import Any, Callable, Dict, Iterable, List, Optional, Tuple


DATA_TYPES = ("string", "integer", "real", "boolean", "frame", "list", "enum")
# S — same: значение наследуется по связи AKO
# U — unique: значение задаётся только в этом фрейме
# R — range: от предка берётся ограничение, но не само значение
INHERITANCE = ("S", "U", "R")

Procedure = Callable[["FrameBase", str], Any]
SlotTrigger = Callable[["FrameBase", str, Any], None]


class KnowledgeError(Exception):
    """Ошибка редактора фреймов или исполнительного механизма."""


@dataclass
class SlotDef:
    name: str
    data_type: str
    inheritance: str = "S"
    default: Any = None
    if_needed: Optional[str] = None
    if_added: Optional[str] = None
    if_removed: Optional[str] = None
    procedure: Optional[str] = None
    facets: Dict[str, Any] = field(default_factory=dict)

    def to_dict(self) -> dict:
        return {
            "name": self.name,
            "data_type": self.data_type,
            "inheritance": self.inheritance,
            "default": self.default,
            "if_needed": self.if_needed,
            "if_added": self.if_added,
            "if_removed": self.if_removed,
            "procedure": self.procedure,
            "facets": self.facets,
        }

    @staticmethod
    def from_dict(data: dict) -> "SlotDef":
        return SlotDef(
            name=data["name"],
            data_type=data["data_type"],
            inheritance=data.get("inheritance", "S"),
            default=data.get("default"),
            if_needed=data.get("if_needed"),
            if_added=data.get("if_added"),
            if_removed=data.get("if_removed"),
            procedure=data.get("procedure"),
            facets=dict(data.get("facets") or {}),
        )


@dataclass
class Frame:
    name: str
    parent: Optional[str] = None
    frame_type: str = "prototype"  # prototype | instance
    slot_defs: Dict[str, SlotDef] = field(default_factory=dict)
    values: Dict[str, Any] = field(default_factory=dict)
    computed: Dict[str, Any] = field(default_factory=dict)
    origins: Dict[str, str] = field(default_factory=dict)

    def to_dict(self) -> dict:
        return {
            "name": self.name,
            "parent": self.parent,
            "frame_type": self.frame_type,
            "slot_defs": [slot.to_dict() for slot in self.slot_defs.values()],
            "values": self.values,
            "origins": self.origins,
        }

    @staticmethod
    def from_dict(data: dict) -> "Frame":
        frame = Frame(
            name=data["name"],
            parent=data.get("parent"),
            frame_type=data.get("frame_type", "prototype"),
            values=dict(data.get("values") or {}),
            origins=dict(data.get("origins") or {}),
        )
        for raw in data.get("slot_defs") or []:
            slot = SlotDef.from_dict(raw)
            frame.slot_defs[slot.name] = slot
        return frame


@dataclass
class TraceStep:
    mechanism: str
    frame: str
    slot: str
    detail: str
    value: Any = None

    def format(self) -> str:
        shown = "∅" if self.value is None else repr(self.value)
        return (
            f"[{self.mechanism}] {self.frame}.{self.slot} = {shown}. {self.detail}"
        )


@dataclass
class Condition:
    slot: str
    op: str
    value: Any


@dataclass
class Query:
    frame_class: str
    conditions: List[Condition]
    procedure: str = "рекомендация"

    def format(self) -> str:
        body = " И ".join(
            f"{item.slot} {item.op} {item.value}" for item in self.conditions
        )
        return f"{self.frame_class} | {body}"


def _is_number(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool)


class FrameBase:
    """База фреймов и системные функции управления выводом."""

    def __init__(self) -> None:
        self.frames: Dict[str, Frame] = {}
        self.procedures: Dict[str, Procedure] = {}
        self.triggers: Dict[str, SlotTrigger] = {}
        self.trace: List[TraceStep] = []
        self._computing: set[Tuple[str, str]] = set()

    # ------------------------------------------------------------------
    # Регистрация процедурных знаний
    # ------------------------------------------------------------------

    def register_function(self, name: str, fn: Callable) -> None:
        if not name or not name.strip():
            raise KnowledgeError("Имя функции не задано.")
        self.procedures[name] = fn

    def register_trigger(self, name: str, fn: SlotTrigger) -> None:
        if not name or not name.strip():
            raise KnowledgeError("Имя демона не задано.")
        self.triggers[name] = fn

    def function_registered(self, name: str) -> bool:
        return name in self.procedures or name in self.triggers

    # ------------------------------------------------------------------
    # a) Функции управления фреймами
    # ------------------------------------------------------------------

    def create_frame(
        self,
        name: str,
        parent: Optional[str] = None,
        frame_type: str = "prototype",
    ) -> Frame:
        self._check_new_frame(name, parent, frame_type)
        frame = Frame(name=name.strip(), parent=parent, frame_type=frame_type)
        self.frames[frame.name] = frame
        return frame

    def destroy_frame(self, name: str) -> None:
        self._require_frame(name)
        children = self.list_depth_one(name)
        if children:
            raise KnowledgeError(
                f"Нельзя удалить фрейм «{name}»: есть потомки {children}."
            )
        del self.frames[name]

    def get_frame(self, name: str) -> Frame:
        return self._require_frame(name)

    def get_frame_type(self, name: str) -> str:
        return self._require_frame(name).frame_type

    def get_slot_names(self, name: str) -> List[str]:
        self._require_frame(name)
        ordered: List[str] = []
        chain = list(reversed(self._ancestor_names(name))) + [name]
        for node_name in chain:
            for slot_name in self.frames[node_name].slot_defs:
                if slot_name not in ordered:
                    ordered.append(slot_name)
        return ordered

    def inherit_frame_type(self, child: str, parent: str) -> List[str]:
        """Копирует на потомка определения слотов предка, которых у него ещё нет."""
        child_frame = self._require_frame(child)
        parent_frame = self._require_frame(parent)
        if child_frame.parent != parent:
            self.set_subclass(child, parent)
        copied: List[str] = []
        for slot_name, slot in parent_frame.slot_defs.items():
            if slot_name not in child_frame.slot_defs:
                child_frame.slot_defs[slot_name] = copy.deepcopy(slot)
                copied.append(slot_name)
        return copied

    def set_subclass(self, child: str, parent: str) -> None:
        child_frame = self._require_frame(child)
        parent_frame = self._require_frame(parent)
        if parent_frame.frame_type != "prototype":
            raise KnowledgeError(
                f"Родителем может быть только фрейм-прототип, а «{parent}» — экземпляр."
            )
        self._ensure_no_cycle(child, parent)
        child_frame.parent = parent

    def hierarchy(self) -> List[Tuple[str, str]]:
        edges = []
        for frame in self.frames.values():
            if frame.parent:
                edges.append((frame.parent, frame.name))
        return edges

    def hierarchy_tree(self) -> str:
        children: Dict[str, List[str]] = {name: [] for name in self.frames}
        roots = []
        for frame in self.frames.values():
            if frame.parent and frame.parent in self.frames:
                children[frame.parent].append(frame.name)
            else:
                roots.append(frame.name)
        lines: List[str] = []

        def walk(node: str, prefix: str, last: bool) -> None:
            frame = self.frames[node]
            mark = "экз." if frame.frame_type == "instance" else "прототип"
            branch = "└─ " if last else "├─ "
            lines.append(f"{prefix}{branch}{node} ({mark})")
            nxt = sorted(children[node])
            extension = "   " if last else "│  "
            for index, kid in enumerate(nxt):
                walk(kid, prefix + extension, index == len(nxt) - 1)

        ordered_roots = sorted(roots)
        for index, root in enumerate(ordered_roots):
            frame = self.frames[root]
            mark = "экз." if frame.frame_type == "instance" else "прототип"
            lines.append(f"{root} ({mark})")
            kids = sorted(children[root])
            for kid_index, kid in enumerate(kids):
                walk(kid, "", kid_index == len(kids) - 1)
        return "\n".join(lines)

    # ------------------------------------------------------------------
    # b) Функции управления слотами
    # ------------------------------------------------------------------

    def create_slot(
        self,
        frame_name: str,
        slot_name: str,
        data_type: str,
        inheritance: str = "S",
        default: Any = None,
        if_needed: Optional[str] = None,
        if_added: Optional[str] = None,
        if_removed: Optional[str] = None,
        procedure: Optional[str] = None,
        facets: Optional[dict] = None,
    ) -> SlotDef:
        frame = self._require_frame(frame_name)
        if not slot_name or not str(slot_name).strip():
            raise KnowledgeError("Имя слота не задано.")
        slot_name = slot_name.strip()
        if slot_name in frame.slot_defs:
            raise KnowledgeError(
                f"Слот «{slot_name}» уже есть во фрейме «{frame_name}»."
            )
        if data_type not in DATA_TYPES:
            raise KnowledgeError(
                f"Неизвестный тип данных «{data_type}». Допустимо: {', '.join(DATA_TYPES)}."
            )
        if inheritance not in INHERITANCE:
            raise KnowledgeError(
                f"Неизвестный указатель наследования «{inheritance}». Допустимо: S, U, R."
            )
        for label, fname in (
            ("if-needed", if_needed),
            ("if-added", if_added),
            ("if-removed", if_removed),
            ("процедура", procedure),
        ):
            if fname and not self.function_registered(fname):
                raise KnowledgeError(
                    f"Для слота «{slot_name}» указан {label} «{fname}», "
                    "но функция не зарегистрирована."
                )
        facets = dict(facets or {})
        if default is not None:
            ok, message = self.check_data(data_type, default, facets)
            if not ok:
                raise KnowledgeError(
                    f"Значение по умолчанию слота «{slot_name}» не прошло проверку: {message}"
                )
        slot = SlotDef(
            name=slot_name,
            data_type=data_type,
            inheritance=inheritance,
            default=copy.deepcopy(default),
            if_needed=if_needed,
            if_added=if_added,
            if_removed=if_removed,
            procedure=procedure,
            facets=facets,
        )
        frame.slot_defs[slot_name] = slot
        return slot

    def destroy_slot(self, frame_name: str, slot_name: str) -> None:
        frame = self._require_frame(frame_name)
        if slot_name not in frame.slot_defs and slot_name not in frame.values:
            raise KnowledgeError(
                f"Во фрейме «{frame_name}» нет собственного слота «{slot_name}»."
            )
        frame.slot_defs.pop(slot_name, None)
        frame.values.pop(slot_name, None)
        frame.computed.pop(slot_name, None)
        frame.origins.pop(slot_name, None)

    def get_slot(self, frame_name: str, slot_name: str, compute: bool = False) -> dict:
        self._require_frame(frame_name)
        definition = self._find_def(frame_name, slot_name)
        if definition is None:
            raise KnowledgeError(
                f"Слот «{slot_name}» не определён во фрейме «{frame_name}» и у его предков."
            )
        value, source = self._read_value(frame_name, slot_name, record=False, compute=compute)
        return {
            "frame": frame_name,
            "name": slot_name,
            "defined_on": definition[0],
            "data_type": definition[1].data_type,
            "inheritance": definition[1].inheritance,
            "value": value,
            "source": source,
            "if_needed": definition[1].if_needed,
            "if_added": definition[1].if_added,
            "if_removed": definition[1].if_removed,
            "procedure": definition[1].procedure,
            "facets": self._merged_facets(frame_name, slot_name),
        }

    def get_slot_value(self, frame_name: str, slot_name: str) -> Any:
        value, _source = self._read_value(frame_name, slot_name, record=True)
        return value

    def get_slot_facet(self, frame_name: str, slot_name: str, facet: str) -> Any:
        self._require_frame(frame_name)
        if self._find_def(frame_name, slot_name) is None:
            raise KnowledgeError(
                f"Слот «{slot_name}» не определён во фрейме «{frame_name}»."
            )
        facets = self._merged_facets(frame_name, slot_name)
        if facet not in facets:
            raise KnowledgeError(
                f"У слота «{slot_name}» нет параметра «{facet}»."
            )
        return copy.deepcopy(facets[facet])

    def set_slot_value(
        self,
        frame_name: str,
        slot_name: str,
        value: Any,
        source: str = "присваивание",
    ) -> None:
        frame = self._require_frame(frame_name)
        definition = self._find_def(frame_name, slot_name)
        if definition is None:
            raise KnowledgeError(
                f"Нельзя записать слот «{slot_name}»: он не объявлен у «{frame_name}»."
            )
        _owner, slot = definition
        facets = self._merged_facets(frame_name, slot_name)
        ok, message = self.check_data(slot.data_type, value, facets)
        if not ok:
            raise KnowledgeError(
                f"Значение {value!r} слота «{frame_name}.{slot_name}» отклонено: {message}"
            )
        previous = frame.values.get(slot_name, None)
        changed = slot_name not in frame.values or previous != value
        frame.values[slot_name] = copy.deepcopy(value)
        frame.origins[slot_name] = (
            "собственное значение" if source == "присваивание" else source
        )
        self._add_trace(source, frame_name, slot_name, "Значение записано в слот.", value)
        if changed and slot.if_added:
            self._add_trace(
                "демон if-added",
                frame_name,
                slot_name,
                f"Запущен демон «{slot.if_added}».",
                value,
            )
            self.triggers[slot.if_added](self, frame_name, value)

    def clear_slot_value(self, frame_name: str, slot_name: str) -> None:
        frame = self._require_frame(frame_name)
        if slot_name not in frame.values and slot_name not in frame.computed:
            return
        definition = self._find_def(frame_name, slot_name)
        if slot_name in frame.values:
            old = frame.values.pop(slot_name)
        else:
            old = frame.computed.pop(slot_name)
        frame.computed.pop(slot_name, None)
        frame.origins.pop(slot_name, None)
        self._add_trace(
            "удаление значения",
            frame_name,
            slot_name,
            "Собственное значение слота удалено.",
            old,
        )
        if definition and definition[1].if_removed:
            self._add_trace(
                "демон if-removed",
                frame_name,
                slot_name,
                f"Запущен демон «{definition[1].if_removed}».",
                old,
            )
            self.triggers[definition[1].if_removed](self, frame_name, old)

    def set_slot_facet(
        self, frame_name: str, slot_name: str, facet: str, value: Any
    ) -> None:
        frame = self._require_frame(frame_name)
        if slot_name not in frame.slot_defs:
            raise KnowledgeError(
                f"Параметр можно задать только у слота, объявленного во фрейме «{frame_name}»."
            )
        frame.slot_defs[slot_name].facets[facet] = copy.deepcopy(value)

    # ------------------------------------------------------------------
    # c) Вызов присоединенной процедуры
    # ------------------------------------------------------------------

    def call_procedure(self, frame_name: str, name: str) -> Any:
        self._require_frame(frame_name)
        procedure_name = name
        slot_name = name
        definition = None
        if self._find_def(frame_name, name):
            definition = self._find_def(frame_name, name)
            if definition[1].procedure:
                procedure_name = definition[1].procedure
        if procedure_name not in self.procedures:
            raise KnowledgeError(
                f"Процедура «{procedure_name}» не зарегистрирована."
            )
        self._add_trace(
            "служебная процедура",
            frame_name,
            slot_name,
            f"Запуск присоединенной процедуры «{procedure_name}».",
            None,
        )
        return self.procedures[procedure_name](self, frame_name)

    # ------------------------------------------------------------------
    # d) Функции проверки
    # ------------------------------------------------------------------

    def check_data(self, data_type: str, value: Any, facets: Optional[dict] = None) -> Tuple[bool, str]:
        facets = facets or {}
        if value is None:
            return True, ""
        if data_type == "string":
            if not isinstance(value, str):
                return False, "ожидалась строка"
        elif data_type == "integer":
            if isinstance(value, bool) or not isinstance(value, int):
                return False, "ожидалось целое число"
        elif data_type == "real":
            if isinstance(value, bool) or not isinstance(value, (int, float)):
                return False, "ожидалось число"
        elif data_type == "boolean":
            if not isinstance(value, bool):
                return False, "ожидалось логическое значение"
        elif data_type == "list":
            if not isinstance(value, list):
                return False, "ожидался список"
        elif data_type == "frame":
            if not isinstance(value, str) or value not in self.frames:
                return False, "ожидалось имя существующего фрейма"
        elif data_type == "enum":
            domain = facets.get("domain") or []
            if value not in domain:
                return False, f"значение вне домена {domain}"
        else:
            return False, f"неизвестный тип {data_type}"

        if _is_number(value):
            if "min" in facets and value < facets["min"]:
                return False, f"меньше минимума {facets['min']}"
            if "max" in facets and value > facets["max"]:
                return False, f"больше максимума {facets['max']}"
        return True, ""

    def frame_defined(self, name: str) -> bool:
        return name in self.frames

    # ------------------------------------------------------------------
    # e) Прочие функции
    # ------------------------------------------------------------------

    def list_depth_one(self, name: str) -> List[str]:
        self._require_frame(name)
        return sorted(
            frame.name for frame in self.frames.values() if frame.parent == name
        )

    def list_intersection(self, left: Iterable, right: Iterable) -> list:
        right_items = list(right)
        return [item for item in left if item in right_items]

    def save_to_file(self, path: str) -> None:
        payload = {
            "frames": [frame.to_dict() for frame in self.frames.values()],
            "procedures": sorted(self.procedures),
            "triggers": sorted(self.triggers),
        }
        with open(path, "w", encoding="utf-8") as handle:
            json.dump(payload, handle, ensure_ascii=False, indent=2)

    def load_from_file(self, path: str) -> None:
        with open(path, "r", encoding="utf-8") as handle:
            payload = json.load(handle)
        self.frames = {}
        for raw in payload.get("frames") or []:
            frame = Frame.from_dict(raw)
            self.frames[frame.name] = frame

    # ------------------------------------------------------------------
    # Чтение значения: наследование и демоны
    # ------------------------------------------------------------------

    def _read_value(
        self,
        frame_name: str,
        slot_name: str,
        record: bool,
        compute: bool = True,
    ) -> Tuple[Any, str]:
        frame = self._require_frame(frame_name)
        found = self._find_def(frame_name, slot_name)
        if found is None:
            raise KnowledgeError(
                f"Слот «{slot_name}» не определён для фрейма «{frame_name}»."
            )
        _owner, slot = found

        if slot_name in frame.values or slot_name in frame.computed:
            stored = frame.values if slot_name in frame.values else frame.computed
            value = copy.deepcopy(stored[slot_name])
            origin = frame.origins.get(slot_name, "собственное значение")
            details = {
                "собственное значение": "Взято собственное значение слота.",
                "демон if-needed": "Взято значение, ранее вычисленное демоном if-needed.",
                "служебная процедура": "Взято значение, записанное служебной процедурой.",
                "демон if-added": "Взято значение, записанное демоном if-added.",
            }
            if record:
                self._add_trace(
                    origin,
                    frame_name,
                    slot_name,
                    details.get(origin, "Взято ранее записанное значение слота."),
                    value,
                )
            return value, origin

        if slot.inheritance == "S":
            inherited = self._inherit_value(frame_name, slot_name)
            if inherited is not None:
                value, source_frame, how = inherited
                if record:
                    self._add_trace(
                        "наследование",
                        frame_name,
                        slot_name,
                        f"{how} из фрейма «{source_frame}».",
                        value,
                    )
                return copy.deepcopy(value), "наследование"

        if compute and slot.if_needed:
            key = (frame_name, slot_name)
            if key in self._computing:
                raise KnowledgeError(
                    f"Цикл демона if-needed при вычислении {frame_name}.{slot_name}."
                )
            if slot.if_needed not in self.procedures:
                raise KnowledgeError(
                    f"Демон if-needed «{slot.if_needed}» не зарегистрирован."
                )
            self._computing.add(key)
            try:
                if record:
                    self._add_trace(
                        "демон if-needed",
                        frame_name,
                        slot_name,
                        f"Значение отсутствует, запущен демон «{slot.if_needed}».",
                        None,
                    )
                computed = self.procedures[slot.if_needed](self, frame_name)
            finally:
                self._computing.discard(key)
            facets = self._merged_facets(frame_name, slot_name)
            ok, message = self.check_data(slot.data_type, computed, facets)
            if not ok:
                raise KnowledgeError(
                    f"Демон «{slot.if_needed}» вернул недопустимое значение: {message}"
                )
            frame.computed[slot_name] = copy.deepcopy(computed)
            frame.origins[slot_name] = "демон if-needed"
            if record:
                self._add_trace(
                    "демон if-needed",
                    frame_name,
                    slot_name,
                    f"Демон «{slot.if_needed}» записал результат в слот.",
                    computed,
                )
            return copy.deepcopy(computed), "демон if-needed"

        if slot.default is not None:
            if record:
                self._add_trace(
                    "значение по умолчанию",
                    frame_name,
                    slot_name,
                    "Использовано значение по умолчанию определения слота.",
                    slot.default,
                )
            return copy.deepcopy(slot.default), "значение по умолчанию"

        if record:
            self._add_trace(
                "пусто",
                frame_name,
                slot_name,
                "Значение не задано и не наследуется.",
                None,
            )
        return None, "пусто"

    def _inherit_value(self, frame_name: str, slot_name: str):
        for ancestor in self._ancestor_names(frame_name):
            frame = self.frames[ancestor]
            if slot_name in frame.values:
                return frame.values[slot_name], ancestor, "Передано собственное значение"
            definition = frame.slot_defs.get(slot_name)
            if definition is not None and definition.default is not None:
                return definition.default, ancestor, "Передано значение по умолчанию"
        return None

    def _find_def(self, frame_name: str, slot_name: str) -> Optional[Tuple[str, SlotDef]]:
        chain = [frame_name] + self._ancestor_names(frame_name)
        for node_name in chain:
            frame = self.frames[node_name]
            if slot_name in frame.slot_defs:
                return node_name, frame.slot_defs[slot_name]
        return None

    def _merged_facets(self, frame_name: str, slot_name: str) -> dict:
        merged: Dict[str, Any] = {}
        chain = list(reversed(self._ancestor_names(frame_name))) + [frame_name]
        for node_name in chain:
            definition = self.frames[node_name].slot_defs.get(slot_name)
            if definition:
                merged.update(definition.facets)
        return merged

    def _ancestor_names(self, name: str) -> List[str]:
        result = []
        seen = {name}
        current = self.frames[name].parent
        while current:
            if current not in self.frames:
                break
            if current in seen:
                break
            result.append(current)
            seen.add(current)
            current = self.frames[current].parent
        return result

    def _require_frame(self, name: str) -> Frame:
        if name not in self.frames:
            raise KnowledgeError(f"Фрейм «{name}» не определён.")
        return self.frames[name]

    def _check_new_frame(self, name: str, parent: Optional[str], frame_type: str) -> None:
        if not name or not str(name).strip():
            raise KnowledgeError("Имя фрейма не задано.")
        name = name.strip()
        if name in self.frames:
            raise KnowledgeError(f"Фрейм «{name}» уже есть в базе знаний.")
        if frame_type not in ("prototype", "instance"):
            raise KnowledgeError("Тип фрейма должен быть prototype или instance.")
        if parent is not None:
            if parent not in self.frames:
                raise KnowledgeError(f"Родительский фрейм «{parent}» не определён.")
            if self.frames[parent].frame_type != "prototype":
                raise KnowledgeError(
                    f"Нельзя наследовать экземпляр «{parent}». Родитель должен быть прототипом."
                )
            if frame_type == "prototype" and parent == name:
                raise KnowledgeError("Фрейм не может быть собственным родителем.")

    def _ensure_no_cycle(self, child: str, parent: str) -> None:
        seen = {child}
        current = parent
        while current:
            if current in seen:
                raise KnowledgeError(
                    f"Назначение родителя «{parent}» создаёт цикл наследования с «{child}»."
                )
            seen.add(current)
            if current not in self.frames:
                break
            current = self.frames[current].parent

    def _add_trace(self, mechanism: str, frame: str, slot: str, detail: str, value: Any) -> None:
        self.trace.append(
            TraceStep(
                mechanism=mechanism,
                frame=frame,
                slot=slot,
                detail=detail,
                value=copy.deepcopy(value),
            )
        )

    def format_trace(self, steps: Optional[List[TraceStep]] = None) -> str:
        items = self.trace if steps is None else steps
        if not items:
            return "(протокол пуст)"
        return "\n".join(f"{index:02d}. {step.format()}" for index, step in enumerate(items, 1))


class RuleChecker:
    """Поиск противоречий и структурных ошибок, которые не ловит редактор в момент ввода."""

    def __init__(self, base: FrameBase) -> None:
        self.base = base

    def check(self) -> List[dict]:
        issues = []
        issues.extend(self._cycles_and_parents())
        issues.extend(self._procedures())
        issues.extend(self._values())
        issues.extend(self._domain_contradictions())
        return issues

    def _cycles_and_parents(self) -> List[dict]:
        issues = []
        for frame in self.base.frames.values():
            if frame.parent and frame.parent not in self.base.frames:
                issues.append(self._issue(
                    "структура",
                    frame.name,
                    f"Родитель «{frame.parent}» отсутствует в базе.",
                ))
                continue
            seen = set()
            current = frame.name
            while current:
                if current in seen:
                    issues.append(self._issue(
                        "структура",
                        frame.name,
                        "Обнаружен цикл в иерархии AKO.",
                    ))
                    break
                seen.add(current)
                parent = self.base.frames[current].parent if current in self.base.frames else None
                current = parent
            if frame.frame_type == "instance" and frame.parent:
                parent = self.base.frames.get(frame.parent)
                if parent and parent.frame_type != "prototype":
                    issues.append(self._issue(
                        "структура",
                        frame.name,
                        "Экземпляр наследует другой экземпляр.",
                    ))
        return issues

    def _procedures(self) -> List[dict]:
        issues = []
        for frame in self.base.frames.values():
            for slot in frame.slot_defs.values():
                for label, fname in (
                    ("if-needed", slot.if_needed),
                    ("if-added", slot.if_added),
                    ("if-removed", slot.if_removed),
                ):
                    if fname and fname not in self.base.triggers and fname not in self.base.procedures:
                        issues.append(self._issue(
                            "процедура",
                            f"{frame.name}.{slot.name}",
                            f"Демон {label} «{fname}» не зарегистрирован.",
                        ))
                if slot.procedure and slot.procedure not in self.base.procedures:
                    issues.append(self._issue(
                        "процедура",
                        f"{frame.name}.{slot.name}",
                        f"Присоединенная процедура «{slot.procedure}» не зарегистрирована.",
                    ))
                if slot.default is not None:
                    facets = self.base._merged_facets(frame.name, slot.name)
                    ok, message = self.base.check_data(slot.data_type, slot.default, facets)
                    if not ok:
                        issues.append(self._issue(
                            "ограничение",
                            f"{frame.name}.{slot.name}",
                            f"Значение по умолчанию нарушает ограничение предка: {message}",
                        ))
        return issues

    def _values(self) -> List[dict]:
        issues = []
        for frame in self.base.frames.values():
            for slot_name, value in frame.values.items():
                found = self.base._find_def(frame.name, slot_name)
                if found is None:
                    issues.append(self._issue(
                        "структура",
                        f"{frame.name}.{slot_name}",
                        "Значение записано в необъявленный слот.",
                    ))
                    continue
                facets = self.base._merged_facets(frame.name, slot_name)
                ok, message = self.base.check_data(found[1].data_type, value, facets)
                if not ok:
                    issues.append(self._issue(
                        "тип",
                        f"{frame.name}.{slot_name}",
                        f"Значение {value!r} не соответствует типу или ограничению: {message}",
                    ))
        return issues

    def _domain_contradictions(self) -> List[dict]:
        issues = []
        for frame in self.base.frames.values():
            if frame.frame_type != "instance":
                continue
            values = frame.values
            screening = values.get("скрининг")
            recommendation = values.get("рекомендация")
            interview = values.get("интервью")
            education = values.get("образование")
            if screening == "нет" and recommendation not in (None, "отказ"):
                issues.append(self._issue(
                    "противоречие",
                    frame.name,
                    "Кандидат не прошёл скрининг, но рекомендация отлична от «отказ».",
                ))
            if interview == "неудовлетворительно" and recommendation not in (None, "отказ"):
                issues.append(self._issue(
                    "противоречие",
                    frame.name,
                    "Техническое интервью неудовлетворительное, но рекомендация отлична от «отказ».",
                ))
            if recommendation == "принять_senior":
                missing = []
                if education not in (None, "профильное"):
                    missing.append("образование должно быть профильным")
                if screening not in (None, "да"):
                    missing.append("скрининг должен быть пройден")
                if values.get("hard_skills") not in (None, "высокий"):
                    missing.append("hard skills должны быть высокими")
                if values.get("soft_skills") not in (None, "высокий"):
                    missing.append("soft skills должны быть высокими")
                if values.get("международная_квалификация") not in (None, "подтверждена"):
                    missing.append("нужна подтверждённая международная квалификация")
                if missing:
                    issues.append(self._issue(
                        "противоречие",
                        frame.name,
                        "Рекомендация «принять_senior» расходится с фактами: " + "; ".join(missing) + ".",
                    ))
        return issues

    @staticmethod
    def _issue(kind: str, where: str, message: str) -> dict:
        return {"вид": kind, "где": where, "описание": message}


class SearchEngine:
    """Поиск экземпляров по запросу с запуском процедур и наследования."""

    OPS = ("=", "!=", ">", ">=", "<", "<=", "содержит")

    def __init__(self, base: FrameBase) -> None:
        self.base = base

    def parse(self, text: str, procedure: str = "рекомендация") -> Query:
        raw = text.strip()
        if "|" in raw:
            class_name, body = raw.split("|", 1)
        else:
            class_name, body = "СоискательIT", raw
        class_name = class_name.strip()
        if not self.base.frame_defined(class_name):
            raise KnowledgeError(f"В запросе указан неизвестный фрейм «{class_name}».")
        chunks = [part.strip() for part in body.replace(" И ", " & ").split("&") if part.strip()]
        conditions = []
        for chunk in chunks:
            matched = None
            for op in (">=", "<=", "!=", "=", ">", "<", "содержит"):
                pieces = chunk.split(op, 1)
                if len(pieces) == 2 and pieces[0].strip() and pieces[1].strip():
                    matched = (pieces[0].strip(), op, self._literal(pieces[1].strip()))
                    break
            if matched is None:
                raise KnowledgeError(f"Не разобрано условие запроса: «{chunk}».")
            conditions.append(Condition(*matched))
        return Query(class_name, conditions, procedure)

    def search(self, query: Query) -> dict:
        instances = [
            name for name in self._instances_of(query.frame_class)
        ]
        matched = []
        rejected = []
        for name in instances:
            start = len(self.base.trace)
            if query.procedure:
                self.base.call_procedure(name, query.procedure)
            checks = []
            ok = True
            for condition in query.conditions:
                value = self.base.get_slot_value(name, condition.slot)
                passed = self.compare(value, condition.op, condition.value)
                checks.append({
                    "слот": condition.slot,
                    "операция": condition.op,
                    "ожидание": condition.value,
                    "факт": value,
                    "выполнено": passed,
                })
                if not passed:
                    ok = False
            record = {
                "фрейм": name,
                "условия": checks,
                "протокол": list(self.base.trace[start:]),
            }
            (matched if ok else rejected).append(record)
        return {"запрос": query.format(), "найдены": matched, "отклонены": rejected}

    def compare(self, fact: Any, op: str, expected: Any) -> bool:
        if op == "содержит":
            return isinstance(fact, list) and expected in fact
        if fact is None:
            return False
        if op == "=":
            return fact == expected
        if op == "!=":
            return fact != expected
        if not _is_number(fact) or not _is_number(expected):
            return False
        if op == ">":
            return fact > expected
        if op == ">=":
            return fact >= expected
        if op == "<":
            return fact < expected
        if op == "<=":
            return fact <= expected
        return False

    def _instances_of(self, class_name: str) -> List[str]:
        result = []
        for frame in self.base.frames.values():
            if frame.frame_type != "instance":
                continue
            if class_name in ([frame.name] + self.base._ancestor_names(frame.name)):
                result.append(frame.name)
        return sorted(result)

    @staticmethod
    def _literal(token: str) -> Any:
        if token.startswith(("\"", "«")) and token.endswith(("\"", "»")):
            return token[1:-1]
        if token.lstrip("-").isdigit():
            return int(token)
        lowered = token.lower()
        if lowered == "true":
            return True
        if lowered == "false":
            return False
        try:
            if "." in token:
                return float(token)
        except ValueError:
            pass
        return token


# ----------------------------------------------------------------------
# Предметная область варианта 8: отбор кандидатов
# ----------------------------------------------------------------------

def _hard_skills(fs: FrameBase, frame: str) -> str:
    years = fs.get_slot_value(frame, "опыт_лет")
    interview = fs.get_slot_value(frame, "интервью")
    thinking = fs.get_slot_value(frame, "системное_мышление")
    if interview == "неудовлетворительно":
        return "низкий"
    if years is None or interview is None:
        return "не_определён"
    if years > 5 and interview == "отлично":
        return "высокий"
    if 3 <= years <= 5 and interview == "отлично" and thinking == "высокое":
        return "высокий"
    if 3 <= years <= 5 and interview == "хорошо":
        return "средний"
    if years < 3 and interview == "отлично":
        return "средний"
    if years < 3 and interview == "хорошо":
        return "базовый"
    return "низкий"


def _soft_skills(fs: FrameBase, frame: str) -> str:
    team = fs.get_slot_value(frame, "командная_работа")
    communication = fs.get_slot_value(frame, "коммуникация")
    if team == "высокая" and communication == "высокие":
        return "высокий"
    if team == "средняя" and communication == "высокие":
        return "средний"
    if team is None or communication is None:
        return "не_определён"
    return "низкий"


def _international(fs: FrameBase, frame: str) -> str:
    english = fs.get_slot_value(frame, "английский")
    abroad = fs.get_slot_value(frame, "международный_опыт")
    if english == "b2_или_выше" and abroad == "да":
        return "подтверждена"
    return "не_подтверждена"


def _on_recommendation(fs: FrameBase, frame: str, value: Any) -> None:
    current = fs.get_slot_value(frame, "журнал")
    items = list(current or [])
    items.append(f"Рекомендация установлена: {value}")
    fs.set_slot_value(frame, "журнал", items, source="демон if-added")
    fs.set_slot_value(frame, "статус_рассмотрения", "рассмотрен", source="демон if-added")


def _recommend(fs: FrameBase, frame: str) -> str:
    screening = fs.get_slot_value(frame, "скрининг")
    interview = fs.get_slot_value(frame, "интервью")
    education = fs.get_slot_value(frame, "образование")
    if screening == "нет" or interview == "неудовлетворительно" or education != "профильное":
        decision = "отказ"
    else:
        hard = fs.get_slot_value(frame, "hard_skills")
        soft = fs.get_slot_value(frame, "soft_skills")
        international = fs.get_slot_value(frame, "международная_квалификация")
        if hard == "высокий" and soft == "высокий" and international == "подтверждена":
            decision = "принять_senior"
        elif hard == "высокий" and soft == "высокий":
            decision = "кадровый_резерв"
        elif hard == "средний" and soft == "высокий":
            decision = "принять_middle"
        elif hard == "базовый" and soft == "средний":
            decision = "испытательный_срок"
        else:
            decision = "отказ"
    fs.set_slot_value(frame, "рекомендация", decision, source="служебная процедура")
    return decision


def register_domain_functions(fs: FrameBase) -> None:
    fs.register_function("вычислить_hard_skills", _hard_skills)
    fs.register_function("вычислить_soft_skills", _soft_skills)
    fs.register_function("вычислить_международную_квалификацию", _international)
    fs.register_function("сформировать_рекомендацию", _recommend)
    fs.register_trigger("зафиксировать_рекомендацию", _on_recommendation)


def build_variant8(fs: FrameBase) -> None:
    """Иерархия фреймов предметной области «Отбор кандидатов на вакантное место»."""
    register_domain_functions(fs)

    edu = ["профильное", "непрофильное"]
    yes_no = ["да", "нет"]
    interview = ["отлично", "хорошо", "неудовлетворительно"]
    team = ["высокая", "средняя", "низкая"]
    communication = ["высокие", "средние", "низкие"]
    english = ["b2_или_выше", "ниже_b2"]
    level = ["высокий", "средний", "базовый", "низкий", "не_определён"]
    decision = [
        "принять_senior",
        "принять_middle",
        "испытательный_срок",
        "кадровый_резерв",
        "отказ",
    ]
    thinking = ["высокое", "среднее", "низкое"]

    fs.create_frame("Сущность")
    fs.create_frame("Кандидат", parent="Сущность")
    fs.create_slot(
        "Кандидат", "фио", "string", "U",
        facets={"comment": "Уникальный идентификатор соискателя"},
    )
    fs.create_slot("Кандидат", "образование", "enum", "S", facets={"domain": edu})
    fs.create_slot("Кандидат", "скрининг", "enum", "S", facets={"domain": yes_no})
    fs.create_slot(
        "Кандидат", "опыт_лет", "integer", "R",
        facets={"min": 0, "max": 50, "comment": "Ограничение наследуется потомками"},
    )
    fs.create_slot("Кандидат", "интервью", "enum", "S", facets={"domain": interview})
    fs.create_slot("Кандидат", "командная_работа", "enum", "S", facets={"domain": team})
    fs.create_slot("Кандидат", "коммуникация", "enum", "S", facets={"domain": communication})
    fs.create_slot("Кандидат", "английский", "enum", "S", facets={"domain": english})
    fs.create_slot("Кандидат", "международный_опыт", "enum", "S", facets={"domain": yes_no})
    fs.create_slot(
        "Кандидат", "системное_мышление", "enum", "S",
        default="среднее", facets={"domain": thinking},
    )
    fs.create_slot(
        "Кандидат", "hard_skills", "enum", "S",
        if_needed="вычислить_hard_skills", facets={"domain": level},
    )
    fs.create_slot(
        "Кандидат", "soft_skills", "enum", "S",
        if_needed="вычислить_soft_skills", facets={"domain": level},
    )
    fs.create_slot(
        "Кандидат", "международная_квалификация", "enum", "S",
        if_needed="вычислить_международную_квалификацию",
        facets={"domain": ["подтверждена", "не_подтверждена"]},
    )
    fs.create_slot(
        "Кандидат", "рекомендация", "enum", "U",
        if_added="зафиксировать_рекомендацию",
        procedure="сформировать_рекомендацию",
        facets={"domain": decision},
    )
    fs.create_slot("Кандидат", "журнал", "list", "U", default=[])
    fs.create_slot(
        "Кандидат", "статус_рассмотрения", "string", "S", default="в_очереди",
    )
    fs.create_slot("Кандидат", "рассматриваемая_вакансия", "frame", "U")

    fs.create_frame("СоискательIT", parent="Кандидат")
    fs.inherit_frame_type("СоискательIT", "Кандидат")
    fs.create_slot(
        "СоискательIT", "направление", "string", "S", default="разработка ПО",
    )

    fs.create_frame("Вакансия", parent="Сущность")
    fs.create_slot("Вакансия", "название", "string", "U")
    fs.create_slot("Вакансия", "требуемый_уровень", "enum", "S", facets={"domain": decision})
    fs.create_slot("Вакансия", "минимальный_опыт", "integer", "R", facets={"min": 0, "max": 50})

    people = {
        "Иванов": {
            "фио": "Иванов",
            "образование": "профильное",
            "скрининг": "да",
            "опыт_лет": 8,
            "интервью": "отлично",
            "командная_работа": "высокая",
            "коммуникация": "высокие",
            "английский": "b2_или_выше",
            "международный_опыт": "да",
            "системное_мышление": "высокое",
            "рассматриваемая_вакансия": "ВакансияSenior",
        },
        "Петров": {
            "фио": "Петров",
            "образование": "профильное",
            "скрининг": "да",
            "опыт_лет": 4,
            "интервью": "хорошо",
            "командная_работа": "высокая",
            "коммуникация": "высокие",
            "английский": "ниже_b2",
            "международный_опыт": "нет",
            "рассматриваемая_вакансия": "ВакансияMiddle",
        },
        "Сидоров": {
            "фио": "Сидоров",
            "образование": "профильное",
            "скрининг": "да",
            "опыт_лет": 2,
            "интервью": "хорошо",
            "командная_работа": "средняя",
            "коммуникация": "высокие",
            "английский": "ниже_b2",
            "международный_опыт": "нет",
            "рассматриваемая_вакансия": "ВакансияJunior",
        },
        "Кузнецов": {
            "фио": "Кузнецов",
            "образование": "непрофильное",
            "скрининг": "нет",
            "опыт_лет": 1,
            "интервью": "неудовлетворительно",
            "командная_работа": "средняя",
            "коммуникация": "средние",
            "английский": "ниже_b2",
            "международный_опыт": "нет",
            "рассматриваемая_вакансия": "ВакансияJunior",
        },
    }

    vacancies = {
        "ВакансияSenior": ("Senior-разработчик", "принять_senior", 5),
        "ВакансияMiddle": ("Middle-разработчик", "принять_middle", 3),
        "ВакансияJunior": ("Junior-разработчик", "испытательный_срок", 0),
    }
    for name, (title, level, years) in vacancies.items():
        fs.create_frame(name, parent="Вакансия", frame_type="instance")
        fs.set_slot_value(name, "название", title)
        fs.set_slot_value(name, "требуемый_уровень", level)
        fs.set_slot_value(name, "минимальный_опыт", years)

    for name, slots in people.items():
        fs.create_frame(name, parent="СоискательIT", frame_type="instance")
        for slot_name, value in slots.items():
            fs.set_slot_value(name, slot_name, value)


def expected_decisions() -> Dict[str, str]:
    return {
        "Иванов": "принять_senior",
        "Петров": "принять_middle",
        "Сидоров": "испытательный_срок",
        "Кузнецов": "отказ",
    }
