const form = document.getElementById("journalForm");
const titleInput = document.getElementById("title");
const abstractInput = document.getElementById("abstractText");
const resultsDiv = document.getElementById("results");
const loadingDiv = document.getElementById("loading");
const errorDiv = document.getElementById("errorMessage");
const submitBtn = document.getElementById("submitBtn");
const exampleBtn = document.getElementById("exampleBtn");
const showMoreContainer = document.getElementById("showMoreContainer");
const showMoreBtn = document.getElementById("showMoreBtn");

let currentLimit = 5;
let lastQuery = null;

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const title = titleInput.value.trim();
    const abstractText = abstractInput.value.trim();

    currentLimit = 5;
    lastQuery = { title, abstractText };

    if (!title && !abstractText) {
        showError("Please enter at least a title or an abstract.");
        return;
    }

    if (!abstractText) {
        console.warn("Only title provided — results may be less accurate.");
    }

    clearError();
    clearResults();
    setLoading(true);

    try {
        const response = await fetch("/api/hybrid/suggest", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                title,
                abstractText,
                limit: currentLimit
            })
        });

        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}`);
        }

        const journals = await response.json();
        renderResults(journals);
    } catch (error) {
        showError("Something went wrong while fetching journal suggestions.");
        console.error(error);
    } finally {
        setLoading(false);
    }
});

showMoreBtn.addEventListener("click", async () => {
    if (!lastQuery) {
        return;
    }

    currentLimit = 10;
    clearError();
    setLoading(true);

    try {
        const response = await fetch("/api/hybrid/suggest", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                title: lastQuery.title,
                abstractText: lastQuery.abstractText,
                limit: currentLimit
            })
        });

        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}`);
        }

        const journals = await response.json();
        renderResults(journals);
    } catch (error) {
        showError("Something went wrong while loading more results.");
        console.error(error);
    } finally {
        setLoading(false);
    }
});

exampleBtn.addEventListener("click", () => {
    titleInput.value = "Deep learning for medical image analysis";
    abstractInput.value = "This paper uses machine learning and computer vision techniques for medical imaging.";

    form.dispatchEvent(new Event("submit"));
});

function renderResults(journals) {
    if (!journals || journals.length === 0) {
        showMoreContainer.classList.add("hidden");
        resultsDiv.innerHTML = `
            <div class="empty-state">
                <strong>No strong matches found.</strong><br>
                Try adding more specific technical keywords like:
                <ul>
                    <li>machine learning</li>
                    <li>computer vision</li>
                    <li>medical imaging</li>
                </ul>
            </div>
        `;
        return;
    }

    resultsDiv.innerHTML = journals.map((journal, index) => `
        <div class="result-card">
            <div class="result-header">
                <div>
                    <div class="result-rank">#${index + 1} Match</div>
                    <h3>${escapeHtml(journal.name || "Unknown Journal")}</h3>
                </div>
                <div class="score-box">
                    <div class="score-label">Hybrid Score</div>
                    <div class="score-value">${formatHybridScore(journal.hybridScore)}</div>
                    <div class="score-quality">${getScoreLabel(journal.hybridScore ?? 0)}</div>
                </div>
            </div>

            <div class="result-meta">
                <div><strong>Publisher:</strong> ${escapeHtml(journal.publisher || "-")}</div>
                <div><strong>Subject Area:</strong> ${escapeHtml(journal.subjectArea || "-")}</div>
                <div><strong>APC (USD):</strong> ${formatApc(journal.apcUsd)}</div>
                <div><strong>Open Access:</strong> ${journal.openAccess ? "Yes" : "No"}</div>
            </div>

            <div class="score-breakdown">
                <div class="mini-score">
                    <span class="mini-score-label">Rule</span>
                    <span class="mini-score-value">${journal.ruleScore ?? 0}</span>
                </div>
                <div class="mini-score">
                    <span class="mini-score-label">TF-IDF</span>
                    <span class="mini-score-value">${formatTfidfScore(journal.tfidfScore)}</span>
                </div>
                <div class="mini-score">
                    <span class="mini-score-label">Embedding</span>
                    <span class="mini-score-value">${formatEmbeddingScore(journal.embeddingScore)}</span>
                </div>
            </div>

            <div class="matched">
                <strong>Why this journal?</strong><br>
                ${getWhyThisJournalText(journal)}
            </div>
        </div>   
    `).join("");

    if (journals.length >= 5 && currentLimit === 5) {
        showMoreContainer.classList.remove("hidden");
    } else {
        showMoreContainer.classList.add("hidden");
    }
}

function setLoading(isLoading) {
    if (isLoading) {
        loadingDiv.textContent = "Analyzing your paper and finding the best journal matches...";
        loadingDiv.classList.remove("hidden");
        submitBtn.disabled = true;
        exampleBtn.disabled = true;
    } else {
        loadingDiv.classList.add("hidden");
        submitBtn.disabled = false;
        exampleBtn.disabled = false;
    }
}

function showError(message) {
    errorDiv.textContent = message;
    errorDiv.classList.remove("hidden");
}

function clearError() {
    errorDiv.textContent = "";
    errorDiv.classList.add("hidden");
}

function clearResults() {
    resultsDiv.innerHTML = "";
}

function formatApc(apcUsd) {
    if (apcUsd === null || apcUsd === undefined) {
        return "-";
    }
    return apcUsd;
}

function formatHybridScore(score) {
    if (score === null || score === undefined) {
        return "-";
    }
    return Number(score).toFixed(2);
}

function formatTfidfScore(score) {
    if (score === null || score === undefined) {
        return "-";
    }
    return Number(score).toFixed(3);
}

function formatEmbeddingScore(score) {
    if (score === null || score === undefined) {
        return "-";
    }
    return Number(score).toFixed(3);
}

function getScoreLabel(score) {
    if (score >= 20) return "Excellent match (very relevant)";
    if (score >= 15) return "Very Strong match";
    if (score >= 10) return "Strong match";
    if (score >= 5) return "Moderate match";
    return "Weak match";
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function getWhyThisJournalText(journal) {
    const matchedKeywords = (journal.matchedKeywords || "").trim();

    if (matchedKeywords.length > 0) {
        return `Matched keywords: ${escapeHtml(matchedKeywords)}`;
    }

    const hasEmbedding = journal.embeddingScore !== null && journal.embeddingScore !== undefined && journal.embeddingScore > 0;
    const hasTfidf = journal.tfidfScore !== null && journal.tfidfScore !== undefined && journal.tfidfScore > 0;
    const subject = (journal.subjectArea || "").trim();

    if (hasEmbedding && hasTfidf && subject) {
        return `Matched through semantic similarity, lexical similarity, and subject relevance in ${escapeHtml(subject)}.`;
    }

    if (hasEmbedding && subject) {
        return `Matched through semantic similarity and subject relevance in ${escapeHtml(subject)}.`;
    }

    if (hasEmbedding) {
        return "Matched through semantic similarity.";
    }

    if (hasTfidf && subject) {
        return `Matched through lexical similarity and subject relevance in ${escapeHtml(subject)}.`;
    }

    if (hasTfidf) {
        return "Matched through lexical similarity.";
    }

    if (subject) {
        return `Matched through subject relevance in ${escapeHtml(subject)}.`;
    }

    return "Matched through hybrid ranking signals.";
}