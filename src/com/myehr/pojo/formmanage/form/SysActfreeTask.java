package com.myehr.pojo.formmanage.form;

import java.io.Serializable;
import java.util.Date;

public class SysActfreeTask implements Serializable{
	private static final long serialVersionUID = 1L;
    private String taskid;

    private String modelkey;

    private String cratename;

    private Date cratetime;

    private String statusa;

    private String actcode;

    private String title;

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid == null ? null : taskid.trim();
    }

    public String getModelkey() {
        return modelkey;
    }

    public void setModelkey(String modelkey) {
        this.modelkey = modelkey == null ? null : modelkey.trim();
    }

    public String getCratename() {
        return cratename;
    }

    public void setCratename(String cratename) {
        this.cratename = cratename == null ? null : cratename.trim();
    }

    public Date getCratetime() {
        return cratetime;
    }

    public void setCratetime(Date cratetime) {
        this.cratetime = cratetime;
    }

    public String getStatusa() {
        return statusa;
    }

    public void setStatusa(String statusa) {
        this.statusa = statusa == null ? null : statusa.trim();
    }

    public String getActcode() {
        return actcode;
    }

    public void setActcode(String actcode) {
        this.actcode = actcode == null ? null : actcode.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }
}