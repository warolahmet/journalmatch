import json
import sys
from pathlib import Path

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


CSV_PATH = Path("src/main/resources/data/openalex_journals.csv")

df = None
vectorizer = None
tfidf_matrix = None


def load_data():
    global df, vectorizer, tfidf_matrix

    if df is not None and vectorizer is not None and tfidf_matrix is not None:
        return

    df = pd.read_csv(CSV_PATH)

    df["name"] = df["name"].fillna("")
    df["publisher"] = df["publisher"].fillna("")
    df["keywords"] = df["keywords"].fillna("")
    df["subject_area"] = df["subject_area"].fillna("")
    df["open_access"] = df["open_access"].fillna(False)
    df["apc_usd"] = df["apc_usd"].fillna(0)

    df["combined_text"] = (
        df["keywords"].astype(str) + " " +
        df["keywords"].astype(str) + " " +
        df["subject_area"].astype(str) + " " +
        df["subject_area"].astype(str) + " " +
        df["name"].astype(str)
    )

    corpus = df["combined_text"].tolist()

    vectorizer = TfidfVectorizer(
        stop_words="english",
        ngram_range=(1, 2),
        max_features=10000
    )

    tfidf_matrix = vectorizer.fit_transform(corpus)


def recommend_journals(query_text, top_n=20):
    load_data()

    query_vector = vectorizer.transform([query_text])
    similarities = cosine_similarity(query_vector, tfidf_matrix).flatten()

    temp_df = df.copy()
    temp_df["similarity"] = similarities

    top_results = temp_df.sort_values(by="similarity", ascending=False).head(top_n)

    results = []
    for _, row in top_results.iterrows():
        results.append({
            "name": row["name"],
            "publisher": row["publisher"],
            "subjectArea": row["subject_area"],
            "openAccess": bool(row["open_access"]),
            "apcUsd": int(row["apc_usd"]) if pd.notna(row["apc_usd"]) else 0,
            "tfidfScore": float(row["similarity"])
        })

    return results


def main():
    if len(sys.argv) < 3:
        print(json.dumps({"error": "title and abstractText are required"}))
        return

    title = sys.argv[1]
    abstract = sys.argv[2]
    query_text = f"{title} {abstract}"

    results = recommend_journals(query_text, top_n=50)
    print(json.dumps(results))


if __name__ == "__main__":
    main()