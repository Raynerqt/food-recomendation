// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements
const form = document.getElementById('recommendationForm');
const loading = document.getElementById('loading');
const results = document.getElementById('results');
const error = document.getElementById('error');
const submitBtn = document.getElementById('submitBtn');

// Global Variables
let currentRecommendationId = null;

// =======================================================
// 1. MAIN FORM HANDLER
// =======================================================
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // RESET UI SAAT MULAI SESI BARU (PENTING!)
    resetFeedbackUI(); 

    const formData = new FormData(form);
    const data = {
        diseaseName: formData.get('diseaseName'),
        diseaseType: formData.get('diseaseType'),
        aiProvider: formData.get('aiProvider'),
    };
    
    const severity = formData.get('severity');
    if (severity) data.severity = severity;
    
    showLoading();
    hideResults();
    hideError();
    
    try {
        const response = await fetch(`${API_BASE_URL}/recommend`, { 
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        if (!response.ok) throw new Error(result.error || 'Failed to get recommendations');
        
        displayResults(result);
        fetchHistory(); // Update history otomatis
        
    } catch (err) {
        console.error('Error:', err);
        showError(err.message || 'Something went wrong. Please try again.');
    } finally {
        hideLoading();
    }
});

// =======================================================
// 2. DISPLAY RESULTS & RESET UI
// =======================================================
function displayResults(payload) {
    let data;

    // Tangkap ID dan Data
    if (payload.data) {
        data = payload.data;
        if (payload.id) {
            currentRecommendationId = payload.id;
            console.log("✅ ID Session Baru:", currentRecommendationId);
        } else {
            console.warn("⚠️ Data tampil tapi ID Null (Cek Controller/DB).");
        }
    } else {
        data = payload;
    }

    // Isi Teks
    document.getElementById('diseaseName-result').textContent = data.disease ? data.disease.name : 'Unknown';
    document.getElementById('aiProvider-result').textContent = data.aiProvider || 'Unknown';
    
    // List Makanan
    fillList('foodsToEat-list', data.foodsToEat);
    fillList('foodsToAvoid-list', data.foodsToAvoid);
    
    // Notes & Raw
    toggleSection('additionalNotes', 'notes-content', data.additionalNotes);
    toggleSection('rawResponse', 'raw-content', data.recommendations);
    
    // Tampilkan Area Hasil
    results.style.display = 'block';
    results.classList.add('fade-in');
    results.scrollIntoView({ behavior: 'smooth', block: 'start' });

    // Tampilkan Area Feedback (Reset Kondisi)
    const feedbackSection = document.getElementById('feedback-section');
    if (feedbackSection) {
        feedbackSection.style.display = 'block';
        resetFeedbackUI(); // Pastikan tombol aktif lagi!
    }
}

// Fungsi Helper untuk mengisi List
function fillList(elementId, items) {
    const list = document.getElementById(elementId);
    list.innerHTML = '';
    if (items && items.length > 0) {
        items.forEach(item => {
            const li = document.createElement('li');
            li.textContent = item;
            list.appendChild(li);
        });
    } else {
        list.innerHTML = '<li>No specific recommendations</li>';
    }
}

// Fungsi Helper Toggle
function toggleSection(divId, contentId, content) {
    const div = document.getElementById(divId);
    const p = document.getElementById(contentId);
    if (content) {
        p.textContent = content;
        div.style.display = 'block';
    } else {
        div.style.display = 'none';
    }
}

// =======================================================
// 3. FITUR FEEDBACK (DENGAN PERBAIKAN TOMBOL)
// =======================================================

// Fungsi untuk me-reset tombol report agar bisa diklik lagi
function resetFeedbackUI() {
    const feedbackBtn = document.querySelector('#feedback-section button');
    const conditionInput = document.getElementById('user-condition');
    const finalAdviceDiv = document.getElementById('final-advice');

    if (feedbackBtn) {
        feedbackBtn.style.display = 'inline-block';
        feedbackBtn.disabled = false;
        feedbackBtn.innerText = "Report Condition to AI";
        feedbackBtn.style.cursor = 'pointer'; // Hilangkan tanda larangan
    }
    if (conditionInput) {
        conditionInput.value = '';
        conditionInput.disabled = false;
    }
    if (finalAdviceDiv) {
        finalAdviceDiv.innerHTML = '';
    }
}

