/**
 * Nitpick VenueHub - Interactive Client Application
 * Clean, modern single-page dashboard with Dark/Light mode and concurrency-safe booking
 */

const API = {
    users: '/api/users',
    venues: '/api/venues',
    events: '/api/events',
    registrations: '/api/registrations'
};

// Application State
const state = {
    currentUser: null,
    users: [],
    venues: [],
    events: [],
    registrations: [],
    activeTab: 'events',
    selectedSlot: null,
    bookingEvent: null,
    theme: localStorage.getItem('venue_hub_theme') || 'dark'
};

// Initializer
document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    setupNavigation();
    setupModals();
    setupForms();
    setupEventListeners();
    refreshAllData();
});

/* ==========================================================================
   Theme Management
   ========================================================================== */
function initTheme() {
    document.documentElement.setAttribute('data-theme', state.theme);
    updateThemeIcon();
}

function toggleTheme() {
    state.theme = state.theme === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', state.theme);
    localStorage.setItem('venue_hub_theme', state.theme);
    updateThemeIcon();
}

function updateThemeIcon() {
    const icon = document.getElementById('theme-icon');
    if (icon) {
        icon.innerHTML = state.theme === 'dark' 
            ? `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>` 
            : `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`;
    }
}

/* ==========================================================================
   Navigation Tabs
   ========================================================================== */
function setupNavigation() {
    const tabs = document.querySelectorAll('.nav-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            state.activeTab = tab.dataset.tab;
            
            document.querySelectorAll('.tab-content').forEach(section => {
                section.style.display = section.id === `tab-${state.activeTab}` ? 'block' : 'none';
            });

            // Specific refresh on tab switch
            if (state.activeTab === 'registrations') {
                loadRegistrations();
            }
        });
    });
}

/* ==========================================================================
   Data Fetching & State
   ========================================================================== */
async function apiCall(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: { 'Content-Type': 'application/json', ...options.headers },
            ...options
        });
        const data = await response.json();
        if (!response.ok) {
            const message = data.message || (data.validationErrors ? Object.values(data.validationErrors).join(', ') : 'Request failed');
            throw new Error(message);
        }
        return data;
    } catch (err) {
        showToast(err.message, 'error');
        throw err;
    }
}

async function refreshAllData() {
    await Promise.allSettled([
        loadUsers(),
        loadVenues(),
        loadEvents(),
        loadRegistrations()
    ]);
    updateStats();
}

async function loadUsers() {
    try {
        const res = await apiCall(API.users);
        state.users = res.data || [];
        populateUserSelector();
        renderUsersTable();
    } catch (e) {
        console.error('Error loading users', e);
    }
}

async function loadVenues() {
    try {
        const res = await apiCall(API.venues);
        state.venues = res.data || [];
        populateVenueFilters();
        renderVenuesList();
    } catch (e) {
        console.error('Error loading venues', e);
    }
}

async function loadEvents() {
    try {
        const res = await apiCall(API.events);
        state.events = res.data || [];
        renderEventsList();
    } catch (e) {
        console.error('Error loading events', e);
    }
}

async function loadRegistrations() {
    try {
        if (state.currentUser) {
            const res = await apiCall(`${API.registrations}/user/${state.currentUser.id}`);
            state.registrations = res.data || [];
        } else {
            state.registrations = [];
        }
        renderRegistrationsList();
    } catch (e) {
        state.registrations = [];
        renderRegistrationsList();
    }
}

function updateStats() {
    const activeVenues = state.venues.filter(v => v.isActive).length;
    const activeEvents = state.events.filter(e => e.status === 'ACTIVE').length;
    const totalSlots = state.events.reduce((sum, e) => sum + (e.totalSlots || 0), 0);
    const bookedSlots = state.events.reduce((sum, e) => sum + (e.bookedSlots || 0), 0);
    const availableSlots = Math.max(0, totalSlots - bookedSlots);

    document.getElementById('stat-venues').textContent = activeVenues;
    document.getElementById('stat-events').textContent = activeEvents;
    document.getElementById('stat-available').textContent = availableSlots;
    document.getElementById('stat-booked').textContent = bookedSlots;
}

