const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
const money = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
});

async function requestJson(url, options = {}) {
    const headers = new Headers(options.headers || {});
    if (options.body) headers.set("Content-Type", "application/json");
    if (csrfToken && csrfHeader && options.method && options.method !== "GET") {
        headers.set(csrfHeader, csrfToken);
    }
    const response = await fetch(url, { ...options, headers, credentials: "same-origin" });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.detail || data.message || "We could not complete that request. Please try again.");
    }
    return data;
}

function setupNavigation() {
    const toggle = document.querySelector("[data-menu-toggle]");
    const navigation = document.querySelector(".aw-top-nav");
    if (!toggle || !navigation) return;

    const closeMenu = () => {
        navigation.classList.remove("is-open");
        toggle.setAttribute("aria-expanded", "false");
    };

    toggle.addEventListener("click", () => {
        const isOpen = navigation.classList.toggle("is-open");
        toggle.setAttribute("aria-expanded", String(isOpen));
    });
    navigation.querySelectorAll("a").forEach((link) => link.addEventListener("click", closeMenu));
}

function setupRideFilters() {
    const filters = [...document.querySelectorAll("[data-ride-filter]")];
    const cards = [...document.querySelectorAll(".aw-ride-grid [data-category]")];
    if (!filters.length || !cards.length) return;

    const search = document.querySelector("#global-search");
    const empty = document.querySelector("#no-results");
    let activeFilter = "ALL";

    const updateCards = () => {
        const query = search?.value.trim().toLowerCase() || "";
        let visibleCount = 0;
        cards.forEach((card) => {
            const categories = (card.dataset.category || "").split(/\s+/);
            const matchesCategory = activeFilter === "ALL" || categories.includes(activeFilter);
            const matchesSearch = !query || card.textContent.toLowerCase().includes(query) ||
                (card.dataset.search || "").toLowerCase().includes(query);
            const visible = matchesCategory && matchesSearch;
            card.classList.toggle("hidden", !visible);
            if (visible) visibleCount += 1;
        });
        if (empty) empty.classList.toggle("hidden", visibleCount > 0);
    };

    filters.forEach((button) => {
        button.addEventListener("click", () => {
            activeFilter = button.dataset.rideFilter || "ALL";
            filters.forEach((filter) => {
                const selected = filter === button;
                filter.classList.toggle("is-active", selected);
                filter.setAttribute("aria-pressed", String(selected));
            });
            updateCards();
        });
    });
    search?.addEventListener("input", updateCards);
    updateCards();
}

function formatCategory(category) {
    return String(category || "PARK").replaceAll("_", " ");
}

async function loadExperienceCards() {
    const grid = document.querySelector("#experience-grid");
    if (!grid) return;
    const loading = document.querySelector("#experience-loading");
    const error = document.querySelector("#experience-error");

    try {
        const experiences = await requestJson("/api/experiences");
        if (loading) loading.remove();
        experiences.slice(0, 6).forEach((experience) => {
            const card = document.createElement("article");
            card.className = "aw-experience-card";

            const imageFrame = document.createElement("div");
            imageFrame.className = "aw-experience-card__image";
            const image = document.createElement("img");
            image.src = experience.imageUrl;
            image.alt = experience.name;
            image.loading = "lazy";
            imageFrame.append(image);

            const category = document.createElement("span");
            category.className = "aw-experience-card__category";
            category.textContent = formatCategory(experience.category);
            imageFrame.append(category);

            const body = document.createElement("div");
            body.className = "aw-experience-card__body";
            const title = document.createElement("h3");
            title.textContent = experience.name;
            const description = document.createElement("p");
            description.textContent = experience.description;
            const meta = document.createElement("div");
            meta.className = "aw-experience-card__meta";
            const height = document.createElement("span");
            height.textContent = experience.heightGuide;
            const duration = document.createElement("span");
            duration.textContent = experience.duration;
            meta.append(height, duration);
            body.append(title, description, meta);
            card.append(imageFrame, body);
            grid.append(card);
        });
    } catch {
        if (loading) loading.remove();
        if (error) error.hidden = false;
    }
}

