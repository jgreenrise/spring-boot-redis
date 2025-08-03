// Application state
let currentQuiz = {
    score: 0,
    questionCount: 0,
    currentItem: null,
    selectedTag: null
};

// DOM elements
const elements = {
    feedTab: document.getElementById('feedTab'),
    quizTab: document.getElementById('quizTab'),
    feedSection: document.getElementById('feedSection'),
    quizSection: document.getElementById('quizSection'),
    fetchFeed: document.getElementById('fetchFeed'),
    rssUrl: document.getElementById('rssUrl'),
    tagFilter: document.getElementById('tagFilter'),
    loadItems: document.getElementById('loadItems'),
    feedItems: document.getElementById('feedItems'),
    quizTagFilter: document.getElementById('quizTagFilter'),
    startQuiz: document.getElementById('startQuiz'),
    score: document.getElementById('score'),
    questionCount: document.getElementById('questionCount'),
    quizWelcome: document.getElementById('quizWelcome'),
    quizQuestion: document.getElementById('quizQuestion'),
    questionTitle: document.getElementById('questionTitle'),
    questionOptions: document.getElementById('questionOptions'),
    nextQuestion: document.getElementById('nextQuestion'),
    showAnswer: document.getElementById('showAnswer'),
    loading: document.getElementById('loading')
};

// API base URL
const API_BASE = '/api/rss';

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    loadTags();
    loadItems();
});

function initializeEventListeners() {
    // Tab switching
    elements.feedTab.addEventListener('click', () => switchTab('feed'));
    elements.quizTab.addEventListener('click', () => switchTab('quiz'));
    
    // RSS feed functionality
    elements.fetchFeed.addEventListener('click', fetchRSSFeed);
    elements.loadItems.addEventListener('click', loadItems);
    
    // Quiz functionality
    elements.startQuiz.addEventListener('click', startQuiz);
    elements.nextQuestion.addEventListener('click', nextQuestion);
    elements.showAnswer.addEventListener('click', showAnswer);
}

function switchTab(tab) {
    if (tab === 'feed') {
        elements.feedTab.classList.add('active');
        elements.quizTab.classList.remove('active');
        elements.feedSection.classList.add('active');
        elements.quizSection.classList.remove('active');
    } else {
        elements.quizTab.classList.add('active');
        elements.feedTab.classList.remove('active');
        elements.quizSection.classList.add('active');
        elements.feedSection.classList.remove('active');
        loadTags(); // Refresh tags for quiz
    }
}

function showLoading() {
    elements.loading.style.display = 'flex';
}

function hideLoading() {
    elements.loading.style.display = 'none';
}

async function fetchRSSFeed() {
    const url = elements.rssUrl.value.trim();
    if (!url) {
        alert('Please enter a valid RSS URL');
        return;
    }
    
    showLoading();
    try {
        const response = await fetch(`${API_BASE}/fetch-and-save-rss?url=${encodeURIComponent(url)}`, {
            method: 'GET'
        });
        
        if (response.ok) {
            alert('RSS feed fetched and saved successfully!');
            await loadTags();
            await loadItems();
        } else {
            throw new Error('Failed to fetch RSS feed');
        }
    } catch (error) {
        console.error('Error fetching RSS feed:', error);
        alert('Error fetching RSS feed. Please check the URL and try again.');
    } finally {
        hideLoading();
    }
}

async function loadTags() {
    try {
        const response = await fetch(`${API_BASE}/tags`);
        if (response.ok) {
            const tags = await response.json();
            populateTagFilters(tags);
        }
    } catch (error) {
        console.error('Error loading tags:', error);
    }
}

function populateTagFilters(tags) {
    // Clear existing options except the first one
    elements.tagFilter.innerHTML = '<option value="">All Tags</option>';
    elements.quizTagFilter.innerHTML = '<option value="">All Categories</option>';
    
    tags.forEach(tag => {
        const option1 = document.createElement('option');
        option1.value = tag;
        option1.textContent = tag.charAt(0).toUpperCase() + tag.slice(1);
        elements.tagFilter.appendChild(option1);
        
        const option2 = document.createElement('option');
        option2.value = tag;
        option2.textContent = tag.charAt(0).toUpperCase() + tag.slice(1);
        elements.quizTagFilter.appendChild(option2);
    });
}

async function loadItems() {
    showLoading();
    try {
        const selectedTag = elements.tagFilter.value;
        let url = `${API_BASE}/items`;
        
        if (selectedTag) {
            url = `${API_BASE}/items/by-tag?tag=${encodeURIComponent(selectedTag)}`;
        }
        
        const response = await fetch(url);
        if (response.ok) {
            const items = await response.json();
            displayFeedItems(items);
        } else {
            throw new Error('Failed to load items');
        }
    } catch (error) {
        console.error('Error loading items:', error);
        elements.feedItems.innerHTML = '<p>Error loading RSS items. Please try again.</p>';
    } finally {
        hideLoading();
    }
}

function displayFeedItems(items) {
    if (items.length === 0) {
        elements.feedItems.innerHTML = '<p>No RSS items found. Try fetching some RSS feeds first.</p>';
        return;
    }
    
    elements.feedItems.innerHTML = items.map(item => `
        <div class="feed-item">
            <div class="meta">
                <span>${formatDate(item.pubDate)}</span>
            </div>
            <h3>${escapeHtml(item.title || 'No Title')}</h3>
            <p>${escapeHtml(truncateText(item.description || 'No description available', 150))}</p>
            <div class="tags">
                ${(item.tags || []).map(tag => `<span class="tag">${escapeHtml(tag)}</span>`).join('')}
            </div>
            <a href="${escapeHtml(item.link)}" target="_blank" rel="noopener noreferrer">Read More</a>
        </div>
    `).join('');
}

