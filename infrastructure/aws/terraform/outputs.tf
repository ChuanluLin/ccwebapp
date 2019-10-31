output "vpc_id" {
  value = "${aws_vpc.default.id}"
}

output "vpc_cidr_block" {
  value = "${aws_vpc.default.cidr_block}"
}

output "public_subnets_id" {
  value = "${join(", ", aws_subnet.public.*.id)}"
}

output "public_subnets_cidr_block" {
  value = "${join(", ", aws_subnet.public.*.cidr_block)}"
}

output "aws_security_group-application" {
  value = "${aws_security_group.application.id}"
}

output "aws_security_group-database" {
  value = "${aws_security_group.database.id}"
}

output "aws_s3_bucket_name" {
  value = "webapp.${var.domain_name}"
}

output "aws_db_instance_identifier" {
  value = "${aws_db_instance.default.identifier}"
}

output "aws_key_pair_name" {
  value = "${aws_key_pair.auth.key_name}"
}

output "aws_instance_ip" {
  value = "${aws_instance.web.public_ip}"
}

output "aws_instance_dns" {
  value = "${aws_instance.web.public_dns}"
}

output "aws_dynamodb_table_name" {
  value = "${aws_dynamodb_table.basic-dynamodb-table.name}"
}

output "aws_db_instance_endpoint" {
  description = "The connection endpoint for RDS"
  value       = "${aws_db_instance.default.endpoint}"
}