function setupTicketPlanner() {
    const form = document.querySelector("#booking-form");
    if (!form) return;

    const dateInput = form.querySelector("#visit-date");
    const passSelect = form.querySelector("#pass-type");
    const adultInput = form.querySelector("#adult-count-input");
    const childInput = form.querySelector("#child-count-input");
    const fastPassInput = form.querySelector("#fast-passes");
    const errorBox = document.querySelector("#booking-error");
    const submitButton = document.querySelector("#booking-submit");
    const submitLabel = submitButton?.querySelector("span");
    const defaultSubmitLabel = submitLabel?.textContent || "Reserve park day";
    const result = document.querySelector("#booking-result");
    let quoteTimeout;

    const localDate = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    };

    if (dateInput) {
        dateInput.min = localDate(new Date());
        const suggested = new Date();
        suggested.setDate(suggested.getDate() + 7);
        dateInput.value = localDate(suggested);
    }

    const payload = () => ({
        fullName: form.querySelector("#guest-name").value.trim() || "Park guest",
        email: form.querySelector("#guest-email").value.trim() || "guest@example.com",
        phone: form.querySelector("#guest-phone").value.trim(),
        visitDate: dateInput.value,
        adults: Number(adultInput.value),
        children: Number(childInput.value),
        passType: passSelect.value,
        fastPasses: Number(fastPassInput.value || 0),
        termsAccepted: form.querySelector("#terms-accepted").checked
    });

    async function refreshQuote() {
        if (!dateInput.value || !passSelect.value) return;
        const data = payload();
        if (data.adults < 1 || data.children < 0 || data.adults + data.children > 12 ||
                data.fastPasses > data.adults + data.children) return;
        try {
            const quote = await requestJson("/api/booking-quotes", {
                method: "POST",
                body: JSON.stringify(data)
            });
            const show = (selector, value) => {
                const node = document.querySelector(selector);
                if (node) node.textContent = value;
            };
            show("#quote-pass-name", quote.passLabel);
            show("#quote-pass-description", passSelect.selectedOptions[0]?.dataset.description || "Park access for your selected date");
            show("#quote-subtotal", money.format(quote.subtotal));
            show("#quote-discount", quote.discount > 0 ? `-${money.format(quote.discount)}` : money.format(0));
            show("#quote-total", money.format(quote.total));
            show("#quote-offer-label", quote.offerLabel);
        } catch {
            const total = document.querySelector("#quote-total");
            if (total) total.textContent = "Check at park";
        }
    }

    async function loadPassOptions() {
        try {
            const options = await requestJson("/api/ticket-options");
            const selected = passSelect.value;
            passSelect.replaceChildren();
            options.forEach((option) => {
                const element = document.createElement("option");
                element.value = option.id;
                element.textContent = `${option.label} - ${money.format(option.adultPrice)} adult`;
                element.dataset.description = option.description;
                element.selected = option.id === selected;
                passSelect.append(element);
            });
            refreshQuote();
        } catch {
            refreshQuote();
        }
    }

    form.addEventListener("input", () => {
        window.clearTimeout(quoteTimeout);
        quoteTimeout = window.setTimeout(refreshQuote, 180);
    });
    form.addEventListener("change", refreshQuote);

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!form.reportValidity()) return;
        if (errorBox) errorBox.hidden = true;
        if (submitButton) submitButton.disabled = true;
        if (submitLabel) submitLabel.textContent = "Saving your park day...";

        try {
            const booking = await requestJson("/api/bookings", {
                method: "POST",
                body: JSON.stringify(payload())
            });
            const name = document.querySelector("#result-name");
            const reference = document.querySelector("#result-reference");
            if (name) name.textContent = booking.fullName;
            if (reference) reference.textContent = booking.reference;
            if (result) {
                result.hidden = false;
                result.scrollIntoView({ behavior: "smooth", block: "center" });
            }
        } catch (error) {
            if (errorBox) {
                errorBox.textContent = error.message;
                errorBox.hidden = false;
                errorBox.scrollIntoView({ behavior: "smooth", block: "center" });
            }
        } finally {
            if (submitButton) submitButton.disabled = false;
            if (submitLabel) submitLabel.textContent = defaultSubmitLabel;
        }
    });

    document.querySelector("#copy-reference")?.addEventListener("click", async () => {
        const reference = document.querySelector("#result-reference")?.textContent;
        if (!reference) return;
        try {
            await navigator.clipboard.writeText(reference);
            document.querySelector("#copy-reference").textContent = "Copied";
        } catch {
            document.querySelector("#copy-reference").textContent = reference;
        }
    });
    document.querySelector("#close-booking-result")?.addEventListener("click", () => {
        if (result) result.hidden = true;
    });

    loadPassOptions();
}

