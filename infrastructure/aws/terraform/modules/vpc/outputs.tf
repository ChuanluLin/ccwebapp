output "vpc_id" {
  value = "${aws_vpc.default.id}"
}

output "vpc_cidr_block" {
  value = "${aws_vpc.default.cidr_block}"
}

output "public_subnets_id1" {
  value = "${aws_subnet.public.0.id}"
}

output "public_subnets_id2" {
  value = "${aws_subnet.public.1.id}"
}

output "public_subnets_id3" {
  value = "${aws_subnet.public.2.id}"
}

output "public_subnets_cidr_block" {
  value = "${join(", ", aws_subnet.public.*.cidr_block)}"
}
