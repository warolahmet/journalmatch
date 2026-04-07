from pathlib import Path

import numpy as np
import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

CSV_PATH = Path("src/main/resources/data/openalex_journals.csv")

app = FastAPI()

df = None
model = None
journal_embeddings = None


class SuggestRequest(BaseModel):
    title: str
    abstractText: str


def load_data():
    global df, model, journal_embeddings

    if df is not None and model is not None and journal_embeddings is not None:
        return

    df = pd.read_csv(CSV_PATH)

    df["name"] = df["name"].fillna("")
    df["publisher"] = df["publisher"].fillna("")
    df["keywords"] = df["keywords"].fillna("")
    df["subject_area"] = df["subject_area"].fillna("")
    df["open_access"] = df["open_access"].fillna(False)
    df["apc_usd"] = df["apc_usd"].fillna(0)

    # Embedding text: alan sinyalini biraz güçlendir
    df["combined_text"] = (
        df["keywords"].astype(str) + " " +
        df["subject_area"].astype(str) + " " +
        df["name"].astype(str)
    )

    model = SentenceTransformer("all-MiniLM-L6-v2")
    journal_embeddings = model.encode(
        df["combined_text"].tolist(),
        convert_to_numpy=True,
        normalize_embeddings=True
    )


@app.on_event("startup")
def startup_event():
    load_data()
    print("Embedding model loaded successfully.")


@app.post("/suggest")
def suggest(request: SuggestRequest):
    query_text = f"{request.title} {request.abstractText}"

    query_embedding = model.encode(
        [query_text],
        convert_to_numpy=True,
        normalize_embeddings=True
    )

    similarities = cosine_similarity(query_embedding, journal_embeddings).flatten()

    temp_df = df.copy()
    temp_df["embedding_score"] = similarities

    top_results = temp_df.sort_values(by="embedding_score", ascending=False).head(20)

    results = []
    for _, row in top_results.iterrows():
        results.append({
            "name": row["name"],
            "publisher": row["publisher"],
            "subjectArea": row["subject_area"],
            "openAccess": bool(row["open_access"]),
            "apcUsd": int(row["apc_usd"]) if pd.notna(row["apc_usd"]) else 0,
            "embeddingScore": float(row["embedding_score"])
        })

    return results