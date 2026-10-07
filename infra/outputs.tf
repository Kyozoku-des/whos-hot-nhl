output "server_ipv4" {
  description = "Point the DNS A record here and use it as the DEPLOY_HOST GitHub secret."
  value       = hcloud_server.app.ipv4_address
}

output "server_ipv6" {
  value = hcloud_server.app.ipv6_address
}
