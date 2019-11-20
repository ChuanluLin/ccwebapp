# AWS PROFILE
provider "aws" {
  profile    = "${var.aws_profile}"
  region     = "${var.aws_region}"
}

# VPC module
module "vpc"{
  source = "./modules/vpc"
  # name = "csye6225-aws-networking"
  availability_zone1 = "${var.availability_zone1}"
  availability_zone2 = "${var.availability_zone2}"
  availability_zone3 = "${var.availability_zone3}"
  aws_profile = "${var.aws_profile}"
  aws_region = "${var.aws_region}"
  subnet1_cidr_block = "${var.subnet1_cidr_block}"
  subnet2_cidr_block = "${var.subnet2_cidr_block}"
  subnet3_cidr_block = "${var.subnet3_cidr_block}"
  vpc_cidr_block = "${var.vpc_cidr_block}"
  vpc_name = "${var.vpc_name}"
}

# Applicaiton module
module "app"{
  source = "./modules/application"
  # name = "csye6225-aws-application"
  aws_profile = "${var.aws_profile}"
  aws_region = "${var.aws_region}"

  domain_name = "${var.domain_name}"
  ami_id = "${var.ami_id}"
  key_name = "${var.key_name}"
  public_key_path = "${var.public_key_path}"
  certificate_arn = "${var.certificate_arn}"

  aws_vpc_id = module.vpc.vpc_id
  subnet_id1 = module.vpc.public_subnets_id1
  subnet_id2 = module.vpc.public_subnets_id2
  subnet_id3 = module.vpc.public_subnets_id3
  aws_access_key = "${var.aws_access_key}"
  aws_secret_key = "${var.aws_secret_key}"
  tomcat_log_dir = "${var.tomcat_log_dir}"
  lambda_function_path = "${var.lambda_function_path}"
}

# Applicaiton module
module "waf"{
  source = "./modules/waf"
  # name = "csye6225-aws-waf"
  aws_profile = "${var.aws_profile}"
  aws_region = "${var.aws_region}"
  domain_name = "${var.domain_name}"
  aws_lb_arn = module.app.aws_lb_arn
  web_acl_id = "${var.web_acl_id}"
}