/* ==========================================================================
   User Selector
   ========================================================================== */
function populateUserSelector() {
    const selector = document.getElementById('user-selector');
    selector.innerHTML = '';

    if (state.users.length === 0) {
        selector.innerHTML = '<option value="">No users registered</option>';
        state.currentUser = null;
        return;
    }

    state.users.forEach(u => {
        const opt = document.createElement('option');
        opt.value = u.id;
        opt.textContent = `${u.firstName} ${u.lastName} (${u.role})`;
        selector.appendChild(opt);
    });

    // Select first or retain existing
    if (!state.currentUser || !state.users.some(u => u.id === state.currentUser.id)) {
        state.currentUser = state.users[0];
    }
    selector.value = state.currentUser ? state.currentUser.id : '';
}

function handleUserChange(e) {
    const userId = Number(e.target.value);
    state.currentUser = state.users.find(u => u.id === userId) || null;
    loadRegistrations();
    renderEventsList();
}

/* ==========================================================================
   Rendering Views
   ========================================================================== */

function renderEventsList() {
    const container = document.getElementById('events-grid');
    const searchFilter = document.getElementById('search-events').value.toLowerCase().trim();
    const venueFilter = document.getElementById('filter-venue').value;

    let filtered = state.events;

    if (searchFilter) {
        filtered = filtered.filter(e => 
            e.name.toLowerCase().includes(searchFilter) || 
            (e.description && e.description.toLowerCase().includes(searchFilter))
        );
    }

    if (venueFilter) {
        filtered = filtered.filter(e => String(e.venueId) === String(venueFilter));
    }

    if (filtered.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-icon">📅</div>
                <div class="empty-title">No events found</div>
                <p class="empty-desc">Create your first event using the "Schedule Event" tab.</p>
                <button class="btn btn-primary" onclick="switchToTab('schedule')">Schedule Event</button>
            </div>
        `;
        return;
    }

    container.innerHTML = filtered.map(event => {
        const total = event.totalSlots || 1;
        const booked = event.bookedSlots || 0;
        const available = Math.max(0, total - booked);
        const percent = Math.min(100, Math.round((booked / total) * 100));
        
        let progressClass = '';
        if (percent >= 100) progressClass = 'full';
        else if (percent >= 80) progressClass = 'warning';

        const isCancelled = event.status === 'CANCELLED';
        const isFull = available === 0;

        return `
            <div class="card">
                <div>
                    <div class="card-top">
                        <h3 class="card-title">${escapeHtml(event.name)}</h3>
                        <span class="badge ${isCancelled ? 'badge-cancelled' : 'badge-active'}">${event.status}</span>
                    </div>
                    <p class="card-description">${escapeHtml(event.description || 'No description provided')}</p>
                    
                    <div class="card-meta-list">
                        <div class="card-meta-item">
                            <span>🏛️</span>
                            <span>Venue: <strong>${escapeHtml(event.venueName)}</strong></span>
                        </div>
                        <div class="card-meta-item">
                            <span>📅</span>
                            <span>Date: <strong>${event.eventDate}</strong></span>
                        </div>
                        <div class="card-meta-item">
                            <span>⏰</span>
                            <span>Time: <strong>${event.startTime} - ${event.endTime}</strong></span>
                        </div>
                    </div>

                    <div class="capacity-box">
                        <div class="capacity-labels">
                            <span><strong>${available}</strong> slots left</span>
                            <span>${booked} / ${total} booked (${percent}%)</span>
                        </div>
                        <div class="progress-track">
                            <div class="progress-fill ${progressClass}" style="width: ${percent}%"></div>
                        </div>
                    </div>
                </div>

                <div class="card-actions">
                    <button class="btn btn-primary btn-sm" style="flex: 1;" 
                        onclick="openBookingModal(${event.id})" 
                        ${isCancelled || isFull ? 'disabled' : ''}>
                        ${isCancelled ? 'Cancelled' : (isFull ? 'Event Full' : 'Book a Slot')}
                    </button>
                    ${!isCancelled ? `
                        <button class="btn btn-secondary btn-sm" onclick="cancelEvent(${event.id})" title="Cancel Event">
                            Cancel
                        </button>
                    ` : ''}
                </div>
            </div>
        `;
    }).join('');
}

function renderVenuesList() {
    const container = document.getElementById('venues-grid');
    if (state.venues.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-icon">🏛️</div>
                <div class="empty-title">No venues registered</div>
                <p class="empty-desc">Add college auditoriums, labs, or conference rooms to start scheduling.</p>
                <button class="btn btn-primary" onclick="openModal('modal-add-venue')">Add Venue</button>
            </div>
        `;
        return;
    }

    container.innerHTML = state.venues.map(v => `
        <div class="card">
            <div>
                <div class="card-top">
                    <h3 class="card-title">${escapeHtml(v.name)}</h3>
                    <span class="badge ${v.isActive ? 'badge-active' : 'badge-cancelled'}">
                        ${v.isActive ? 'Active' : 'Inactive'}
                    </span>
                </div>
                <p class="card-description">${escapeHtml(v.description || 'No description available')}</p>
                
                <div class="card-meta-list">
                    <div class="card-meta-item">
                        <span>👥</span>
                        <span>Capacity: <strong>${v.capacity} seats</strong></span>
                    </div>
                    <div class="card-meta-item">
                        <span>📍</span>
                        <span>Location: <strong>${escapeHtml(v.location || 'Campus')}</strong></span>
                    </div>
                </div>
            </div>

            <div class="card-actions">
                <button class="btn btn-secondary btn-sm" style="width: 100%;" 
                    onclick="toggleVenueStatus(${v.id}, ${!v.isActive})">
                    ${v.isActive ? 'Deactivate Venue' : 'Activate Venue'}
                </button>
            </div>
        </div>
    `).join('');
}

