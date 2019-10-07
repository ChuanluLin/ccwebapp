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