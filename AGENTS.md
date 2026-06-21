# Repository Guidelines

## Project Structure & Module Organization

This is a Java 8 Spring Boot 2.4 application built with Maven. Main application code lives under `src/main/java/com/wwh/home/center`, with controllers in `controller`, services in `service` and `service/impl`, persistence interfaces in `dao/mapper`, configuration in `config`, and shared utilities or models in `common` and `model`.

Runtime resources are in `src/main/resources`: MyBatis XML mappings in `mapper`, Thymeleaf templates in `templates`, environment configuration in `application*.yml`, and browser assets in `static` (`js`, `css`, `device`, `weather`, `bootstrap`, `lib`). Tests and test-only helpers are under `src/test/java`; temporary test resources are under `src/test/resources`.

## Product Context

Home Center is a personal home portal deployed on the user's own server and exposed on the public internet. It combines a public navigation homepage, a login-protected private navigation page, backend administration, unified authentication for proxied internal systems, SSO for the user's own applications, and device/PC control.

The system is not strictly single-user. The owner may create accounts for family members or friends, so permissions, private navigation isolation, audit logs, and backend management must not be simplified as if only one user exists. The public homepage is the default public entry point. It contains public tools, bookmarked links, and links to independent apps that may have their own login screens. The private navigation page is shown after Home Center login and contains internal management tools, server administration links, and nginx-proxied private systems. The backend administration area manages users, permissions, navigation entries, configuration, devices, and related system data.

The initial role model has three roles only: unauthenticated user, normal user, and super admin. Do not add a generic administrator role unless explicitly requested later.

Use `docs/PROJECT_CONTEXT.md` as the product and architecture context source before making navigation, authentication, nginx proxy, or device-control changes.

## Companion Agent Project

The paired PC agent repository is in the sibling directory `/opt/projects/home-center-pc-agent`. This server project exposes device and PC agent APIs; the agent project runs on the target PC and handles local actions such as shutdown, screenshots, version reporting, and update/download workflows. When changing PC control contracts, update both repositories together and keep endpoint paths, payloads, ports, and auth expectations aligned.

The PC agent is deployed on computers that need to be controlled. It handles screenshots, command execution, device status, power-related operations, version reporting, and execution results. Frontend and backend code must never assume an agent is online; handle offline, timeout, executing, failure, and success states explicitly.

PC Agent communication is based on a WebSocket long connection initiated by the agent to the Home Center server. Use `docs/design/agent-websocket-plan.md` as the source of truth for the endpoint, message protocol, device identity, migration, and test plan. Do not reintroduce a model where the server actively connects to an agent IP/port for command delivery unless explicitly requested.

When an agent connects for the first time and no existing `agentId` matches, automatically create an enabled device record instead of putting it into a pending approval flow. The device can be renamed, disabled, or managed later in the backend.

Sensitive PC Agent operations require audit logs, including screenshots, shutdown/restart, remote command execution, and future high-risk control actions. Audit records should include the actor, target device, operation type, request time, completion time, result, failure reason, requestId, source IP, and User-Agent.

For remote command execution audits, record only a command summary and the execution result. Do not persist full command output by default.

Device operations are risk-tiered. Shutdown and restart require a second confirmation. Remote command execution is intended to be a Web Shell experience: require permission checks and a risk confirmation before entering the shell, then do not prompt for every individual command; rely on audit logging and clear target-device context instead.

## nginx Authentication Model

nginx protects proxied private systems by calling Home Center before forwarding the request. The shared nginx auth flow uses an internal `/checkauth` location that proxies to Home Center `/checkAuth`, passes `X-Original-URI`, skips the request body, and expects an auth-request-compatible status code.

Home Center should also become the SSO provider for the user's own applications. nginx auth is primarily for existing systems that cannot be modified to integrate with Home Center directly. When designing authentication changes, keep both modes working: nginx `auth_request` for legacy/proxied systems and direct login/session validation APIs for first-party applications.

The first SSO version should stay lightweight: first-party apps share the browser `home_center_token` cookie and call Home Center validation/current-user APIs such as `/api/sso/me`. Do not introduce OAuth2 or OIDC unless explicitly requested later.

First-party SSO app integrations require `appId` and `appSecret`. `appId` identifies the application and scopes access; `appSecret` is only for trusted server-to-server calls and must never be exposed in browser code, static assets, or committed docs.

The default `/api/sso/me` style response should be minimal: `userId`, `username`, `nickname`, `avatar`, `roles`, and `permissions`. Do not return sensitive fields such as phone or email unless the requesting app is explicitly authorized for them.

Audit first-party SSO validation and `/api/sso/me` calls. Records should include appId, user, endpoint, call time, source IP, User-Agent, result, failure reason, and returned information scope.

SSO logout is lazy for first-party apps: Home Center invalidates `home_center_token`, and apps discover the invalid state on their next `/api/sso/me`, validation, or backend-auth check. Do not build active logout notifications to every app unless explicitly requested.

First-party apps are expected to live on subdomains of the same parent domain, so SSO cookie design should support cross-subdomain sharing, for example with a placeholder `Domain=.<public-domain>`, `SameSite=Lax`, `Secure`, and `HttpOnly` settings. Never commit the real parent domain.

Production is expected to run fully over HTTPS. Production `home_center_token` cookies must use `Secure`; local HTTP development may omit `Secure`.

Use `SameSite=Lax` by default. Only consider `SameSite=None; Secure` later if cross-site iframe or third-party browser contexts become a real requirement.

