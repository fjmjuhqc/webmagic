package us.codecraft.webmagic.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.ipPoolService.IPPoolManageService;
import us.codecraft.webmagic.ipPoolService.redisDB.RedisDB;
import us.codecraft.webmagic.utils.ProxyUtils;

import java.util.*;

public class MyTimeJob extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(MyTimeJob.class);

    @Override
    public void run() {
        //先把dbIndex = 1的数据库更新
        logger.info("清洗可用ip已过期");
        Jedis jedis = RedisDB.getJedis();
        jedis.select(1);
        //获取所有key
        Set<String> keySet = jedis.keys("*");
        Iterator<String> it =  keySet.iterator();
        while (it.hasNext()){
            String key = it.next();
            Set<String> value = jedis.smembers(key);
            //单线程验证
            for (String v : value){
                //验证代理的有效性
                if (!ProxyUtils.validateProxy(v)){
                    jedis.srem(key,v);//无效的value 删除
                }
            }
        }
        RedisDB.close(jedis);

        //定期清洗IP动态库
        logger.info("清洗刚获取的ip动态库......");
        IPPoolManageService.checkProxy();
    }
}
