const storageKey = "adventure-world-itinerary";
const authKey = "adventure-world-auth";
const itinerary = new Map();
const basketCount = document.querySelector("[data-basket-count]");
const dialog = document.querySelector("#itinerary-dialog");
const list = document.querySelector("#itinerary-list");
const total = document.querySelector("#itinerary-total");
const emptyMessage = document.querySelector("#empty-itinerary");
const saveButton = document.querySelector("#save-plan");
const dateInput = document.querySelector("#visit-date");
const toast = document.querySelector("#toast");
const rideCards = document.querySelectorAll(".ride-card");
const searchInput = document.querySelector("#ride-search");
const noResults = document.querySelector("#no-results");
const globalSearch = document.querySelector("#global-search");

function formatPrice(value) {
    return `₹${Number(value).toLocaleString("en-IN")}`;
}

function isItemMapAvailable() { return !!dialog && !!list && !!total && !!saveButton && !!dateInput && !!toast; }

function loadPlan() {
    if (!isItemMapAvailable()) return;
    try {
        const saved = JSON.parse(localStorage.getItem(storageKey) || "{}");
        Object.entries(saved.items || {}).forEach(([name, quantity]) => {
            if (name && Number(quantity) > 0) itinerary.set(name, Number(quantity));
        });
        if (saved.date) dateInput.value = saved.date;
    } catch {
        localStorage.removeItem(storageKey);
    }
}

function savePlan() {
    if (!isItemMapAvailable()) return;
    localStorage.setItem(storageKey, JSON.stringify({ items: Object.fromEntries(itinerary), date: dateInput.value }));
}

function getCount() {
    return [...itinerary.values()].reduce((sum, quantity) => sum + quantity, 0);
}

function getTotal() {
    return [...itinerary.entries()].reduce((sum, [name, quantity]) => {
        const card = [...rideCards].find((ride) => ride.dataset.name === name);
        return sum + (Number(card?.dataset.price || 0) * quantity);
    }, 0);
}

function renderBasket() {
    if (!isItemMapAvailable()) return;
    const count = getCount();
    if (basketCount) basketCount.textContent = count;
    list.replaceChildren();
    emptyMessage.hidden = count > 0;
    saveButton.disabled = count === 0;

    itinerary.forEach((quantity, name) => {
        const card = [...rideCards].find((ride) => ride.dataset.name === name);
        if (!card) return;
        const price = Number(card.dataset.price || 0);
        const row = document.createElement("li");
        row.className = "itinerary-row";

        const details = document.createElement("div");
        const itemName = document.createElement("strong");
        itemName.textContent = name;
        const priceLine = document.createElement("span");
        priceLine.textContent = `${formatPrice(price)} each`;
        details.append(itemName, priceLine);

        const quantityControl = document.createElement("div");
        quantityControl.className = "quantity-control";

        const decrease = document.createElement("button");
        decrease.type = "button";
        decrease.textContent = "−";
        decrease.addEventListener("click", () => changeQuantity(name, -1));

        const quantityText = document.createElement("span");
        quantityText.textContent = quantity;

        const increase = document.createElement("button");
        increase.type = "button";
        increase.textContent = "+";
        increase.addEventListener("click", () => changeQuantity(name, 1));

        quantityControl.append(decrease, quantityText, increase);

        const remove = document.createElement("button");
        remove.type = "button";
        remove.className = "remove-item";
        remove.textContent = "Remove";
        remove.addEventListener("click", () => {
            itinerary.delete(name);
            updateBasket();
        });

        row.append(details, quantityControl, remove);
        list.append(row);
    });

    total.textContent = formatPrice(getTotal());
}

function updateBasket() {
    renderBasket();
    savePlan();
}

function changeQuantity(name, amount) {
    const nextQuantity = (itinerary.get(name) || 0) + amount;
    if (nextQuantity <= 0) itinerary.delete(name);
    else itinerary.set(name, nextQuantity);
    updateBasket();
}

function showToast(message) {
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add("is-visible");
    window.setTimeout(() => toast.classList.remove("is-visible"), 2600);
}

