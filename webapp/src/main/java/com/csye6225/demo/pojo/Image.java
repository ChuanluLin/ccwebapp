package com.csye6225.demo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import javax.persistence.Embeddable;

@Embeddable
public class Image {
    @Column(name="imageid", length = 32)
    private String imageid;

    @Column(name = "url")
    private String url;

    @Column(name = "filename")
    private String filename;

    @Column(name = "Date")
    private String Date;
    @Column(name = "Content_Length")
    private String Content_Length;
    @Column(name = "Last_Modified")
    private String Last_Modified;
    @Column(name = "Content_MD5")
    private String Content_MD5;
    @Column(name = "x_amz_server_side_encryption")
    private String x_amz_server_side_encryption;
    @Column(name = "x_amz_version_id")
    private String x_amz_version_id;
    @Column(name = "x_amz_delete_marker")
    private String x_amz_delete_marker;
    @Column(name = "x_amz_storage_class")
    private String x_amz_storage_class;
    @Column(name = "x_amz_website_redirect_location")
    private String x_amz_website_redirect_location;
    @Column(name = "x_amz_server_side_encryption_aws_kms_key_id")
    private String x_amz_server_side_encryption_aws_kms_key_id;
    @Column(name = "x_amz_server_side_encryption_customer_algorithm")
    private String x_amz_server_side_encryption_customer_algorithm;

    public String getImageid() {
        return imageid;
    }

    public void setImageid(String imageid) {
        this.imageid = imageid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonIgnore
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    //metadata
    @JsonIgnore
    public String getDate() {
        return Date;
    }
    @JsonIgnore
    public String getContent_Length() {
        return Content_Length;
    }
    @JsonIgnore
    public String getLast_Modified() {
        return Last_Modified;
    }
    @JsonIgnore
    public String getContent_MD5() {
        return Content_MD5;
    }
    @JsonIgnore
    public String getX_amz_server_side_encryption() {
        return x_amz_server_side_encryption;
    }
    @JsonIgnore
    public String getX_amz_version_id() {
        return x_amz_version_id;
    }
    @JsonIgnore
    public String getX_amz_delete_marker() {
        return x_amz_delete_marker;
    }
    @JsonIgnore
    public String getX_amz_storage_class() {
        return x_amz_storage_class;
    }
    @JsonIgnore
    public String getX_amz_website_redirect_location() {
        return x_amz_website_redirect_location;
    }
    @JsonIgnore
    public String getX_amz_server_side_encryption_aws_kms_key_id() {
        return x_amz_server_side_encryption_aws_kms_key_id;
    }
    @JsonIgnore
    public String getX_amz_server_side_encryption_customer_algorithm() {
        return x_amz_server_side_encryption_customer_algorithm;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setContent_Length(String content_Length) {
        Content_Length = content_Length;
    }

    public void setLast_Modified(String last_Modified) {
        Last_Modified = last_Modified;
    }

    public void setContent_MD5(String content_MD5) {
        Content_MD5 = content_MD5;
    }

    public void setX_amz_server_side_encryption(String x_amz_server_side_encryption) {
        this.x_amz_server_side_encryption = x_amz_server_side_encryption;
    }

    public void setX_amz_version_id(String x_amz_version_id) {
        this.x_amz_version_id = x_amz_version_id;
    }

    public void setX_amz_delete_marker(String x_amz_delete_marker) {
        this.x_amz_delete_marker = x_amz_delete_marker;
    }

    public void setX_amz_storage_class(String x_amz_storage_class) {
        this.x_amz_storage_class = x_amz_storage_class;
    }

    public void setX_amz_website_redirect_location(String x_amz_website_redirect_location) {
        this.x_amz_website_redirect_location = x_amz_website_redirect_location;
    }

    public void setX_amz_server_side_encryption_aws_kms_key_id(String x_amz_server_side_encryption_aws_kms_key_id) {
        this.x_amz_server_side_encryption_aws_kms_key_id = x_amz_server_side_encryption_aws_kms_key_id;
    }

    public void setX_amz_server_side_encryption_customer_algorithm(String x_amz_server_side_encryption_customer_algorithm) {
        this.x_amz_server_side_encryption_customer_algorithm = x_amz_server_side_encryption_customer_algorithm;
    }
}

