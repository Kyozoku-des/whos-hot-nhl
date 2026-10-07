variable "hcloud_token" {
  description = "Hetzner Cloud API token (read/write)."
  type        = string
  sensitive   = true
}

variable "name" {
  description = "Name prefix for all resources."
  type        = string
  default     = "whos-hot-nhl"
}

variable "server_type" {
  type    = string
  default = "cx23"
}

variable "location" {
  type    = string
  default = "nbg1"
}

variable "ssh_public_key" {
  description = "Public key for the deploy user and Hetzner SSH key. Never the private key."
  type        = string
}

variable "ssh_allowed_cidrs" {
  description = "CIDRs allowed to reach SSH. Restrict to your own IP where practical. GitHub Actions runners need access too, so a restricted list will block CI deploys."
  type        = list(string)
  default     = ["0.0.0.0/0", "::/0"]
}

variable "domain" {
  description = "Public site address Caddy serves (e.g. whoshot.example.com). Use \":80\" for plain HTTP until a domain exists."
  type        = string
  default     = ":80"
}

variable "db_password" {
  description = "Postgres password, owned by the maintainer (e.g. Bitwarden). Not generated here."
  type        = string
  sensitive   = true
}

variable "ghcr_owner" {
  description = "Lowercase GitHub owner of the GHCR images."
  type        = string
  default     = "kyozoku-des"
}

variable "repo_raw_url" {
  description = "Raw URL base the server downloads compose.yaml and deploy/Caddyfile from."
  type        = string
  default     = "https://raw.githubusercontent.com/Kyozoku-des/whos-hot-nhl/master"
}
