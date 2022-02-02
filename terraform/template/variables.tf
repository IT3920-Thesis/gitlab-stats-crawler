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

variable "tags" {
  type = map(string)
}
