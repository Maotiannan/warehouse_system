package com.warehousequery.app.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 表示仓库条目的数据模型
 */
public class WarehouseEntry {
    private final StringProperty jcbh; // 进仓编号
    private final StringProperty jczyh; // 进仓作业号
    private final StringProperty zyh; // 作业号（API字段名）
    private final StringProperty yjrq; // 预进日期
    private final StringProperty jcrq; // 进仓日期
    private final StringProperty lf; // L/F
    private final StringProperty bzgg; // 包装规格
    private final StringProperty hwmc; // 货物名称
    private final StringProperty mt; // 唛头
    private final StringProperty hh; // 货号
    private final IntegerProperty js; // 件数
    private final DoubleProperty tj; // 体积
    private final DoubleProperty kctj; // 库存体积
    private final DoubleProperty mz; // 毛重
    private final IntegerProperty ts; // 托数
    private final StringProperty shdw; // 送货单位
    private final DoubleProperty kcjs; // 库存件数
    private final StringProperty bgzt; // 报关状态
    private final StringProperty bz; // 备注
    private final StringProperty jcid; // 进仓ID（用于查看照片和托信息）
    
    // 预约进仓状态专用字段
    private final StringProperty yyh; // 预约号
    private final StringProperty srrq; // 录入日期
    private final StringProperty hd; // 货代
    private final StringProperty ch; // 车牌号
    private final StringProperty jsy; // 司机
    private final StringProperty jsydh; // 司机电话
    private final StringProperty fgsmc; // 分公司名称
    private final StringProperty yqqdsj; // 要求取单时间
    
    // 30个字段完整版本新增字段
    private final StringProperty hz; // 货主
    private final IntegerProperty yjjs; // 预计件数
    private final DoubleProperty yjtj; // 预计体积
    private final DoubleProperty yjmz; // 预计毛重
    private final DoubleProperty kcmz; // 库存毛重
    private final StringProperty xhrq; // 卸货日期
    private final StringProperty xhrq2; // 卸货完成
    private final StringProperty hwmc1; // 货物名称1
    private final StringProperty driverdh; // 司机电话（API字段名）
    private final StringProperty cleng; // 车长
    private final StringProperty chengzhong; // 承重
    private final StringProperty bz2; // 备注2
    private final StringProperty hdmc; // 货代名称
    
    // 选中状态属性
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    
    public WarehouseEntry() {
        this.jcbh = new SimpleStringProperty("");
        this.jczyh = new SimpleStringProperty("");
        this.zyh = new SimpleStringProperty("");
        this.yjrq = new SimpleStringProperty("");
        this.jcrq = new SimpleStringProperty("");
        this.lf = new SimpleStringProperty("");
        this.bzgg = new SimpleStringProperty("");
        this.hwmc = new SimpleStringProperty("");
        this.mt = new SimpleStringProperty("");
        this.hh = new SimpleStringProperty("");
        this.js = new SimpleIntegerProperty(0);
        this.tj = new SimpleDoubleProperty(0);
        this.kctj = new SimpleDoubleProperty(0);
        this.mz = new SimpleDoubleProperty(0);
        this.ts = new SimpleIntegerProperty(0);
        this.shdw = new SimpleStringProperty("");
        this.kcjs = new SimpleDoubleProperty(0);
        this.bgzt = new SimpleStringProperty("");
        this.bz = new SimpleStringProperty("");
        this.jcid = new SimpleStringProperty("");
        
        // 预约进仓专用字段初始化
        this.yyh = new SimpleStringProperty("");
        this.srrq = new SimpleStringProperty("");
        this.hd = new SimpleStringProperty("");
        this.ch = new SimpleStringProperty("");
        this.jsy = new SimpleStringProperty("");
        this.jsydh = new SimpleStringProperty("");
        this.fgsmc = new SimpleStringProperty("");
        this.yqqdsj = new SimpleStringProperty("");
        
        // 30个字段完整版本新增字段初始化
        this.hz = new SimpleStringProperty("");
        this.yjjs = new SimpleIntegerProperty(0);
        this.yjtj = new SimpleDoubleProperty(0);
        this.yjmz = new SimpleDoubleProperty(0);
        this.kcmz = new SimpleDoubleProperty(0);
        this.xhrq = new SimpleStringProperty("");
        this.xhrq2 = new SimpleStringProperty("");
        this.hwmc1 = new SimpleStringProperty("");
        this.driverdh = new SimpleStringProperty("");
        this.cleng = new SimpleStringProperty("");
        this.chengzhong = new SimpleStringProperty("");
        this.bz2 = new SimpleStringProperty("");
        this.hdmc = new SimpleStringProperty("");
    }
    