function renderRegistrationsList() {
    const container = document.getElementById('registrations-container');
    if (!state.currentUser) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">👤</div>
                <div class="empty-title">No user selected</div>
                <p class="empty-desc">Select or create a user in the top navigation bar to view your registrations.</p>
            </div>
        `;
        return;
    }

    if (state.registrations.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🎟️</div>
                <div class="empty-title">No registrations found</div>
                <p class="empty-desc">You have not registered for any events yet. Check out the Events tab to book a slot.</p>
                <button class="btn btn-primary" onclick="switchToTab('events')">Explore Events</button>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div class="table-container">
            <table class="modern-table">
                <thead>
                    <tr>
                        <th>Event</th>
                        <th>Slot Number</th>
                        <th>Status</th>
                        <th>Registered At</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    ${state.registrations.map(r => `
                        <tr>
                            <td><strong>${escapeHtml(r.eventName)}</strong></td>
                            <td><span class="badge badge-role">Slot #${r.slotNumber || 'Auto'}</span></td>
                            <td>
                                <span class="badge ${r.status === 'CONFIRMED' ? 'badge-active' : 'badge-cancelled'}">
                                    ${r.status}
                                </span>
                            </td>
                            <td>${new Date(r.registeredAt).toLocaleString()}</td>
                            <td>
                                ${r.status === 'CONFIRMED' ? `
                                    <button class="btn btn-danger btn-sm" onclick="cancelRegistration(${r.id})">
                                        Cancel Booking
                                    </button>
                                ` : '<span style="color: var(--text-muted);">None</span>'}
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    `;
}

function renderUsersTable() {
    const tbody = document.getElementById('users-tbody');
    if (!tbody) return;

    if (state.users.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No users found</td></tr>`;
        return;
    }

    tbody.innerHTML = state.users.map(u => `
        <tr>
            <td><strong>${escapeHtml(u.firstName)} ${escapeHtml(u.lastName)}</strong></td>
            <td>${escapeHtml(u.email)}</td>
            <td>${escapeHtml(u.studentId || '-')}</td>
            <td><span class="badge badge-role">${u.role}</span></td>
            <td><span class="badge ${u.isActive ? 'badge-active' : 'badge-cancelled'}">${u.isActive ? 'Active' : 'Inactive'}</span></td>
        </tr>
    `).join('');
}