async function submitFeedback() {
    const conditionInput = document.getElementById('user-condition');
    const feedbackSection = document.getElementById('final-advice');
    const feedbackBtn = document.querySelector('#feedback-section button');

    if (!conditionInput || !conditionInput.value.trim()) {
        alert("Mohon isi kondisi Anda terlebih dahulu!");
        return;
    }
    
    if (!currentRecommendationId) {
        alert("Error: ID sesi tidak ditemukan. Coba refresh.");
        return;
    }

    feedbackSection.innerHTML = "<i>⏳ Sedang berkonsultasi dengan AI...</i>";
    feedbackBtn.disabled = true;
    feedbackBtn.style.cursor = 'wait';

    try {
        const response = await fetch(`${API_BASE_URL}/recommend/feedback/${currentRecommendationId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ condition: conditionInput.value })
        });

        if (!response.ok) throw new Error("Gagal mengirim laporan.");

        const data = await response.json();
        
        // Logika Status & Warna
        let color = "#004085"; 
        let bgColor = "#cce5ff";
        
        // Cek status untuk mematikan/menghidupkan tombol
        if (data.status === "DOCTOR_REQUIRED" || data.status === "RECOVERED") {
            // Sesi Selesai
            if (data.status === "DOCTOR_REQUIRED") { color = "#721c24"; bgColor = "#f8d7da"; }
            else { color = "#155724"; bgColor = "#d4edda"; }
            
            feedbackBtn.style.display = 'none'; // Sembunyikan tombol
        } else {
            // Monitoring (Bisa lapor lagi)
            color = "#856404"; bgColor = "#fff3cd";
            feedbackBtn.style.display = 'inline-block';
            feedbackBtn.disabled = false;
            feedbackBtn.style.cursor = 'pointer';
            feedbackBtn.innerText = "Update Kondisi Lagi";
        }
        
        feedbackSection.innerHTML = `
            <div style="margin-top: 15px; padding: 15px; background-color: ${bgColor}; border-radius: 5px; border-left: 5px solid ${color};">
                <h4 style="color: ${color}; margin-top: 0;">STATUS: ${data.status}</h4>
                <p style="margin-bottom: 0;">${data.message}</p>
            </div>
        `;
        
        fetchHistory(); // Refresh sidebar

    } catch (error) {
        console.error(error);
        feedbackSection.innerHTML = `<p style="color: red;">⚠️ Error: ${error.message}</p>`;
        feedbackBtn.disabled = false;
        feedbackBtn.style.cursor = 'pointer';
    }
}

// =======================================================
// 4. HISTORY & UTILS (SOLUSI GAGAL MEMUAT DATA)
// =======================================================

async function fetchHistory() {
    const listContainer = document.getElementById('history-list');
    try {
        const response = await fetch(`${API_BASE_URL}/history?page=0&size=20`);
        const result = await response.json();
        
        // Fallback jika result kosong/null
        const historyItems = result.content || [];

        listContainer.innerHTML = ''; 

        if (historyItems.length === 0) {
            listContainer.innerHTML = '<li style="text-align:center; padding:10px; color:#888;">Belum ada riwayat.</li>';
            return;
        }

        historyItems.forEach(item => {
            // FORMAT TANGGAL YANG AMAN (Handle Array vs String)
            let dateStr = "";
            if (Array.isArray(item.createdAt)) {
                // Jika formatnya [2024, 12, 28, 14, 30]
                const [y, m, d, h, min] = item.createdAt;
                dateStr = `${d}/${m}/${y} ${h}:${min}`; // Manual format
            } else {
                // Jika formatnya String ISO "2024-12-28T..."
                dateStr = new Date(item.createdAt).toLocaleDateString('id-ID', {
                    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
                });
            }

            // Tentukan Badge
            let statusBadge = '<span style="background:#e2e3e5; color:#383d41; font-size:10px; padding:2px 5px; border-radius:4px;">Monitoring</span>';
            if (item.followUpStatus === 'RECOVERED') {
                statusBadge = '<span style="background:#d4edda; color:#155724; font-size:10px; padding:2px 5px; border-radius:4px;">Sembuh</span>';
            } else if (item.followUpStatus === 'REFERRED_TO_DOCTOR' || item.followUpStatus === 'DOCTOR_REQUIRED') {
                statusBadge = '<span style="background:#f8d7da; color:#721c24; font-size:10px; padding:2px 5px; border-radius:4px;">Ke Dokter</span>';
            }

            const li = document.createElement('li');
            li.style.cssText = "padding: 10px; border-bottom: 1px solid #eee; cursor: pointer;";
            
            // Klik history -> Load sesi lama
            li.onclick = () => loadSession(item.id);

            li.innerHTML = `
                <div style="font-weight: bold; font-size: 0.9rem;">${item.diseaseName}</div>
                <div style="display:flex; justify-content:space-between; align-items:center; margin-top:5px;">
                    <span style="font-size: 0.75rem; color: #666;">${dateStr}</span>
                    ${statusBadge}
                </div>
            `;
            listContainer.appendChild(li);
        });

    } catch (error) {
        console.error("History Error:", error);
        // Jangan tampilkan error merah di UI agar user tidak panik, cukup di console
    }
}

async function loadSession(id) {
    showLoading();
    hideResults();
    
    try {
        const response = await fetch(`${API_BASE_URL}/history/${id}`);
        if (!response.ok) throw new Error("Sesi tidak ditemukan");
        
        const data = await response.json();
        
        // Mapping data DB ke format Tampilan
        const displayData = {
            disease: { name: data.diseaseName },
            aiProvider: data.aiProvider,
            foodsToEat: JSON.parse(data.foodsToEat || "[]"),
            foodsToAvoid: JSON.parse(data.foodsToAvoid || "[]"),
            additionalNotes: data.additionalNotes,
            recommendations: data.rawResponse
        };

        // Tampilkan Hasil (Ini akan memanggil displayResults)
        displayResults({ data: displayData, id: data.id });
        
        // Khusus Sesi Lama: Cek status untuk mematikan tombol
        const feedbackBtn = document.querySelector('#feedback-section button');
        const conditionInput = document.getElementById('user-condition');
        const finalAdviceDiv = document.getElementById('final-advice');

        if (data.userFeedback) {
            conditionInput.value = data.userFeedback;
            conditionInput.disabled = true; // Readonly
            
            // Tampilkan pesan akhir
            let color = "#333"; let bgColor = "#eee";
            if (data.followUpStatus === "DOCTOR_REQUIRED") { color = "#721c24"; bgColor = "#f8d7da"; }
            else if (data.followUpStatus === "RECOVERED") { color = "#155724"; bgColor = "#d4edda"; }
            
            finalAdviceDiv.innerHTML = `
                <div style="margin-top: 15px; padding: 15px; background-color: ${bgColor}; border-left: 5px solid ${color};">
                    <h4 style="color: ${color}; margin: 0;">STATUS: ${data.followUpStatus}</h4>
                    <p>${data.aiFinalAdvice || ""}</p>
                </div>
                <p style="text-align: center; font-size: 0.8em; color: #666;">(Sesi Lampau)</p>
            `;
            
            // Jika sesi closed, sembunyikan tombol
            if(data.sessionClosed) {
                feedbackBtn.style.display = 'none';
            } else {
                feedbackBtn.style.display = 'inline-block'; // Jika monitoring, boleh lanjut
                feedbackBtn.innerText = "Update Lagi";
            }
        } 

    } catch (err) {
        console.error(err);
    } finally {
        hideLoading();
    }
}

// UI Utilities
function showLoading() {
    loading.style.display = 'block';
    submitBtn.disabled = true;
    submitBtn.textContent = 'Processing...';
}

function hideLoading() {
    loading.style.display = 'none';
    submitBtn.disabled = false;
    submitBtn.textContent = 'Get Recommendations';
}

function hideResults() {
    results.style.display = 'none';
}

function hideError() {
    error.style.display = 'none';
}

function showError(message) {
    document.getElementById('error-message').textContent = message;
    error.style.display = 'block';
}

function resetForm() {
    form.reset();
    hideResults();
    hideError();
    currentRecommendationId = null;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    resetFeedbackUI(); // Reset tombol feedback juga
}

// On Load
window.addEventListener('load', () => {
    fetchHistory();
});