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


## Running the tests

Make sure AWS CLI is configured with your access and secret keys. The below command will help you to provide the details for your aws setup.

```
aws configure
aws configure --profile dev
```