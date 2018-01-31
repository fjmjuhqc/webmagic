package us.codecraft.webmagic.pipeline.redisDB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.model.IPMessage;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.utils.ProxyUtils;
import us.codecraft.webmagic.utils.SerializeUtil;

import java.util.List;


/**
 * Created by hg_yi on 17-8-9.
 */
public class MyRedis {
    private static final Logger logger = LoggerFactory.getLogger(MyRedis.class);

    Jedis jedis = RedisDB.getJedis();

    //将ip信息保存在Redis列表中
    public void setIPToList(String key,List<IPMessage> ipMessages) {
        //对得到的IP进行筛选，将IP速度在两秒以内的并且类型是https的留下，其余删除
        ipMessages = ProxyUtils.Filter(ipMessages);
        //TODO:这里有个问题，若是进行质量检测，代码没有进入保存，而是直接跳过
        //对拿到的ip进行质量检测，将质量不合格的ip在List里进行删除
        //ProxyUtils.IPIsable(ipMessages);
        //保存
        for (IPMessage ipMessage : ipMessages) {
            //首先将ipMessage进行序列化
            byte[] bytes = SerializeUtil.serialize(ipMessage);
            jedis.rpush(key.getBytes(), bytes);//添加到尾部（右边）
        }
    }

    //将Redis中保存的对象进行反序列化
    public IPMessage getIPByList(String key) {
        int rand = (int)(Math.random()*jedis.llen(key));//IPPool列表中的长度
        Object o = SerializeUtil.unserialize(jedis.lindex(key.getBytes(), 0));//按索引获取
        if (o instanceof IPMessage) {
            return (IPMessage)o;
        } else {
            logger.info("不是IPMessage的一个实例~");
            return null;
        }
    }

    //使用的IP 即从列表移除
    public Proxy getIPByRedisList(String key){
        if (jedis.llen(key)<=0) return null;
        Object ipInfo = SerializeUtil.unserialize(jedis.lpop(key.getBytes()));//移除并获取列表第一个元素
        if (ipInfo instanceof IPMessage) {
            IPMessage ipMessage = (IPMessage) ipInfo;
            return new Proxy(ipMessage.getIPAddress(),Integer.parseInt(ipMessage.getIPPort()));
        } else {
            logger.info("不是IPMessage的一个实例");
            return null;
        }
    }

    public  String getByKey(String key){
        String value = null;
        if (jedis != null){
            value = jedis.get(key);
        }
        return value;
    }

    public void deleteKey(String key) {
        jedis.del(key);
    }

    public void close() {
        RedisDB.close(jedis);
    }
}
