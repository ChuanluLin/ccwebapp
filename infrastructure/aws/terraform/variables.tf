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

variable "subnet_cidr_blocks" {
  description = "A comma-delimited list of CIDR block for the Subnets (x.x.x.x/x)"
}

variable "availability_zones" {
  description = "A comma-delimited list of availability zones for the VPC."
}