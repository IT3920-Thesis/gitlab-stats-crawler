resource "random_password" "database_password" {
  length = 64
  special = false
}


resource "aws_db_instance" "database" {
  allocated_storage = 20
  apply_immediately = true
  auto_minor_version_upgrade = true
  engine = "postgres"
  engine_version = "14.1"
  publicly_accessible = true
  instance_class = "db.t4g.small"
  name = replace("${var.application_name}${var.environment}", "-", "")
  username = "gitlabcrawler"
  password = random_password.database_password.result
  skip_final_snapshot = true
}

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
  role = aws_iam_role.application_image_builder.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"
}

locals {
  // We require SSL but aren't currently validating the actual certificate
  // (We need to provide the certificate if we want to validate it)
  database_uri = "jdbc:postgresql://${aws_db_instance.database.endpoint}/${aws_db_instance.database.name}?stringtype=unspecified&sslmode=require"
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

        runtime_environment_variables = {
          DATABASE_USER = aws_db_instance.database.username
          DATABASE_PASSWORD = random_password.database_password.result
          DATABASE_URI = local.database_uri
          GITLAB_ACCESS_TOKEN = var.gitlab_access_token
          RABBITMQ_USER = random_string.rabbitmq_user.result
          RABBITMQ_PASSWORD = random_password.rabbitmq_password.result
          RABBITMQ_HOST = aws_mq_broker.rabbitmq.instances[0].endpoints[0]
          COURSE_ROOT_GROUP_PATH = "it3920-gitlab-projects-examples"
        }
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

resource "aws_secretsmanager_secret" "database_uri" {
  name = "${var.application_name}-${var.environment}/database_uri"
  recovery_window_in_days = 0
  force_overwrite_replica_secret = true
  tags = var.tags
}
resource "aws_secretsmanager_secret_version" "database_uri" {
  secret_id = aws_secretsmanager_secret.database_uri.id
  secret_string = local.database_uri
}

resource "aws_secretsmanager_secret" "database_user" {
  name = "${var.application_name}-${var.environment}/database_user"
  recovery_window_in_days = 0
  force_overwrite_replica_secret = true
  tags = var.tags
}
resource "aws_secretsmanager_secret_version" "database_user" {
  secret_id = aws_secretsmanager_secret.database_user.id
  secret_string = aws_db_instance.database.username
}

resource "aws_secretsmanager_secret" "database_password" {
  name = "${var.application_name}-${var.environment}/database_password"
  recovery_window_in_days = 0
  force_overwrite_replica_secret = true
  tags = var.tags
}
resource "aws_secretsmanager_secret_version" "database_password" {
  secret_id = aws_secretsmanager_secret.database_password.id
  secret_string = random_password.database_password.result
}

resource "random_string" "rabbitmq_user" {
  special = false
  length = 20
}

resource "random_password" "rabbitmq_password" {
  special = false
  length = 40
}

resource "aws_mq_broker" "rabbitmq" {
  broker_name = "${var.application_name}-${var.environment}"

  engine_type = "RabbitMQ"
  engine_version = "3.9.13"
  host_instance_type = "mq.t3.micro" // This is free tier

  apply_immediately = true
  auto_minor_version_upgrade = true
  deployment_mode = "SINGLE_INSTANCE"
  publicly_accessible = true

  user {
    username = random_string.rabbitmq_user.result
    password = random_password.rabbitmq_password.result
  }

  tags = var.tags
}

resource "aws_secretsmanager_secret" "rabbitmq_password" {
  name = "${var.application_name}-${var.environment}/rabbitmq_password"
  recovery_window_in_days = 0
  force_overwrite_replica_secret = true
  tags = var.tags
}
resource "aws_secretsmanager_secret_version" "rabbitmq_password" {
  secret_id = aws_secretsmanager_secret.rabbitmq_password.id
  secret_string = random_password.rabbitmq_password.result
}
resource "aws_secretsmanager_secret" "rabbitmq_username" {
  name = "${var.application_name}-${var.environment}/rabbitmq_username"
  recovery_window_in_days = 0
  force_overwrite_replica_secret = true
  tags = var.tags
}
resource "aws_secretsmanager_secret_version" "rabbitmq_username" {
  secret_id = aws_secretsmanager_secret.rabbitmq_username.id
  secret_string = random_string.rabbitmq_user.result
}
