variable "aws_profile" {
  description = "The AWS profile name"
}

variable "aws_region" {
  description = "The AWS region"
}

variable "domain_name" {
  description = "The domain name"
}

variable "public_key_path" {
  description = <<DESCRIPTION
Path to the SSH public key to be used for authentication.
Ensure this keypair is added to your local SSH agent so provisioners can
connect.
Example: ~/.ssh/id_rsa.pub
DESCRIPTION
}

variable "key_name" {
  description = "Desired name of AWS key pair"
}

# variable "aws_amis" {
#   default = {
#     "us-east-1"="ami-9887c6e7", 
#     "us-east-2"="ami-9c0638f9"
#   }
# }

variable "ami_id" {
  description = "The Amazon Machine Image ID for launching the EC2 instance"
}

variable "aws_vpc_id" {
  description = "The id of vpc"
}

variable "subnet_id1" {
  description = "The id of subnet1"
}

variable "subnet_id2" {
  description = "The id of subnet2"
}

variable "subnet_id3" {
  description = "The id of subnet3"
}