import sys
import pandas as pd

def main():
    path = sys.argv[1] if len(sys.argv) > 1 else "benchmark-results.csv"
    df = pd.read_csv(path)

    df["gammon"] = df["gammon"].astype(str).str.lower() == "true"
    df["backgammon"] = df["backgammon"].astype(str).str.lower() == "true"

    rows = []
    for matchup_id, group in df.groupby("matchupId", sort=False):
        games = len(group)
        side_a_wind = (group["winnerSide"] == "A").sum()
        win_rate_a = side_a_wind / games * 100

        rows.append({
            "matchup" : matchup_id,
            "games" : games,
            "sideALabel" : group["sideALabel"].iloc[0],
            "sideBLabel" : group["sideBLabel"].iloc[0],
            "winRateA_%" : round(win_rate_a, 2),
            "gammonRate_%" : round(group["gammon"].mean() * 100, 2),
            "backgammonRate_%" : round(group["backgammon"].mean() * 100, 2),
            "avgTurns" : round(group["turnCount"].mean(), 2),
            "avgDecisionMs_A" : round(group["sideAAvgDecisionMillis"].mean(), 3),
            "avgDecisionMs_B" : round(group["sideBAvgDecisionMillis"].mean(), 3),
        })

    summary = pd.DataFrame(rows)

    pd.set_option("display.width", 160)
    pd.set_option("display.max_columns", None)
    print(summary.to_string(index=False))

    out_path = path.rsplit(".", 1)[0] + "-summary.csv"
    summary.to_csv(out_path, index=False)
    print(f"\nZapisano podsumowanie do {out_path}")

if __name__ == "__main__":
    main()