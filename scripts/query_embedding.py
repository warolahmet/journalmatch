print("PYTHON SCRIPT: file started", flush=True)

import json
import sys

print("PYTHON SCRIPT: std imports loaded", flush=True)

from sentence_transformers import SentenceTransformer

print("PYTHON SCRIPT: sentence_transformers imported", flush=True)

def main():
    print("PYTHON SCRIPT: inside main", flush=True)

    if len(sys.argv) < 2:
        print(json.dumps({"error": "query text is required"}), flush=True)
        return

    query_text = sys.argv[1]
    print("PYTHON SCRIPT: loading model", flush=True)

    model = SentenceTransformer("all-MiniLM-L6-v2")

    print("PYTHON SCRIPT: model loaded", flush=True)
    print("PYTHON SCRIPT: generating embedding", flush=True)

    embedding = model.encode(query_text, normalize_embeddings=True)

    print("PYTHON SCRIPT: embedding ready", flush=True)
    print(json.dumps(embedding.tolist()), flush=True)


if __name__ == "__main__":
    main()