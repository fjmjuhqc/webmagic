package us.codecraft.webmagic.thread;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.ipPoolService.IPPoolManageService;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.utils.ProxyUtils;

import java.util.List;

/**
 * 多线程验证爬取的ip代理是否有效
 */
public class CheckIpThread extends Thread {
    private List<String> proxys;
    private Jedis jedis;
    private String key;

    public CheckIpThread(String key,List<String> proxys,Jedis jedis){
        this.key = key;
        this.proxys = proxys;
        this.jedis = jedis;
    }
    @Override
    public void run() {
        for (String v : proxys){
            //验证代理的有效性
            if (ProxyUtils.validateProxy(v)){
                jedis.select(1);//db1
                jedis.sadd(key,v);
                //复制完后删除
                jedis.select(0);
                jedis.srem(key,v);
            }else {
                jedis.srem(key,v);//无效的value 删除
            }
        }
    }
}
