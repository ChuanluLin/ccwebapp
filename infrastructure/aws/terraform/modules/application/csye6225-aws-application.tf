# AWS PROFILE
provider "aws" {
  profile    = "${var.aws_profile}"
  region     = "${var.aws_region}"
}

data "aws_caller_identity" "current" { }

# Securtiy Group - Application
resource "aws_security_group" "application" {
  vpc_id      = "${var.aws_vpc_id}"

  tags = {
    Name = "application"
  }

  # SSH access from anywhere
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTP access from the load balancer
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    security_groups = ["${aws_security_group.loadbalancer.id}"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    security_groups = ["${aws_security_group.loadbalancer.id}"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    security_groups = ["${aws_security_group.loadbalancer.id}"]
  }

  # outbound internet access
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Securtiy Group - Databse
resource "aws_security_group" "database" {
  vpc_id      = "${var.aws_vpc_id}"

  tags = {
    Name = "database"
  }

  # DB rules
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    security_groups = ["${aws_security_group.application.id}"]
  }

  # outbound internet access
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Securtiy Group - Load Balancer
resource "aws_security_group" "loadbalancer" {
  vpc_id      = "${var.aws_vpc_id}"

  tags = {
    Name = "loadbalancer"
  }

  # LB rules
  # HTTPS access from the anywhere
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # outbound internet access
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

#AWS KMS Key
resource "aws_kms_key" "mykey" {
  description             = "This key is used to encrypt bucket objects"
  deletion_window_in_days = 10
}

# S3 Bucket
resource "aws_s3_bucket" "webapp" {
  bucket        = "webapp.${var.domain_name}"
  acl           = "private"
  force_destroy = true

  tags = {
    Name        = "webapp.${var.domain_name}"
  }

  lifecycle_rule {
    id      = "log"
    enabled = true

    prefix = "log/"

    tags = {
      "rule"      = "log"
      "autoclean" = "true"
    }

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }

    expiration {
      days = 90
    }
  }

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        kms_master_key_id = "${aws_kms_key.mykey.arn}"
        sse_algorithm     = "aws:kms"
      }
    }
  }
}

# RDS Subnet Group
resource "aws_db_subnet_group" "default" {
  name       = "main"
  subnet_ids = ["${var.subnet_id1}","${var.subnet_id2}","${var.subnet_id3}"]

  tags = {
    Name = "My DB subnet group"
  }
}

# RDS instance
resource "aws_db_instance" "default" {
  allocated_storage    = 20
  engine               = "mysql"
  engine_version       = "8.0.16"
  instance_class       = "db.t2.micro"
  multi_az             = false
  identifier           = "csye6225-fall2019"
  username             = "dbuser"
  password             = "Qwer123!"
  db_subnet_group_name = "${aws_db_subnet_group.default.name}"
  publicly_accessible  = true
  name                 = "csye6225"
  vpc_security_group_ids  = ["${aws_security_group.database.id}"]
  skip_final_snapshot  = true
}

# Key pair
resource "aws_key_pair" "auth" {
  key_name   = "${var.key_name}"
  public_key = "${file(var.public_key_path)}"
}

# EC2 instance
# resource "aws_instance" "web" {
#   # connection {
#   #   # The default username for our AMI
#   #   user = "centos"
#   #   host = "${self.public_ip}"
#   #   # The connection will use the local SSH agent for authentication.
#   #   private_key = "${file("")}"
#   # }

#   instance_type           = "t2.micro"
#   disable_api_termination = false
#   ami = "${var.ami_id}"

#   # The name of our SSH keypair we created above.
#   key_name = "${aws_key_pair.auth.id}"

#   # Our Security group to allow HTTP and SSH access
#   vpc_security_group_ids = ["${aws_security_group.application.id}"]

#   subnet_id = "${var.subnet_id1}"

#   ebs_block_device {
#       device_name           = "/dev/sda1"  
#       delete_on_termination = true
#   }

#   root_block_device {
#       volume_type = "gp2"
#       volume_size = 20
#   }

#   # This EC2 instance must be created only after the RDS instance has been created.
#   depends_on = [aws_db_instance.default]

#   # IAM
#   iam_instance_profile = "${aws_iam_instance_profile.codedeployec2.name}"