async function startQuiz() {
    const selectedTag = elements.quizTagFilter.value;
    currentQuiz.selectedTag = selectedTag;
    currentQuiz.score = 0;
    currentQuiz.questionCount = 0;
    
    updateQuizStats();
    await loadNextQuestion();
}

async function loadNextQuestion() {
    showLoading();
    try {
        let url = `${API_BASE}/quiz`;
        if (currentQuiz.selectedTag) {
            url += `?tag=${encodeURIComponent(currentQuiz.selectedTag)}`;
        }
        
        const response = await fetch(url);
        if (response.ok) {
            const item = await response.json();
            currentQuiz.currentItem = item;
            displayQuizQuestion(item);
        } else {
            throw new Error('No more quiz items available');
        }
    } catch (error) {
        console.error('Error loading quiz question:', error);
        alert('No more quiz items available for this category.');
    } finally {
        hideLoading();
    }
}

function displayQuizQuestion(item) {
    elements.quizWelcome.style.display = 'none';
    elements.quizQuestion.style.display = 'block';
    
    // Generate quiz question
    const questionType = Math.random() < 0.5 ? 'title' : 'content';
    let question, correctAnswer, options;
    
    if (questionType === 'title') {
        question = "What is the title of this article?";
        correctAnswer = item.title;
        options = generateTitleOptions(item.title);
    } else {
        question = "Which article contains this content?";
        correctAnswer = item.title;
        options = generateContentOptions(item.title, item.description);
    }
    
    elements.questionTitle.textContent = question;
    
    if (questionType === 'content') {
        elements.questionTitle.innerHTML = `
            <p>${question}</p>
            <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; font-style: italic;">
                "${truncateText(item.description, 200)}"
            </div>
        `;
    }
    
    elements.questionOptions.innerHTML = options.map((option, index) => `
        <div class="option" data-answer="${option === correctAnswer}" onclick="selectOption(this)">
            ${escapeHtml(option)}
        </div>
    `).join('');
    
    elements.nextQuestion.style.display = 'none';
    elements.showAnswer.style.display = 'inline-block';
}

function generateTitleOptions(correctTitle) {
    // In a real application, you'd fetch other titles from the database
    const dummyTitles = [
        "Advanced Machine Learning Techniques for Modern Applications",
        "The Future of Web Development: Trends and Technologies",
        "Understanding Cloud Computing: A Comprehensive Guide",
        "Cybersecurity Best Practices for Enterprise Solutions"
    ];
    
    const options = [correctTitle];
    const shuffledDummies = dummyTitles.sort(() => 0.5 - Math.random());
    
    for (let i = 0; i < 3 && i < shuffledDummies.length; i++) {
        if (shuffledDummies[i] !== correctTitle) {
            options.push(shuffledDummies[i]);
        }
    }
    
    return options.sort(() => 0.5 - Math.random());
}

function generateContentOptions(correctTitle, content) {
    // Similar to generateTitleOptions but for content-based questions
    const dummyTitles = [
        "Introduction to Artificial Intelligence",
        "Building Scalable Web Applications",
        "Data Science and Analytics Fundamentals",
        "Mobile App Development Best Practices"
    ];
    
    const options = [correctTitle];
    const shuffledDummies = dummyTitles.sort(() => 0.5 - Math.random());
    
    for (let i = 0; i < 3 && i < shuffledDummies.length; i++) {
        if (shuffledDummies[i] !== correctTitle) {
            options.push(shuffledDummies[i]);
        }
    }
    
    return options.sort(() => 0.5 - Math.random());
}

function selectOption(optionElement) {
    // Remove previous selections
    document.querySelectorAll('.option').forEach(opt => {
        opt.classList.remove('selected');
    });
    
    // Mark this option as selected
    optionElement.classList.add('selected');
}

function showAnswer() {
    const options = document.querySelectorAll('.option');
    let selectedOption = null;
    
    options.forEach(option => {
        if (option.classList.contains('selected')) {
            selectedOption = option;
        }
        
        if (option.dataset.answer === 'true') {
            option.classList.add('correct');
        } else if (option.classList.contains('selected')) {
            option.classList.add('incorrect');
        }
        
        option.style.pointerEvents = 'none';
    });
    
    // Update score
    if (selectedOption && selectedOption.dataset.answer === 'true') {
        currentQuiz.score++;
    }
    
    currentQuiz.questionCount++;
    updateQuizStats();
    
    elements.showAnswer.style.display = 'none';
    elements.nextQuestion.style.display = 'inline-block';
}

function nextQuestion() {
    // Reset options
    document.querySelectorAll('.option').forEach(option => {
        option.classList.remove('selected', 'correct', 'incorrect');
        option.style.pointerEvents = 'auto';
    });
    
    loadNextQuestion();
}

function updateQuizStats() {
    elements.score.textContent = currentQuiz.score;
    elements.questionCount.textContent = currentQuiz.questionCount;
}

// Utility functions
function formatDate(dateString) {
    if (!dateString) return 'Unknown date';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}