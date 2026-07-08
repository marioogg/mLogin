# mLogin

Authentication (login/register) system for Minecraft networks running behind a **Velocity** proxy. Passwords are hashed with BCrypt, encrypted in transit with AES-GCM, and player auth state is shared across the network through Redis.

> **Note:** These are temporary docs. Full documentation is coming to GitBook.

---

## How it works

mLogin is split across two sides of your network:

- **mlogin-velocity** runs on the proxy. It holds the real authentication state, talks to the SQL database, enforces rate limiting, and gates access to your backends.
- **mlogin-spigot** runs on your **auth/login backend**. It exposes the `/login`, `/register` and `/changepass` commands and freezes/protects players until they authenticate.

State travels between them over Redis, so a player who logs in is recognized network-wide.

**You only need mlogin-spigot on the server where players actually log in.** The proxy prevents unauthenticated players from reaching any other backend, so the rest of your servers don't need the plugin installed to stay protected.

---

## Requirements

- A **Velocity** proxy
- At least one **Spigot/Paper** backend (`api-version 1.20`)
- **Redis** (shared by proxy and backends)
- **MySQL / MariaDB**
- **Java 17** or newer

---

## Installation

1. Drop `mlogin-velocity.jar` into the `plugins/` folder of your **Velocity proxy**.
2. Drop `mlogin-spigot.jar` into the `plugins/` folder of your **login backend** (e.g. your lobby).
3. Start both once so the config files generate, then stop them.
4. Configure both sides (see below). The **secret key** and **Redis** settings must match on both.
5. Start the proxy first, then the backend.

---

## Configuration

### Proxy — `plugins/mlogin/config.conf`

```hocon
security {
    secret-key = ""          # See "The secret key" below
}

redis {
    host = "localhost"
    port = "6379"
    password = ""
}

# The server every player is locked to until they authenticate.
auth-server = "lobby"

sql {
    host = "localhost"
    port = 3306
    database = "mlogin"
    username = "root"
    password = ""
}

network-redirect {
    enabled = false          # Send players to a server after they log in
    server = "lobby"
}

mojang-check {
    enabled = true           # Auto-detect premium accounts and skip auth for them
}

rate-limit {
    max-attempts = 5             # Failed attempts before a block kicks in
    attempt-window-seconds = 60  # Window in which those attempts are counted
    base-block-seconds = 30      # First block duration
    max-block-seconds = 900      # Cap for the block duration
    strike-window-seconds = 3600 # Window in which repeated blocks escalate
}
```

### Backend — `plugins/mLogin/config.yml`

```yaml
secret-key: ""               # Must be identical to the proxy's security.secret-key

redis:
    host: localhost
    port: 6379
    password: ""

settings:
    debug: false             # Log all attempts, commands and Redis queries
    update-check: true
    language: en             # en, es or fr

protection:
    general-protection: true # Freeze/block players until they log in
    effects:
        blindness: true
        slowness: true

password:
    min: 8
    max: 32
```

---

## The secret key

Passwords are encrypted before leaving a backend and decrypted on the proxy. Both sides derive the AES key from the **same shared secret**, so the value must be **identical** in:

- `security.secret-key` in the proxy's `config.conf`
- `secret-key` in the backend's `config.yml`

If they don't match, decryption fails and **nobody can log in**. On first run the proxy generates a random key for you — copy that exact value into every backend's `config.yml`.

The secret can be any length; it's hashed to a fixed-size AES key internally, so you don't need to worry about matching a specific character count.

---

## The auth-server gate

In a multi-backend network, the proxy is the real security boundary. Set `auth-server` to the name of your login server (as it appears in Velocity's `velocity.toml`).

While a player is **not** authenticated:

- They can only be on the `auth-server`.
- Any attempt to reach another backend — initial join, `/server`, plugin-initiated moves, or a proxy fallback — is redirected back to the `auth-server`.

This **fails closed**: if `auth-server` is empty or points to a server that doesn't exist, connections are denied rather than letting an unauthenticated player through. If you run a single backend you can leave it empty, and mLogin falls back to `network-redirect.server`.

---

## Commands

### Player commands (backend)

| Command | Description |
|---|---|
| `/register <password> <confirmPassword>` | Create an account. |
| `/login <password>` | Log in to an existing account. |
| `/changepass <oldPassword> <newPassword> <confirmNewPassword>` | Change your password (must be logged in). |

### Admin commands

| Command | Where | Permission | Description |
|---|---|---|---|
| `/unregister <player>` | Backend | `mlogin.admin` (or OP) | Delete a player's account. |
| `/mlogin backup` | Proxy | `mlogin.admin` or `mlogin.backup` | Create a database backup. |
| `/mlogin restore <file.sql>` | Proxy | `mlogin.admin` or `mlogin.backup` | Restore a database backup. |

---

## Permissions

| Permission | Grants |
|---|---|
| `mlogin.admin` | All admin commands (unregister, backup/restore). |
| `mlogin.command.unregister` | Access to `/unregister`. |
| `mlogin.backup` | Access to `/mlogin backup` and `/mlogin restore`. |

---

## Languages

Set `settings.language` on the backend to `en`, `es` or `fr`. Messages load from the matching `messages_<lang>.yml`, which you can edit freely. Color codes with `&` are supported.

---

## Troubleshooting

- **Nobody can log in / decryption errors** — The secret key doesn't match between proxy and backend. Copy the proxy's key into every backend's `config.yml` exactly.
- **Plugin disables on backend startup** — The backend's `secret-key` is empty. Copy it from the proxy.
- **Players land on other servers without logging in** — Check `auth-server` matches your login server's name in `velocity.toml`.
- **Login attempts hang for ~5 seconds then fail** — The proxy or backend can't reach Redis. Verify Redis host/port/password on both sides.
- **Legit players getting rate-limited** — Loosen the `rate-limit` values in the proxy config.

---

## Support

- Discord: https://discord.gg/VXsGUyR5wA
- Author: marioogg