function setupStayPlanner() {
    const form = document.querySelector("#stay-form");
    if (!form) return;

    const checkInInput = form.querySelector("#stay-date");
    const nightsInput = form.querySelector("#stay-nights");
    const guestsInput = form.querySelector("#stay-guests");
    const roomSelect = form.querySelector("#room-type");
    const error = document.querySelector("#stay-error");
    const submitButton = document.querySelector("#stay-submit");
    const submitText = submitButton?.querySelector("span");
    const defaultSubmitText = submitText?.textContent || "Reserve this stay";
    const result = document.querySelector("#stay-result");
    let rooms = [];
    let quoteTimer;

    const localDate = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    };
    checkInInput.min = localDate(new Date());
    const suggestedDate = new Date();
    suggestedDate.setDate(suggestedDate.getDate() + 14);
    checkInInput.value = localDate(suggestedDate);

    const payload = () => ({
        fullName: form.querySelector("#stay-guest-name").value.trim() || "Resort guest",
        email: form.querySelector("#stay-guest-email").value.trim() || "guest@example.com",
        phone: form.querySelector("#stay-guest-phone").value.trim(),
        checkIn: checkInInput.value,
        nights: Number(nightsInput.value),
        guests: Number(guestsInput.value),
        roomType: roomSelect.value,
        termsAccepted: form.querySelector("#stay-terms-accepted").checked
    });

    function setStayError(message) {
        error.textContent = message;
        error.hidden = false;
    }

    async function updateStayQuote() {
        if (!checkInInput.value || !roomSelect.value) return;
        const data = payload();
        if (data.nights < 1 || data.nights > 14 || data.guests < 1) return;
        try {
            const total = await requestJson("/api/stay-reservations/quote", {
                method: "POST",
                body: JSON.stringify(data)
            });
            const room = rooms.find((option) => option.id === data.roomType);
            document.querySelector("#stay-quote-room").textContent = room?.label || "Resort stay";
            document.querySelector("#stay-quote-rate").textContent = room
                ? `${money.format(room.nightlyRate)} per night`
                : "Per night";
            document.querySelector("#stay-quote-nights").textContent = data.nights;
            document.querySelector("#stay-quote-total").textContent = money.format(total);
        } catch (quoteError) {
            document.querySelector("#stay-quote-total").textContent = "Check availability";
        }
    }

    async function loadRooms() {
        try {
            rooms = await requestJson("/api/stay-reservations/options");
            const selectedRoom = roomSelect.value;
            roomSelect.replaceChildren();
            rooms.forEach((room) => {
                const option = document.createElement("option");
                option.value = room.id;
                option.textContent = `${room.label} - ${money.format(room.nightlyRate)} / night`;
                option.selected = room.id === selectedRoom;
                roomSelect.append(option);
                const card = document.querySelector(`[data-room-option="${room.id}"]`);
                const rate = card?.querySelector(".aw-stay-option__meta strong");
                if (rate) rate.textContent = `${money.format(room.nightlyRate)} / night`;
            });
            updateStayQuote();
        } catch {
            const optionsError = document.querySelector("#stay-options-error");
            if (optionsError) optionsError.hidden = false;
        }
    }

    document.querySelectorAll("[data-choose-room]").forEach((button) => {
        button.addEventListener("click", () => {
            roomSelect.value = button.dataset.chooseRoom;
            roomSelect.dispatchEvent(new Event("change", { bubbles: true }));
            form.scrollIntoView({ behavior: "smooth", block: "center" });
        });
    });

    form.addEventListener("input", () => {
        window.clearTimeout(quoteTimer);
        quoteTimer = window.setTimeout(updateStayQuote, 180);
    });
    form.addEventListener("change", updateStayQuote);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!form.reportValidity()) return;
        error.hidden = true;
        submitButton.disabled = true;
        if (submitText) submitText.textContent = "Saving your stay...";
        try {
            const stay = await requestJson("/api/stay-reservations", {
                method: "POST",
                body: JSON.stringify(payload())
            });
            document.querySelector("#stay-result-name").textContent = stay.fullName;
            document.querySelector("#stay-result-reference").textContent = stay.reference;
            result.hidden = false;
            result.scrollIntoView({ behavior: "smooth", block: "center" });
        } catch (submitError) {
            setStayError(submitError.message);
        } finally {
            submitButton.disabled = false;
            if (submitText) submitText.textContent = defaultSubmitText;
        }
    });

    loadRooms();
}

