from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

print("PYTHON API: loading model...", flush=True)
model = SentenceTransformer("all-MiniLM-L6-v2")
print("PYTHON API: model loaded", flush=True)

app = FastAPI()


class EmbedRequest(BaseModel):
    text: str


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/embed")
def embed(request: EmbedRequest):
    embedding = model.encode(request.text, normalize_embeddings=True)
    return {"embedding": embedding.tolist()}