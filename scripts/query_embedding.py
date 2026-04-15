import json
import sys
from sentence_transformers import SentenceTransformer


def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "query text is required"}))
        return

    query_text = sys.argv[1]

    model = SentenceTransformer("all-MiniLM-L6-v2")
    embedding = model.encode(query_text, normalize_embeddings=True)

    print(json.dumps(embedding.tolist()))


if __name__ == "__main__":
    main()