#!/bin/bash
#******************************************************************************
#    AWS VPC Creation Shell Script
#******************************************************************************
echo "Enter profile name:"
read Profile_name
export AWS_PROFILE=$Profile_name

echo "Enter NetWork Stack Name:"
read STACK_NAME

# input format
# ./csye6225-aws-cf-create-stack.sh 10.0.0.0/16 10.0.1.0/24 10.0.2.0/24 10.0.3.0/24


IPV4REGEX="^((25[0-5]|2[0-4][0-9]|[01]?[0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?)(\/([0-9]|[1-2][0-9]|3[0-2]))$"

if [[ "$1" =~ $IPV4REGEX ]] && [[ "$2" =~ $IPV4REGEX ]] && [[ "$3" =~ $IPV4REGEX ]] && [[ "$4" =~ $IPV4REGEX ]]
then 
  echo "Start"
else
  echo "Invalid Input"
  exit 0
fi

RC=$(aws cloudformation validate-template --template-body file://./csye6225-cf-networking.json)
if [ $? -eq 0 ]
then
	echo "Template is Correct"
else
	echo "Invalid Template"
	exit 0
fi

#Create Stack
RC1=$(aws cloudformation create-stack --stack-name $STACK_NAME --template-body file://csye6225-cf-networking.json --parameters ParameterKey=VPCCIDR,ParameterValue=$1 ParameterKey=SUBNETPUBLICCIDR,ParameterValue=$2 ParameterKey=SUBNETPUBLICCIDR1,ParameterValue=$3 ParameterKey=SUBNETPUBLICCIDR2,ParameterValue=$4)
if [ $? -eq 0 ]
then
	echo "Started with creating stack using cloud formation"
else
	echo "Stack Formation Failed"
	exit 0
fi

#Check Stack Status
STACK_STATUS=`aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[][ [StackStatus ] ][]" --output text`

#Wait until stack completely created
echo "Please wait..."

while [ $STACK_STATUS != "CREATE_COMPLETE" ]
do
	STACK_STATUS=`aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[][ [StackStatus ] ][]" --output text`
done

if [ $? -eq 0 ]
then
	echo "Stack creation complete"
else
	echo "Not Created,Something went wrong"
	exit 0
fi