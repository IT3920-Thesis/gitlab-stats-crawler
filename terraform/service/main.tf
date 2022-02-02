// This is responsible for setting up any resources outside our own
terraform {
  required_version = "~> 1.0"

  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "3.70.0"
    }
  }

  backend "s3" {
    bucket = "lokalvert-terraform-state"
    key    = "gitlab-stats-crawler/service.terraform.tfstate"
    region = "us-east-1"
    profile = "linio"
  }
}

locals {
  application_name = "gitlab-stats-crawler"
  environment = "service"

  tags = {
    "managedBy" = "terraform"
    "application" = local.application_name
  }
}

provider "aws" {
  profile = "linio"
  region = "us-east-1"
}

resource "aws_ecr_repository" "image" {
  name = local.application_name
  image_tag_mutability = "MUTABLE"

  tags = local.tags

  image_scanning_configuration {
    scan_on_push = false
  }
}
