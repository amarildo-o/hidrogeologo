#!/usr/bin/env python3
"""
Reproduce las figuras 2, 4, 6 y 13 de:
  Yang et al., "Simulation of the Telluric Electrical Field Frequency Selection
  Method and Its Application in Mineral Water Exploration", Water 2025, 17, 3314.

Los datos (lecturas en mV) se ingresan desde un archivo CSV o Excel (.xlsx).

USO
---
  # Figuras 2, 4 y 6 (modulo de Ey: curvas + pseudo-seccion; fase si existe)
  python tefsm_figuras.py modulo  datos_fig2.csv --nombre fig2
  python tefsm_figuras.py modulo  datos_fig4.csv --nombre fig4
  python tefsm_figuras.py modulo  datos_fig6.csv --nombre fig6   # con columna fase_deg -> panel (c)

  # Figura 13 (pseudo-seccion normalizada de dV, 40 frecuencias)
  python tefsm_figuras.py pseudo  datos_L8.csv --nombre fig13b --zk 23 --c 0.1 --rho 220

  # Archivos de ejemplo sinteticos para probar el formato
  python tefsm_figuras.py demo --salida ejemplos

FORMATO DE ENTRADA (formato "largo": una fila por lectura)
----------------------------------------------------------
  Figuras 2, 4, 6  -> columnas:  y_m, f_hz, ey_mv  [, fase_deg]
       y_m      posicion sobre el perfil (m)
       f_hz     frecuencia (Hz)
       ey_mv    |Ey| en mV/m  (modulo del campo electrico)
       fase_deg fase de Ey en grados (opcional; solo para el panel c de la fig. 6)

  Figura 13        -> columnas:  y_m, f_hz, dv_mv  [, rho_ohm_m]
       dv_mv      diferencia de potencial dV medida (mV)
       rho_ohm_m  resistividad aparente (opcional; si falta se usa --rho)

  Tambien se aceptan los alias: y/x/pos, f/freq/frecuencia, ey/e/modulo/mv,
  phase/fase, dv/v/delta_v, rho/resistividad.
"""
import argparse
import os
import sys

import numpy as np
import pandas as pd
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
from matplotlib.colors import LinearSegmentedColormap  # noqa: E402

# ----------------------------------------------------------------------------
# Lectura de datos
# ----------------------------------------------------------------------------
ALIAS = {
    "y_m": ["y_m", "y", "x", "pos", "posicion", "distancia"],
    "f_hz": ["f_hz", "f", "freq", "frecuencia", "frequency"],
    "ey_mv": ["ey_mv", "ey", "e", "modulo", "mv", "mv_m", "ey_mv_m"],
    "fase_deg": ["fase_deg", "fase", "phase", "phase_deg"],
    "dv_mv": ["dv_mv", "dv", "v", "delta_v", "deltav", "v_mv"],
    "rho_ohm_m": ["rho_ohm_m", "rho", "resistividad"],
}


def leer(path, requeridas, opcionales=()):
    ext = os.path.splitext(path)[1].lower()
    if ext in (".xlsx", ".xls"):
        df = pd.read_excel(path)
    else:
        df = pd.read_csv(path, sep=None, engine="python", decimal=".")
    df.columns = [str(c).strip().lower().replace(" ", "_") for c in df.columns]
    out = {}
    for canon in list(requeridas) + list(opcionales):
        for a in ALIAS[canon]:
            if a in df.columns:
                out[canon] = pd.to_numeric(df[a], errors="coerce")
                break
        else:
            if canon in requeridas:
                sys.exit(f"Falta la columna '{canon}' (alias aceptados: {ALIAS[canon]}). "
                         f"Columnas encontradas: {list(df.columns)}")
    res = pd.DataFrame(out).dropna(subset=list(requeridas))
    return res


