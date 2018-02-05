package us.codecraft.webmagic.ipPoolService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.ipPoolService.redisDB.RedisDB;
import us.codecraft.webmagic.proxy.Proxy;

import java.util.List;

/**
 * 基于Redis的代理池管理服务
 * 存储结构是redis的set 集合
 * 创建于2018/02/01 by huangqc
 */
public class IPPoolManageService {

    private static final Logger logger = LoggerFactory.getLogger(IPPoolManageService.class);

    private static Jedis jedis = null;

    /**
     * 从代理池随机获取一条代理
     * @return Proxy
     */
    public static Proxy getProxy(){
        jedis = RedisDB.getJedis();
        String key = jedis.randomKey();//随机key值
        if (key==null) return null;
        String value = jedis.srandmember(key);//返回key集合的一个随机数
        //String value = jedis.spop(key);//移除并返回集合中的一个随机元素
        String[] result = value.split(":");
        String ip = result[0];
        int port = Integer.parseInt(result[1]);
        String type = result[2];
        Proxy proxy = new Proxy(ip,port,type);
        proxy.setRedisKey(key);
        logger.info("IPPoolManageService get a proxy "+value);
        RedisDB.close(jedis);
        return proxy;
    }

    /**
     * 从代理池删除一条代理
     * @param key 免费代理商
     * @param proxy ip代理
     */
    public static void deleteProxy(String key,Proxy proxy){
        jedis = RedisDB.getJedis();
        StringBuffer sb = new StringBuffer(proxy.getHost());
        sb.append(":").append(proxy.getPort()).append(":").append(proxy.getType());
        jedis.srem(key,sb.toString());
        logger.info("IPPoolManageService delete a proxy from redis "+sb.toString());
        RedisDB.close(jedis);
    }

    /**
     * 添加一条代理到IP代理池
     * @param key 免费代理商
     * @param proxy ip代理
     */
    public static void addProxy(String key,Proxy proxy){
        jedis = RedisDB.getJedis();
        StringBuffer sb = new StringBuffer(proxy.getHost());
        sb.append(":").append(proxy.getPort()).append(":").append(proxy.getType());
        jedis.sadd(key,sb.toString());
        logger.info("IPPoolManageService add a proxy to redis "+sb.toString());
        RedisDB.close(jedis);
    }
    /**
     * 添加多条代理到IP代理池
     * @param key 免费代理商
     * @param proxies ip代理列表
     */
    public static void addProxyList(String key, List<Proxy> proxies){
        jedis = RedisDB.getJedis();
        for (Proxy proxy : proxies){
            StringBuffer sb = new StringBuffer(proxy.getHost());
            sb.append(":").append(proxy.getPort()).append(":").append(proxy.getType());
            jedis.sadd(key,sb.toString());
        }
        logger.info("IPPoolManageService add a proxy list to redis ,the size is "+proxies.size());
        RedisDB.close(jedis);
    }
}
