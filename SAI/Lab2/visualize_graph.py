import matplotlib.pyplot as plt
import networkx as nx
import numpy as np
from expert_system import BackwardChainingEngine, Predicate

plt.style.use('seaborn-v0_8-whitegrid' if 'seaborn-v0_8-whitegrid' in plt.style.available else 'default')
plt.rcParams['font.sans-serif'] = ['DejaVu Sans', 'Arial', 'Helvetica']
plt.rcParams['axes.unicode_minus'] = False

def build_full_and_or_graph(engine: BackwardChainingEngine):
    G = nx.DiGraph()
    for rule in engine.rules:
        r_node = f"[{rule.id}]"
        G.add_node(r_node, type="rule", label=f"{rule.id}\np={rule.priority}")
        c_node = rule.conclusion.to_string()
        G.add_node(c_node, type="fact", label=c_node)
        G.add_edge(r_node, c_node, label="concl")
        
        for p in rule.get_premises_list():
            p_node = p.to_string()
            G.add_node(p_node, type="fact", label=p_node)
            G.add_edge(p_node, r_node, label="prem")
    return G

def plot_and_or_graph(G, filename="images/and_or_graph.png", title="Граф И/ИЛИ базы знаний (Вариант 8: Отбор кандидатов)"):
    plt.figure(figsize=(18, 12))
    
    # Layered hierarchical layout based on distance to final recommendation
    pos = {}
    rule_nodes = [n for n, d in G.nodes(data=True) if d.get('type') == 'rule']
    fact_nodes = [n for n, d in G.nodes(data=True) if d.get('type') == 'fact']
    
    pos = nx.spring_layout(G, k=0.45, iterations=120, seed=42)
    
    nx.draw_networkx_nodes(
        G, pos, nodelist=fact_nodes, node_color='#A0CBE8',
        node_shape='o', node_size=1800, alpha=0.9, edgecolors='#1F77B4', linewidths=1.5
    )
    nx.draw_networkx_nodes(
        G, pos, nodelist=rule_nodes, node_color='#FFBE7D',
        node_shape='s', node_size=1400, alpha=0.9, edgecolors='#D95F02', linewidths=1.8
    )
    
    nx.draw_networkx_edges(
        G, pos, arrows=True, arrowsize=14, arrowstyle='-|>',
        edge_color='#555555', width=1.2, connectionstyle='arc3,rad=0.05'
    )
    
    labels = {n: G.nodes[n].get('label', n) for n in G.nodes()}
    nx.draw_networkx_labels(G, pos, labels=labels, font_size=7, font_family='DejaVu Sans')
    
    plt.title(title, fontsize=14, fontweight='bold', pad=20)
    plt.axis('off')
    plt.tight_layout()
    plt.savefig(filename, dpi=300, bbox_inches='tight')
    plt.close()
    print(f"Saved: {filename}")

def plot_partial_proof_graph(engine: BackwardChainingEngine, candidate_name: str, goal: Predicate, filename: str):
    plt.figure(figsize=(15, 9))
    G_partial = nx.DiGraph()
    
    # Extract edges from successful proof steps
    for step in engine.explanation_steps:
        if step['type'] == 'rule':
            r_node = f"[{step['rule_id']}]"
            g_node = step['goal']
            G_partial.add_node(r_node, type='rule', label=f"{step['rule_id']}")
            G_partial.add_node(g_node, type='fact', label=g_node)
            G_partial.add_edge(r_node, g_node)
            for p in step['premises']:
                G_partial.add_node(p, type='fact', label=p)
                G_partial.add_edge(p, r_node)
        elif step['type'] == 'fact':
            f_node = step['fact']
            G_partial.add_node(f_node, type='input_fact', label=f_node)
            
    pos = nx.spring_layout(G_partial, k=0.6, iterations=80, seed=24)
    
    rule_nodes = [n for n, d in G_partial.nodes(data=True) if d.get('type') == 'rule']
    input_facts = [n for n, d in G_partial.nodes(data=True) if d.get('type') == 'input_fact']
    derived_facts = [n for n, d in G_partial.nodes(data=True) if d.get('type') == 'fact' and n not in input_facts]
    
    nx.draw_networkx_nodes(G_partial, pos, nodelist=input_facts, node_color='#86efac', node_shape='o', node_size=2000, edgecolors='#16a34a', linewidths=2, label='Исходные факты (РП)')
    nx.draw_networkx_nodes(G_partial, pos, nodelist=derived_facts, node_color='#93c5fd', node_shape='o', node_size=2200, edgecolors='#2563eb', linewidths=2, label='Выведенные факты / Цель')
    nx.draw_networkx_nodes(G_partial, pos, nodelist=rule_nodes, node_color='#fde047', node_shape='s', node_size=1600, edgecolors='#ca8a04', linewidths=2, label='Примененные правила')
    
    nx.draw_networkx_edges(G_partial, pos, arrows=True, arrowsize=16, arrowstyle='-|>', edge_color='#334155', width=1.6)
    
    labels = {n: G_partial.nodes[n].get('label', n) for n in G_partial.nodes()}
    nx.draw_networkx_labels(G_partial, pos, labels=labels, font_size=8, font_family='DejaVu Sans')
    
    plt.title(f"Частичный граф успешного вывода для кандидата '{candidate_name}'\nЦель: {goal.to_string()}", fontsize=13, fontweight='bold', pad=15)
    plt.legend(scatterpoints=1, loc='upper left', frameon=True)
    plt.axis('off')
    plt.tight_layout()
    plt.savefig(filename, dpi=300, bbox_inches='tight')
    plt.close()
    print(f"Saved: {filename}")

if __name__ == "__main__":
    eng = BackwardChainingEngine()
    eng.load_rules_from_file("data/rules.json")
    G_full = build_full_and_or_graph(eng)
    plot_and_or_graph(G_full, "images/and_or_graph.png")
    
    # Generate partial graph for Ivanov
    eng.load_facts_from_file("data/facts.json", candidate_name="Иванов")
    goal_iv = Predicate("рекомендация_кандидату", ["Иванов", "принять_senior"])
    eng.prove_goal(goal_iv)
    plot_partial_proof_graph(eng, "Иванов", goal_iv, "images/partial_graph_ivanov.png")
