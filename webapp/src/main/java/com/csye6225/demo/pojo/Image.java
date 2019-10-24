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

    @Column(name = "accept_ranges")
    private String accept_ranges;
    @Column(name = "content_length")
    private String content_length;
    @Column(name = "content_type")
    private String content_type;
    @Column(name = "md5")
    private String md5;
    @Column(name = "last_modified")
    private String last_modified;

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
    public String getAccept_ranges() {
        return accept_ranges;
    }
    @JsonIgnore
    public String getContent_length() {
        return content_length;
    }
    @JsonIgnore
    public String getContent_type() {
        return content_type;
    }
    @JsonIgnore
    public String getMd5() {
        return md5;
    }
    @JsonIgnore
    public String getLast_modified() {
        return last_modified;
    }

    public void setAccept_ranges(String accept_ranges) {
        this.accept_ranges = accept_ranges;
    }

    public void setContent_length(String content_length) {
        this.content_length = content_length;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setLast_modified(String last_modified) {
        this.last_modified = last_modified;
    }
}

