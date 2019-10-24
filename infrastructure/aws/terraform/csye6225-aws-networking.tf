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

# Securtiy Group - Application
resource "aws_security_group" "application" {
  vpc_id      = "${aws_vpc.default.id}"

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

  # HTTP access from the VPC
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
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

# Securtiy Group - Databse
resource "aws_security_group" "database" {
  vpc_id      = "${aws_vpc.default.id}"

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

  ingress {
    from_port   = 5432
    to_port     = 5432
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
  subnet_ids = ["${aws_subnet.public.0.id}","${aws_subnet.public.1.id}","${aws_subnet.public.2.id}"]

  tags = {
    Name = "My DB subnet group"
  }
}

# RDS instance
resource "aws_db_instance" "default" {
  allocated_storage    = 20
  engine               = "mysql"
  engine_version       = "8.0.16"
  instance_class       = "db.t2.medium"
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
resource "aws_instance" "web" {
  # connection {
  #   # The default username for our AMI
  #   user = "centos"
  #   host = "${self.public_ip}"
  #   # The connection will use the local SSH agent for authentication.
  #   private_key = "${file("")}"
  # }

  instance_type           = "t2.micro"
  disable_api_termination = false
  ami = "${var.ami_id}"


  key_name = "${aws_key_pair.auth.id}"

  # Our Security group to allow HTTP and SSH access
  vpc_security_group_ids = ["${aws_security_group.application.id}"]

  subnet_id = "${aws_subnet.public.0.id}"

  ebs_block_device {
      device_name           = "/dev/sda1"  
      delete_on_termination = true
  }

  root_block_device {
      volume_type = "gp2"
      volume_size = 20
  }

  # This EC2 instance must be created only after the RDS instance has been created.
  depends_on = [aws_db_instance.default]

  tags = {
    Name       = "csye6225-ec2"
    Enironment = "${var.aws_profile}"
  }
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

  tags = {
    Name        = "dynamodb-table-1"
  }
}
