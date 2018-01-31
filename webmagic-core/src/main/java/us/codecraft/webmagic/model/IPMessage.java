package us.codecraft.webmagic.model;

import java.io.Serializable;

public class IPMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String IPAddress;//ip地址
    private String IPPort;//端口
    private String IPType;//类型
    private String IPSpeed;//速度

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public String getIPPort() {
        return IPPort;
    }

    public void setIPPort(String IPPort) {
        this.IPPort = IPPort;
    }

    public String getIPType() {
        return IPType;
    }

    public void setIPType(String IPType) {
        this.IPType = IPType;
    }

    public String getIPSpeed() {
        return IPSpeed;
    }

    public void setIPSpeed(String IPSpeed) {
        this.IPSpeed = IPSpeed;
    }

    @Override
    public String toString() {
        return IPAddress + ":" + IPPort;
    }

}
