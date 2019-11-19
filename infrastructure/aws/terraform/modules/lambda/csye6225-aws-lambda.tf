# AWS PROFILE
provider "aws" {
  profile    = "${var.aws_profile}"
  region     = "${var.aws_region}"
}

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
  filename      = "/Users/ftl/Documents/GitHub/lambda/target/lambda-1.0-SNAPSHOT.jar"
  function_name = "snsandlambda"
  role          = "${aws_iam_role.lambda_exec_role.arn}"
  handler       = "lambda.Main::myHandler"
  source_code_hash = "${filebase64sha256("/Users/ftl/Documents/GitHub/lambda/target/lambda-1.0-SNAPSHOT.jar")}"

  runtime = "java8"
  memory_size = 1024
  timeout = 10

  environment {
    variables = {
      foo = "bar"
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
  key_name      = "${var.aws_key_pair_id}"
  associate_public_ip_address = true	
  #user_data     = 
  #IAM Role      = 
  #security_groups = 
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
  #security_groups    = ["${aws_security_group.lb_sg.id}"]
  subnets            = ["${var.subnet_id1}","${var.subnet_id2}","${var.subnet_id3}"]

  tags = {
    Environment = "${var.aws_profile}"
  }
}

resource "aws_lb_target_group" "default" {
  name     = "csye6225-lb-tg"
  port     = 80
  protocol = "HTTP"
  vpc_id   = "${var.aws_vpc_id}"
}

resource "aws_lb_listener" "default" {
  load_balancer_arn = "${aws_lb.default.arn}"
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = "arn:aws:acm:us-east-1:350647533114:certificate/e0b4f882-7c2d-4cc0-aa50-13fee5f04651"

  default_action {
    type             = "forward"
    target_group_arn = "${aws_lb_target_group.default.arn}"
  }
}

# Route53 record
# resource "aws_route53_record" "lb_record" {
#   zone_id = "ZGVN288LIGABC"
#   name    = "prod.tianlifeng.me"
#   type    = "A"
#   ttl     = "300"
#   records = ["${aws_lb.default.public_ip}"]
# }

# CloudFormation
# resource "aws_cloudformation_stack" "waf" {
#   name = "waf-stack"
#   template_url = "https://s3.us-east-2.amazonaws.com/awswaf-owasp/owasp_10_base.yml"
#   timeout_in_minutes = 60
# }

# WAF and ALB Association
# resource "aws_wafregional_web_acl_association" "default" {
#   resource_arn = "${aws_lb.default.arn}"
#   web_acl_id   = "${aws_wafregional_web_acl.foo.id}"
# }