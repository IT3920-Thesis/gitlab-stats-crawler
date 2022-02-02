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
    key    = "gitlab-stats-crawler/staging.terraform.tfstate"
    region = "us-east-1"
    profile = "linio"
  }
}

locals {
  application_name = "gitlab-stats-crawler"
  environment = "staging"

  // Git tag is used as Docker image tag
  last_commit_sha = trimspace(file("../../.git/${trimspace(trimprefix(file("../../.git/HEAD"), "ref:"))}"))

  tags = {
    managedBy = "terraform"
    application = local.application_name
    environment = "staging"
  }
}

provider "aws" {
  profile = "linio"
  region = "us-east-1"
}

data "aws_ecr_repository" "image" {
  name = local.application_name
}

module "crawler" {
  source = "../template"

  application_name = local.application_name
  environment = local.environment
  tags = local.tags

  ecr_image_url = data.aws_ecr_repository.image.repository_url
  image_tag = local.last_commit_sha
}
