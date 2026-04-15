import json
from pathlib import Path

import pandas as pd
from sentence_transformers import SentenceTransformer


INPUT_CSV = Path("src/main/resources/data/openalex_journals.csv")
OUTPUT_CSV = Path("src/main/resources/data/openalex_journals_with_embeddings.csv")


def build_combined_text(row):
    keywords = str(row.get("keywords", "") or "")
    subject_area = str(row.get("subject_area", "") or "")
    name = str(row.get("name", "") or "")

    return f"{keywords} {subject_area} {name}".strip()


def main():
    print("Loading dataset...")
    df = pd.read_csv(INPUT_CSV)

    df["name"] = df["name"].fillna("")
    df["publisher"] = df["publisher"].fillna("")
    df["keywords"] = df["keywords"].fillna("")
    df["subject_area"] = df["subject_area"].fillna("")
    df["open_access"] = df["open_access"].fillna(False)
    df["apc_usd"] = df["apc_usd"].fillna(0)

    print("Preparing text fields...")
    df["combined_text"] = df.apply(build_combined_text, axis=1)

    print("Loading embedding model...")
    model = SentenceTransformer("all-MiniLM-L6-v2")

    print("Generating embeddings...")
    embeddings = model.encode(
        df["combined_text"].tolist(),
        convert_to_numpy=True,
        normalize_embeddings=True,
        show_progress_bar=True
    )

    print("Serializing embeddings...")
    df["embedding_json"] = [json.dumps(vector.tolist()) for vector in embeddings]

    print(f"Writing output to {OUTPUT_CSV} ...")
    df.to_csv(OUTPUT_CSV, index=False)

    print("Done.")
    print(f"Saved {len(df)} journals with embeddings.")


if __name__ == "__main__":
    main()