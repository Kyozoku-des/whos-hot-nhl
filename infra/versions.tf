terraform {
  required_version = ">= 1.6"

  required_providers {
    hcloud = {
      source  = "hetznercloud/hcloud"
      version = "~> 1.49"
    }
  }

  # Local state on purpose (see README): it contains secrets, so it is gitignored.
}

provider "hcloud" {
  token = var.hcloud_token
}
