package us.codecraft.webmagic.pipeline;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.ipPoolService.IPPoolManageService;
import us.codecraft.webmagic.proxy.Proxy;

import java.util.List;
import java.util.Map;

public class RedisPipeline implements Pipeline{
    @Override
    public void process(ResultItems resultItems, Task task) {
        System.out.println("RedisPipeline get page: " + resultItems.getRequest().getUrl());
        for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
            //System.out.println(entry.getKey() + ":\t" + entry.getValue());
            List<Proxy> proxies = (List<Proxy>)entry.getValue();
            IPPoolManageService.addProxyList(entry.getKey(),proxies);
        }
    }
}