function localDateString(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function syncAuthState() {
    const loggedIn = localStorage.getItem(authKey) === "true";
    document.querySelectorAll("[data-auth-button]").forEach((button) => {
        button.textContent = loggedIn ? "Logout" : "Login";
    });
    document.querySelectorAll("[data-auth-link]").forEach((link) => {
        link.textContent = "Login";
        link.href = "/login";
        link.style.display = loggedIn ? "none" : "inline-flex";
    });
    document.querySelectorAll("[data-logout]").forEach((button) => {
        button.style.display = loggedIn ? "inline-flex" : "none";
    });
}

function bindAuthActions() {
    document.querySelectorAll("[data-auth-button]").forEach((button) => {
        button.addEventListener("click", () => {
            const loggedIn = localStorage.getItem(authKey) === "true";
            if (loggedIn) localStorage.removeItem(authKey);
            else localStorage.setItem(authKey, "true");
            syncAuthState();
            showToast(loggedIn ? "You have been logged out." : "Login successful.");
        });
    });

    document.querySelectorAll("[data-logout]").forEach((button) => {
        button.addEventListener("click", () => {
            localStorage.removeItem(authKey);
            syncAuthState();
            showToast("You have been logged out.");
        });
    });
}

function bindRideAddButtons() {
    document.querySelectorAll(".add-to-day").forEach((button) => {
        button.addEventListener("click", () => {
            const name = button.dataset.name;
            if (!name) return;
            changeQuantity(name, 1);
            showToast(`${name} added to your day.`);
        });
    });
}

function bindItineraryDialog() {
    if (!dialog) return;
    document.querySelectorAll("[data-open-itinerary]").forEach((button) => {
        button.addEventListener("click", () => dialog.showModal());
    });

    const closeButton = document.querySelector("[data-close-dialog]");
    if (closeButton) closeButton.addEventListener("click", () => dialog.close());

    dialog.addEventListener("click", (event) => {
        if (event.target === dialog) dialog.close();
    });

    const clearButton = document.querySelector("#clear-plan");
    if (clearButton) {
        clearButton.addEventListener("click", () => {
            itinerary.clear();
            updateBasket();
            showToast("Your itinerary has been cleared.");
        });
    }
}

function bindSearch() {
    if (!searchInput) return;
    searchInput.addEventListener("input", () => {
        const query = searchInput.value.trim().toLowerCase();
        let visibleCount = 0;
        rideCards.forEach((card) => {
            const match = !query || card.dataset.search.toLowerCase().includes(query);
            card.classList.toggle("hidden", !match);
            if (match) visibleCount += 1;
        });
        if (noResults) noResults.classList.toggle("hidden", visibleCount > 0 || !query);
    });
}

function bindGlobalSearch() {
    if (!globalSearch) return;
    globalSearch.addEventListener("input", () => {
        const query = globalSearch.value.trim().toLowerCase();
        const searchable = document.querySelectorAll(".searchable-items .ride-card, .room-card, .food-card");
        searchable.forEach((item) => {
            const text = (item.textContent || "").toLowerCase();
            item.classList.toggle("hidden", query && !text.includes(query));
        });
    });
}

function bindTicketSummary() {
    const adults = document.querySelector("#adults");
    const children = document.querySelector("#children");
    const ticketType = document.querySelector("#ticket-type");
    const subtotalEl = document.querySelector("#subtotal");
    const discountEl = document.querySelector("#discount");
    const taxEl = document.querySelector("#tax");
    const finalTotalEl = document.querySelector("#final-total");
    const ticketForm = document.querySelector("#ticket-form");

    if (!adults || !children || !ticketType || !subtotalEl || !discountEl || !taxEl || !finalTotalEl || !ticketForm) return;

    const calculate = () => {
        const adultCount = Number(adults.value || 0);
        const childCount = Number(children.value || 0);
        const ticketValue = Number(ticketType.value || 0);
        const base = (adultCount * ticketValue) + (childCount * Math.round(ticketValue * 0.7));
        const discount = Math.round(base * 0.08);
        const tax = Math.round((base - discount) * 0.12);
        const totalValue = base - discount + tax;

        subtotalEl.textContent = formatPrice(base);
        discountEl.textContent = `-${formatPrice(discount)}`;
        taxEl.textContent = formatPrice(tax);
        finalTotalEl.textContent = formatPrice(totalValue);
    };

    adults.addEventListener("input", calculate);
    children.addEventListener("input", calculate);
    ticketType.addEventListener("change", calculate);

    ticketForm.addEventListener("submit", (event) => {
        event.preventDefault();
        showToast("Ticket booking details reviewed. Please complete payment.");
    });

    calculate();
}

function bindHamburger() {
    const toggle = document.querySelector(".hamburger-toggle");
    const sidebar = document.querySelector(".sidebar");
    if (!toggle || !sidebar) return;

    toggle.addEventListener("click", () => {
        const isOpen = sidebar.classList.contains("is-open");
        sidebar.classList.toggle("is-open", !isOpen);
        document.body.classList.toggle("menu-open", !isOpen);
    });

    sidebar.querySelectorAll("a").forEach((link) => {
        link.addEventListener("click", () => {
            sidebar.classList.remove("is-open");
            document.body.classList.remove("menu-open");
        });
    });
}

function setupDatePicker() {
    const expeditionDate = document.querySelector("#visit-date");
    if (expeditionDate) expeditionDate.min = localDateString(new Date());

    const ticketDate = document.querySelector("#ticket-date");
    if (ticketDate) ticketDate.min = localDateString(new Date());
}

syncAuthState();
bindAuthActions();
bindRideAddButtons();
bindItineraryDialog();
bindSearch();
bindGlobalSearch();
bindTicketSummary();
bindHamburger();
setupDatePicker();
loadPlan();
renderBasket();

if (saveButton && dateInput) {
    saveButton.addEventListener("click", () => {
        if (!dateInput.value) {
            dateInput.focus();
            dateInput.reportValidity();
            return;
        }
        savePlan();
        showToast("Your plan is saved on this device.");
        dialog.close();
    });
}

if (document.querySelector('#clear-plan')) {
    document.querySelector('#clear-plan').addEventListener('click', () => {
        itinerary.clear();
        updateBasket();
    });
}
