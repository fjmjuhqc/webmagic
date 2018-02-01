package us.codecraft.webmagic.thread;

import us.codecraft.webmagic.model.IPMessage;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.utils.ProxyUtils;

import java.util.List;

/**
 * 多线程验证爬取的ip代理是否有效
 */
public class CheckIpThread extends Thread {
    private List<IPMessage> ipMessages;
    private volatile List<IPMessage> ipMessageChecked;

    public CheckIpThread(List<IPMessage> ipMessages,List<IPMessage> ipMessageChecked){
        this.ipMessages = ipMessages;
        this.ipMessageChecked = ipMessageChecked;
    }
    @Override
    public void run() {
        for (IPMessage ipMessage : ipMessages){
            if (ProxyUtils.validateProxy(new Proxy(ipMessage.getIPAddress(),Integer.parseInt(ipMessage.getIPPort())))){
                ipMessageChecked.add(ipMessage);
            }
        }
    }
}
