package us.codecraft.webmagic.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.ipPoolService.IPPoolManageService;
import us.codecraft.webmagic.utils.ProxyUtils;

public class IPProxyProvider implements ProxyProvider{

    private static final Logger logger = LoggerFactory.getLogger(IPProxyProvider.class);
    @Override
    public void returnProxy(Proxy proxy, Page page, Task task) {
        //把用完的代理重新存入IP动态池
        //IPPoolManageService.addProxy(proxy.getRedisKey(),proxy);
    }

    @Override
    public Proxy getProxy(Task task) {
        logger.info("-------------获取redis IPPool的一个proxy------------");
        Proxy proxy = IPPoolManageService.getProxy();

        while (proxy!=null&&!ProxyUtils.validateProxy(proxy)){
            //无效的IP要删除
            IPPoolManageService.deleteProxy(proxy.getRedisKey(),proxy);
            proxy = IPPoolManageService.getProxy();
        }

        logger.info("IPProxyProvider get the ip proxy = "+proxy);
        return proxy;
    }
}
