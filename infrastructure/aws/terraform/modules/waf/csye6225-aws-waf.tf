# AWS PROFILE
provider "aws" {
  profile    = "${var.aws_profile}"
  region     = "${var.aws_region}"
}

#Cloudformation stack for AWS WAF
resource "aws_cloudformation_stack" "waf" {
  name = "waf-stack"
  template_url = "https://s3.amazonaws.com/codedeploy.${var.domain_name}/owasp_10_base.yml"
}

resource "aws_wafregional_web_acl_association" "default" {
  resource_arn = "${var.aws_lb_arn}" # ARN of the ALB
  web_acl_id   = "${var.web_acl_id}"
}
