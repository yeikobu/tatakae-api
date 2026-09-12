# Server-Sent Events (SSE)

Real-time push for friend requests and leaderboard score updates.

## Endpoint

`GET /api/v1/events`

- **Content-Type:** `text/event-stream`
- **Auth (required):** Apple identity JWT
  - Preferred: `Authorization: Bearer <jwt>` (iOS `URLSession` data task)
  - EventSource / web fallback: `GET /api/v1/events?access_token=<jwt>`  
    Query token is accepted **only** on `/api/v1/events*` to limit accidental leakage.
- **Timeout:** 30 minutes (reconnect afterwards)
- **Keep-alive:** SSE comment `: keepalive` about every 20 seconds

## Event names

| `event:` field | When | Payload (JSON `data:`) |
|---|---|---|
| `connected` | Immediately after subscribe | `{ "ok": true }` |
| `friend_request` | Someone sends a request **to** the connected user | `{ "id", "fromUser": UserResponse, "createdAt" }` |
| `friend_request_accepted` | Your outgoing request was accepted | `{ "id", "requesterId", "addresseeId", "acceptedBy": UserResponse, "respondedAt" }` |
| `leaderboard_update` | A training session is recorded | `{ "exercise", "scope": "GLOBAL"\|"FRIENDS", "userId", "username", "reps", "country" }` |

### Leaderboard MVP fan-out

On score record, the API notifies the **scorer** and their **accepted friends**, emitting both `GLOBAL` and `FRIENDS` scope hints. Clients should refetch the relevant leaderboard (or patch locally). There is no full ranking payload in the event.

## Caddy (Contabo / production)

Response buffering breaks SSE. Before enabling in production, adjust the reverse proxy (do **not** change prod without Jacob). Example:

```caddy
api.tatakae.fit {
    handle /api/v1/events* {
        reverse_proxy api:8080 {
            flush_interval -1
        }
    }

    reverse_proxy api:8080

    encode gzip
    log {
        output file /data/access.log
        format json
    }
}
```

Notes:

- Disable buffering / set `flush_interval -1` for the events path.
- Prefer **not** gzipping the SSE stream (or exclude `/api/v1/events*` from `encode gzip`).
- Long-lived connections: ensure proxy idle timeouts exceed the keep-alive interval.

## curl smoke test

```bash
# Terminal A — open the stream (Bearer)
curl -N -H "Authorization: Bearer $APPLE_JWT" \
  -H "Accept: text/event-stream" \
  http://localhost:8080/api/v1/events

# Or EventSource-style query token:
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8080/api/v1/events?access_token=$APPLE_JWT"

# Terminal B — send a friend request TO that user (authenticated as another athlete)
curl -X POST http://localhost:8080/api/v1/friendships \
  -H "Authorization: Bearer $OTHER_JWT" \
  -H "Content-Type: application/json" \
  -d '{"requesterId":"<other-user-id>","addresseeId":"<stream-user-id>"}'
```

You should see an `event: friend_request` block on Terminal A.
