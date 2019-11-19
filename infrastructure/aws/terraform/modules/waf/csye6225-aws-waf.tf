# AWS PROFILE
provider "aws" {
  profile    = "${var.aws_profile}"
  region     = "${var.aws_region}"
}

resource "aws_wafregional_web_acl_association" "default" {
  resource_arn = "${var.aws_lb_arn}" # ARN of the ALB
  web_acl_id   = "${var.web_acl_id}"
}
