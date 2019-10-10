# AWS PROFILE
provider "aws" {
  profile    = "${var.aws_profile}"
  region     = "${var.aws_region}"
}

# VPC
resource "aws_vpc" "default" {
  cidr_block           = "${var.vpc_cidr_block}"

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
  cidr_block              = "${element(list(var.subnet1_cidr_block, var.subnet2_cidr_block, var.subnet1_cidr_block), count.index)}"
  availability_zone       = "${element(list(var.availability_zone1, var.availability_zone2, var.availability_zone3), count.index)}"
  # map_public_ip_on_launch = true

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