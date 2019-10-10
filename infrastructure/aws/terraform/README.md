# CSYE 6225 Infrastructure

This folder deals with the Infrastructure configs in AWS

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You would require:

* VirtualBox or VMWare Fusion
* Ubuntu Linux VM
* Pip
* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/awscli-install-linux.html)

```
sudo apt-get install python-pip
```

* AWS CLI Configuration

Make sure AWS CLI is configured with your access and secret keys. The below command will help you to provide the details for your aws setup.

```
aws configure
aws configure --profile dev
```

* [Terraform](https://www.terraform.io/)

Make sure install Terraform before running scripts in this folder.

### Run Script

Before running the script you should set all variables at files with suffix with \*.tfvars.
You should set availability zones, aws profile, aws region, subnet cider blocks, vpc cidr block, vpc name.
example: prodVars.tfvars
```
availability_zones = "us-east-2b,us-east-2c,us-east-2d"
aws_profile = "prod"
aws_region = "us-east-2"
subnet_cidr_blocks = "10.2.0.0/24,10.2.4.0/24,10.2.6.0/24"
vpc_cidr_block = "10.2.0.0/16"
vpc_name = "prod"

```

Run the below command to initialize terraform. THIS STEP IS NECESSARY WHEN USING TERRAFORM.
```
terraform init
```

Run the below command to test whether all variables are accepted; variables can be input manually or read from file. 
```
terraform plan
terraform plan -var-file=devVars.tfvars
```

Run the below command to create network infrastructure; variables can be input manually or read from file. 
```
terraform apply
terraform apply -var-file=devVars.tfvars
```

Run the below command to teardown network infrastructure, variables can be input manually or read from file. 
```
terraform destroy
terraform destroy -var-file=devVars.tfvars
```

