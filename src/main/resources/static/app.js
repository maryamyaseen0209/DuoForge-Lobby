const state = {
  data: null,
  playerView: "leaderboard",
  activities: [],
  loading: false,
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => [...document.querySelectorAll(selector)];

async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  let payload = {};
  try { payload = await response.json(); } catch { /* empty response */ }
  if (!response.ok) throw new Error(payload.error || `Request failed (${response.status})`);
  return payload;
}

async function loadState() {
  try {
    state.data = await api("/api/state");
    render();
  } catch (error) {
    toast(error.message, true);
  }
}

function render() {
  if (!state.data) return;
  const { overview, players, bstPlayers, matches, queues } = state.data;
  $("#heroPlayers").textContent = pad(overview.players);
  $("#heroMatches").textContent = pad(overview.matches);
  $("#totalPlayers").textContent = overview.players;
  $("#totalMatches").textContent = overview.matches;
  $("#queuedPlayers").textContent = overview.queued;
  renderQueues(queues);
  renderPlayers(state.playerView === "bst" ? bstPlayers : players);
  renderMatches(matches);
  renderActivity();
}

function renderQueues(queues) {
  const metadata = {
    ROOKIE: { subtitle: "Ranks 1–2", color: "#68f5df" },
    ELITE: { subtitle: "Ranks 3–4", color: "#6ca8ff" },
    MASTER: { subtitle: "Rank 5+", color: "#a783ff" },
  };
  $("#queueGrid").innerHTML = Object.entries(metadata).map(([tier, meta]) => {
    const players = queues[tier] || [];
    return `<section class="queue-lane" style="--tier:${meta.color}">
      <header><div><h4>${tier}</h4><small>${meta.subtitle}</small></div><span>${players.length}</span></header>
      <div class="queue-list">
        ${players.length ? players.map(queuePlayer).join("") : `<div class="queue-empty">No players waiting.<br>The next challenger starts this lane.</div>`}
      </div>
    </section>`;
  }).join("");
  $$(".cancel-queue").forEach((button) => button.addEventListener("click", () => cancelQueue(button.dataset.id)));
}

function queuePlayer(player) {
  return `<div class="queue-player">
    <div><span class="player-avatar">${initials(player.playerId)}</span><p><b>#${player.playerId}</b><small>Rank ${player.rank} · ${player.points} XP</small></p></div>
    <button class="cancel-queue" data-id="${player.playerId}" title="Cancel queue">×</button>
  </div>`;
}

function renderPlayers(players) {
  $("#playersTable").innerHTML = players.map((player, index) => {
    const progress = Math.min(100, (player.points / 200) * 100);
    return `<div class="player-row" data-player="${player.playerId}">
      <div class="player-cell"><span class="player-avatar">${state.playerView === "leaderboard" ? index + 1 : initials(player.playerId)}</span><div><strong>Player #${player.playerId}</strong><small>${player.friends.length} linked friend${player.friends.length === 1 ? "" : "s"}</small></div></div>
      <span class="tier-chip">${player.tier}</span>
      <strong>R${player.rank}</strong>
      <span class="record-cell"><b>${player.wins}W</b> / ${player.losses}L</span>
      <div class="progress-wrap"><div class="progress-bar"><i style="width:${progress}%"></i></div><span>${player.points}/200</span></div>
    </div>`;
  }).join("");
  $$(".player-row").forEach((row) => row.addEventListener("click", () => openPlayer(Number(row.dataset.player))));
}

function renderMatches(matches) {
  $("#matchList").innerHTML = matches.length ? matches.map((match) => `
    <article class="match-item">
      <div class="match-top"><span>MATCH #${String(match.matchId).padStart(3,"0")}</span><span class="match-type">${match.matchType}</span></div>
      <div class="versus"><strong>#${match.player1Id}</strong><i></i><strong>#${match.player2Id}</strong></div>
      <div class="winner-line"><b>#${match.winnerId}</b> secured the win · ${match.tier} tier</div>
    </article>`).join("") : `<div class="queue-empty">No matches recorded.</div>`;
}

function renderActivity() {
  const feed = $("#activityFeed");
  if (!state.activities.length) {
    feed.innerHTML = `<div class="empty-state"><i></i><p>Waiting for your first action.</p></div>`;
    return;
  }
  feed.innerHTML = state.activities.slice(0, 8).map((activity) => `
    <div class="activity-item"><span>${activity.icon}</span><div><p>${escapeHtml(activity.message)}</p><small>${activity.time}</small></div></div>`).join("");
}