function setupBookingLookup() {
    const form = document.querySelector("#lookup-form");
    if (!form) return;

    const errorBox = document.querySelector("#lookup-error");
    const detail = document.querySelector("#booking-detail");
    const cancelError = document.querySelector("#cancel-error");
    const emailInput = form.querySelector("#lookup-email");
    let currentBooking;

    function showError(node, message) {
        node.textContent = message;
        node.hidden = false;
    }

    function renderBooking(booking) {
        currentBooking = booking;
        document.querySelector("#detail-name").textContent = booking.fullName;
        document.querySelector("#detail-reference").textContent = booking.reference;
        document.querySelector("#detail-date").textContent = new Intl.DateTimeFormat("en-IN", {
            dateStyle: "long"
        }).format(new Date(`${booking.visitDate}T00:00:00`));
        document.querySelector("#detail-pass").textContent = booking.passLabel;
        document.querySelector("#detail-guests").textContent = `${booking.adults} adults, ${booking.children} children`;
        document.querySelector("#detail-fast-passes").textContent = booking.fastPasses;
        document.querySelector("#detail-total").textContent = money.format(booking.total);
        const status = document.querySelector("#detail-status");
        status.textContent = booking.status;
        status.classList.toggle("aw-status--cancelled", booking.status === "CANCELLED");
        const cancelButton = document.querySelector("#cancel-booking");
        cancelButton.hidden = booking.status !== "CONFIRMED";
        detail.hidden = false;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        errorBox.hidden = true;
        detail.hidden = true;
        try {
            const booking = await requestJson("/api/bookings/lookup", {
                method: "POST",
                body: JSON.stringify({
                    reference: form.querySelector("#lookup-reference").value.trim(),
                    email: emailInput.value.trim()
                })
            });
            renderBooking(booking);
        } catch (error) {
            showError(errorBox, error.message);
        }
    });

    document.querySelector("#cancel-booking")?.addEventListener("click", async () => {
        if (!currentBooking || !window.confirm("Cancel this Adventure World reservation?")) return;
        cancelError.hidden = true;
        try {
            const updated = await requestJson(`/api/bookings/${encodeURIComponent(currentBooking.reference)}/cancel`, {
                method: "POST",
                body: JSON.stringify({ reference: currentBooking.reference, email: emailInput.value.trim() })
            });
            renderBooking(updated);
        } catch (error) {
            showError(cancelError, error.message);
        }
    });
}

setupNavigation();
setupRideFilters();
loadExperienceCards();
setupTicketPlanner();
setupStayPlanner();
setupBookingLookup();