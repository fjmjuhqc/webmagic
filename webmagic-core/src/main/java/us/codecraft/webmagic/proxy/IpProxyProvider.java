package us.codecraft.webmagic.proxy;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.redisDB.MyRedis;
import us.codecraft.webmagic.utils.ProxyUtils;

public class IpProxyProvider implements ProxyProvider{
    @Override
    public void returnProxy(Proxy proxy, Page page, Task task) {
        //do nothing
    }

    @Override
    public Proxy getProxy(Task task) {
        System.out.println("-------------进入代理------------");
        MyRedis myRedis = new MyRedis();
        Proxy proxy = myRedis.getIPByRedisList("IPPool");
        if (proxy==null)
            return null;
        while (!ProxyUtils.ipIsValidate(proxy)){
            proxy = myRedis.getIPByRedisList("IPPool");
            if (proxy==null)
                break;
        }
        System.out.println("get the ip proxy"+proxy);
        return proxy;
    }
}