function addActivity(message, icon = "↗") {
  state.activities.unshift({ message, icon, time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) });
  renderActivity();
}

async function handleQuickMatch(event) {
  event.preventDefault();
  if (state.loading) return;
  const playerId = Number($("#quickPlayerId").value);
  await execute(async () => {
    const response = await api("/api/matchmaking/online", { method: "POST", body: JSON.stringify({ playerId }) });
    state.data = response.state;
    addActivity(response.message, response.matched ? "⚔" : "⇥");
    toast(response.message);
    if (response.matched) animateMatch(response.match);
    render();
    event.target.reset();
  });
}

async function handleFriendMatch(event) {
  event.preventDefault();
  if (state.loading) return;
  const playerId = Number($("#friendPlayerId").value);
  const friendId = Number($("#friendOpponentId").value);
  await execute(async () => {
    const response = await api("/api/matches/friend", { method: "POST", body: JSON.stringify({ playerId, friendId }) });
    state.data = response.state;
    addActivity(`Friend duel: #${playerId} challenged #${friendId}. Winner: #${response.match.winnerId}.`, "⚔");
    toast(`Duel complete — Player #${response.match.winnerId} wins.`);
    animateMatch(response.match);
    render();
    event.target.reset();
  });
}

async function cancelQueue(playerId) {
  await execute(async () => {
    const response = await api(`/api/matchmaking/queue/${playerId}`, { method: "DELETE" });
    state.data = response.state;
    addActivity(`Player #${playerId} left matchmaking.`, "×");
    toast(response.message);
    render();
  });
}

async function resetDemo() {
  await execute(async () => {
    const response = await api("/api/reset-demo", { method: "POST" });
    state.data = response.state;
    state.activities = [];
    addActivity("Arena restored to its presentation-ready demo state.", "↻");
    toast(response.message);
    render();
  });
}

async function runDemoDuel() {
  const player1 = 700 + Math.floor(Math.random() * 150);
  let player2 = 850 + Math.floor(Math.random() * 140);
  if (player1 === player2) player2++;
  await execute(async () => {
    const response = await api("/api/matches/friend", { method: "POST", body: JSON.stringify({ playerId: player1, friendId: player2 }) });
    state.data = response.state;
    addActivity(`Demo duel generated for #${player1} and #${player2}.`, "✦");
    animateMatch(response.match);
    toast(`Demo duel complete — #${response.match.winnerId} wins.`);
    render();
  });
}

async function execute(operation) {
  state.loading = true;
  $$('button[type="submit"]').forEach((button) => button.disabled = true);
  try { await operation(); }
  catch (error) { toast(error.message, true); addActivity(error.message, "!"); }
  finally {
    state.loading = false;
    $$('button[type="submit"]').forEach((button) => button.disabled = false);
  }
}

function animateMatch(match) {
  $("#fighterOne").textContent = `#${match.player1Id}`;
  $("#fighterTwo").textContent = `#${match.player2Id}`;
  const signal = $("#matchSignal");
  signal.textContent = `WINNER #${match.winnerId}`;
  signal.classList.add("show");
  document.querySelector(".arena-core").animate([
    { transform: "translate(-50%,-50%) scale(1)" },
    { transform: "translate(-50%,-50%) scale(1.12)", filter: "brightness(1.5)" },
    { transform: "translate(-50%,-50%) scale(1)" },
  ], { duration: 900, easing: "ease-out" });
  setTimeout(() => signal.classList.remove("show"), 2600);
}

function openPlayer(playerId) {
  const player = [...state.data.players, ...state.data.bstPlayers].find((item) => item.playerId === playerId);
  if (!player) return;
  $("#drawerPlayerId").textContent = `Player #${player.playerId}`;
  $("#drawerTier").textContent = `${player.tier} TIER`;
  $("#drawerStats").innerHTML = `
    <div><span>Rank</span><strong>${player.rank}</strong></div>
    <div><span>Points</span><strong>${player.points}</strong></div>
    <div><span>Wins</span><strong>${player.wins}</strong></div>
    <div><span>Losses</span><strong>${player.losses}</strong></div>`;
  $("#drawerFriends").innerHTML = player.friends.length
    ? player.friends.map((id) => `<span class="friend-chip">Player #${id}</span>`).join("")
    : `<span class="friend-chip">No friends linked yet</span>`;
  $("#playerDrawer").classList.add("open");
  $("#drawerBackdrop").classList.add("open");
  $("#playerDrawer").setAttribute("aria-hidden", "false");
}