def a_malla(df, valor):
    """Pivota a matriz [frecuencias x posiciones]; promedia duplicados."""
    p = df.pivot_table(index="f_hz", columns="y_m", values=valor, aggfunc="mean")
    p = p.sort_index().sort_index(axis=1)
    return p.columns.values.astype(float), p.index.values.astype(float), p.values


# ----------------------------------------------------------------------------
# Paleta tipo "jet" usada en las figuras del articulo
# ----------------------------------------------------------------------------
CMAP_E = plt.get_cmap("jet")
CMAP_K = LinearSegmentedColormap.from_list(
    "k13", ["#0000ff", "#00b4ff", "#a8ffd8", "#ffff80", "#ffb000", "#ff3000", "#ff0000"])

# Frecuencias mostradas en el panel (a) de las figuras 2, 4 y 6: 10^x Hz
FREC_CURVAS = [1.1, 1.4, 1.6, 1.85, 2.0, 2.25]
ESTILOS = {1.1: (":", None), 1.4: ("-", None), 1.6: (":", "^"),
           1.85: (":", None), 2.0: (":", "x"), 2.25: ("-", "^")}


# ----------------------------------------------------------------------------
# Figuras 2, 4, 6
# ----------------------------------------------------------------------------
def figura_modulo(args):
    df = leer(args.archivo, ["y_m", "f_hz", "ey_mv"], ["fase_deg"])
    y, f, E = a_malla(df, "ey_mv")
    lgf = np.log10(f)
    paneles = 3 if "fase_deg" in df.columns and df["fase_deg"].notna().any() else 2

    fig, axs = plt.subplots(1, paneles, figsize=(5.2 * paneles, 5.2))

    # (a) curvas de modulo vs y
    ax = axs[0]
    for lg in FREC_CURVAS:
        i = int(np.argmin(np.abs(lgf - lg)))
        if abs(lgf[i] - lg) > 0.06:  # no hay dato cercano a esa frecuencia
            continue
        ls, mk = ESTILOS[lg]
        ax.plot(y, E[i], color="k", lw=0.9, ls=ls, marker=mk, ms=4, mfc="k",
                markevery=max(1, len(y) // 20))
        etiqueta = f"$10^{{{lg:g}}}$ Hz"
        ax.annotate(etiqueta, (y[-1], E[i][-1]), xytext=(-4, -12),
                    textcoords="offset points", ha="right", fontsize=8)
    ax.set_xlabel("y / m")
    ax.set_ylabel(r"$|E_y|$ / mV·m$^{-1}$")
    ax.set_xlim(y.min(), y.max())
    ax.set_title("(a)", y=-0.2)

    # (b) pseudo-seccion del modulo
    niveles = [0, .5, .75, 1, 1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
               15, 16, 17, 18, 19, max(20, float(np.nanmax(E)))]
    ax = axs[1]
    cf = ax.contourf(y, lgf, E, levels=niveles, cmap=CMAP_E, extend="max")
    cs = ax.contour(y, lgf, E, levels=[.75, 1, 1.5, 2, 3, 5, 10, 15],
                    colors="k", linewidths=0.6)
    ax.clabel(cs, fmt="%g", fontsize=7)
    ax.set_xlabel("y / m")
    ax.set_ylabel("lg f / Hz")
    ax.xaxis.set_label_position("top")
    ax.xaxis.tick_top()
    ax.set_title("(b)", y=-0.2)
    fig.colorbar(cf, ax=ax, label=r"$|E_y|$ / mV·m$^{-1}$", ticks=niveles[:-1], pad=0.03)

    # (c) pseudo-seccion de la fase
    if paneles == 3:
        _, _, Ph = a_malla(df, "fase_deg")
        ax = axs[2]
        cf = ax.contourf(y, lgf, Ph, levels=20, cmap=CMAP_E)
        cs = ax.contour(y, lgf, Ph, levels=10, colors="k", linewidths=0.5)
        ax.clabel(cs, fmt="%g", fontsize=6)
        ax.set_xlabel("y / m")
        ax.set_ylabel("lg f / Hz")
        ax.xaxis.set_label_position("top")
        ax.xaxis.tick_top()
        ax.set_title("(c)", y=-0.2)
        fig.colorbar(cf, ax=ax, label="φ / (°)", pad=0.03)

    guardar(fig, args)


# ----------------------------------------------------------------------------
# Figura 13: pseudo-seccion normalizada
#   K_i = log10(dV_i / dV_min)        (ec. 13)
#   h_s = c * 503 * sqrt(rho / f)     (ec. 12)
# ----------------------------------------------------------------------------
def figura_pseudo(args):
    df = leer(args.archivo, ["y_m", "f_hz", "dv_mv"], ["rho_ohm_m"])
    if "rho_ohm_m" not in df.columns:
        df["rho_ohm_m"] = args.rho
    df["rho_ohm_m"] = df["rho_ohm_m"].fillna(args.rho)
    df = df[df["dv_mv"] > 0]

    dvmin = df["dv_mv"].min()  # minimo de todo el perfil
    df["K"] = np.log10(df["dv_mv"] / dvmin)
    df["hs"] = args.c * 503.0 * np.sqrt(df["rho_ohm_m"] / df["f_hz"])

    # interpolar K(hs) en cada posicion y sobre una malla de profundidad comun
    ys = np.sort(df["y_m"].unique())
    hmax = args.hmax if args.hmax else float(df["hs"].max())
    hmin = float(df["hs"].min())
    prof = np.linspace(hmin, hmax, 200)
    K = np.full((len(prof), len(ys)), np.nan)
    for j, yy in enumerate(ys):
        s = df[df["y_m"] == yy].sort_values("hs")
        if len(s) >= 2:
            K[:, j] = np.interp(prof, s["hs"], s["K"], left=np.nan, right=np.nan)

    fig, ax = plt.subplots(figsize=(7.5, 6))
    niveles = np.linspace(0, max(2.0, np.nanmax(K)), 21)
    cf = ax.contourf(ys, -prof, K, levels=niveles, cmap=CMAP_K, extend="neither")
    cs = ax.contour(ys, -prof, K, levels=np.arange(0.1, 2.0, 0.1), colors="k", linewidths=0.4)
    ax.clabel(cs, fmt="%.1f", fontsize=6)
    if args.zk is not None:
        ax.axvline(args.zk, color="red", lw=1.5)
        ax.annotate("ZK", (args.zk, 0), xytext=(0, 22), textcoords="offset points",
                    color="red", ha="center", weight="bold")
    ax.set_xlabel("y / m")
    ax.set_ylabel(r"$h_s$ / m")
    ax.xaxis.set_label_position("top")
    ax.xaxis.tick_top()
    ax.set_xticks(np.round(ys))
    ax.set_ylim(-hmax, 0)
    fig.colorbar(cf, ax=ax, orientation="horizontal", pad=0.04, shrink=0.8,
                 label=r"$\log_{10}(\Delta V/\Delta V_{min})$")
    guardar(fig, args)


def guardar(fig, args):
    os.makedirs(args.salida, exist_ok=True)
    ruta = os.path.join(args.salida, f"{args.nombre}.png")
    fig.tight_layout()
    fig.savefig(ruta, dpi=args.dpi)
    print("Figura guardada en", ruta)


# ----------------------------------------------------------------------------
# Datos sinteticos de ejemplo (solo para probar el programa, NO son del articulo)
# ----------------------------------------------------------------------------
def demo(args):
    os.makedirs(args.salida, exist_ok=True)
    y = np.concatenate([np.arange(-100, -20, 1.0), np.arange(-20, 20.5, 0.5), np.arange(21, 101, 1.0)])
    lgf = np.arange(1.0, 4.0001, 0.05)
    rows = []
    for l in lgf:
        base = 0.2 * 10 ** (1.2 * (l - 1.0) / 3 * 2.0) * 1.0  # crece con f
        base = 0.5 + 18.5 * ((l - 1.0) / 3.0) ** 2.2
        for yy in y:
            plato = 1 - 0.45 * np.exp(-(yy / 4.0) ** 2)            # fig. 2 (estrecho)
            esfera = 1 - 0.17 * np.exp(-(yy / 45.0) ** 2)           # fig. 4 (ancho)
            fase = 45 + 6 * (1 - np.exp(-(yy / 40.0) ** 2)) * (4 - l) / 3 - 1
            rows.append((yy, 10 ** l, base * plato, base * esfera,
                         base * plato * esfera, fase))
    d = pd.DataFrame(rows, columns=["y_m", "f_hz", "fig2", "fig4", "fig6", "fase_deg"])
    for k in ("fig2", "fig4"):
        d[["y_m", "f_hz"]].assign(ey_mv=d[k]).to_csv(
            os.path.join(args.salida, f"datos_{k}.csv"), index=False)
    d[["y_m", "f_hz", "fase_deg"]].assign(ey_mv=d["fig6"])[
        ["y_m", "f_hz", "ey_mv", "fase_deg"]].to_csv(
        os.path.join(args.salida, "datos_fig6.csv"), index=False)

    # Fig. 13: 40 frecuencias entre 12 y 5000 Hz, y = 15..29 m, anomalia en 23 m
    ys = np.arange(15, 30, 1.0)
    fs = np.logspace(np.log10(12), np.log10(5000), 40)
    rng = np.random.default_rng(0)
    r = []
    for fq in fs:
        for yy in ys:
            v = 3 + 30 * (np.log10(fq) - 1) / 2.7
            v *= 1 - 0.8 * np.exp(-((yy - 23.5) / 2.5) ** 2) * (0.4 + 0.6 * np.exp(-((np.log10(fq) - 1.8) / 0.4) ** 2))
            r.append((yy, fq, max(v * (1 + 0.03 * rng.standard_normal()), 0.3)))
    pd.DataFrame(r, columns=["y_m", "f_hz", "dv_mv"]).to_csv(
        os.path.join(args.salida, "datos_fig13_L8.csv"), index=False)
    print("Ejemplos escritos en", args.salida)


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = p.add_subparsers(dest="cmd", required=True)

    def comun(sp):
        sp.add_argument("--nombre", default=None, help="nombre del PNG de salida (sin extension)")
        sp.add_argument("--salida", default="figuras", help="carpeta de salida")
        sp.add_argument("--dpi", type=int, default=200)

    a = sub.add_parser("modulo", help="Figuras 2, 4 y 6 (|Ey| y fase)")
    a.add_argument("archivo")
    comun(a)
    a.set_defaults(fn=figura_modulo)

    b = sub.add_parser("pseudo", help="Figura 13 (pseudo-seccion normalizada de dV)")
    b.add_argument("archivo")
    b.add_argument("--rho", type=float, default=220.0,
                   help="resistividad aparente (ohm.m) si no hay columna rho (def. 220, zona fracturada)")
    b.add_argument("--c", type=float, default=1.0, help="coeficiente empirico c de la ec. 12")
    b.add_argument("--hmax", type=float, default=300.0, help="profundidad maxima mostrada (m)")
    b.add_argument("--zk", type=float, default=None, help="posicion (m) del sondeo ZK a marcar")
    comun(b)
    b.set_defaults(fn=figura_pseudo)

    c = sub.add_parser("demo", help="genera CSV sinteticos de ejemplo")
    c.add_argument("--salida", default="ejemplos")
    c.set_defaults(fn=demo)

    args = p.parse_args()
    if args.cmd != "demo" and args.nombre is None:
        args.nombre = os.path.splitext(os.path.basename(args.archivo))[0]
    args.fn(args)


if __name__ == "__main__":
    main()
