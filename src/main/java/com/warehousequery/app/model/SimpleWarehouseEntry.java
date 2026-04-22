/*
 * Decompiled with CFR 0.152.
 */
package com.warehousequery.app.model;

public class SimpleWarehouseEntry {
    private String jcbh;
    private String jczyh;
    private String yjrq;
    private String jcrq;
    private String lf;
    private String bzgg;
    private String hwmc;
    private String mt;
    private String hh;
    private int js;
    private double tj;
    private double kctj;
    private double mz;
    private int ts;
    private String shdw;
    private double kcjs;
    private String bgzt;
    private String bz;
    private String jcid;
    private boolean selected = false;

    public String getJcbh() {
        return this.jcbh;
    }

    public void setJcbh(String jcbh) {
        this.jcbh = jcbh;
    }

    public String getJczyh() {
        return this.jczyh;
    }

    public void setJczyh(String jczyh) {
        this.jczyh = jczyh;
    }

    public String getYjrq() {
        return this.yjrq;
    }

    public void setYjrq(String yjrq) {
        this.yjrq = yjrq;
    }

    public String getJcrq() {
        return this.jcrq;
    }

    public void setJcrq(String jcrq) {
        this.jcrq = jcrq;
    }

    public String getLf() {
        return this.lf;
    }

    public void setLf(String lf) {
        this.lf = lf;
    }

    public String getBzgg() {
        return this.bzgg;
    }

    public void setBzgg(String bzgg) {
        this.bzgg = bzgg;
    }

    public String getHwmc() {
        return this.hwmc;
    }

    public void setHwmc(String hwmc) {
        this.hwmc = hwmc;
    }

    public String getMt() {
        return this.mt;
    }

    public void setMt(String mt) {
        this.mt = mt;
    }

    public String getHh() {
        return this.hh;
    }

    public void setHh(String hh) {
        this.hh = hh;
    }

    public int getJs() {
        return this.js;
    }

    public void setJs(int js) {
        this.js = js;
    }

    public double getTj() {
        return this.tj;
    }

    public void setTj(double tj) {
        this.tj = tj;
    }

    public double getKctj() {
        return this.kctj;
    }

    public void setKctj(double kctj) {
        this.kctj = kctj;
    }

    public double getMz() {
        return this.mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public int getTs() {
        return this.ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public String getShdw() {
        return this.shdw;
    }

    public void setShdw(String shdw) {
        this.shdw = shdw;
    }

    public double getKcjs() {
        return this.kcjs;
    }

    public void setKcjs(double kcjs) {
        this.kcjs = kcjs;
    }

    public String getBgzt() {
        return this.bgzt;
    }

    public void setBgzt(String bgzt) {
        this.bgzt = bgzt;
    }

    public String getBz() {
        return this.bz;
    }

    public void setBz(String bz) {
        this.bz = bz;
    }

    public String getJcid() {
        return this.jcid;
    }

    public void setJcid(String jcid) {
        this.jcid = jcid;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String toString() {
        return String.format("\u8fdb\u4ed3\u7f16\u53f7: %s, \u8d27\u7269\u540d\u79f0: %s, \u4ef6\u6570: %d, \u72b6\u6001: %s", this.jcbh, this.hwmc, this.js, this.bgzt);
    }
}
