# AWS PROFILE
provider "aws" {
  profile    = "${var.aws_profile}"
  region     = "${var.aws_region}"
}

# VPC
resource "aws_vpc" "default" {
  cidr_block           = "${var.vpc_cidr_block}"
  EnableDnsHostnames = true
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

# Securtiy Group
resource "aws_security_group" "default" {
  vpc_id      = "${aws_vpc.default.id}"

  tags = {
    Name = "sg-${var.vpc_name}"
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
    cidr_blocks = ["10.0.0.0/16"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  # DB rules
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }
  # outbound internet access
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Key pair
resource "aws_key_pair" "auth" {
  key_name   = "${var.key_name}"
  public_key = "${file(var.public_key_path)}"
}

# RDS instance
resource "aws_db_instance" "default" {
  engine               = "mysql"
  engine_version       = "5.7"
  instance_class       = "db.t2.medium"
  multi_az             = false
  identifier           = "csye6225-fall2019"
  username             = "dbuser"
  password             = "QWE123qwe!@#"
  db_subnet_group_name = 
  publicly_accessible  = true
  name                 = "csye6225"
}


# S3 Bucket
resource "aws_s3_bucket" "webapp.${var.domain_name}" {
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

resource "aws_kms_key" "mykey" {
  description             = "This key is used to encrypt bucket objects"
  deletion_window_in_days = 10
}


# DynamoDB Table


# EC2 instance
resource "aws_instance" "web" {
  connection {
    # The default username for our AMI
    user = "ubuntu"
    host = "${self.public_ip}"
    # The connection will use the local SSH agent for authentication.
  }

  instance_type           = "t2.micro"
  disable_api_termination = false
  name                    = "csye6225-ec2"

  # custom ami
  ami = "${lookup(var.aws_amis, var.aws_region)}"

  # The name of our SSH keypair we created above.
  key_name = "${aws_key_pair.auth.id}"

  # Our Security group to allow HTTP and SSH access
  vpc_security_group_ids = ["${aws_security_group.default.id}"]

  subnet_id = "${aws_subnet.default.id}"

  root_block_device = [
    {
      volume_type = "gp2"
      volume_size = 20
    },
  ]

  provisioner "remote-exec" {
    inline = [
      "sudo apt-get -y update",
      "sudo apt-get -y install nginx",
      "sudo service nginx start",
    ]
  }

  tags = {
    "Env"      = "Private"
    "Location" = "Secret"
  }
}

