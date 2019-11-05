variable "aws_profile" {
  description = "The AWS profile name"
}

variable "aws_region" {
  description = "The AWS region"
}

variable "vpc_name" {
  description = "The name of the VPC"
}

variable "vpc_cidr_block" {
  description = "The VPC CIDR block (x.x.x.x/x)"
}

variable "subnet_count" {
  default = "3"
}

variable "subnet1_cidr_block" {
  description = "The CIDR block for the 1st Subnet (x.x.x.x/x)"
}

variable "subnet2_cidr_block" {
  description = "The CIDR block for the 2nd Subnet (x.x.x.x/x)"
}

variable "subnet3_cidr_block" {
  description = "The CIDR block for the 3rd Subnet (x.x.x.x/x)"
}

variable "availability_zone1" {
  description = "The availability zone for the 1st Subnet."
}

variable "availability_zone2" {
  description = "The availability zone for the 2nd Subnet."
}

variable "availability_zone3" {
  description = "The availability zone for the 3rd Subnet."
}