function populateVenueFilters() {
    const eventFilter = document.getElementById('filter-venue');
    const scheduleVenue = document.getElementById('schedule-venue');

    const options = state.venues
        .filter(v => v.isActive)
        .map(v => `<option value="${v.id}">${escapeHtml(v.name)} (Capacity: ${v.capacity})</option>`)
        .join('');

    if (eventFilter) {
        eventFilter.innerHTML = '<option value="">All Venues</option>' + options;
    }
    if (scheduleVenue) {
        scheduleVenue.innerHTML = '<option value="">Select a venue...</option>' + options;
    }
}

/* ==========================================================================
   Booking & Slot Selector Modal
   ========================================================================== */
async function openBookingModal(eventId) {
    if (!state.currentUser) {
        showToast('Please select or create a user before booking', 'error');
        openModal('modal-add-user');
        return;
    }

    const event = state.events.find(e => e.id === eventId);
    if (!event) return;

    state.bookingEvent = event;
    state.selectedSlot = null;

    document.getElementById('booking-event-title').textContent = event.name;
    document.getElementById('booking-event-meta').textContent = `${event.venueName} • ${event.eventDate} (${event.startTime} - ${event.endTime})`;
    document.getElementById('booking-user-name').textContent = `${state.currentUser.firstName} ${state.currentUser.lastName} (${state.currentUser.email})`;

    // Fetch existing registrations for this event to identify reserved slots
    const slotGrid = document.getElementById('slot-grid');
    slotGrid.innerHTML = '<div style="grid-column: 1 / -1; text-align: center; color: var(--text-muted);">Loading slots...</div>';
    openModal('modal-booking');

    try {
        const res = await apiCall(`${API.registrations}/event/${eventId}`);
        const activeBookings = (res.data || []).filter(r => r.status !== 'CANCELLED');
        const bookedSlotNumbers = new Set(activeBookings.map(r => r.slotNumber).filter(Boolean));

        slotGrid.innerHTML = '';
        for (let i = 1; i <= event.totalSlots; i++) {
            const isBooked = bookedSlotNumbers.has(i);
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = `slot-btn ${isBooked ? 'booked' : ''}`;
            btn.disabled = isBooked;
            btn.innerHTML = `<span>#${i}</span><span style="font-size: 0.65rem; opacity: 0.8;">${isBooked ? 'Taken' : 'Free'}</span>`;
            
            if (!isBooked) {
                btn.addEventListener('click', () => {
                    document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('selected'));
                    btn.classList.add('selected');
                    state.selectedSlot = i;
                    document.getElementById('selected-slot-label').textContent = `Slot #${i} selected`;
                });
            }
            slotGrid.appendChild(btn);
        }
        document.getElementById('selected-slot-label').textContent = 'Click a slot above or leave unselected for auto-allocation';
    } catch (err) {
        slotGrid.innerHTML = '<div style="grid-column: 1 / -1; color: var(--danger);">Failed to load slots</div>';
    }
}

async function confirmBooking() {
    if (!state.bookingEvent || !state.currentUser) return;

    const payload = {
        eventId: state.bookingEvent.id,
        userId: state.currentUser.id,
        slotNumber: state.selectedSlot || null
    };

    try {
        const res = await apiCall(API.registrations, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        closeModal('modal-booking');
        showToast(`Booked slot #${res.data.slotNumber} successfully!`, 'success');
        await refreshAllData();
    } catch (err) {
        // Error toast handled by apiCall
    }
}

/* ==========================================================================
   Action Handlers
   ========================================================================== */
async function cancelRegistration(registrationId) {
    if (!confirm('Are you sure you want to cancel this booking? The slot will immediately become available for others.')) {
        return;
    }

    try {
        await apiCall(`${API.registrations}/${registrationId}/cancel`, { method: 'POST' });
        showToast('Registration cancelled and slot released', 'success');
        await refreshAllData();
    } catch (err) {
        // Handled in apiCall
    }
}

async function cancelEvent(eventId) {
    if (!confirm('Are you sure you want to cancel this entire event?')) return;

    try {
        await apiCall(`${API.events}/${eventId}/cancel`, { method: 'POST' });
        showToast('Event has been cancelled', 'success');
        await refreshAllData();
    } catch (err) {
        // Handled in apiCall
    }
}

async function toggleVenueStatus(venueId, newStatus) {
    const venue = state.venues.find(v => v.id === venueId);
    if (!venue) return;

    try {
        await apiCall(`${API.venues}/${venueId}`, {
            method: 'PUT',
            body: JSON.stringify({
                name: venue.name,
                capacity: venue.capacity,
                location: venue.location,
                description: venue.description,
                isActive: newStatus
            })
        });
        showToast(`Venue ${newStatus ? 'activated' : 'deactivated'}`, 'success');
        await loadVenues();
    } catch (err) {
        // Handled in apiCall
    }
}

/* ==========================================================================
   Modals & Forms
   ========================================================================== */
function setupModals() {
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.classList.remove('active');
            }
        });
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('active');
}

