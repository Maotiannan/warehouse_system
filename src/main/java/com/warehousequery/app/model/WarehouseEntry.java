package com.warehousequery.app.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class WarehouseEntry {
    private final StringProperty jcbh = new SimpleStringProperty("");
    private final StringProperty jczyh = new SimpleStringProperty("");
    private final StringProperty zyh = new SimpleStringProperty("");
    private final StringProperty yjrq = new SimpleStringProperty("");
    private final StringProperty jcrq = new SimpleStringProperty("");
    private final StringProperty lf = new SimpleStringProperty("");
    private final StringProperty bzgg = new SimpleStringProperty("");
    private final StringProperty hwmc = new SimpleStringProperty("");
    private final StringProperty mt = new SimpleStringProperty("");
    private final StringProperty hh = new SimpleStringProperty("");
    private final IntegerProperty js = new SimpleIntegerProperty(0);
    private final DoubleProperty tj = new SimpleDoubleProperty(0.0);
    private final DoubleProperty kctj = new SimpleDoubleProperty(0.0);
    private final DoubleProperty mz = new SimpleDoubleProperty(0.0);
    private final IntegerProperty ts = new SimpleIntegerProperty(0);
    private final StringProperty shdw = new SimpleStringProperty("");
    private final DoubleProperty kcjs = new SimpleDoubleProperty(0.0);
    private final StringProperty bgzt = new SimpleStringProperty("");
    private final StringProperty bz = new SimpleStringProperty("");
    private final StringProperty jcid = new SimpleStringProperty("");
    private final StringProperty inguid = new SimpleStringProperty("");
    private final StringProperty yyh = new SimpleStringProperty("");
    private final StringProperty srrq = new SimpleStringProperty("");
    private final StringProperty hd = new SimpleStringProperty("");
    private final StringProperty ch = new SimpleStringProperty("");
    private final StringProperty jsy = new SimpleStringProperty("");
    private final StringProperty jsydh = new SimpleStringProperty("");
    private final StringProperty fgsmc = new SimpleStringProperty("");
    private final StringProperty yqqdsj = new SimpleStringProperty("");
    private final StringProperty hz = new SimpleStringProperty("");
    private final IntegerProperty yjjs = new SimpleIntegerProperty(0);
    private final DoubleProperty yjtj = new SimpleDoubleProperty(0.0);
    private final DoubleProperty yjmz = new SimpleDoubleProperty(0.0);
    private final DoubleProperty kcmz = new SimpleDoubleProperty(0.0);
    private final StringProperty xhrq = new SimpleStringProperty("");
    private final StringProperty xhrq2 = new SimpleStringProperty("");
    private final StringProperty hwmc1 = new SimpleStringProperty("");
    private final StringProperty driverdh = new SimpleStringProperty("");
    private final StringProperty cleng = new SimpleStringProperty("");
    private final StringProperty chengzhong = new SimpleStringProperty("");
    private final StringProperty bz2 = new SimpleStringProperty("");
    private final StringProperty hdmc = new SimpleStringProperty("");
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public String getJcbh() { return jcbh.get(); }
    public StringProperty jcbhProperty() { return jcbh; }
    public void setJcbh(String value) { jcbh.set(value); }

    public String getJczyh() { return jczyh.get(); }
    public StringProperty jczyhProperty() { return jczyh; }
    public void setJczyh(String value) { jczyh.set(value); }

    public String getZyh() { return zyh.get(); }
    public StringProperty zyhProperty() { return zyh; }
    public void setZyh(String value) { zyh.set(value); }

    public String getYjrq() { return yjrq.get(); }
    public StringProperty yjrqProperty() { return yjrq; }
    public void setYjrq(String value) { yjrq.set(value); }

    public String getJcrq() { return jcrq.get(); }
    public StringProperty jcrqProperty() { return jcrq; }
    public void setJcrq(String value) { jcrq.set(value); }

    public String getLf() { return lf.get(); }
    public StringProperty lfProperty() { return lf; }
    public void setLf(String value) { lf.set(value); }

    public String getBzgg() { return bzgg.get(); }
    public StringProperty bzggProperty() { return bzgg; }
    public void setBzgg(String value) { bzgg.set(value); }

    public String getHwmc() { return hwmc.get(); }
    public StringProperty hwmcProperty() { return hwmc; }
    public void setHwmc(String value) { hwmc.set(value); }

    public String getMt() { return mt.get(); }
    public StringProperty mtProperty() { return mt; }
    public void setMt(String value) { mt.set(value); }

    public String getHh() { return hh.get(); }
    public StringProperty hhProperty() { return hh; }
    public void setHh(String value) { hh.set(value); }

    public int getJs() { return js.get(); }
    public IntegerProperty jsProperty() { return js; }
    public void setJs(int value) { js.set(value); }

    public double getTj() { return tj.get(); }
    public DoubleProperty tjProperty() { return tj; }
    public void setTj(double value) { tj.set(value); }

    public double getKctj() { return kctj.get(); }
    public DoubleProperty kctjProperty() { return kctj; }
    public void setKctj(double value) { kctj.set(value); }

    public double getMz() { return mz.get(); }
    public DoubleProperty mzProperty() { return mz; }
    public void setMz(double value) { mz.set(value); }

    public int getTs() { return ts.get(); }
    public IntegerProperty tsProperty() { return ts; }
    public void setTs(int value) { ts.set(value); }

    public String getShdw() { return shdw.get(); }
    public StringProperty shdwProperty() { return shdw; }
    public void setShdw(String value) { shdw.set(value); }

    public double getKcjs() { return kcjs.get(); }
    public DoubleProperty kcjsProperty() { return kcjs; }
    public void setKcjs(double value) { kcjs.set(value); }

    public String getBgzt() { return bgzt.get(); }
    public StringProperty bgztProperty() { return bgzt; }
    public void setBgzt(String value) { bgzt.set(value); }

    public String getBz() { return bz.get(); }
    public StringProperty bzProperty() { return bz; }
    public void setBz(String value) { bz.set(value); }

    public String getJcid() { return jcid.get(); }
    public StringProperty jcidProperty() { return jcid; }
    public void setJcid(String value) { jcid.set(value); }

    public String getInguid() { return inguid.get(); }
    public StringProperty inguidProperty() { return inguid; }
    public void setInguid(String value) { inguid.set(value); }

    public String getYyh() { return yyh.get(); }
    public StringProperty yyhProperty() { return yyh; }
    public void setYyh(String value) { yyh.set(value); }

    public String getSrrq() { return srrq.get(); }
    public StringProperty srrqProperty() { return srrq; }
    public void setSrrq(String value) { srrq.set(value); }

    public String getHd() { return hd.get(); }
    public StringProperty hdProperty() { return hd; }
    public void setHd(String value) { hd.set(value); }

    public String getCh() { return ch.get(); }
    public StringProperty chProperty() { return ch; }
    public void setCh(String value) { ch.set(value); }

    public String getJsy() { return jsy.get(); }
    public StringProperty jsyProperty() { return jsy; }
    public void setJsy(String value) { jsy.set(value); }

    public String getJsydh() { return jsydh.get(); }
    public StringProperty jsydhProperty() { return jsydh; }
    public void setJsydh(String value) { jsydh.set(value); }

    public String getFgsmc() { return fgsmc.get(); }
    public StringProperty fgsmcProperty() { return fgsmc; }
    public void setFgsmc(String value) { fgsmc.set(value); }

    public String getYqqdsj() { return yqqdsj.get(); }
    public StringProperty yqqdsjProperty() { return yqqdsj; }
    public void setYqqdsj(String value) { yqqdsj.set(value); }

    public boolean isSelected() { return selected.get(); }
    public BooleanProperty selectedProperty() { return selected; }
    public void setSelected(boolean value) { selected.set(value); }

    public String getHz() { return hz.get(); }
    public StringProperty hzProperty() { return hz; }
    public void setHz(String value) { hz.set(value); }

    public int getYjjs() { return yjjs.get(); }
    public IntegerProperty yjjsProperty() { return yjjs; }
    public void setYjjs(int value) { yjjs.set(value); }

    public double getYjtj() { return yjtj.get(); }
    public DoubleProperty yjtjProperty() { return yjtj; }
    public void setYjtj(double value) { yjtj.set(value); }

    public double getYjmz() { return yjmz.get(); }
    public DoubleProperty yjmzProperty() { return yjmz; }
    public void setYjmz(double value) { yjmz.set(value); }

    public double getKcmz() { return kcmz.get(); }
    public DoubleProperty kcmzProperty() { return kcmz; }
    public void setKcmz(double value) { kcmz.set(value); }

    public String getXhrq() { return xhrq.get(); }
    public StringProperty xhrqProperty() { return xhrq; }
    public void setXhrq(String value) { xhrq.set(value); }

    public String getXhrq2() { return xhrq2.get(); }
    public StringProperty xhrq2Property() { return xhrq2; }
    public void setXhrq2(String value) { xhrq2.set(value); }

    public String getHwmc1() { return hwmc1.get(); }
    public StringProperty hwmc1Property() { return hwmc1; }
    public void setHwmc1(String value) { hwmc1.set(value); }

    public String getDriverdh() { return driverdh.get(); }
    public StringProperty driverdhProperty() { return driverdh; }
    public void setDriverdh(String value) { driverdh.set(value); }

    public String getCleng() { return cleng.get(); }
    public StringProperty clengProperty() { return cleng; }
    public void setCleng(String value) { cleng.set(value); }

    public String getChengzhong() { return chengzhong.get(); }
    public StringProperty chengzhongProperty() { return chengzhong; }
    public void setChengzhong(String value) { chengzhong.set(value); }

    public String getBz2() { return bz2.get(); }
    public StringProperty bz2Property() { return bz2; }
    public void setBz2(String value) { bz2.set(value); }

    public String getHdmc() { return hdmc.get(); }
    public StringProperty hdmcProperty() { return hdmc; }
    public void setHdmc(String value) { hdmc.set(value); }
}
