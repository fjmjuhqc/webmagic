package us.codecraft.webmagic.pipeline;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.model.IPMessage;
import us.codecraft.webmagic.pipeline.redisDB.MyRedis;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.utils.ProxyUtils;

import java.util.ArrayList;
import java.util.List;

public class RedisPipeline implements Pipeline{
    @Override
    public void process(ResultItems resultItems, Task task) {
        List<IPMessage> ipMessages = resultItems.get("IPPool");
        MyRedis redis = new MyRedis();
        redis.setIPToList("IPPool",ipMessages);
        redis.close();
    }
}
