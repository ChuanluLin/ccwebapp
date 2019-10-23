#!/bin/bash
#******************************************************************************
#    AWS VPC Creation Shell Script
#******************************************************************************
set -e
echo "Enter profile name:"
read Profile_Name
export AWS_PROFILE=$Profile_Name


# input format
# ./csye6225-aws-networking-setup.sh dev2 10.0.0.0/16 10.0.1.0/24 10.0.2.0/24 10.0.3.0/24


VPC_NAME=$1
if [ `aws ec2 describe-vpcs --filter "Name=tag:Name,Values=$VPC_NAME" --query 'Vpcs[*].{id:VpcId}' --output text` ]
then 
  echo "$VPC_NAME VPC existed"
  exit 0
fi
VPC_CIDR=$2
AWS_Region=$(aws configure get region)
IGW_NAME=$VPC_NAME"-InternetGateway"
ROUTE_TABLE_NAME=$VPC_NAME"-public-route-table"
SUBNET_PUBLIC_CIDR=$3
SUBNET_PUBLIC_AZ=$AWS_Region"a"
SUBNET_PUBLIC_NAME=$VPC_NAME"-Subnet1"
SUBNET_PUBLIC_CIDR1=$4
SUBNET_PUBLIC_AZ1=$AWS_Region"b"
SUBNET_PUBLIC_NAME1=$VPC_NAME"-Subnet2"
SUBNET_PUBLIC_CIDR2=$5
SUBNET_PUBLIC_AZ2=$AWS_Region"c"
SUBNET_PUBLIC_NAME2=$VPC_NAME"-Subnet3"
CHECK_FREQUENCY=5

IPV4REGEX="^((25[0-5]|2[0-4][0-9]|[01]?[0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?)(\/([0-9]|[1-2][0-9]|3[0-2]))$"

if [[ "$VPC_CIDR" =~ $IPV4REGEX ]] && [[ "$SUBNET_PUBLIC_CIDR" =~ $IPV4REGEX ]] && [[ "$SUBNET_PUBLIC_CIDR1" =~ $IPV4REGEX ]] && [[ "$SUBNET_PUBLIC_CIDR2" =~ $IPV4REGEX ]]
then 
  echo "Start"
else
  echo "Invalid Input"
  exit 0
fi


# VPC_NAME=$VPC_NAME"-vpc"
# VPC_CIDR="10.0.0.0/16"
# AWS_Region=$(aws configure get region)
# IGW_NAME=$VPC_NAME"-InternetGateway"
# ROUTE_TABLE_NAME=$VPC_NAME"-public-route-table"
# SUBNET_PUBLIC_CIDR="10.0.1.0/24"
# SUBNET_PUBLIC_AZ=$AWS_Region"a"
# SUBNET_PUBLIC_NAME="10.0.1.0"
# SUBNET_PUBLIC_CIDR1="10.0.2.0/24"
# SUBNET_PUBLIC_AZ1=$AWS_Region"b"
# SUBNET_PUBLIC_NAME1="10.0.2.0"
# SUBNET_PUBLIC_CIDR2="10.0.3.0/24"
# SUBNET_PUBLIC_AZ2=$AWS_Region"c"
# SUBNET_PUBLIC_NAME2="10.0.3.0"
# CHECK_FREQUENCY=5

# Create VPC
echo "Creating VPC in preferred region..."
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block $VPC_CIDR \
  --query 'Vpc.{VpcId:VpcId}' \
  --output text)
echo "  VPC ID '$VPC_ID' CREATED in '$AWS_Region' region."

# Add Name tag to VPC
aws ec2 create-tags \
  --resources $VPC_ID \
  --tags "Key=Name,Value=$VPC_NAME"
echo "  VPC ID '$VPC_ID' NAMED as '$VPC_NAME'."

# Create Public Subnet
echo "Creating Public Subnet1..."
SUBNET_PUBLIC_ID=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block $SUBNET_PUBLIC_CIDR \
  --availability-zone $SUBNET_PUBLIC_AZ \
  --query 'Subnet.{SubnetId:SubnetId}' \
  --output text)
echo "  Subnet ID '$SUBNET_PUBLIC_ID' CREATED in '$SUBNET_PUBLIC_AZ'" \
  "Availability Zone."

# Add Name tag to Public Subnet
aws ec2 create-tags \
  --resources $SUBNET_PUBLIC_ID \
  --tags "Key=Name,Value=$SUBNET_PUBLIC_NAME"
echo "  Subnet ID '$SUBNET_PUBLIC_ID' NAMED as" \
  "'$SUBNET_PUBLIC_NAME'."
  
