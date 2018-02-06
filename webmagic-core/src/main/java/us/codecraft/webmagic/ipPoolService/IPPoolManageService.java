package us.codecraft.webmagic.ipPoolService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.ipPoolService.redisDB.RedisDB;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.thread.CheckIpThread;
import us.codecraft.webmagic.utils.ProxyUtils;

import java.util.*;

/**
 * 基于Redis的代理池管理服务
 * 存储结构是redis的set 集合
 * 创建于2018/02/01 by huangqc
 */
public class IPPoolManageService {

    private static final Logger logger = LoggerFactory.getLogger(IPPoolManageService.class);

    private static Jedis jedis = null;

    /**
     * redis的字符串转化为proxy
     * @param proxyInfo redis存储字符串
     * @return Proxy
     */
    private static Proxy stringToProxy(String proxyInfo){
        String[] result = proxyInfo.split(":");
        String ip = result[0];
        int port = Integer.parseInt(result[1]);
        String type = result[2];
        return new Proxy(ip,port,type);
    }

    /**
     * proxy 转化为string
     * @param proxy ip代理
     * @return String
     */
    private static String proxyToString(Proxy proxy){
        StringBuffer sb = new StringBuffer(proxy.getHost());
        sb.append(":").append(proxy.getPort()).append(":").append(proxy.getType());
        return sb.toString();
    }
    /**
     * 从代理池随机获取一条代理
     * @return Proxy
     */
    public static Proxy getProxy(){
        jedis = RedisDB.getJedis();
        jedis.select(1);
        String key = jedis.randomKey();//随机key值
        if (key==null) return null;
        String value = jedis.srandmember(key);//返回key集合的一个随机数
        Proxy proxy = stringToProxy(value);
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
        jedis.srem(key,proxyToString(proxy));
        logger.info("IPPoolManageService delete a proxy from redis "+proxyToString(proxy));
        RedisDB.close(jedis);
    }

    /**
     * 添加一条代理到IP代理池
     * @param key 免费代理商
     * @param proxy ip代理
     */
    public static void addProxy(String key,Proxy proxy){
        jedis = RedisDB.getJedis();
        jedis.sadd(key,proxyToString(proxy));
        logger.info("IPPoolManageService add a proxy to redis "+proxyToString(proxy));
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
            jedis.sadd(key,proxyToString(proxy));
        }
        logger.info("IPPoolManageService add a proxy list to redis ,the size is "+proxies.size());
        RedisDB.close(jedis);
    }

    /**
     * 检测IP代理的有效性
     */
    public static void  checkProxy(){
        jedis = RedisDB.getJedis();
        //获取所有key
        Set<String> keySet = jedis.keys("*");
        Iterator<String> it =  keySet.iterator();
        while (it.hasNext()){
            String key = it.next();
            Set<String> value = jedis.smembers(key);
            List<String> result = new ArrayList<String>(value);
            //对创建的子线程进行验证
            List<Thread> threads = new ArrayList<Thread>();
            //五个线程进行验证
            int threadNum = 5;
            int num = result.size()/threadNum;
            if (result.size()%2!=0||num==0){
                num++;
            }
            for (int i = 0; i < threadNum ; i++) {
                int end = num*(i+1)>result.size()? result.size():num*(i+1);
                CheckIpThread checkIpThread = new CheckIpThread(key,result.subList(num*i,end),jedis);
                threads.add(checkIpThread);
                checkIpThread.start();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            //单线程验证
//            for (String v : value){
//                //验证代理的有效性
//                if (ProxyUtils.validateProxy(stringToProxy(v))){
//                    jedis.select(1);//db1
//                    jedis.sadd(key,v);
//                    //复制完后删除
//                    jedis.select(0);
//                    jedis.srem(key,v);
//                }else {
//                    jedis.srem(key,v);//无效的value 删除
//                }
//            }
        }
        RedisDB.close(jedis);
    }

}