    // Getters and Setters
    public String getJcbh() { return jcbh.get(); }
    public StringProperty jcbhProperty() { return jcbh; }
    public void setJcbh(String jcbh) { this.jcbh.set(jcbh); }

    public String getJczyh() { return jczyh.get(); }
    public StringProperty jczyhProperty() { return jczyh; }
    public void setJczyh(String jczyh) { this.jczyh.set(jczyh); }

    public String getZyh() { return zyh.get(); }
    public StringProperty zyhProperty() { return zyh; }
    public void setZyh(String zyh) { this.zyh.set(zyh); }

    public String getYjrq() { return yjrq.get(); }
    public StringProperty yjrqProperty() { return yjrq; }
    public void setYjrq(String yjrq) { this.yjrq.set(yjrq); }

    public String getJcrq() { return jcrq.get(); }
    public StringProperty jcrqProperty() { return jcrq; }
    public void setJcrq(String jcrq) { this.jcrq.set(jcrq); }

    public String getLf() { return lf.get(); }
    public StringProperty lfProperty() { return lf; }
    public void setLf(String lf) { this.lf.set(lf); }

    public String getBzgg() { return bzgg.get(); }
    public StringProperty bzggProperty() { return bzgg; }
    public void setBzgg(String bzgg) { this.bzgg.set(bzgg); }

    public String getHwmc() { return hwmc.get(); }
    public StringProperty hwmcProperty() { return hwmc; }
    public void setHwmc(String hwmc) { this.hwmc.set(hwmc); }

    public String getMt() { return mt.get(); }
    public StringProperty mtProperty() { return mt; }
    public void setMt(String mt) { this.mt.set(mt); }

    public String getHh() { return hh.get(); }
    public StringProperty hhProperty() { return hh; }
    public void setHh(String hh) { this.hh.set(hh); }

    public int getJs() { return js.get(); }
    public IntegerProperty jsProperty() { return js; }
    public void setJs(int js) { this.js.set(js); }

    public double getTj() { return tj.get(); }
    public DoubleProperty tjProperty() { return tj; }
    public void setTj(double tj) { this.tj.set(tj); }

    public double getKctj() { return kctj.get(); }
    public DoubleProperty kctjProperty() { return kctj; }
    public void setKctj(double kctj) { this.kctj.set(kctj); }

    public double getMz() { return mz.get(); }
    public DoubleProperty mzProperty() { return mz; }
    public void setMz(double mz) { this.mz.set(mz); }

    public int getTs() { return ts.get(); }
    public IntegerProperty tsProperty() { return ts; }
    public void setTs(int ts) { this.ts.set(ts); }

    public String getShdw() { return shdw.get(); }
    public StringProperty shdwProperty() { return shdw; }
    public void setShdw(String shdw) { this.shdw.set(shdw); }

    public double getKcjs() { return kcjs.get(); }
    public DoubleProperty kcjsProperty() { return kcjs; }
    public void setKcjs(double kcjs) { this.kcjs.set(kcjs); }

    public String getBgzt() { return bgzt.get(); }
    public StringProperty bgztProperty() { return bgzt; }
    public void setBgzt(String bgzt) { this.bgzt.set(bgzt); }

    public String getBz() { return bz.get(); }
    public StringProperty bzProperty() { return bz; }
    public void setBz(String bz) { this.bz.set(bz); }
    
    public String getJcid() { return jcid.get(); }
    public StringProperty jcidProperty() { return jcid; }
    public void setJcid(String jcid) { this.jcid.set(jcid); }

    // 预约进仓专用字段的Getters and Setters
    public String getYyh() { return yyh.get(); }
    public StringProperty yyhProperty() { return yyh; }
    public void setYyh(String yyh) { this.yyh.set(yyh); }

