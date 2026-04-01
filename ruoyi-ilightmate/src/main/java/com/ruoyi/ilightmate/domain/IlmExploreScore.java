package com.ruoyi.ilightmate.domain;

import java.util.Date;

public class IlmExploreScore {
    private Long id;
    private Long userId;
    private Integer dimAndun;
    private Integer dimLijie;
    private Integer dimQingxu;
    private Integer dimLianjie;
    private Integer dimXingdong;
    private Integer dimJiena;
    private Integer dimJuecha;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getDimAndun() { return dimAndun; }
    public void setDimAndun(Integer dimAndun) { this.dimAndun = dimAndun; }
    public Integer getDimLijie() { return dimLijie; }
    public void setDimLijie(Integer dimLijie) { this.dimLijie = dimLijie; }
    public Integer getDimQingxu() { return dimQingxu; }
    public void setDimQingxu(Integer dimQingxu) { this.dimQingxu = dimQingxu; }
    public Integer getDimLianjie() { return dimLianjie; }
    public void setDimLianjie(Integer dimLianjie) { this.dimLianjie = dimLianjie; }
    public Integer getDimXingdong() { return dimXingdong; }
    public void setDimXingdong(Integer dimXingdong) { this.dimXingdong = dimXingdong; }
    public Integer getDimJiena() { return dimJiena; }
    public void setDimJiena(Integer dimJiena) { this.dimJiena = dimJiena; }
    public Integer getDimJuecha() { return dimJuecha; }
    public void setDimJuecha(Integer dimJuecha) { this.dimJuecha = dimJuecha; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
