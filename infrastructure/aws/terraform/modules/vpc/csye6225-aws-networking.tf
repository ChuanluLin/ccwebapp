# AWS PROFILE
provider "aws" {
  profile    = "${var.aws_profile}"
  region     = "${var.aws_region}"
}

# VPC
resource "aws_vpc" "default" {
  cidr_block           = "${var.vpc_cidr_block}"
  enable_dns_hostnames = true
  tags = {
    Name = "vpc-${var.vpc_name}"
  }
}

# DEFAULT INTERNET GATEWAY
resource "aws_internet_gateway" "default" {
  vpc_id = "${aws_vpc.default.id}"
  
  tags = {
    Name = "igw-${var.vpc_name}"
  }
}

# PUBLIC SUBNETS
resource "aws_subnet" "public" {
  vpc_id = "${aws_vpc.default.id}"

  count                   = "${var.subnet_count}"
  cidr_block              = "${element(list(var.subnet1_cidr_block, var.subnet2_cidr_block, var.subnet3_cidr_block), count.index)}"
  availability_zone       = "${element(list(var.availability_zone1, var.availability_zone2, var.availability_zone3), count.index)}"
  map_public_ip_on_launch = true

  tags = {
    Name               = "public${count.index}-${var.vpc_name}"
  }
}

# PUBLIC SUBNETS - Default Route
resource "aws_route_table" "public" {
  vpc_id = "${aws_vpc.default.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.default.id}"
  }

  tags = {
    Name = "publicrt-${var.vpc_name}"
  }
}

# PUBLIC SUBNETS - Route associations
resource "aws_route_table_association" "public" {
  count          = "${var.subnet_count}"
  subnet_id      = "${element(aws_subnet.public.*.id, count.index)}"
  route_table_id = "${aws_route_table.public.id}"
}

# IAM Policy
resource "aws_iam_policy" "policy3" {
  name        = "circleci-ec2-ami"
  description = "Allows CircleCI to use EC2 instance."

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [{
      "Effect": "Allow",
      "Action" : [
        "ec2:AttachVolume",
        "ec2:AuthorizeSecurityGroupIngress",
        "ec2:CopyImage",
        "ec2:CreateImage",
        "ec2:CreateKeypair",
        "ec2:CreateSecurityGroup",
        "ec2:CreateSnapshot",
        "ec2:CreateTags",
        "ec2:CreateVolume",
        "ec2:DeleteKeyPair",
        "ec2:DeleteSecurityGroup",
        "ec2:DeleteSnapshot",
        "ec2:DeleteVolume",
        "ec2:DeregisterImage",
        "ec2:DescribeImageAttribute",
        "ec2:DescribeImages",
        "ec2:DescribeInstances",
        "ec2:DescribeInstanceStatus",
        "ec2:DescribeRegions",
        "ec2:DescribeSecurityGroups",
        "ec2:DescribeSnapshots",
        "ec2:DescribeSubnets",
        "ec2:DescribeTags",
        "ec2:DescribeVolumes",
        "ec2:DetachVolume",
        "ec2:GetPasswordData",
        "ec2:ModifyImageAttribute",
        "ec2:ModifyInstanceAttribute",
        "ec2:ModifySnapshotAttribute",
        "ec2:RegisterImage",
        "ec2:RunInstances",
        "ec2:StopInstances",
        "ec2:TerminateInstances"
      ],
      "Resource" : "*"
  }]
}
EOF
}

# IAM Policy Attachment
resource "aws_iam_user_policy_attachment" "attach3" {
  user       = "circleci"
  policy_arn = "${aws_iam_policy.policy3.arn}"
}