variable "application_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "ecr_image_url" {
  type = string
}

variable "image_tag" {
  type = string
  description = "The tag used to "
}

variable "gitlab_access_token" {
  type = string
  description = "Access token which grants API and read_repository access to repositories"
}

variable "tags" {
  type = map(string)
}