function setupForms() {
    // Schedule Event Form
    const scheduleForm = document.getElementById('form-schedule-event');
    if (scheduleForm) {
        scheduleForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const venueId = Number(document.getElementById('schedule-venue').value);
            const name = document.getElementById('schedule-name').value;
            const description = document.getElementById('schedule-desc').value;
            const eventDate = document.getElementById('schedule-date').value;
            const startTime = document.getElementById('schedule-start').value;
            const endTime = document.getElementById('schedule-end').value;
            const totalSlots = Number(document.getElementById('schedule-slots').value);

            try {
                await apiCall(API.events, {
                    method: 'POST',
                    body: JSON.stringify({
                        venueId,
                        name,
                        description,
                        eventDate,
                        startTime,
                        endTime,
                        totalSlots,
                        status: 'ACTIVE'
                    })
                });

                showToast('Event scheduled successfully!', 'success');
                scheduleForm.reset();
                switchToTab('events');
                await refreshAllData();
            } catch (err) {
                // Handled in apiCall
            }
        });
    }

    // Add Venue Form
    const venueForm = document.getElementById('form-add-venue');
    if (venueForm) {
        venueForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('venue-name').value;
            const capacity = Number(document.getElementById('venue-capacity').value);
            const location = document.getElementById('venue-location').value;
            const description = document.getElementById('venue-desc').value;

            try {
                await apiCall(API.venues, {
                    method: 'POST',
                    body: JSON.stringify({ name, capacity, location, description, isActive: true })
                });

                showToast('Venue created successfully!', 'success');
                closeModal('modal-add-venue');
                venueForm.reset();
                await loadVenues();
                updateStats();
            } catch (err) {
                // Handled in apiCall
            }
        });
    }

    // Add User Form
    const userForm = document.getElementById('form-add-user');
    if (userForm) {
        userForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const firstName = document.getElementById('user-firstname').value;
            const lastName = document.getElementById('user-lastname').value;
            const email = document.getElementById('user-email').value;
            const studentId = document.getElementById('user-studentid').value;
            const role = document.getElementById('user-role').value;

            try {
                const res = await apiCall(API.users, {
                    method: 'POST',
                    body: JSON.stringify({ firstName, lastName, email, studentId, role })
                });

                showToast('User created successfully!', 'success');
                closeModal('modal-add-user');
                userForm.reset();
                await loadUsers();
                state.currentUser = res.data;
                document.getElementById('user-selector').value = res.data.id;
            } catch (err) {
                // Handled in apiCall
            }
        });
    }
}

function setupEventListeners() {
    document.getElementById('user-selector')?.addEventListener('change', handleUserChange);
    document.getElementById('theme-toggle')?.addEventListener('click', toggleTheme);
    document.getElementById('search-events')?.addEventListener('input', renderEventsList);
    document.getElementById('filter-venue')?.addEventListener('change', renderEventsList);
    document.getElementById('btn-confirm-booking')?.addEventListener('click', confirmBooking);

    // Auto set default date for scheduling to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateInput = document.getElementById('schedule-date');
    if (dateInput) {
        dateInput.value = tomorrow.toISOString().split('T')[0];
    }
}

function switchToTab(tabName) {
    const tabBtn = document.querySelector(`.nav-tab[data-tab="${tabName}"]`);
    if (tabBtn) tabBtn.click();
}

/* ==========================================================================
   Toast Notifications
   ========================================================================== */
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, m => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[m]));
}
