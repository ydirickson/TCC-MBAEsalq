# /// script
# requires-python = ">=3.10"
# dependencies = [
#   "matplotlib>=3.8",
#   "numpy>=1.26",
# ]
# ///
"""
Gera os gráficos comparativos do TCC a partir dos arquivos de resultado do k6.
Uso: uv run monitoramento/gerar-graficos.py
Saída: monitoramento/graficos/*.png
"""

import re
from pathlib import Path
from statistics import median

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

# ── Caminhos ─────────────────────────────────────────────────────────────────

RESULTS_DIR = Path(__file__).parent / "resultados"
OUTPUT_DIR  = Path(__file__).parent / "graficos"
OUTPUT_DIR.mkdir(exist_ok=True)

# ── Configuração ──────────────────────────────────────────────────────────────

# Runs do C2A2 coletados com o bug de FK (antes do fix de 2026-04-08)
INVALID_RUNS = {
    # C2A2 com bug de FK (antes do fix de 2026-04-08)
    "c2a2-low-run_20260404_130721.txt",
    "c2a2-low-run_20260404_133653.txt",
    "c2a2-low-run_20260404_143521.txt",
    # C2A3 low runs interrompidos antes de completar (sem bloco de métricas)
    "c2a3-low-run_20260405_112840.txt",
    "c2a3-low-run_20260405_124259.txt",
}

CENARIOS = ["c1", "c2a1", "c2a2", "c2a3"]
PERFIS   = ["low", "medium", "high"]

LABELS = {
    "c1":   "C1 — Baseline",
    "c2a1": "C2A1 — DB Based",
    "c2a2": "C2A2 — CDC+Kafka",
    "c2a3": "C2A3 — EDA+Kafka",
}

CORES = {
    "c1":   "#4C72B0",
    "c2a1": "#55A868",
    "c2a2": "#C44E52",
    "c2a3": "#DD8452",
}

# Duração líquida do teste por cenário/perfil (em minutos), excluindo warmup.
# Derivado de executar-metricas.sh: base_min × DURATION_MULTIPLIER.
#   c1, c2a1 → multiplier=1 | c2a3 → multiplier=2 | c2a2 → multiplier=3
DURACAO_MIN = {
    "c1":   {"low": 5,  "medium": 10, "high": 15},
    "c2a1": {"low": 5,  "medium": 10, "high": 15},
    "c2a2": {"low": 15, "medium": 30, "high": 45},
    "c2a3": {"low": 10, "medium": 20, "high": 30},
}

# ── Parser dos arquivos de resultado ─────────────────────────────────────────

def parse_metrics(filepath: Path) -> dict | None:
    text = filepath.read_text(errors="ignore")

    # Extrai o bloco de métricas do log do k6
    block_match = re.search(r"METRICAS DA PESQUISA.*?╚[═]+╝", text, re.DOTALL)
    if not block_match:
        return None
    block = block_match.group(0)

    # Separa seções M1 e M5 (ambas têm avg/p95/p99)
    m1_sec = re.search(r"M1 Latencia.*?(?=M2 Throughput)", block, re.DOTALL)
    m5_sec = re.search(r"M5 Staleness.*?╚",               block, re.DOTALL)

    def get(pattern, src):
        m = re.search(pattern, src.group(0) if src else "")
        return float(m.group(1)) if m else None

    def get_global(pattern):
        m = re.search(pattern, block)
        return float(m.group(1)) if m else None

    return {
        "m1_avg": get(r"avg = ([\d.]+) ms", m1_sec),
        "m1_p95": get(r"p95 = ([\d.]+) ms", m1_sec),
        "m1_p99": get(r"p99 = ([\d.]+) ms", m1_sec),
        "m2_fluxos": get_global(r"fluxos completos = (\d+)"),
        "m2_ok":     get_global(r"replicacoes ok\s+=\s+(\d+)"),
        "m3_erro":   get_global(r"Taxa de erro = ([\d.]+)%"),
        "m4_consist":get_global(r"Consistencia \(checks rate\) = ([\d.]+)%"),
        "m5_avg": get(r"avg = ([\d.]+) ms", m5_sec),
        "m5_p95": get(r"p95 = ([\d.]+) ms", m5_sec),
        "m5_p99": get(r"p99 = ([\d.]+) ms", m5_sec),
    }

def load_all() -> dict:
    data = {c: {p: [] for p in PERFIS} for c in CENARIOS}

    for filepath in sorted(RESULTS_DIR.glob("*.txt")):
        if filepath.name in INVALID_RUNS:
            continue

        m = re.match(r"(c\d+a?\d*)-(low|medium|high)-run_\d+_\d+\.txt", filepath.name)
        if not m:
            continue

        cenario, perfil = m.group(1), m.group(2)
        if cenario not in CENARIOS:
            continue

        metrics = parse_metrics(filepath)
        if metrics:
            data[cenario][perfil].append(metrics)

    return data

