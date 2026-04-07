import csv
import requests
from pathlib import Path
import time

BASE_URL = "https://api.openalex.org/sources"
OUTPUT_PATH = Path("openalex_journals.csv")
TOTAL_RECORDS = 3000
PER_PAGE = 200

ALLOWED_TERMS = [
    "computer science",
    "artificial intelligence",
    "machine learning",
    "deep learning",
    "computer vision",
    "image processing",
    "data science",
    "pattern recognition",
    "neural network",
    "medical imaging",
    "medical image"
]

def is_relevant_source(source):
    text_parts = []

    topics = source.get("topics", [])
    for topic in topics:
        name = topic.get("display_name")
        if name:
            text_parts.append(name.lower())

    x_concepts = source.get("x_concepts", [])
    for concept in x_concepts:
        name = concept.get("display_name")
        if name:
            text_parts.append(name.lower())

    combined = " ".join(text_parts)

    for term in ALLOWED_TERMS:
        if term in combined:
            return True

    return False

def build_keywords(source):
    parts = []

    topics = source.get("topics", [])
    for topic in topics[:5]:
        topic_name = topic.get("display_name")
        if topic_name:
            parts.append(topic_name.lower())

    x_concepts = source.get("x_concepts", [])
    for concept in x_concepts[:5]:
        concept_name = concept.get("display_name")
        score = concept.get("score", 0)
        if concept_name and score > 0.3:
            parts.append(concept_name.lower())

    unique_parts = []
    seen = set()
    for part in parts:
        if part not in seen:
            seen.add(part)
            unique_parts.append(part)

    return ",".join(unique_parts)


def extract_subject_area(source):
    topics = source.get("topics", [])
    if topics:
        first_topic = topics[0]
        display_name = first_topic.get("display_name")
        if display_name:
            return display_name

    x_concepts = source.get("x_concepts", [])
    if x_concepts:
        best = max(x_concepts, key=lambda c: c.get("score", 0))
        display_name = best.get("display_name")
        if display_name:
            return display_name

    return ""


def get_apc_usd(source):
    apc_prices = source.get("apc_prices", [])
    if isinstance(apc_prices, list) and len(apc_prices) > 0:
        first_price = apc_prices[0]
        return first_price.get("price", 0)
    return 0


def main():
    print("Fetching OpenAlex journals...")

    cursor = "*"
    total_written = 0

    with OUTPUT_PATH.open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow([
            "name",
            "publisher",
            "issn",
            "h_index",
            "keywords",
            "subject_area",
            "open_access",
            "apc_usd",
            "openalex_id",
            "works_count",
            "is_oa",
            "is_in_doaj"
        ])

        while total_written < TOTAL_RECORDS:
            params = {
                "filter": "type:journal,works_count:>100",
                "per-page": PER_PAGE,
                "sort": "works_count:desc",
                "cursor": cursor
            }

            response = requests.get(BASE_URL, params=params, timeout=60)
            response.raise_for_status()

            data = response.json()
            results = data.get("results", [])

            if not results:
                break

            for source in results:
                if not is_relevant_source(source):
                    continue
                name = source.get("display_name", "")
                publisher = source.get("host_organization_name", "") or source.get("display_name", "")
                issn_list = source.get("issn", [])
                issn = issn_list[0] if issn_list else ""
                h_index = source.get("summary_stats", {}).get("h_index", 0)

                keywords = build_keywords(source)
                subject_area = extract_subject_area(source)

                is_oa = source.get("is_oa", False)
                is_in_doaj = source.get("is_in_doaj", False)
                apc_usd = get_apc_usd(source)
                openalex_id = source.get("id", "")
                works_count = source.get("works_count", 0)

                writer.writerow([
                    name,
                    publisher,
                    issn,
                    h_index,
                    keywords,
                    subject_area,
                    str(is_oa).lower(),
                    apc_usd,
                    openalex_id,
                    works_count,
                    str(is_oa).lower(),
                    str(is_in_doaj).lower()
                ])

                total_written += 1
                if total_written >= TOTAL_RECORDS:
                    break

            print(f"Written so far: {total_written}")

            meta = data.get("meta", {})
            next_cursor = meta.get("next_cursor")
            if not next_cursor:
                break

            cursor = next_cursor
            time.sleep(0.2)

    print(f"CSV created: {OUTPUT_PATH.resolve()}")
    print(f"Total journals written: {total_written}")


if __name__ == "__main__":
    main()