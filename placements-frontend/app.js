// PlacementOS - Single Page Application Core Logic

// 1. Initial State
const state = {
    token: localStorage.getItem('pos_token') || null,
    user: null,
    schedules: [],
    currentSchedule: null,
    currentDay: 1,
    authMode: 'login', // 'login' | 'register'
    wizardSelectedTopics: new Set(),
    notes: JSON.parse(localStorage.getItem('pos_notes_v1')) || {},
    reminders: JSON.parse(localStorage.getItem('pos_reminders_v1')) || {},
    ytApiReady: false,
    ytPlayers: {},
    ytPollIntervals: {}
};

// Topic Taxonomy Constants
const DSA_TOPICS = [
    "ARRAYS", "HASHING", "SORTING", "BINARY_SEARCH", "STRINGS",
    "TWO_POINTERS", "SLIDING_WINDOW", "LINKED_LIST", "RECURSION", "BIT_MANIPULATION",
    "STACK_QUEUE", "HEAP", "GREEDY", "BACKTRACKING", "TREES",
    "BST", "GRAPHS", "DP", "TRIE"
];

const SUBJECTS = [
    "JAVA_OOP", "SQL", "DBMS", "OS", "CN",
    "PROJECT", "INTERVIEW_PREP", "RESUME_PORTFOLIO"
];

// Helper to format topic names beautifully
function formatTopicKey(key) {
    return key.split('_').map(word => word.charAt(0) + word.slice(1).toLowerCase()).join(' ');
}

// 2. DOM Elements
const authScreen = document.getElementById('authScreen');
const wizardScreen = document.getElementById('wizardScreen');
const trackerScreen = document.getElementById('trackerScreen');
const authForm = document.getElementById('authForm');
const authEmail = document.getElementById('authEmail');
const authPassword = document.getElementById('authPassword');
const authSubmitBtn = document.getElementById('authSubmitBtn');
const authTitle = document.getElementById('authTitle');
const authSubtitle = document.getElementById('authSubtitle');
const authToggleLink = document.getElementById('authToggleLink');
const authToggleText = document.getElementById('authToggleText');
const authError = document.getElementById('authError');
const authApiUrl = document.getElementById('authApiUrl');

const wizardStartDate = document.getElementById('wizardStartDate');
const wizardEndDate = document.getElementById('wizardEndDate');
const wizardSubmitBtn = document.getElementById('wizardSubmitBtn');
const wizardLoading = document.getElementById('wizardLoading');
const dsaTopicChips = document.getElementById('dsaTopicChips');
const subjectTopicChips = document.getElementById('subjectTopicChips');
const runwayBox = document.getElementById('runwayBox');
const runwayCount = document.getElementById('runwayCount');
const wizardError = document.getElementById('wizardError');
const selectAllDsa = document.getElementById('selectAllDsa');
const selectAllSubjects = document.getElementById('selectAllSubjects');

const sidebarUserEmail = document.getElementById('sidebarUserEmail');
const ringFill = document.getElementById('ringFill');
const progressPercent = document.getElementById('progressPercent');
const streakDays = document.getElementById('streakDays');
const sidebarProgressCount = document.getElementById('sidebarProgressCount');
const dayNav = document.getElementById('dayNav');
const menuToggleBtn = document.getElementById('menuToggleBtn');
const sidebar = document.getElementById('sidebar');
const scrim = document.getElementById('scrim');

const eyebrowDayNum = document.getElementById('eyebrowDayNum');
const dayTitleHeader = document.getElementById('dayTitleHeader');
const countdownTimer = document.getElementById('countdownTimer');
const countdownMount = document.getElementById('countdownMount');
const missionTopicsText = document.getElementById('missionTopicsText');
const tasksListContainer = document.getElementById('tasksListContainer');
const notesTextarea = document.getElementById('notesTextarea');
const btnPrevDay = document.getElementById('btnPrevDay');
const btnNextDay = document.getElementById('btnNextDay');

const settingsModal = document.getElementById('settingsModal');
const settingsModalCloseBtn = document.getElementById('settingsModalCloseBtn');
const settingsLeetcodeUsername = document.getElementById('settingsLeetcodeUsername');
const settingsLastSyncText = document.getElementById('settingsLastSyncText');
const settingsSaveBtn = document.getElementById('settingsSaveBtn');
const settingsError = document.getElementById('settingsError');

const btnToggleNotifications = document.getElementById('btnToggleNotifications');
const btnSyncLeetcode = document.getElementById('btnSyncLeetcode');
const btnOpenSettings = document.getElementById('btnOpenSettings');
const btnNewSchedule = document.getElementById('btnNewSchedule');
const btnLogOut = document.getElementById('btnLogOut');
const dayNightSwitch = document.getElementById('dayNightSwitch');
const themeToggleIcon = document.getElementById('themeToggleIcon');
const themeToggleText = document.getElementById('themeToggleText');