# Create Public Subnet
echo "Creating Public Subnet2..."
SUBNET_PUBLIC_ID1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block $SUBNET_PUBLIC_CIDR1 \
  --availability-zone $SUBNET_PUBLIC_AZ1 \
  --query 'Subnet.{SubnetId:SubnetId}' \
  --output text)
echo "  Subnet ID '$SUBNET_PUBLIC_ID1' CREATED in '$SUBNET_PUBLIC_AZ1'" \
  "Availability Zone."

# Add Name tag to Public Subnet
aws ec2 create-tags \
  --resources $SUBNET_PUBLIC_ID1 \
  --tags "Key=Name,Value=$SUBNET_PUBLIC_NAME1"
echo "  Subnet ID '$SUBNET_PUBLIC_ID1' NAMED as" \
  "'$SUBNET_PUBLIC_NAME1'."
  
# Create Public Subnet
echo "Creating Public Subnet3..."
SUBNET_PUBLIC_ID2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block $SUBNET_PUBLIC_CIDR2 \
  --availability-zone $SUBNET_PUBLIC_AZ2 \
  --query 'Subnet.{SubnetId:SubnetId}' \
  --output text)
echo "  Subnet ID '$SUBNET_PUBLIC_ID2' CREATED in '$SUBNET_PUBLIC_AZ2'" \
  "Availability Zone."

# Add Name tag to Public Subnet
aws ec2 create-tags \
  --resources $SUBNET_PUBLIC_ID2 \
  --tags "Key=Name,Value=$SUBNET_PUBLIC_NAME2"
echo "  Subnet ID '$SUBNET_PUBLIC_ID2' NAMED as" \
  "'$SUBNET_PUBLIC_NAME2'."

# Create Internet gateway
echo "Creating Internet Gateway..."
IGW_ID=$(aws ec2 create-internet-gateway \
  --query 'InternetGateway.{InternetGatewayId:InternetGatewayId}' \
  --output text)
echo "  Internet Gateway ID '$IGW_ID' CREATED."

# Add Name tag to Internet gateway
aws ec2 create-tags \
  --resources $IGW_ID \
  --tags "Key=Name,Value=$IGW_NAME"
echo "  Internet Gateway ID '$IGW_ID' NAMED as '$IGW_NAME'."

# Attach Internet gateway to your VPC
aws ec2 attach-internet-gateway \
  --vpc-id $VPC_ID \
  --internet-gateway-id $IGW_ID
echo "  Internet Gateway ID '$IGW_ID' ATTACHED to VPC ID '$VPC_ID'."

# Create Public Route Table
echo "Creating Public Route Table..."
ROUTE_TABLE_ID=$(aws ec2 create-route-table \
  --vpc-id $VPC_ID \
  --query 'RouteTable.{RouteTableId:RouteTableId}' \
  --output text)
echo "  Route Table ID '$ROUTE_TABLE_ID' CREATED."

# Add Name tag to Public Route table
aws ec2 create-tags \
  --resources $ROUTE_TABLE_ID \
  --tags "Key=Name,Value=$ROUTE_TABLE_NAME"
echo "  Route Table ID '$ROUTE_TABLE_ID' NAMED as '$ROUTE_TABLE_NAME'."

# Associate Public Subnet with Public Route Table
RESULT=$(aws ec2 associate-route-table  \
  --subnet-id $SUBNET_PUBLIC_ID \
  --route-table-id $ROUTE_TABLE_ID)
echo "  Public Subnet ID '$SUBNET_PUBLIC_ID' ASSOCIATED with Route Table ID" \
  "'$ROUTE_TABLE_ID'."
 
# Associate Public Subnet with Public Route Table
RESULT=$(aws ec2 associate-route-table  \
  --subnet-id $SUBNET_PUBLIC_ID1 \
  --route-table-id $ROUTE_TABLE_ID)
echo "  Public Subnet ID '$SUBNET_PUBLIC_ID1' ASSOCIATED with Route Table ID" \
  "'$ROUTE_TABLE_ID'."
  
# Associate Public Subnet with Public Route Table
RESULT=$(aws ec2 associate-route-table  \
  --subnet-id $SUBNET_PUBLIC_ID2 \
  --route-table-id $ROUTE_TABLE_ID)
echo "  Public Subnet ID '$SUBNET_PUBLIC_ID2' ASSOCIATED with Route Table ID" \
  "'$ROUTE_TABLE_ID'."

# Create route to Internet Gateway
RESULT=$(aws ec2 create-route \
  --route-table-id $ROUTE_TABLE_ID \
  --destination-cidr-block 0.0.0.0/0 \
  --gateway-id $IGW_ID)
echo "  Route to '0.0.0.0/0' via Internet Gateway ID '$IGW_ID' ADDED to" \
  "Route Table ID '$ROUTE_TABLE_ID'."