# Operation Risk Levels

Home Center uses three operation risk levels.

- `normal`: Read-only or low-impact operations, such as listing devices, opening navigation entries, checking status, and loading cached screenshots.
- `sensitive`: Operations that expose private information or affect another system but are not destructive by themselves, such as screenshot capture and entering Web Shell.
- `dangerous`: Operations that can interrupt service, change device state, or execute arbitrary actions, such as shutdown, restart, and remote command execution.

Risk handling rules:

- `normal` operations do not require second confirmation.
- `sensitive` operations require permission checks and audit logs when they expose private or device data.
- `dangerous` operations require permission checks, clear target context, audit logs, and second confirmation where the workflow is a single high-risk action.

Web Shell is entered through a sensitive confirmation. Individual commands are dangerous and audited, but they do not repeat confirmation for every command after the shell is entered.
