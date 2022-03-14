output "database_uri" {
  sensitive = true
  value = local.database_uri
}

output "database_user" {
  sensitive = true
  value = aws_db_instance.database.username
}

output "database_password" {
  sensitive = true
  value = random_password.database_password.result
}