def med(runs: list[dict], key: str) -> float | None:
    vals = [r[key] for r in runs if r.get(key) is not None]
    return median(vals) if vals else None

# ── Helpers de plot ───────────────────────────────────────────────────────────

plt.rcParams.update({
    "font.family": "sans-serif",
    "axes.spines.top":   False,
    "axes.spines.right": False,
    "figure.facecolor":  "white",
    "axes.facecolor":    "white",
})

def salvar(fig, nome):
    caminho = OUTPUT_DIR / nome
    fig.savefig(caminho, dpi=150, bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print(f"  ✓  {caminho}")

def barra_labels(ax, bars, fmt="{:.0f}"):
    for bar in bars:
        h = bar.get_height()
        if h > 0:
            ax.text(
                bar.get_x() + bar.get_width() / 2,
                h + h * 0.02 + 0.5,
                fmt.format(h),
                ha="center", va="bottom", fontsize=8,
            )

# ── Gráfico 1 — M1 Latência P95 ──────────────────────────────────────────────

def plot_m1_p95(data):
    cenarios_comp = ["c2a1", "c2a2", "c2a3"]
    x     = np.arange(len(PERFIS))
    width = 0.25

    fig, ax = plt.subplots(figsize=(10, 5))

    for i, c in enumerate(cenarios_comp):
        vals = [med(data[c][p], "m1_p95") or 0 for p in PERFIS]
        bars = ax.bar(x + i * width, vals, width,
                      label=LABELS[c], color=CORES[c], alpha=0.87)
        barra_labels(ax, bars)

    # Referência C1
    c1_ref = [f"{med(data['c1'][p], 'm1_p95'):.0f} ms" for p in PERFIS]
    ax.text(0.99, 0.97,
            "C1 Baseline: " + " / ".join(c1_ref),
            transform=ax.transAxes, ha="right", va="top", fontsize=8,
            color=CORES["c1"],
            bbox=dict(boxstyle="round,pad=0.3", facecolor="white",
                      edgecolor=CORES["c1"], alpha=0.85))

    ax.set_title("M1 — Latência de Replicação P95 por Arquitetura e Perfil de Carga",
                 fontsize=13, pad=12)
    ax.set_xlabel("Perfil de Carga")
    ax.set_ylabel("Latência P95 (ms)")
    ax.set_xticks(x + width)
    ax.set_xticklabels(["Low (5 min)", "Medium (10 min)", "High (15 min)"])
    ax.legend()
    ax.grid(axis="y", alpha=0.3)
    ax.set_ylim(bottom=0)
    fig.tight_layout()
    salvar(fig, "m1_latencia_p95.png")

# ── Gráfico 2 — M5 Staleness P95 ─────────────────────────────────────────────

def plot_m5_p95(data):
    cenarios_comp = ["c2a1", "c2a2", "c2a3"]
    x     = np.arange(len(PERFIS))
    width = 0.25

    fig, ax = plt.subplots(figsize=(10, 5))

    for i, c in enumerate(cenarios_comp):
        vals = [med(data[c][p], "m5_p95") or 0 for p in PERFIS]
        bars = ax.bar(x + i * width, vals, width,
                      label=LABELS[c], color=CORES[c], alpha=0.87)
        barra_labels(ax, bars)

    ax.set_title("M5 — Staleness (Inconsistência Temporária) P95 por Arquitetura e Perfil",
                 fontsize=13, pad=12)
    ax.set_xlabel("Perfil de Carga")
    ax.set_ylabel("Staleness P95 (ms)")
    ax.set_xticks(x + width)
    ax.set_xticklabels(["Low (5 min)", "Medium (10 min)", "High (15 min)"])
    ax.legend()
    ax.grid(axis="y", alpha=0.3)
    ax.set_ylim(bottom=0)
    fig.tight_layout()
    salvar(fig, "m5_staleness_p95.png")

# ── Gráfico 3 — M2 Throughput (fluxos/min) ───────────────────────────────────

def plot_m2_throughput(data):
    cenarios_comp = ["c2a1", "c2a2", "c2a3"]
    x     = np.arange(len(PERFIS))
    width = 0.25

    fig, ax = plt.subplots(figsize=(10, 5))

    for i, c in enumerate(cenarios_comp):
        vals = [
            (med(data[c][p], "m2_fluxos") or 0) / DURACAO_MIN[c][p]
            for p in PERFIS
        ]
        bars = ax.bar(x + i * width, vals, width,
                      label=LABELS[c], color=CORES[c], alpha=0.87)
        barra_labels(ax, bars)

    # Referência C1
    c1_ref = [
        f"{(med(data['c1'][p], 'm2_fluxos') or 0) / DURACAO_MIN['c1'][p]:.0f}"
        for p in PERFIS
    ]
    ax.text(0.99, 0.97,
            "C1 Baseline: " + " / ".join(c1_ref) + " fluxos/min",
            transform=ax.transAxes, ha="right", va="top", fontsize=8,
            color=CORES["c1"],
            bbox=dict(boxstyle="round,pad=0.3", facecolor="white",
                      edgecolor=CORES["c1"], alpha=0.85))

    ax.set_title("M2 — Throughput (Fluxos Completos/min) por Arquitetura e Perfil",
                 fontsize=13, pad=12)
    ax.set_xlabel("Perfil de Carga")
    ax.set_ylabel("Fluxos Completos por Minuto")
    ax.set_xticks(x + width)
    ax.set_xticklabels(["Low", "Medium", "High"])
    ax.legend()
    ax.grid(axis="y", alpha=0.3)
    ax.set_ylim(bottom=0)
    fig.tight_layout()
    salvar(fig, "m2_throughput.png")

# ── Gráfico 4 — Radar comparativo ────────────────────────────────────────────

def plot_radar(data):
    """
    Score 0-100 por métrica no perfil medium.
    M1, M3, M5: menor=melhor → escala invertida.
    M2, M4:     maior=melhor → escala direta.
    """
    perfil = "medium"
    cenarios_comp = ["c2a1", "c2a2", "c2a3"]

    raw = {}
    for c in cenarios_comp:
        runs = data[c][perfil]
        raw[c] = {
            "m1": med(runs, "m1_p95")   or 0,
            "m2": (med(runs, "m2_fluxos") or 0) / DURACAO_MIN[c][perfil],
            "m3": med(runs, "m3_erro")  or 0,
            "m4": med(runs, "m4_consist") or 0,
            "m5": med(runs, "m5_p95")   or 0,
        }

    def norm_inv(vals):
        mn, mx = min(vals), max(vals)
        if mx == mn:
            return [100.0] * len(vals)
        return [100.0 * (1 - (v - mn) / (mx - mn)) for v in vals]

    def norm(vals):
        mn, mx = min(vals), max(vals)
        if mx == mn:
            return [100.0] * len(vals)
        return [100.0 * (v - mn) / (mx - mn) for v in vals]

    m1s = norm_inv([raw[c]["m1"] for c in cenarios_comp])
    m2s = norm    ([raw[c]["m2"] for c in cenarios_comp])
    m3s = norm_inv([raw[c]["m3"] for c in cenarios_comp])
    m4s = norm    ([raw[c]["m4"] for c in cenarios_comp])
    m5s = norm_inv([raw[c]["m5"] for c in cenarios_comp])

    scores = {
        c: [m1s[i], m2s[i], m3s[i], m4s[i], m5s[i]]
        for i, c in enumerate(cenarios_comp)
    }

    labels_radar = [
        "M1 — Latência\n(menor=melhor)",
        "M2 — Throughput\n(maior=melhor)",
        "M3 — Confiabilidade\n(menor erro=melhor)",
        "M4 — Consistência\n(maior=melhor)",
        "M5 — Staleness\n(menor=melhor)",
    ]

    N      = len(labels_radar)
    angles = np.linspace(0, 2 * np.pi, N, endpoint=False).tolist()
    angles += angles[:1]

    fig, ax = plt.subplots(figsize=(8, 8), subplot_kw=dict(polar=True))

    for c in cenarios_comp:
        vals = scores[c] + scores[c][:1]
        ax.plot(angles, vals, "o-", linewidth=2,
                label=LABELS[c], color=CORES[c])
        ax.fill(angles, vals, alpha=0.12, color=CORES[c])

    ax.set_xticks(angles[:-1])
    ax.set_xticklabels(labels_radar, size=10)
    ax.set_ylim(0, 100)
    ax.set_yticks([25, 50, 75, 100])
    ax.set_yticklabels(["25", "50", "75", "100"], size=8)
    ax.grid(True, alpha=0.3)
    ax.set_title(
        "Perfil Comparativo das Arquiteturas — Perfil Medium\n"
        "(score 0-100 normalizado por métrica, maior = melhor)",
        fontsize=12, pad=24,
    )
    ax.legend(loc="upper right", bbox_to_anchor=(1.38, 1.12))
    fig.tight_layout()
    salvar(fig, "radar_comparativo.png")

# ── Main ──────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    print("Carregando resultados...")
    data = load_all()

    print("\nRuns válidos por cenário/perfil:")
    for c in CENARIOS:
        for p in PERFIS:
            n = len(data[c][p])
            print(f"  {c:6}  {p:8}  →  {n} run(s)")

    print("\nGerando gráficos...")
    plot_m1_p95(data)
    plot_m5_p95(data)
    plot_m2_throughput(data)
    plot_radar(data)

    print(f"\nPronto! 4 gráficos em: {OUTPUT_DIR.resolve()}")