// Set URL footer inside Auth screen
authApiUrl.textContent = API_BASE_URL;

// 3. API Request Wrapper with Auth Injection
async function apiFetch(endpoint, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }

    const config = {
        ...options,
        headers
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP error! status: ${response.status}`);
    }

    if (response.status === 204) return null;
    return response.json();
}

// 4. Initial Load Page Router
async function initApp() {
    setupTheme();
    setupWizardTopicChips();
    setupEventListeners();

    if (state.token) {
        try {
            await fetchUserProfile();
            await fetchSchedules();
            if (state.schedules.length === 0) {
                showScreen('wizard');
            } else {
                // Pick most recent schedule
                state.currentSchedule = state.schedules[0];
                state.currentDay = calculateCurrentDayNumber();
                showScreen('tracker');
                renderTracker();
            }
        } catch (e) {
            console.error("Token invalid or backend unreachable. Logging out.", e);
            logout();
        }
    } else {
        showScreen('auth');
    }
}

// Helper to toggle visible screens
function showScreen(screenName) {
    authScreen.style.display = 'none';
    wizardScreen.style.display = 'none';
    trackerScreen.style.display = 'none';

    if (screenName === 'auth') {
        authScreen.style.display = 'flex';
    } else if (screenName === 'wizard') {
        wizardScreen.style.display = 'flex';
        resetWizard();
    } else if (screenName === 'tracker') {
        trackerScreen.style.display = 'flex';
    }
}

// Calculations for current day sequence
function calculateCurrentDayNumber() {
    if (!state.currentSchedule) return 1;
    const start = new Date(state.currentSchedule.startDate);
    const today = new Date();
    // Reset hours to match date boundaries
    start.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    const diffTime = today - start;
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1;

    // Constrain to schedule scope
    const totalDays = state.currentSchedule.items.reduce((max, item) => Math.max(max, item.dayNumber), 1);
    if (diffDays < 1) return 1;
    if (diffDays > totalDays) return totalDays;
    return diffDays;
}

// 5. Auth Flow Logic
authToggleLink.addEventListener('click', (e) => {
    e.preventDefault();
    authError.style.display = 'none';
    if (state.authMode === 'login') {
        state.authMode = 'register';
        authTitle.textContent = "Create Account";
        authSubtitle.textContent = "Register to PlacementOS to start preparation";
        authSubmitBtn.textContent = "Register";
        authToggleText.textContent = "Already have an account?";
        authToggleLink.textContent = "Login";
    } else {
        state.authMode = 'login';
        authTitle.textContent = "PlacementOS";
        authSubtitle.textContent = "Self-hosted Placement Prep Roadmap Tracker";
        authSubmitBtn.textContent = "Login";
        authToggleText.textContent = "Don't have an account?";
        authToggleLink.textContent = "Register";
    }
});

authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    authError.style.display = 'none';

    const email = authEmail.value.trim();
    const password = authPassword.value;

    if (!email || !password) {
        showAuthError("Please fill out all fields.");
        return;
    }

    if (state.authMode === 'register' && password.length < 8) {
        showAuthError("Password must be at least 8 characters long.");
        return;
    }

    try {
        const endpoint = state.authMode === 'register' ? '/api/auth/register' : '/api/auth/login';
        const data = await apiFetch(endpoint, {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });

        state.token = data.token;
        localStorage.setItem('pos_token', data.token);
        
        state.user = {
            id: data.userId,
            email: data.email,
            leetcodeUsername: data.leetcodeUsername,
            lastLeetcodeSyncAt: data.lastLeetcodeSyncAt
        };

        await afterAuthSuccess();
    } catch (err) {
        showAuthError(err.message);
    }
});

function showAuthError(msg) {
    authError.textContent = msg;
    authError.style.display = 'block';
}

async function afterAuthSuccess() {
    await fetchUserProfile();
    await fetchSchedules();
    if (state.schedules.length === 0) {
        showScreen('wizard');
    } else {
        state.currentSchedule = state.schedules[0];
        state.currentDay = calculateCurrentDayNumber();
        showScreen('tracker');
        renderTracker();
    }
}

async function fetchUserProfile() {
    const data = await apiFetch('/api/users/me');
    state.user = data;
}

async function fetchSchedules() {
    state.schedules = await apiFetch('/api/schedules');
}

function logout() {
    state.token = null;
    state.user = null;
    state.schedules = [];
    state.currentSchedule = null;
    state.currentDay = 1;
    localStorage.removeItem('pos_token');
    showScreen('auth');
}

// 6. Setup Wizard Logic
function setupWizardTopicChips() {
    dsaTopicChips.innerHTML = '';
    subjectTopicChips.innerHTML = '';

    DSA_TOPICS.forEach(topic => {
        const btn = document.createElement('button');
        btn.className = 'chip';
        btn.textContent = formatTopicKey(topic);
        btn.type = 'button';
        btn.addEventListener('click', () => toggleWizardTopic(topic, btn));
        dsaTopicChips.appendChild(btn);
    });

    SUBJECTS.forEach(subject => {
        const btn = document.createElement('button');
        btn.className = 'chip';
        btn.textContent = formatTopicKey(subject);
        btn.type = 'button';
        btn.addEventListener('click', () => toggleWizardTopic(subject, btn));
        subjectTopicChips.appendChild(btn);
    });
}

function toggleWizardTopic(topic, chipElement) {
    if (state.wizardSelectedTopics.has(topic)) {
        state.wizardSelectedTopics.delete(topic);
        chipElement.classList.remove('active');
    } else {
        state.wizardSelectedTopics.add(topic);
        chipElement.classList.add('active');
    }
}

// Date selection listeners & Quick chip triggers
function updateRunwayVisual() {
    const startVal = wizardStartDate.value;
    const endVal = wizardEndDate.value;

    if (startVal && endVal) {
        const start = new Date(startVal);
        const end = new Date(endVal);
        
        if (end >= start) {
            const diffTime = end - start;
            const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1;
            runwayCount.textContent = diffDays;
            runwayBox.style.display = 'block';
            return;
        }
    }
    runwayBox.style.display = 'none';
}

wizardStartDate.addEventListener('change', updateRunwayVisual);
wizardEndDate.addEventListener('change', updateRunwayVisual);

document.querySelectorAll('.duration-chips .chip').forEach(btn => {
    btn.addEventListener('click', (e) => {
        e.preventDefault();
        document.querySelectorAll('.duration-chips .chip').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');

        const days = parseInt(btn.getAttribute('data-days'), 10);
        let start = wizardStartDate.value ? new Date(wizardStartDate.value) : new Date();
        
        if (!wizardStartDate.value) {
            // Default to today if empty
            const offset = start.getTimezoneOffset();
            start = new Date(start.getTime() - (offset*60*1000));
            wizardStartDate.value = start.toISOString().split('T')[0];
        }

        const end = new Date(start.getTime() + (days - 1) * 24 * 60 * 60 * 1000);
        const offsetEnd = end.getTimezoneOffset();
        const localEnd = new Date(end.getTime() - (offsetEnd*60*1000));
        wizardEndDate.value = localEnd.toISOString().split('T')[0];
        
        updateRunwayVisual();
    });
});

selectAllDsa.addEventListener('click', () => {
    const allActive = Array.from(dsaTopicChips.children).every(c => c.classList.contains('active'));
    dsaTopicChips.querySelectorAll('.chip').forEach(chip => {
        const topic = DSA_TOPICS[Array.from(dsaTopicChips.children).indexOf(chip)];
        if (allActive) {
            state.wizardSelectedTopics.delete(topic);
            chip.classList.remove('active');
        } else {
            state.wizardSelectedTopics.add(topic);
            chip.classList.add('active');
        }
    });
});

selectAllSubjects.addEventListener('click', () => {
    const allActive = Array.from(subjectTopicChips.children).every(c => c.classList.contains('active'));
    subjectTopicChips.querySelectorAll('.chip').forEach(chip => {
        const subject = SUBJECTS[Array.from(subjectTopicChips.children).indexOf(chip)];
        if (allActive) {
            state.wizardSelectedTopics.delete(subject);
            chip.classList.remove('active');
        } else {
            state.wizardSelectedTopics.add(subject);
            chip.classList.add('active');
        }
    });
});

wizardSubmitBtn.addEventListener('click', async () => {
    wizardError.style.display = 'none';
    const startDate = wizardStartDate.value;
    const endDate = wizardEndDate.value;
    const topics = Array.from(state.wizardSelectedTopics);

    if (!startDate || !endDate) {
        showWizardError("Please select both start and end dates.");
        return;
    }

    if (new Date(startDate) > new Date(endDate)) {
        showWizardError("End date must be on or after start date.");
        return;
    }

    if (topics.length === 0) {
        showWizardError("Please select at least one topic or subject.");
        return;
    }

    // Toggle loading UI
    wizardSubmitBtn.style.display = 'none';
    wizardLoading.style.display = 'flex';

    try {
        const schedule = await apiFetch('/api/schedules', {
            method: 'POST',
            body: JSON.stringify({ startDate, endDate, topics })
        });

        // Add to schedules list and transition
        state.schedules.unshift(schedule);
        state.currentSchedule = schedule;
        state.currentDay = 1;

        showScreen('tracker');
        renderTracker();
    } catch (err) {
        showWizardError(err.message);
        wizardSubmitBtn.style.display = 'inline-flex';
        wizardLoading.style.display = 'none';
    }
});

function showWizardError(msg) {
    wizardError.textContent = msg;
    wizardError.style.display = 'block';
}

function resetWizard() {
    wizardStartDate.value = '';
    wizardEndDate.value = '';
    document.querySelectorAll('.duration-chips .chip').forEach(c => c.classList.remove('active'));
    state.wizardSelectedTopics.clear();
    setupWizardTopicChips();
    runwayBox.style.display = 'none';
    wizardError.style.display = 'none';
    wizardSubmitBtn.style.display = 'inline-flex';
    wizardLoading.style.display = 'none';
}

// 7. Tracker Screen Rendering & Logic
function renderTracker() {
    if (!state.currentSchedule) return;

    // Render User Header
    sidebarUserEmail.textContent = state.user ? state.user.email : '';

    // Render Sidebar Day Navigation
    renderSidebarDayNav();

    // Render Progress Ring & Metrics
    renderProgressAndMetrics();

    // Render Selected Day Content
    renderActiveDayContent();
}

function renderSidebarDayNav() {
    dayNav.innerHTML = '';
    const items = state.currentSchedule.items;
    
    // Group items by dayNumber
    const daysMap = {};
    items.forEach(item => {
        if (!daysMap[item.dayNumber]) {
            daysMap[item.dayNumber] = {
                dayNumber: item.dayNumber,
                itemDate: item.itemDate,
                topics: new Set(),
                items: []
            };
        }
        daysMap[item.dayNumber].topics.add(item.topicKey);
        daysMap[item.dayNumber].items.push(item);
    });

    const sortedDays = Object.values(daysMap).sort((a, b) => a.dayNumber - b.dayNumber);

    // Group sorted days by weeks (7 days per week)
    let currentWeek = 0;
    sortedDays.forEach(day => {
        const weekNum = Math.floor((day.dayNumber - 1) / 7) + 1;
        if (weekNum !== currentWeek) {
            currentWeek = weekNum;
            const weekHeader = document.createElement('div');
            weekHeader.className = 'week-header';
            weekHeader.textContent = `Week ${currentWeek}`;
            dayNav.appendChild(weekHeader);
        }

        const isComplete = day.items.every(it => it.completed);
        const dayBtn = document.createElement('button');
        dayBtn.className = 'day-nav-item';
        if (day.dayNumber === state.currentDay) dayBtn.classList.add('active');
        if (isComplete) dayBtn.classList.add('complete');

        // Check if this day number matches actual calendar date (today)
        const dayDate = new Date(day.itemDate);
        const today = new Date();
        dayDate.setHours(0, 0, 0, 0);
        today.setHours(0, 0, 0, 0);
        if (dayDate.getTime() === today.getTime()) {
            dayBtn.classList.add('today');
        }

        // Distinct topic titles (first 2 distinct topics)
        const topicsSummary = Array.from(day.topics).slice(0, 2).map(formatTopicKey).join(' • ');

        dayBtn.innerHTML = `
            <div class="day-title-row">
                <span>Day ${day.dayNumber}</span>
            </div>
            <div class="day-nav-topics">${topicsSummary || 'Rest Day'}</div>
        `;

        dayBtn.addEventListener('click', () => {
            state.currentDay = day.dayNumber;
            // Remove active from all nav list
            document.querySelectorAll('.day-nav-item').forEach(btn => btn.classList.remove('active'));
            dayBtn.classList.add('active');
            renderActiveDayContent();
            
            // Close mobile menu if open
            if (sidebar.classList.contains('open')) {
                sidebar.classList.remove('open');
            }
        });

        dayNav.appendChild(dayBtn);
    });
}

function renderProgressAndMetrics() {
    const items = state.currentSchedule.items;
    
    // Calculate distinct days completion status
    const daysMap = {};
    items.forEach(item => {
        if (!daysMap[item.dayNumber]) {
            daysMap[item.dayNumber] = [];
        }
        daysMap[item.dayNumber].push(item);
    });

    const totalDays = Object.keys(daysMap).length;
    let completedDays = 0;
    Object.values(daysMap).forEach(dayItems => {
        if (dayItems.every(it => it.completed)) {
            completedDays++;
        }
    });

    // Calculate percent
    const percent = totalDays > 0 ? Math.round((completedDays / totalDays) * 100) : 0;
    progressPercent.textContent = `${percent}%`;

    // SVG Progress Ring Calculation (Radius 34, Circumference = 2 * PI * 34 = 213.6)
    const circumference = 2 * Math.PI * 34;
    ringFill.style.strokeDasharray = `${circumference} ${circumference}`;
    const offset = circumference - (percent / 100) * circumference;
    ringFill.style.strokeDashoffset = offset;

    // Display count
    sidebarProgressCount.textContent = `${completedDays}/${totalDays} Days Complete`;

    // Calculate Streak
    // Streak is defined as consecutive completed days ending today (or yesterday if today isn't fully completed yet)
    let streak = 0;
    const sortedDayNumbers = Object.keys(daysMap).map(Number).sort((a,b) => a-b);
    const todayIndex = calculateCurrentDayNumber();

    // Iterate backwards from yesterday or today to check completion streak
    let checkDay = todayIndex;
    // If today is not complete, we start checking from yesterday
    const todayComplete = daysMap[todayIndex] ? daysMap[todayIndex].every(it => it.completed) : false;
    if (!todayComplete) {
        checkDay = todayIndex - 1;
    }

    while (checkDay > 0) {
        const dayComplete = daysMap[checkDay] ? daysMap[checkDay].every(it => it.completed) : false;
        if (dayComplete) {
            streak++;
            checkDay--;
        } else {
            break;
        }
    }
    streakDays.textContent = streak;
}

// Ticking Countdown timer
let countdownInterval = null;

function renderActiveDayContent() {
    const schedule = state.currentSchedule;
    const dayNumber = state.currentDay;

    // Clean up active YT intervals & players
    Object.values(state.ytPollIntervals).forEach(clearInterval);
    state.ytPollIntervals = {};
    state.ytPlayers = {};

    // Get current day's items
    const dayItems = schedule.items.filter(it => it.dayNumber === dayNumber);
    const dayDateStr = dayItems.length > 0 ? dayItems[0].itemDate : schedule.startDate;
    const formattedDate = new Date(dayDateStr).toLocaleDateString('en-US', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });

    eyebrowDayNum.textContent = `Day ${dayNumber} of ${schedule.items.reduce((max, item) => Math.max(max, item.dayNumber), 1)}`;
    dayTitleHeader.textContent = formattedDate;

    // Reset notes textarea
    const noteKey = `${schedule.id}-${dayNumber}`;
    notesTextarea.value = state.notes[noteKey] || '';

    // Handle Prev/Next day buttons
    const totalScheduleDays = schedule.items.reduce((max, item) => Math.max(max, item.dayNumber), 0);
    btnPrevDay.disabled = dayNumber <= 1;
    btnNextDay.disabled = dayNumber >= totalScheduleDays;

    // Render Countdown Timer
    startCountdownClock(schedule.endDate);

    // Renders Day Topics list or rest day
    if (dayItems.length === 0) {
        missionCard.style.display = 'none';
        tasksListContainer.innerHTML = `
            <div class="rest-day-card">
                <h3>☕ Rest Day</h3>
                <p>No study tasks scheduled for today. Rest up, review your notes, or catch up on missed targets!</p>
            </div>
        `;
        return;
    }

    missionCard.style.display = 'block';
    const distinctTopics = Array.from(new Set(dayItems.map(it => it.topicKey)));
    missionTopicsText.textContent = distinctTopics.map(formatTopicKey).join(' • ');

    // Render items list
    tasksListContainer.innerHTML = '';
    dayItems.forEach(item => {
        const itemRow = document.createElement('div');
        itemRow.className = `item-row ${item.completed ? 'completed' : ''}`;
        itemRow.id = `item-row-${item.id}`;

        let itemTitleHtml = '';
        let badgeClass = 'badge-task';
        if (item.type === 'DSA_QUESTION') {
            badgeClass = 'badge-dsa';
            itemTitleHtml = `<a class="item-title" href="${item.resourceUrl}" target="_blank">${item.title} ↗</a>`;
        } else if (item.type === 'VIDEO') {
            badgeClass = 'badge-video';
            itemTitleHtml = `<span class="item-title">${item.title}</span>`;
        } else {
            itemTitleHtml = `<span class="item-title">${item.title}</span>`;
        }

        const autoBadgeHtml = item.completedVia ? `<span class="item-auto-badge">${item.completedVia === 'LEETCODE_SYNC' ? 'LeetCode Sync' : 'Watched'}</span>` : '';

        itemRow.innerHTML = `
            <div class="item-row-header">
                <div class="item-checkbox ${item.completed ? 'checked' : ''}" data-item-id="${item.id}" tabindex="0"></div>
                <div class="item-title-col">
                    ${itemTitleHtml}
                    <span class="item-type-badge ${badgeClass}">${formatTopicKey(item.type)}</span>
                    ${autoBadgeHtml}
                </div>
            </div>
        `;

        // If it's a video item and not completed, embed player container
        if (item.type === 'VIDEO') {
            const playerContainer = document.createElement('div');
            playerContainer.className = 'youtube-player-container';
            playerContainer.innerHTML = `
                <div class="video-iframe-wrapper">
                    <div id="yt-player-${item.id}"></div>
                </div>
                <div class="video-progress-container" id="progress-container-${item.id}" style="${item.completed ? 'display:none;' : ''}">
                    <div class="video-progress-bar">
                        <div class="video-progress-fill" id="progress-fill-${item.id}"></div>
                    </div>
                    <span class="video-percent-label" id="progress-label-${item.id}">0%</span>
                </div>
                <div style="font-size:13px; margin-top:2px;">
                    <a href="${item.resourceUrl}" target="_blank" style="font-weight:500;">Open on YouTube ↗</a>
                </div>
            `;
            itemRow.appendChild(playerContainer);

            // Lazy inject YouTube Embed script if not present
            loadYoutubeApiScript();
        }

        tasksListContainer.appendChild(itemRow);
    });

    // Checkbox click handlers
    tasksListContainer.querySelectorAll('.item-checkbox').forEach(box => {
        box.addEventListener('click', () => {
            const itemId = parseInt(box.getAttribute('data-item-id'), 10);
            toggleItemCompleted(itemId);
        });

        box.addEventListener('keydown', (e) => {
            if (e.key === ' ' || e.key === 'Enter') {
                e.preventDefault();
                const itemId = parseInt(box.getAttribute('data-item-id'), 10);
                toggleItemCompleted(itemId);
            }
        });
    });

    // Instantiate YouTube Players for this page
    if (state.ytApiReady) {
        dayItems.forEach(item => {
            if (item.type === 'VIDEO') {
                initYoutubePlayer(item.id, item.youtubeVideoId);
            }
        });
    }
}

function startCountdownClock(endDateStr) {
    if (countdownInterval) {
        clearInterval(countdownInterval);
    }

    const targetDate = new Date(`${endDateStr}T23:59:59`).getTime();

    function update() {
        const now = new Date().getTime();
        const diff = targetDate - now;

        if (diff <= 0) {
            countdownTimer.textContent = "🏁 Target date reached!";
            countdownMount.classList.remove('urgent');
            clearInterval(countdownInterval);
            return;
        }

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const secs = Math.floor((diff % (1000 * 60)) / 1000);

        countdownTimer.textContent = `${days}d : ${String(hours).padStart(2, '0')}h : ${String(mins).padStart(2, '0')}m : ${String(secs).padStart(2, '0')}s`;

        // Switch to urgent color if less than 3 days remain
        if (days < 3) {
            countdownMount.classList.add('urgent');
        } else {
            countdownMount.classList.remove('urgent');
        }
    }

    update();
    countdownInterval = setInterval(update, 1000);
}

// 8. YouTube Embedding & watch-tracking
function loadYoutubeApiScript() {
    if (window.YT) {
        state.ytApiReady = true;
        return;
    }

    if (document.getElementById('yt-iframe-api-script')) return;

    const tag = document.createElement('script');
    tag.id = 'yt-iframe-api-script';
    tag.src = "https://www.youtube.com/iframe_api";
    const firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
}

// Called automatically by YouTube IFrame API
window.onYouTubeIframeAPIReady = function() {
    state.ytApiReady = true;
    // Check active day video items and initialize
    if (state.currentSchedule) {
        const dayNumber = state.currentDay;
        const dayItems = state.currentSchedule.items.filter(it => it.dayNumber === dayNumber);
        dayItems.forEach(item => {
            if (item.type === 'VIDEO') {
                initYoutubePlayer(item.id, item.youtubeVideoId);
            }
        });
    }
};

function initYoutubePlayer(itemId, videoId) {
    if (!state.ytApiReady || !window.YT) return;

    state.ytPlayers[itemId] = new YT.Player(`yt-player-${itemId}`, {
        height: '100%',
        width: '100%',
        videoId: videoId,
        playerVars: {
            'playsinline': 1,
            'rel': 0
        },
        events: {
            'onStateChange': (e) => onPlayerStateChange(e, itemId)
        }
    });
}

function onPlayerStateChange(event, itemId) {
    // If state is PLAYING (1), begin polling watch progress
    if (event.data === YT.PlayerState.PLAYING) {
        startVideoProgressPolling(itemId);
    } else {
        stopVideoProgressPolling(itemId);
    }
}

function startVideoProgressPolling(itemId) {
    if (state.ytPollIntervals[itemId]) return;

    state.ytPollIntervals[itemId] = setInterval(() => {
        const player = state.ytPlayers[itemId];
        if (player && typeof player.getCurrentTime === 'function') {
            const current = player.getCurrentTime();
            const duration = player.getDuration();
            
            if (duration > 0) {
                const ratio = current / duration;
                const percent = Math.round(ratio * 100);
                
                // Update DOM elements in place
                const fill = document.getElementById(`progress-fill-${itemId}`);
                const label = document.getElementById(`progress-label-${itemId}`);
                if (fill) fill.style.width = `${percent}%`;
                if (label) label.textContent = `${percent}%`;

                // If progress reaches 90% (0.9 ratio), auto-complete and stop poller
                if (ratio >= 0.9) {
                    markItemVideoWatched(itemId);
                }
            }
        }
    }, 3000);
}

function stopVideoProgressPolling(itemId) {
    if (state.ytPollIntervals[itemId]) {
        clearInterval(state.ytPollIntervals[itemId]);
        delete state.ytPollIntervals[itemId];
    }
}

// 9. Manual & Auto completion API Sync
async function toggleItemCompleted(itemId) {
    const item = state.currentSchedule.items.find(it => it.id === itemId);
    if (!item) return;

    const nextState = !item.completed;
    try {
        const updatedItem = await apiFetch(`/api/items/${itemId}/progress`, {
            method: 'PATCH',
            body: JSON.stringify({
                completed: nextState,
                completedVia: 'MANUAL'
            })
        });

        // Update item in state in-place
        item.completed = updatedItem.completed;
        item.completedVia = updatedItem.completedVia;
        item.completedAt = updatedItem.completedAt;

        // Animate row in-place
        updateItemRowInPlace(itemId, updatedItem);
        renderProgressAndMetrics();
    } catch (e) {
        console.error("Failed to toggle task progress", e);
    }
}

async function markItemVideoWatched(itemId) {
    stopVideoProgressPolling(itemId);

    const item = state.currentSchedule.items.find(it => it.id === itemId);
    if (!item || item.completed) return; // avoid double call

    try {
        const updatedItem = await apiFetch(`/api/items/${itemId}/progress`, {
            method: 'PATCH',
            body: JSON.stringify({
                completed: true,
                completedVia: 'VIDEO_WATCHED'
            })
        });

        item.completed = updatedItem.completed;
        item.completedVia = updatedItem.completedVia;
        item.completedAt = updatedItem.completedAt;

        // Hide progress UI
        const progressContainer = document.getElementById(`progress-container-${itemId}`);
        if (progressContainer) progressContainer.style.display = 'none';

        updateItemRowInPlace(itemId, updatedItem);
        renderProgressAndMetrics();
    } catch (e) {
        console.error("Failed to mark video watched", e);
    }
}

// Incremental UI dom patching to avoid heavy page flash and cursor resets
function updateItemRowInPlace(itemId, updatedItem) {
    const row = document.getElementById(`item-row-${itemId}`);
    if (!row) return;

    const box = row.querySelector('.item-checkbox');
    if (updatedItem.completed) {
        row.classList.add('completed');
        if (box) box.classList.add('checked');
    } else {
        row.classList.remove('completed');
        if (box) box.classList.remove('checked');
    }

    // Refresh tags list badges
    const titleCol = row.querySelector('.item-title-col');
    if (titleCol) {
        // Remove existing auto badge
        const oldBadge = titleCol.querySelector('.item-auto-badge');
        if (oldBadge) oldBadge.remove();

        if (updatedItem.completedVia) {
            const badge = document.createElement('span');
            badge.className = 'item-auto-badge';
            badge.textContent = updatedItem.completedVia === 'LEETCODE_SYNC' ? 'LeetCode Sync' : 'Watched';
            titleCol.appendChild(badge);
        }
    }

    // Check if entire day is complete now to update sidebar checkmark
    const currentDayItems = state.currentSchedule.items.filter(it => it.dayNumber === state.currentDay);
    const dayComplete = currentDayItems.every(it => it.completed);
    
    // Find active day button in nav list
    const activeNavBtn = dayNav.querySelector('.day-nav-item.active');
    if (activeNavBtn) {
        if (dayComplete) {
            activeNavBtn.classList.add('complete');
        } else {
            activeNavBtn.classList.remove('complete');
        }
    }
}

// 10. Notes Debounce Auto-Saving
let notesTimeout = null;
notesTextarea.addEventListener('input', () => {
    if (notesTimeout) clearTimeout(notesTimeout);

    notesTimeout = setTimeout(() => {
        const noteKey = `${state.currentSchedule.id}-${state.currentDay}`;
        const val = notesTextarea.value;
        state.notes[noteKey] = val;
        localStorage.setItem('pos_notes_v1', JSON.stringify(state.notes));
    }, 400);
});

// 11. LeetCode progress synchronizer
btnSyncLeetcode.addEventListener('click', async () => {
    if (!state.user || !state.user.leetcodeUsername) {
        showSettingsModal("Please configure your LeetCode Username first to sync progress.");
        return;
    }

    // Visual button loading indicator
    btnSyncLeetcode.textContent = "⏳";
    btnSyncLeetcode.disabled = true;

    try {
        const syncRes = await apiFetch('/api/users/me/leetcode-sync', { method: 'POST' });
        
        // Notify count
        alert(`LeetCode Sync Successful!\n${syncRes.itemsAutoCompleted} items marked complete.`);

        // Refresh current schedule from server
        const updatedSchedule = await apiFetch(`/api/schedules/${state.currentSchedule.id}`);
        state.currentSchedule = updatedSchedule;

        // Replace schedule in state array
        const idx = state.schedules.findIndex(s => s.id === updatedSchedule.id);
        if (idx !== -1) {
            state.schedules[idx] = updatedSchedule;
        }

        // Re-render tracker
        renderTracker();
    } catch (err) {
        alert("Failed to sync LeetCode progress: " + err.message);
    } finally {
        btnSyncLeetcode.textContent = "🔄";
        btnSyncLeetcode.disabled = false;
    }
});

// 12. Settings Modal Logic
function showSettingsModal(errorMsg = '') {
    settingsError.style.display = 'none';
    if (errorMsg) {
        settingsError.textContent = errorMsg;
        settingsError.style.display = 'block';
    }

    settingsLeetcodeUsername.value = state.user && state.user.leetcodeUsername ? state.user.leetcodeUsername : '';
    
    if (state.user && state.user.lastLeetcodeSyncAt) {
        const d = new Date(state.user.lastLeetcodeSyncAt);
        settingsLastSyncText.textContent = `Last sync: ${d.toLocaleString()}`;
    } else {
        settingsLastSyncText.textContent = "Never synced.";
    }

    settingsModal.style.display = 'flex';
}

btnOpenSettings.addEventListener('click', () => showSettingsModal());
settingsModalCloseBtn.addEventListener('click', () => { settingsModal.style.display = 'none'; });

settingsSaveBtn.addEventListener('click', async () => {
    settingsError.style.display = 'none';
    const leetcodeUsername = settingsLeetcodeUsername.value.trim();

    try {
        const res = await apiFetch('/api/users/me', {
            method: 'PATCH',
            body: JSON.stringify({ leetcodeUsername })
        });

        if (state.user) {
            state.user.leetcodeUsername = res.leetcodeUsername;
        }
        settingsModal.style.display = 'none';
    } catch (err) {
        settingsError.textContent = err.message;
        settingsError.style.display = 'block';
    }
});

// 13. Notifications Toggle (MDN Notification API)
btnToggleNotifications.addEventListener('click', async () => {
    if (!("Notification" in window)) {
        alert("This browser does not support desktop notifications.");
        return;
    }

    if (Notification.permission === "granted") {
        alert("Notifications already permitted!");
        return;
    }

    const permission = await Notification.requestPermission();
    if (permission === "granted") {
        alert("Notifications enabled successfully!");
    } else {
        alert("Notification permission denied.");
    }
});

// 14. Navigation Header Controls
btnPrevDay.addEventListener('click', () => {
    if (state.currentDay > 1) {
        state.currentDay--;
        renderTracker();
    }
});

btnNextDay.addEventListener('click', () => {
    const totalDays = state.currentSchedule.items.reduce((max, item) => Math.max(max, item.dayNumber), 1);
    if (state.currentDay < totalDays) {
        state.currentDay++;
        renderTracker();
    }
});

btnNewSchedule.addEventListener('click', () => {
    if (confirm("Create a new schedule? This will keep your current logs but create a new preparation runway timeline.")) {
        showScreen('wizard');
    }
});

btnLogOut.addEventListener('click', () => {
    if (confirm("Are you sure you want to log out?")) {
        logout();
    }
});

// Mobile Hamburger Menu drawer toggles
menuToggleBtn.addEventListener('click', () => {
    sidebar.classList.add('open');
});

scrim.addEventListener('click', () => {
    sidebar.classList.remove('open');
});

// 15. Light/Dark Theme Switcher
function setupTheme() {
    const activeTheme = localStorage.getItem('pos_theme') || 'light';
    document.documentElement.setAttribute('data-theme', activeTheme);
    updateThemeSwitchUI(activeTheme);
}

dayNightSwitch.addEventListener('click', () => {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const targetTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    document.documentElement.setAttribute('data-theme', targetTheme);
    localStorage.setItem('pos_theme', targetTheme);
    updateThemeSwitchUI(targetTheme);
});

function updateThemeSwitchUI(theme) {
    if (theme === 'dark') {
        themeToggleIcon.textContent = "🌙";
        themeToggleText.textContent = "Dark Mode";
    } else {
        themeToggleIcon.textContent = "☀️";
        themeToggleText.textContent = "Light Mode";
    }
}

// Event bindings & start point
function setupEventListeners() {
    // Avoid double creation bug mentioned in Wizard double-submission
    // Done by disabling submit action / hiding and showing loading immediately.
}

window.addEventListener('load', initApp);
