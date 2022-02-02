
resource "aws_iam_role" "application_image_builder" {
  name = "${var.application_name}-${var.environment}-deploy"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Principal = {
          Service = [
            "build.apprunner.amazonaws.com",
            "tasks.apprunner.amazonaws.com",
          ]
        }
        Effect = "Allow"
        Sid = ""
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "application_image_builder" {
  role       = aws_iam_role.application_image_builder.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"
}

resource "aws_apprunner_service" "application" {
  service_name = "${var.application_name}-${var.environment}"

  health_check_configuration {
    interval = 20
    path = "/health"
    protocol = "HTTP"
    healthy_threshold = 2
    unhealthy_threshold = 3
    timeout = 2
  }

  source_configuration {
    auto_deployments_enabled = false

    image_repository {
      image_configuration {
        port = "8080"
      }
      image_identifier = "${var.ecr_image_url}:${var.image_tag}"
      image_repository_type = "ECR"
    }

    authentication_configuration {
      access_role_arn = aws_iam_role.application_image_builder.arn
    }
  }

  tags = var.tags
}
