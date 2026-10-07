# Infrastructure (Terraform)

One Hetzner Cloud CX23 VPS running the whole stack under Docker Compose
(Caddy -> nginx frontend -> API, Postgres, data-job). Terraform **bootstraps the
machine only**; app versions are deployed by `.github/workflows/deploy.yml`.

## Resources

- `hcloud_server` (Ubuntu 24.04, `prevent_destroy`, `ignore_changes = [user_data, ...]`)
- `hcloud_firewall` (inbound 22, 80, 443 only)
- `hcloud_ssh_key` (public key only)
- DNS: manual. Point an A/AAAA record for `domain` at the `server_ipv4`/`server_ipv6` outputs.

cloud-init installs Docker (json-file log rotation), creates the `deploy` user,
enables unattended-upgrades, adds 1 GB swap, and places `compose.yaml`, `deploy/Caddyfile`
and `.env` in `/opt/whos-hot`. The stack is started by the first deploy run.

## Usage

```sh
cd infra
cp terraform.tfvars.example terraform.tfvars   # edit
export TF_VAR_hcloud_token=...  TF_VAR_db_password=...   # db password lives in Bitwarden
terraform init && terraform plan && terraform apply
```

## State

State is **local and gitignored**. It contains secrets (the DB password via user_data);
back it up privately (e.g. Bitwarden attachment). Losing it means importing resources by hand.
CI only runs `fmt`/`validate`; never `apply` from CI while state is local.

## GitHub setup (environment `production`)

Secrets: `DEPLOY_HOST` (server IP), `DEPLOY_SSH_KEY` (private key of the deploy user),
`GHCR_READ_TOKEN` (PAT with `read:packages`; optional if packages are public).
If `ssh_allowed_cidrs` is restricted, GitHub-hosted runners cannot deploy.

## Deploy and rollback

Merging to `master` runs tests, builds `api`, `data-job` and `frontend` images tagged with
the commit SHA and `latest`, then sets `IMAGE_TAG` on the server and runs
`docker compose --profile prod pull && up -d`. It fails early if `DB_PASSWORD` is missing,
and smoke-tests `/` and `/api/search/all`. Roll back with **Run workflow** and an older SHA as `tag`.

Each deploy also copies the checked-out `compose.yaml` and `deploy/Caddyfile` to the server.
