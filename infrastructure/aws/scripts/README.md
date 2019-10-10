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

* Configure your AWS CLI

Make sure AWS CLI is configured with your access and secret keys. The below command will help you to provide the details for your aws setup.

```
aws configure
aws configure --profile dev
```
### Run Script

Run the below command to create network infrastructure, you should input profile name when it appears on the terminal
```
./csye6225-aws-networking-setup.sh VPC_NAME VPC_CIDR SUBNET_PUBLIC_CIDR1 SUBNET_PUBLIC_CIDR2 SUBNET_PUBLIC_CIDR3
```
example: 
```
./csye6225-aws-networking-setup.sh dev1 10.0.0.0/16 10.0.1.0/24 10.0.2.0/24 10.0.3.0/24
```

Run the below command to teardown network infrastructure, you should input profile name and VPC name when it appears on the terminal
```
./csye6225-aws-networking-setup.sh
```

