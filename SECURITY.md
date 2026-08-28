# Security Policy

## Reporting a vulnerability

Please **do not** open a public issue for a security problem.

Report it privately through GitHub, from the **Security** tab of this repository,
using **Report a vulnerability**. That opens a private advisory only the maintainer
can read, and it lets us discuss and fix the issue before anything becomes public.

If you cannot use that channel, email the maintainer at contact@tatakae.fit.

Please include enough detail to reproduce the issue: the affected endpoint, the
request that triggers it, what you expected, and what happened instead.

## What to expect

- An acknowledgement within 5 working days.
- An assessment, and a fix or a rejection with reasons, within 30 days for anything
  that turns out to be exploitable.
- Credit in the release notes if you want it, once the fix is public.

This is a small project maintained by one person, so those are honest targets
rather than a contractual guarantee.

## Scope

This repository is the Tatakae API: friendships, training sessions and leaderboards.

In scope: anything in this codebase, including authentication and authorization gaps,
injection, broken access control between athletes, denial of service reachable through
a documented endpoint, and leaks of athlete data across accounts.

Out of scope:

- The `dev` profile. It intentionally exposes Swagger UI, enables `ddl-auto: update` and
  ships a well known local database password in `compose.yml`. It is a local development
  configuration and is never meant to run in production, which is why
  `application-prod.yaml` requires `DB_USER` and `DB_PASSWORD` with no fallback and keeps
  the OpenAPI endpoints disabled.
- Findings that require access to the machine already running the service.
- Reports produced by an automated scanner with no demonstrated impact.

## Current status

At the time of writing this service is **not deployed to production** and holds no real
user data. Reports are still welcome: fixing a problem before the first user exists is
cheaper than fixing it afterwards.
