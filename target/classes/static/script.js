const API_BASE_URL = 'http://localhost:8080/api';

const form = document.getElementById('recommendationForm');
const loading = document.getElementById('loading');
const results = document.getElementById('results');
const inputSection = document.getElementById('inputSection');

if (form) {
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(form);
        const data = {
            diseaseName: formData.get('diseaseName'),
            diseaseType: formData.get('diseaseType'),
            aiProvider: formData.get('aiProvider')
        };

        if (inputSection) inputSection.style.display = 'none';
        if (loading) loading.style.display = 'block';
        if (results) results.style.display = 'none';

        try {
            const response = await fetch(`${API_BASE_URL}/recommend`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include', 
                body: JSON.stringify(data)
            });

            const result = await response.json();
            if (!response.ok) throw new Error(result.error || 'Failed');
            displayResults(result);

        } catch (err) {
            alert("⚠️ Error: " + err.message);
            resetForm();
        } finally {
            if (loading) loading.style.display = 'none';
        }
    });
}

function displayResults(data) {
    if (results) results.style.display = 'block';
    
    const diseaseTitle = document.getElementById('diseaseName-result');
    if (diseaseTitle) {
        diseaseTitle.textContent = (data.disease ? data.disease.name : 'Unknown Condition');
        // Scroll halus ke bagian hasil
        results.scrollIntoView({ behavior: 'smooth' });
    }

    const fillList = (elementId, items) => {
        const list = document.getElementById(elementId);
        if (list) {
            list.innerHTML = '';
            if (items && items.length > 0) {
                items.forEach(item => {
                    const li = document.createElement('li');
                    li.textContent = cleanListText(item); // Pakai pembersih sederhana untuk list
                    list.appendChild(li);
                });
            } else {
                list.innerHTML = '<li>No specific data provided by AI.</li>';
            }
        }
    };

    fillList('foodsToEat-list', data.foodsToEat);
    fillList('foodsToAvoid-list', data.foodsToAvoid);

    // --- BAGIAN PENTING: FORMAT NOTES ---
    const notesContent = document.getElementById('notes-content');
    if (notesContent) {
        let rawNotes = data.additionalNotes || "Stay hydrated and rest well.";
        // Gunakan fungsi formatter canggih kita, lalu masukkan sebagai HTML
        notesContent.innerHTML = formatDoctorNotes(rawNotes);
    }
}

// Fungsi Pembersih Sederhana untuk List Item (Makanan)
function cleanListText(text) {
    if (!text) return "";
    return text.replace(/\["|"]/g, '').replace(/[{"}]/g, '').trim();
}

// 🔥 FUNGSI CANGGIH: Merapikan Catatan Dokter (Markdown -> HTML) 🔥
function formatDoctorNotes(text) {
    if (!text) return "<p>No specific notes available.</p>";

    // [FIX UTAMA]: Jika text terlihat seperti JSON object penuh (seperti di screenshotmu)
    // Contoh: {"foodsToEat": [...], "additionalNotes": "INI YANG KITA MAU"}
    if (text.trim().startsWith('{')) {
        try {
            // Coba parsing sebagai JSON
            const jsonObject = JSON.parse(text);
            // Jika berhasil, ambil hanya bagian 'additionalNotes'
            if (jsonObject.additionalNotes) {
                text = jsonObject.additionalNotes;
            }
        } catch (e) {
            // Jika gagal parse JSON, mungkin formatnya rusak
            // Kita coba bersihkan manual pakai Regex
            console.log("JSON Parse error in notes, switching to regex cleanup");
            
            // Cari teks setelah "additionalNotes": "..."
            const match = text.match(/"additionalNotes"\s*:\s*"([^"]+)"/);
            if (match && match[1]) {
                text = match[1];
            }
        }
    }

    // Lanjut ke formatting standar (Markdown -> HTML)
    // 1. Bersihkan sisa-sisa karakter JSON jika masih ada
    text = text.replace(/[{}"[\]]/g, ''); 
    text = text.replace(/additionalNotes:/g, ''); // Hapus label key

    // 2. Bold text (**teks**)
    let formatted = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

    // 3. Pisahkan paragraf
    let lines = formatted.split('\n');
    let htmlOutput = '';
    let inList = false;

    lines.forEach(line => {
        let trimmedLine = line.trim();
        if (trimmedLine.length === 0) return;

        // Deteksi List Bullet (* atau -)
        if (trimmedLine.startsWith('* ') || trimmedLine.startsWith('- ')) {
            if (!inList) { htmlOutput += '<ul>'; inList = true; }
            htmlOutput += `<li>${trimmedLine.substring(2)}</li>`;
        } else {
            if (inList) { htmlOutput += '</ul>'; inList = false; }
            // Deteksi kalimat panjang, jadikan paragraf
            htmlOutput += `<p>${trimmedLine}</p>`;
        }
    });
    if (inList) htmlOutput += '</ul>';

    return htmlOutput;
}

function resetForm() {
    if (form) form.reset();
    if (inputSection) inputSection.style.display = 'block';
    if (results) results.style.display = 'none';
    if (loading) loading.style.display = 'none';
    // Scroll balik ke atas
    if (inputSection) inputSection.scrollIntoView({ behavior: 'smooth' });
}