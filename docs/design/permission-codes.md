# Permission Codes

Home Center v1 uses `sys_permission.urls` as the permission code source. Multiple URL patterns are separated with semicolons.

## Base Roles

- Unauthenticated user: no role record. Access is limited to the public homepage, login, static public assets, nginx `/checkAuth`, PC Agent upload/WebSocket callbacks, public navigation APIs, and SSO `/api/sso/me` invalid-session checks.
- Normal user: role ID `2`. Access is granted only through assigned `sys_permission.urls`.
- Super admin: role ID `1`. The interceptor treats this role as all-permission. Do not add a generic administrator role unless the product plan changes.

## Top-Level Menu Codes

- Public home: `/`
- Private navigation: `/private.html`
- PC monitor: `/device/pc/monitor.html`
- PC power control: `/device/pc/power.html`
- Backend management: `/admin/manage.html`

Static HTML pages still require login before access. Their functional APIs must also be authorized separately.

## Backend Management Codes

- Backend all: `/backend/**`
- User management: `/backend/user/**`
- Role management: `/backend/role/**`
- Permission management: `/backend/permission/**`
- PC device management and PC Agent audit: `/backend/device/pc/**`
- Data/audit management: `/backend/data/**`

## PC Device Codes

- Device view: `/device/pc/devices;/device/pc/permissions;/device/pc/*/screenshot/latest`
- Screenshot capture: `/device/pc/*/screenshot`
- Power control: `/device/pc/power/*/*`
- Web Shell command execution: `/device/pc/command/*`

Do not grant `/device/pc/**` to ordinary users because it merges view, screenshot, power, and Web Shell permissions. Web Shell must stay separate from ordinary device viewing.
