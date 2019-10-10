#!/bin/bash
#******************************************************************************
#    CloudFormation Stack Deletion Shell Script
#******************************************************************************

set -e
echo "Enter profile name:"
read Profile_name
export AWS_PROFILE=$Profile_name

echo "Type in the stack you want to delete"
read STACK_NAME

echo "The stack you want to delete: "
#Query the stack
aws cloudformation describe-stacks --stack-name $STACK_NAME

#Delete the cloudformation stack
aws cloudformation delete-stack --stack-name $STACK_NAME
echo "Please wait..."
#Wait until the stack is deleted.
aws cloudformation wait stack-delete-complete --stack-name $STACK_NAME

echo "Deleted Completion"