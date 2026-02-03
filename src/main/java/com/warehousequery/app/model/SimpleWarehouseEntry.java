package com.warehousequery.app.model;

/**
 * 简化的仓库条目类 - 不依赖JavaFX
 */
public class SimpleWarehouseEntry {
    private String jcbh; // 进仓编号
    private String jczyh; // 进仓作业号
    private String yjrq; // 预进日期
    private String jcrq; // 进仓日期
    private String lf; // L/F
    private String bzgg; // 包装规格
    private String hwmc; // 货物名称
    private String mt; // 唛头
    private String hh; // 货号
    private int js; // 件数
    private double tj; // 体积
    private double kctj; // 库存体积
    private double mz; // 毛重
    private int ts; // 托数
    private String shdw; // 送货单位
    private double kcjs; // 库存件数
    private String bgzt; // 报关状态
    private String bz; // 备注
    private String jcid; // 进仓ID（用于查看照片和托信息）
    private boolean selected = false;

    // 构造函数
    public SimpleWarehouseEntry() {}

    // Getter和Setter方法
    public String getJcbh() { return jcbh; }
    public void setJcbh(String jcbh) { this.jcbh = jcbh; }

    public String getJczyh() { return jczyh; }
    public void setJczyh(String jczyh) { this.jczyh = jczyh; }

    public String getYjrq() { return yjrq; }
    public void setYjrq(String yjrq) { this.yjrq = yjrq; }

    public String getJcrq() { return jcrq; }
    public void setJcrq(String jcrq) { this.jcrq = jcrq; }

    public String getLf() { return lf; }
    public void setLf(String lf) { this.lf = lf; }

    public String getBzgg() { return bzgg; }
    public void setBzgg(String bzgg) { this.bzgg = bzgg; }

    public String getHwmc() { return hwmc; }
    public void setHwmc(String hwmc) { this.hwmc = hwmc; }

    public String getMt() { return mt; }
    public void setMt(String mt) { this.mt = mt; }

    public String getHh() { return hh; }
    public void setHh(String hh) { this.hh = hh; }

    public int getJs() { return js; }
    public void setJs(int js) { this.js = js; }

    public double getTj() { return tj; }
    public void setTj(double tj) { this.tj = tj; }

    public double getKctj() { return kctj; }
    public void setKctj(double kctj) { this.kctj = kctj; }

    public double getMz() { return mz; }
    public void setMz(double mz) { this.mz = mz; }

    public int getTs() { return ts; }
    public void setTs(int ts) { this.ts = ts; }

    public String getShdw() { return shdw; }
    public void setShdw(String shdw) { this.shdw = shdw; }

    public double getKcjs() { return kcjs; }
    public void setKcjs(double kcjs) { this.kcjs = kcjs; }

    public String getBgzt() { return bgzt; }
    public void setBgzt(String bgzt) { this.bgzt = bgzt; }

    public String getBz() { return bz; }
    public void setBz(String bz) { this.bz = bz; }

    public String getJcid() { return jcid; }
    public void setJcid(String jcid) { this.jcid = jcid; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public String toString() {
        return String.format("进仓编号: %s, 货物名称: %s, 件数: %d, 状态: %s", 
                           jcbh, hwmc, js, bgzt);
    }
} 