    public String getSrrq() { return srrq.get(); }
    public StringProperty srrqProperty() { return srrq; }
    public void setSrrq(String srrq) { this.srrq.set(srrq); }

    public String getHd() { return hd.get(); }
    public StringProperty hdProperty() { return hd; }
    public void setHd(String hd) { this.hd.set(hd); }

    public String getCh() { return ch.get(); }
    public StringProperty chProperty() { return ch; }
    public void setCh(String ch) { this.ch.set(ch); }

    public String getJsy() { return jsy.get(); }
    public StringProperty jsyProperty() { return jsy; }
    public void setJsy(String jsy) { this.jsy.set(jsy); }

    public String getJsydh() { return jsydh.get(); }
    public StringProperty jsydhProperty() { return jsydh; }
    public void setJsydh(String jsydh) { this.jsydh.set(jsydh); }

    public String getFgsmc() { return fgsmc.get(); }
    public StringProperty fgsmcProperty() { return fgsmc; }
    public void setFgsmc(String fgsmc) { this.fgsmc.set(fgsmc); }

    public String getYqqdsj() { return yqqdsj.get(); }
    public StringProperty yqqdsjProperty() { return yqqdsj; }
    public void setYqqdsj(String yqqdsj) { this.yqqdsj.set(yqqdsj); }

    // 选中状态的Getter/Setter/Property方法
    public boolean isSelected() {
        return selected.get();
    }
    
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
    
    public BooleanProperty selectedProperty() {
        return selected;
    }
    
    // 30个字段完整版本新增字段的Getters and Setters
    public String getHz() { return hz.get(); }
    public StringProperty hzProperty() { return hz; }
    public void setHz(String hz) { this.hz.set(hz); }

    public int getYjjs() { return yjjs.get(); }
    public IntegerProperty yjjsProperty() { return yjjs; }
    public void setYjjs(int yjjs) { this.yjjs.set(yjjs); }

    public double getYjtj() { return yjtj.get(); }
    public DoubleProperty yjtjProperty() { return yjtj; }
    public void setYjtj(double yjtj) { this.yjtj.set(yjtj); }

    public double getYjmz() { return yjmz.get(); }
    public DoubleProperty yjmzProperty() { return yjmz; }
    public void setYjmz(double yjmz) { this.yjmz.set(yjmz); }

    public double getKcmz() { return kcmz.get(); }
    public DoubleProperty kcmzProperty() { return kcmz; }
    public void setKcmz(double kcmz) { this.kcmz.set(kcmz); }

    public String getXhrq() { return xhrq.get(); }
    public StringProperty xhrqProperty() { return xhrq; }
    public void setXhrq(String xhrq) { this.xhrq.set(xhrq); }

    public String getXhrq2() { return xhrq2.get(); }
    public StringProperty xhrq2Property() { return xhrq2; }
    public void setXhrq2(String xhrq2) { this.xhrq2.set(xhrq2); }

    public String getHwmc1() { return hwmc1.get(); }
    public StringProperty hwmc1Property() { return hwmc1; }
    public void setHwmc1(String hwmc1) { this.hwmc1.set(hwmc1); }

    public String getDriverdh() { return driverdh.get(); }
    public StringProperty driverdhProperty() { return driverdh; }
    public void setDriverdh(String driverdh) { this.driverdh.set(driverdh); }

    public String getCleng() { return cleng.get(); }
    public StringProperty clengProperty() { return cleng; }
    public void setCleng(String cleng) { this.cleng.set(cleng); }

    public String getChengzhong() { return chengzhong.get(); }
    public StringProperty chengzhongProperty() { return chengzhong; }
    public void setChengzhong(String chengzhong) { this.chengzhong.set(chengzhong); }

    public String getBz2() { return bz2.get(); }
    public StringProperty bz2Property() { return bz2; }
    public void setBz2(String bz2) { this.bz2.set(bz2); }

    public String getHdmc() { return hdmc.get(); }
    public StringProperty hdmcProperty() { return hdmc; }
    public void setHdmc(String hdmc) { this.hdmc.set(hdmc); }
} 