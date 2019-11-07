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
You should set availability zones, aws profile, aws region, subnet cider blocks, vpc cidr block, vpc name, domain name, ami id, key name, public key path
example: devVars.tfvars
```
availability_zone1 = "us-east-1a"
availability_zone2 = "us-east-1b"
availability_zone3 = "us-east-1c"
aws_profile = "dev"
aws_region = "us-east-1"
subnet1_cidr_block = "10.0.1.0/24"
subnet2_cidr_block = "10.0.2.0/24"
subnet3_cidr_block = "10.0.3.0/24"
vpc_cidr_block = "10.0.0.0/16"
vpc_name = "dev"


domain_name = "*.me"
ami_id = "ami-*************"
key_name = "key"
public_key_path = "~/key.pub"

aws_access_key = "*********"
aws_secret_key = "************"
tomcat_log_dir = "/home/centos"

```

If you havn't yet created an ssh key, it can be done with the following command:
NOTE: REMEMBER THE PASSWORD YOU SET AND NEVER SHARE YOUR PRIVATE KEY TO ANYONE, INCLUDING AWS
```
ssh-keygen
```

The command will create a private key and public key. 
Public key should be set as terraform variable values and uploaded to AWS Key Pair.
Private key should be store in a safe place, set as read-only and NEVER SHARE to others.
```
chmod 400 key
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
NOTE: If you want to create multiple VPCs, please copy csye6225-aws-networking.tf, devVars.tfvars, outputs.tf and variables.tf to another folder and run all scripts above again.
