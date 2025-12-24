// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements
const form = document.getElementById('recommendationForm');
const loading = document.getElementById('loading');
const results = document.getElementById('results');
const error = document.getElementById('error');
const submitBtn = document.getElementById('submitBtn');

// Form Submit Handler
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // Get form data
    const formData = new FormData(form);
    const data = {
        diseaseName: formData.get('diseaseName'),
        diseaseType: formData.get('diseaseType'),
        aiProvider: formData.get('aiProvider'),
    };
    
    // Add severity if provided
    const severity = formData.get('severity');
    if (severity) {
        data.severity = severity;
    }
    
    // Show loading, hide results and errors
    showLoading();
    hideResults();
    hideError();
    
    try {
        // Call API
        const response = await fetch(`${API_BASE_URL}/recommend/detailed`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.error || 'Failed to get recommendations');
        }
        
        // Display results
        displayResults(result);
        
    } catch (err) {
        console.error('Error:', err);
        showError(err.message || 'Something went wrong. Please try again.');
    } finally {
        hideLoading();
    }
});

// Show Loading
function showLoading() {
    loading.style.display = 'block';
    submitBtn.disabled = true;
    submitBtn.textContent = 'Processing...';
}

// Hide Loading
function hideLoading() {
    loading.style.display = 'none';
    submitBtn.disabled = false;
    submitBtn.textContent = 'Get Recommendations';
}

// Show Results
function displayResults(data) {
    // Set disease name and AI provider
    document.getElementById('diseaseName-result').textContent = 
        data.disease ? data.disease.name : 'Unknown';
    document.getElementById('aiProvider-result').textContent = 
        data.aiProvider || 'Unknown';
    
    // Display foods to eat
    const foodsToEatList = document.getElementById('foodsToEat-list');
    foodsToEatList.innerHTML = '';
    
    if (data.foodsToEat && data.foodsToEat.length > 0) {
        data.foodsToEat.forEach(food => {
            const li = document.createElement('li');
            li.textContent = food;
            foodsToEatList.appendChild(li);
        });
    } else {
        foodsToEatList.innerHTML = '<li>No specific recommendations</li>';
    }
    
    // Display foods to avoid
    const foodsToAvoidList = document.getElementById('foodsToAvoid-list');
    foodsToAvoidList.innerHTML = '';
    
    if (data.foodsToAvoid && data.foodsToAvoid.length > 0) {
        data.foodsToAvoid.forEach(food => {
            const li = document.createElement('li');
            li.textContent = food;
            foodsToAvoidList.appendChild(li);
        });
    } else {
        foodsToAvoidList.innerHTML = '<li>No specific restrictions</li>';
    }
    
    // Display additional notes
    const additionalNotesDiv = document.getElementById('additionalNotes');
    const notesContent = document.getElementById('notes-content');
    
    if (data.additionalNotes) {
        notesContent.textContent = data.additionalNotes;
        additionalNotesDiv.style.display = 'block';
    } else {
        additionalNotesDiv.style.display = 'none';
    }
    
    // Display raw response if available
    const rawResponseDiv = document.getElementById('rawResponse');
    const rawContent = document.getElementById('raw-content');
    
    if (data.recommendations) {
        rawContent.textContent = data.recommendations;
        rawResponseDiv.style.display = 'block';
    } else {
        rawResponseDiv.style.display = 'none';
    }
    
    // Show results with animation
    results.style.display = 'block';
    results.classList.add('fade-in');
    
    // Scroll to results
    results.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// Hide Results
function hideResults() {
    results.style.display = 'none';
}

// Show Error
function showError(message) {
    document.getElementById('error-message').textContent = message;
    error.style.display = 'block';
    error.classList.add('fade-in');
    error.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// Hide Error
function hideError() {
    error.style.display = 'none';
}

// Reset Form
function resetForm() {
    form.reset();
    hideResults();
    hideError();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Test API Connection on page load
window.addEventListener('load', async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/health`);
        const data = await response.json();
        console.log('API Status:', data);
    } catch (err) {
        console.warn('API not reachable:', err);
    }
});