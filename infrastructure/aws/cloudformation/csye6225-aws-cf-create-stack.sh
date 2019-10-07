#!/bin/bash
#******************************************************************************
#    AWS VPC Creation Shell Script
#******************************************************************************
echo "Enter profile name:"
read Profile_name
export AWS_PROFILE=$Profile_name

echo "Enter NetWork Stack Name:"
read STACK_NAME
AWS_Region=$(aws configure get region)
SUBNET_PUBLIC_CIDR="10.0.1.0/24"
SUBNET_PUBLIC_AZ=$AWS_Region"a"
SUBNET_PUBLIC_NAME="10.0.1.0"
SUBNET_PUBLIC_CIDR1="10.0.2.0/24"
SUBNET_PUBLIC_AZ1=$AWS_Region"b"
SUBNET_PUBLIC_NAME1="10.0.2.0"
SUBNET_PUBLIC_CIDR2="10.0.3.0/24"
SUBNET_PUBLIC_AZ2=$AWS_Region"c"
SUBNET_PUBLIC_NAME2="10.0.3.0"

#Create Stack
aws cloudformation create-stack --stack-name $STACK_NAME --template-body file://csye6225-cf-networking.json
#Check Stack Status
STACK_STATUS=`aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[][ [StackStatus ] ][]" --output text`

#Wait until stack completely created
echo "Please wait..."

while [ $STACK_STATUS != "CREATE_COMPLETE" ]
do
	STACK_STATUS=`aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[][ [StackStatus ] ][]" --output text`
done

#Find vpc Id
vpcId=`aws ec2 describe-vpcs --filter "Name=tag:Name,Values=$STACK_NAME" --query 'Vpcs[*].{id:VpcId}' --output text`
#Rename vpc
aws ec2 create-tags --resources $vpcId --tags "Key=Name,Value=$STACK_NAME-csye6225-vpc"

#Find public subnet
#pub_subnet=`aws ec2 describe-subnets --filter "Name=tag:Name,Values=pub_subnet" --query 'Vpcs[*].{id:VpcId}' --output text`
#Rename public subnet
#aws ec2 create-tags --resources $pub_subnet --tahs "Key=Name,Value="

#Find public subnet1
#pub_subnet1=`aws ec2 describe-subnets --filter "Name=tag:Name,Values=pub_subnet1" --query 'Vpcs[*].{id:VpcId}' --output text`
#Rename public subnet1
#aws ec2 create-subnet --resources $pub_subnet1 --vpc-id $vpcId --cidr-block $SUBNET_PUBLIC_CIDR1 --availability-zone $SUBNET_PUBLIC_AZ1

#Find public subnet2
#pub_subnet2=`aws ec2 describe-subnets --filter "Name=tag:Name,Values=pub_subnet2" --query 'Vpcs[*].{id:VpcId}' --output text`
#Rename public subnet2
#aws ec2 create-subnet --resources $pub_subnet2 --vpc-id $vpcId --cidr-block $SUBNET_PUBLIC_CIDR2 --availability-zone $SUBNET_PUBLIC_AZ2

#Find Internet Gateway
gatewayId=`aws ec2 describe-internet-gateways --filter "Name=tag:Name,Values=$STACK_NAME" --query 'InternetGateways[*].{id:InternetGatewayId}' --output text`
#Rename Internet Gateway
aws ec2 create-tags --resources $gatewayId --tags "Key=Name,Value=$STACK_NAME-csye6225-InternetGateway"

#Find Public Route Table
routeTableId=`aws ec2 describe-route-tables --filter "Name=tag:Name,Values=$STACK_NAME" --query 'RouteTables[*].{id:RouteTableId}' --output text` 
#Rename Public Route Table
aws ec2 create-tags --resources $routeTableId --tags "Key=Name,Value=$STACK_NAME-csye6225-public-route-table"

echo "Created Completion"