function closePlayer() {
  $("#playerDrawer").classList.remove("open");
  $("#drawerBackdrop").classList.remove("open");
  $("#playerDrawer").setAttribute("aria-hidden", "true");
}

let toastTimer;
function toast(message, isError = false) {
  const element = $("#toast");
  element.textContent = message;
  element.classList.toggle("error", isError);
  element.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => element.classList.remove("show"), 3400);
}

function pad(value) { return String(value).padStart(2, "0"); }
function initials(id) { return String(id).slice(-2).padStart(2, "0"); }
function escapeHtml(value) { return String(value).replace(/[&<>'"]/g, (char) => ({ "&":"&amp;", "<":"&lt;", ">":"&gt;", "'":"&#039;", '"':"&quot;" }[char])); }

function initArena() {
  const canvas = $("#arenaCanvas");
  const context = canvas.getContext("2d");
  const particles = Array.from({ length: 110 }, () => ({
    x: Math.random() * 2 - 1,
    y: Math.random() * 2 - 1,
    z: Math.random() * 2 - 1,
    size: Math.random() * 1.4 + .35,
    speed: Math.random() * .002 + .0008,
  }));
  let width = 0;
  let height = 0;
  let dpr = 1;
  let time = 0;

  function resize() {
    const bounds = canvas.getBoundingClientRect();
    dpr = Math.min(window.devicePixelRatio || 1, 1.7);
    width = bounds.width;
    height = bounds.height;
    canvas.width = width * dpr;
    canvas.height = height * dpr;
    context.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  function draw() {
    time += .008;
    context.clearRect(0, 0, width, height);
    const cx = width / 2;
    const cy = height / 2;
    const radius = Math.min(width, height) * .36;

    particles.forEach((particle, index) => {
      particle.z -= particle.speed;
      if (particle.z < -1) particle.z = 1;
      const angle = time * (index % 2 ? .22 : -.16);
      const rx = particle.x * Math.cos(angle) - particle.z * Math.sin(angle);
      const rz = particle.x * Math.sin(angle) + particle.z * Math.cos(angle);
      const scale = 1 / (2.25 - rz);
      const x = cx + rx * radius * scale * 2.1;
      const y = cy + particle.y * radius * scale * 2.1;
      const alpha = Math.max(.08, scale * .9);
      context.beginPath();
      context.fillStyle = index % 5 === 0 ? `rgba(170,132,255,${alpha})` : `rgba(104,245,223,${alpha})`;
      context.arc(x, y, particle.size * (scale + .35), 0, Math.PI * 2);
      context.fill();
    });

    context.save();
    context.translate(cx, cy);
    context.rotate(time * .18);
    const gradient = context.createLinearGradient(-radius, 0, radius, 0);
    gradient.addColorStop(0, "rgba(104,245,223,0)");
    gradient.addColorStop(.5, "rgba(104,245,223,.28)");
    gradient.addColorStop(1, "rgba(167,131,255,0)");
    context.strokeStyle = gradient;
    context.lineWidth = 1;
    context.beginPath();
    context.ellipse(0, 0, radius * 1.15, radius * .27, 0, 0, Math.PI * 2);
    context.stroke();
    context.restore();
    requestAnimationFrame(draw);
  }

  new ResizeObserver(resize).observe(canvas);
  resize();
  draw();
}

function initEvents() {
  $("#quickMatchForm").addEventListener("submit", handleQuickMatch);
  $("#friendMatchForm").addEventListener("submit", handleFriendMatch);
  $("#resetButton").addEventListener("click", resetDemo);
  $("#demoMatchButton").addEventListener("click", runDemoDuel);
  $("#drawerClose").addEventListener("click", closePlayer);
  $("#drawerBackdrop").addEventListener("click", closePlayer);
  $$(".view-toggle button").forEach((button) => button.addEventListener("click", () => {
    state.playerView = button.dataset.view;
    $$(".view-toggle button").forEach((item) => item.classList.toggle("active", item === button));
    renderPlayers(state.playerView === "bst" ? state.data.bstPlayers : state.data.players);
  }));
  window.addEventListener("mousemove", (event) => {
    $("#cursorGlow").style.left = `${event.clientX}px`;
    $("#cursorGlow").style.top = `${event.clientY}px`;
  }, { passive: true });
  window.addEventListener("keydown", (event) => { if (event.key === "Escape") closePlayer(); });
}

initEvents();
initArena();
loadState();