#   # user_data  = "${file("ec2_user_data.sh")}"
#   user_data = <<-EOF
#           #! /bin/bash
#           echo export DB_ENDPOINT=${aws_db_instance.default.endpoint}>>/etc/profile
#           echo export DB_USER=${aws_db_instance.default.username}>>/etc/profile
#           echo export DB_PASSSWORD='${aws_db_instance.default.password}'>>/etc/profile
#           echo export AWS_ACCESS_KEY=${var.aws_access_key}>>/etc/profile
#           echo export AWS_SECRET_KEY=${var.aws_secret_key}>>/etc/profile
#           echo export AWS_BUCKET_NAME=webapp.${var.domain_name}>>/etc/profile
#           echo export TOMCAT_LOG_DIR=${var.tomcat_log_dir}>>/etc/profile
#   EOF

#   tags = {
#     Name       = "csye6225-ec2"
#     Enironment = "${var.aws_profile}"
#   }
# }

resource "aws_iam_instance_profile" "codedeployec2" {
  name = "CodeDeployEC2ServiceRoleProfile"
  role = "${aws_iam_role.codedeployec2role.name}"
}

# DynamoDB Table
resource "aws_dynamodb_table" "basic-dynamodb-table" {
  name           = "csye6225"
  billing_mode   = "PROVISIONED"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }

  tags = {
    Name        = "dynamodb-table-1"
  }
}

# S3 Bucket for CodeDeploy
resource "aws_s3_bucket" "codedeploy" {
  bucket        = "codedeploy.${var.domain_name}"
  acl           = "private"
  force_destroy = true

  tags = {
    Name        = "codedeploy.${var.domain_name}"
  }

  lifecycle_rule {
    id      = "cleanup"
    enabled = true

    prefix = "cleanup/"

    tags = {
      "rule"      = "cleanup"
      "autoclean" = "true"
    }

    expiration {
      days = 60
    }

    noncurrent_version_expiration {
      days = 1
    }
  }

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        kms_master_key_id = "${aws_kms_key.mykey.arn}"
        sse_algorithm     = "aws:kms"
      }
    }
  }
}

# IAM Policy
resource "aws_iam_policy" "policy1" {
  name        = "CircleCI-Upload-To-S3"
  description = "Allows CircleCI to upload artifacts from latest successful build to dedicated S3 bucket used by code deploy."

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject"
            ],
            "Resource": [
                "arn:aws:s3:::${aws_s3_bucket.codedeploy.bucket}",
                "arn:aws:s3:::${aws_s3_bucket.codedeploy.bucket}/*"
            ]
        }
    ]
}
EOF
}

resource "aws_iam_policy" "policy2" {
  name        = "CircleCI-Code-Deploy"
  description = "Allows CircleCI to call CodeDeploy APIs to initiate application deployment on EC2 instances."

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:RegisterApplicationRevision",
        "codedeploy:GetApplicationRevision"
      ],
      "Resource": [
        "arn:aws:codedeploy:${var.aws_region}:${data.aws_caller_identity.current.account_id}:application:csye6225-webapp"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:CreateDeployment",
        "codedeploy:GetDeployment"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:GetDeploymentConfig"
      ],
      "Resource": [
        "arn:aws:codedeploy:${var.aws_region}:${data.aws_caller_identity.current.account_id}:deploymentconfig:CodeDeployDefault.OneAtATime",
        "arn:aws:codedeploy:${var.aws_region}:${data.aws_caller_identity.current.account_id}:deploymentconfig:CodeDeployDefault.HalfAtATime",
        "arn:aws:codedeploy:${var.aws_region}:${data.aws_caller_identity.current.account_id}:deploymentconfig:CodeDeployDefault.AllAtOnce"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_policy" "policy4" {
  name        = "CodeDeploy-EC2-S3"
  description = "Allows EC2 instances to read data from S3 buckets. This policy is required for EC2 instances to download latest application revision."

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "s3:Get*",
                "s3:List*"
            ],
            "Effect": "Allow",
            "Resource": [
                "arn:aws:s3:::${aws_s3_bucket.codedeploy.bucket}",
                "arn:aws:s3:::${aws_s3_bucket.codedeploy.bucket}/*"
            ]
        }
    ]
}
EOF
}

# IAM Policy Attachment
resource "aws_iam_user_policy_attachment" "attach1" {
  user       = "circleci"
  policy_arn = "${aws_iam_policy.policy1.arn}"
}

resource "aws_iam_user_policy_attachment" "attach2" {
  user       = "circleci"
  policy_arn = "${aws_iam_policy.policy2.arn}"
}

