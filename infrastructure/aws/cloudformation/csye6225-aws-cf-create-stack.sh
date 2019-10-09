#!/bin/bash
#******************************************************************************
#    AWS VPC Creation Shell Script
#******************************************************************************
echo "Enter profile name:"
read Profile_name
export AWS_PROFILE=$Profile_name

echo "Enter NetWork Stack Name:"
read STACK_NAME

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

echo "Created Completion"