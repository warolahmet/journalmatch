from pathlib import Path

import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

CSV_PATH = Path("src/main/resources/data/openalex_journals.csv")

app = FastAPI()

df = None
vectorizer = None
tfidf_matrix = None


class SuggestRequest(BaseModel):
    title: str
    abstractText: str


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
        df["name"].astype(str) + " " +
        df["keywords"].astype(str) + " " +
        df["subject_area"].astype(str)
    )

    corpus = df["combined_text"].tolist()

    vectorizer = TfidfVectorizer(
        stop_words="english",
        ngram_range=(1, 2),
        max_features=10000
    )

    tfidf_matrix = vectorizer.fit_transform(corpus)


@app.on_event("startup")
def startup_event():
    load_data()
    print("TF-IDF model loaded successfully.")


@app.post("/suggest")
def suggest(request: SuggestRequest):
    query_text = f"{request.title} {request.abstractText}"

    query_vector = vectorizer.transform([query_text])
    similarities = cosine_similarity(query_vector, tfidf_matrix).flatten()

    temp_df = df.copy()
    temp_df["similarity"] = similarities

    top_results = temp_df.sort_values(by="similarity", ascending=False).head(20)

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