# IAM Role
resource "aws_iam_role" "codedeployrole" {
  name        = "CodeDeployServiceRole"
  description = "Allows CodeDeploy to call AWS services such as Auto Scalling on your behalf."

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "codedeploy.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

# IAM Role Policy Attachment
resource "aws_iam_role_policy_attachment" "AWSCodeDeployRole" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole"
  role       = "${aws_iam_role.codedeployrole.name}"
}

# CodeDeploy Applcation
resource "aws_codedeploy_app" "default" {
  compute_platform = "Server"
  name = "csye6225-webapp"
}

#CodeDeployment config
resource "aws_codedeploy_deployment_config" "default" {
  deployment_config_name = "codeDeploy-config"

  minimum_healthy_hosts {
    type  = "HOST_COUNT"
    value = 1
  }
}

# CodeDeploy Deployment Group
resource "aws_codedeploy_deployment_group" "default" {
  app_name              = "${aws_codedeploy_app.default.name}"
  deployment_config_name = "${aws_codedeploy_deployment_config.default.deployment_config_name}"
  deployment_group_name = "csye6225-webapp-deployment"
  service_role_arn      = "${aws_iam_role.codedeployrole.arn}"
  autoscaling_groups    = ["${aws_autoscaling_group.default.name}"]

  ec2_tag_set {
    ec2_tag_filter {
      key   = "Name"
      type  = "KEY_AND_VALUE"
      value = "csye6225-ec2"
    }
  }

  auto_rollback_configuration {
    enabled = true
    events  = ["DEPLOYMENT_FAILURE"]
  }
}

# IAM Role
resource "aws_iam_role" "codedeployec2role" {
  name        = "CodeDeployEC2ServiceRole"
  description = "Allows EC2 instances to call AWS services on your behalf."

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

# IAM Role Policy Attachment
resource "aws_iam_role_policy_attachment" "attach4" {
  policy_arn = "${aws_iam_policy.policy4.arn}"
  role       = "${aws_iam_role.codedeployec2role.name}"
}

# IAM Role Policy Attachment
resource "aws_iam_role_policy_attachment" "attachCloudWatch" {
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
  role       = "${aws_iam_role.codedeployec2role.name}"
}


## Assignment 8
# Lambda
resource "aws_iam_role" "lambda_exec_role" {
  name = "lambda_exec_role"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

# IAM Role Policy Attachment for Lambda Role
resource "aws_iam_role_policy_attachment" "attachlambda1" {
  policy_arn = "arn:aws:iam::aws:policy/AWSXrayWriteOnlyAccess"
  role       = "${aws_iam_role.lambda_exec_role.name}"
}

# IAM Role Policy Attachment for Lambda Role
resource "aws_iam_role_policy_attachment" "attachlambda2" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = "${aws_iam_role.lambda_exec_role.name}"
}

# IAM Role Policy Attachment for Lambda Role
resource "aws_iam_role_policy_attachment" "attachlambda3" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonSESFullAccess"
  role       = "${aws_iam_role.lambda_exec_role.name}"
}

# IAM Role Policy Attachment for Lambda Role
resource "aws_iam_role_policy_attachment" "attachlambda4" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
  role       = "${aws_iam_role.lambda_exec_role.name}"
}

resource "aws_sns_topic" "default" {
  name = "email_request"
}

resource "aws_lambda_function" "default" {
  filename      = "${var.lambda_function_path}"
  function_name = "snsandlambda"
  role          = "${aws_iam_role.lambda_exec_role.arn}"
  handler       = "lambda.Main::myHandler"
  source_code_hash = "${filebase64sha256("${var.lambda_function_path}")}"

  runtime = "java8"
  memory_size = 1024
  timeout = 10

  environment {
    variables = {
      DOMAIN = "prod.${var.domain_name}"
    }
  }
}

# Allow the SNS topic to invoke the Lambda
resource "aws_lambda_permission" "with_sns" {
  statement_id  = "AllowExecutionFromSNS"
  action        = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.default.function_name}"
  principal     = "sns.amazonaws.com"
  source_arn    = "${aws_sns_topic.default.arn}"
}

# Subscribe the Lambda to the SNS topic
resource "aws_sns_topic_subscription" "sns_trigger_lambda" {
  topic_arn = "${aws_sns_topic.default.arn}"
  protocol  = "lambda"
  endpoint  = "${aws_lambda_function.default.arn}"
}

