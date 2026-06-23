# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Authoritative context (read first)

`AGENTS.md` is the canonical rulebook: product context, nginx/SSO auth model, PC-agent contract, session/token policy, navigation data model, security/IP rules, coding style. **Consult it before touching auth, nginx, device control, or navigation.** Complementary docs:
- `docs/PROJECT_CONTEXT.md` — product & architecture source of truth.
- `docs/UI_DESIGN.md` + `docs/frontend-development-rules.md` — frontend rules.
- `docs/design/ui-frontend-refactor-todo.md` — the in-progress UI refactor (status + per-task notes).

This file intentionally avoids duplicating `AGENTS.md`; it adds build commands, big-picture architecture, and current refactor state.

## Build, run, test

Java 8 source target on Spring Boot 2.4.13 (compiles/runs on JDK 21). No Maven wrapper — use system `mvn`.

- `mvn clean package` — compile + test + build jar.
- `mvn test` — tests only. They are plain unit tests (no `@SpringBootTest`), ~33 of them, **do not need DB/Redis** — a reliable build gate for any change.
- `mvn -Dtest=UserServiceTest test` — single test class.
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev` — run locally on port **8866**. The dev profile (`application-dev.yml`) points at LAN MySQL/MariaDB + Redis; **both are reachable from this machine**, so prefer running the real backend for E2E over mocking. (Frontend-only checks can instead serve `src/main/resources/static` with `python3 -m http.server`.)

## Architecture (big picture)

Single-instance Spring Boot app combining: public portal, login-protected private navigation, unified login + SSO provider, device/PC control, and backend admin. Code under `src/main/java/com/wwh/home/center`: `controller` (`backend`/`common`/`device` subpackages + `LoginController`/`SsoController`/`UserController`/`InternalSysAccessController`), `service`+`service/impl`, `dao/mapper` (MyBatis-Plus + XML in `resources/mapper`), `config`, `security`, `model/{entity,vo,qo}`, `device` (PC agent / WebSocket). Entry point: `HomeCenterApp.java`. Persistence: MyBatis-Plus + Druid; Spring Data Redis for caching + rate limiting.

Cross-cutting contracts (details in AGENTS.md / PROJECT_CONTEXT — **do not break**):
- **nginx auth**: internal `/checkAuth` (`X-Original-URI`, no body) + `/home-center/token` cookie write; cookie name `home_center_token`. Don't rename these without updating nginx.
- **Sessions**: in-memory short-lived token (no JWT, not Redis-backed), sliding expiry with a **12-hour hard cap**, **one session per user**, invalidated on login/​password/​role/​permission change.
- **PC agent**: sibling repo `/opt/projects/home-center-pc-agent`. The agent opens a WebSocket **to this server** (server never dials out); first connect auto-creates an enabled device. Protocol in `docs/design/agent-websocket-plan.md`. Change contracts in both repos together.

## Frontend (native, no framework)

Static assets in `src/main/resources/static`. **No Vue/React/Angular, no npm/build-chain, no external CDN** — plain HTML/CSS/JS (+ Thymeleaf only when it clearly reduces duplication). Shared capabilities to reuse, not reinvent:
- `css/app-theme.css` (theme variables `--hc-*`, Bootstrap-compat `.modal/.table/.btn/.form-control`, `.hc-button`, `.hc-theme-toggle`), `css/entry.css` (entry-page shell + particle canvas + `.hc-entry-card`), `css/admin-shell.css` (`.admin-shell` sidebar+topbar+content).
- `js/app-theme.js` → `window.HomeCenter` (`request/get/post/put/del/toast/confirm/serializeForm/renderTable/escapeHtml`), `window.HomeCenterTheme`, HcModal/Bootstrap-compat; auto-binds `.hc-theme-toggle`.
- `js/main.js` → legacy globals (`request/getRequest/postRequest/showConfirm/showToastSimple/MsgTypes/Position`). `js/bg-particles.js` → `window.HomeCenterBg`. `js/admin-shell.js` → `window.HcAdminShell`.

Conventions:
- **Entry pages** (`index.html`, `login.html`, `private.html`) use the entry shell (`.hc-entry-topbar`/`.hc-entry-footer`, particle background) and **must work on mobile**.
- **Admin/device pages** use `.admin-shell` (desktop-only, min 1180px). Module pages are `admin/*.html`.
- `admin/manage.html` is the **legacy self-contained hub** (own shadcn dark design system + one big inline CRUD `<script>`, not `admin-manage.js` which is orphaned). All its modules are already extracted into individual `admin/*.html` shell pages — treat `manage.html` as deprecated.
- Dynamic API data must be escaped (`window.HomeCenter.escapeHtml` or `textContent`) before insertion.
- After UI changes: load the page (real backend, or static server) and verify console errors + layout + Chinese encoding (UTF-8, no BOM).

## UI refactor in progress

`docs/design/ui-frontend-refactor-todo.md` tracks it (~40/41 done). Work in task order, mark `[ ]`→`[x]` with a dated completion note, keep code runnable, and record any doc/code conflicts in the TODO. Remaining gap is 6.5 (SSO app management) which needs backend CRUD that doesn't exist yet. Local verification notes live in memory `frontend-refactor-local-verification` (prefer the real backend now that DB+Redis are reachable).

## Security

Never put real domains, private IPs, tokens, or `appSecret` in frontend code, static assets, or committed docs (use placeholders) — full rules in AGENTS.md. `application-dev.yml` does contain the dev LAN endpoints; keep such deployment info out of the frontend and public docs.