The nginx token callback path is `/home-center/token`. It stores the `token` query parameter in the `home_center_token` HttpOnly cookie and redirects to the `ref` query parameter when present, or `/` otherwise. Do not change `/checkAuth`, `/checkauth`, `/home-center/token`, `home_center_token`, or the `X-Original-URI` contract without updating the nginx configuration and documenting the migration.

Authentication uses a short-lived custom token stored in server memory for this single-instance application. Do not introduce JWT or Redis for this unless explicitly requested. Valid access may refresh the sliding expiration, but every session must have a hard maximum lifetime of 12 hours so polling pages cannot keep a login alive forever.

Only one active session is allowed per user. A successful new login must invalidate that user's previous active session and should be reflected in session/audit records.

Disabling a user, changing roles or permissions, or changing a password must invalidate affected active sessions immediately. Frontend code should use a shared session-invalid/forced-logout prompt instead of duplicating this behavior page by page.

Each private internal system can have its own nginx `server` block and should include shared Home Center common/auth config, for example `conf.d/home-center.common` and `conf.d/home-center.auth`. Keep concrete private domains, private IPs, tokens, and secrets out of committed docs and frontend code.

## Build, Test, and Development Commands

- `mvn clean package`: compile, run tests, and build the Spring Boot jar.
- `mvn test`: run the test suite only.
- `mvn spring-boot:run`: start the app locally using Maven.
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev`: run with the `dev` profile when local configuration is available.

There is no Maven wrapper in this repository, so use an installed Maven version compatible with Java 8.

## Coding Style & Naming Conventions

Use standard Java formatting with 4-space indentation. Keep package names lowercase and aligned with `com.wwh.home.center`. Follow existing suffix patterns: `*Controller`, `*Service`, `*ServiceImpl`, `*Mapper`, `*Config`, `*Vo`, `*Qo`, and entity classes in `model/entity`. Prefer Lombok where the project already uses it, and keep comments concise. Static frontend code is plain HTML, CSS, and JavaScript; keep file-local style consistent with the surrounding page.

Frontend work should follow `docs/UI_DESIGN.md` and `docs/frontend-development-rules.md`. Prefer native HTML/CSS/JavaScript and reuse existing project assets such as `app-theme.css`, `app-theme.js`, and `window.HomeCenter`. Use Thymeleaf when it materially reduces duplication for shared headers, footers, server-rendered context, or common page shells. Do not introduce Vue, React, Angular, npm build tooling, or external CDNs unless explicitly approved.

The public homepage and login page must remain usable on mobile. Backend administration pages are desktop-first and do not need mobile-specific layouts. Keep the public navigation and private navigation concepts separate, and do not place private/internal links into the public navigation by accident.

Private navigation entries are user-owned and may include nginx-proxied systems, ordinary external links, direct intranet URLs, and note/command-style entries such as SSH or RDP instructions. Do not assume every private navigation entry is an nginx-authenticated proxy target.

Public navigation and private navigation use separate data tables, not one table with a visibility/type flag. Their base fields are similar, but private navigation also needs user ownership and entry type fields.

Navigation entries that have URLs should open in a new window/tab by default. Note/command-style entries such as SSH or RDP instructions should not navigate directly; show instructions or provide copy-to-clipboard behavior instead.

Navigation icons are uploaded images managed by the user. Do not build default favicon scraping or depend on external icon/CDN services unless explicitly requested. Provide a stable placeholder icon when no image is uploaded.

Uploaded navigation icon images should be stored in a local server file directory; the database stores only the public access path and necessary metadata. Do not store icon binaries as database BLOBs by default, and do not expose real filesystem paths in public URLs.

Navigation icon uploads allow common image formats such as PNG, JPG/JPEG, WEBP, and SVG, with a maximum size of 10MB per file. Treat SVG as active content: sanitize or reject scripts, event attributes, and external resources.

Navigation link health should be checked by backend scheduled jobs and displayed from stored/cached results. Do not make navigation pages probe every link during page load.

Keep the public homepage visually clean: do not show link health status there. Health status may be shown in backend management and private navigation.

Private navigation should show only lightweight health indicators, such as a small dot or badge. Detailed health errors, HTTP status codes, and timings belong in backend management.

## Testing Guidelines

Tests use Spring Boot Test and JUnit. Place tests in the matching package under `src/test/java`, and name test classes with a `Test` suffix, for example `UserServiceTest`. For service or database behavior, prefer focused tests around the changed method and profile/config dependencies. Run `mvn test` before submitting changes; use `mvn -Dtest=UserServiceTest test` for a single class.

## Commit & Pull Request Guidelines

Recent history uses concise messages such as `feat: add pc-agent API` and short Chinese summaries. Keep commits brief and action-oriented; use a prefix like `feat:`, `fix:`, or `refactor:` when it clarifies the change.

Pull requests should describe the behavior changed, list verification commands, and mention any configuration, database, Redis, MQTT, or static asset impact. Include screenshots for visible UI changes in `static` or `templates`, and link related issues when available.

## Security & Configuration Tips

Do not commit secrets, tokens, private hostnames, or production credentials. Keep environment-specific values in the appropriate `application-*.yml` file and document any new required property.

Repository docs should use placeholders for deployment details. Do not commit real public domains, private IPs, server paths, nginx config paths, cookie/token examples, private hostnames, or anything that exposes the user's personal server, home network, or internal systems. Keep real deployment details in server config or private local docs outside the repository.
