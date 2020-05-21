package com.goqii.goqiisdk.network.ApiModels;

import java.io.File;
import java.io.Serializable;

public class FilesPreSignedUrl implements Serializable {
    private File file;
    private String preSignedUrl;
    private String publicUrl;

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getPreSignedUrl() {
        return preSignedUrl;
    }

    public void setPreSignedUrl(String preSignedUrl) {
        this.preSignedUrl = preSignedUrl;
    }
}
