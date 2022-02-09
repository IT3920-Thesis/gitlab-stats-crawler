variable "gitlab_access_token" {
  type = string
  sensitive = true
  description = "Access token which grants API and read_repository access to repositories"
}