# Auto Scaling Group
resource "aws_launch_configuration" "default" {
  name          = "asg_launch_config"
  image_id      = "${var.ami_id}"
  instance_type = "t2.micro"
  associate_public_ip_address = true	

  # The name of our SSH keypair we created above.
  key_name = "${aws_key_pair.auth.id}"

  # Our Security group to allow HTTP and SSH access
  security_groups = ["${aws_security_group.application.id}"]

  # This EC2 instance must be created only after the RDS instance has been created.
  depends_on = [aws_db_instance.default]

  iam_instance_profile = "${aws_iam_instance_profile.codedeployec2.name}"

  user_data = <<-EOF
          #! /bin/bash
          echo export DB_ENDPOINT=${aws_db_instance.default.endpoint}>>/etc/profile
          echo export DB_USER=${aws_db_instance.default.username}>>/etc/profile
          echo export DB_PASSSWORD='${aws_db_instance.default.password}'>>/etc/profile
          echo export AWS_ACCESS_KEY=${var.aws_access_key}>>/etc/profile
          echo export AWS_SECRET_KEY=${var.aws_secret_key}>>/etc/profile
          echo export AWS_BUCKET_NAME=webapp.${var.domain_name}>>/etc/profile
          echo export TOMCAT_LOG_DIR=${var.tomcat_log_dir}>>/etc/profile
  EOF
}

resource "aws_autoscaling_group" "default" {
  desired_capacity     = 3
  max_size             = 10
  min_size             = 3
  launch_configuration = "${aws_launch_configuration.default.name}"
  default_cooldown     = 60
  vpc_zone_identifier  = ["${var.subnet_id1}","${var.subnet_id2}","${var.subnet_id3}"]
  target_group_arns    = ["${aws_lb_target_group.default.arn}"]

  # add tags for the EC2 instances created
  tags = [
    {
      key                 = "Name"
      value               = "csye6225-ec2"
      propagate_at_launch = true
    }
  ]
}

# Auto Scaling Group Policy
resource "aws_autoscaling_policy" "CPUAlarmHighPolicy" {
  name                   = "CPUAlarmHighPolicy"
  scaling_adjustment     = 1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 60
  autoscaling_group_name = "${aws_autoscaling_group.default.name}"
}

resource "aws_autoscaling_policy" "CPUAlarmLowPolicy" {
  name                   = "CPUAlarmLowPolicy"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 60
  autoscaling_group_name = "${aws_autoscaling_group.default.name}"
}

resource "aws_cloudwatch_metric_alarm" "CPUAlarmHigh" {
  alarm_name          = "CPUAlarmHigh"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "60"
  statistic           = "Average"
  threshold           = "5"

  dimensions = {
    AutoScalingGroupName = "${aws_autoscaling_group.default.name}"
  }

  alarm_description = "Scale-up if CPU > 5% for 2 minutes"
  alarm_actions     = ["${aws_autoscaling_policy.CPUAlarmHighPolicy.arn}"]
}

resource "aws_cloudwatch_metric_alarm" "CPUAlarmLow" {
  alarm_name          = "CPUAlarmLow"
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "60"
  statistic           = "Average"
  threshold           = "3"

  dimensions = {
    AutoScalingGroupName = "${aws_autoscaling_group.default.name}"
  }

  alarm_description = "Scale-up if CPU < 3% for 2 minutes"
  alarm_actions     = ["${aws_autoscaling_policy.CPUAlarmLowPolicy.arn}"]
}

# Load Balancer
resource "aws_lb" "default" {
  name               = "csye6225-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = ["${aws_security_group.loadbalancer.id}"]
  subnets            = ["${var.subnet_id1}","${var.subnet_id2}","${var.subnet_id3}"]

  tags = {
    Environment = "${var.aws_profile}"
  }
}

resource "aws_lb_target_group" "default" {
  name     = "csye6225-lb-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = "${var.aws_vpc_id}"
}

resource "aws_lb_listener" "default" {
  load_balancer_arn = "${aws_lb.default.arn}"
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = "${var.certificate_arn}"

  default_action {
    type             = "forward"
    target_group_arn = "${aws_lb_target_group.default.arn}"
  }
}

# Route53 record
resource "aws_route53_record" "lb_record" {
  zone_id = "${var.route53_zone_id}"
  name    = "prod.${var.domain_name}"
  type    = "A"

  alias {
    name                   = "${aws_lb.default.dns_name}"
    zone_id                = "${aws_lb.default.zone_id}"
    evaluate_target_health = true
  }
}
