package us.codecraft.webmagic.scheduler;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.RedisPipeline;
import us.codecraft.webmagic.processor.example.IP3366PageProcessor;
import us.codecraft.webmagic.processor.example.XiciDailiPageProcessor;
import us.codecraft.webmagic.proxy.IPProxyProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class MyTimer {
    public static void main (String[] args) {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(new IPProxyProvider());
        //本地IP抓取云代理的100条免费国内高匿代理
        Spider.create(new IP3366PageProcessor()).addUrl("http://www.ip3366.net/free/?stype=1&page=1")
                .addPipeline(new RedisPipeline())
                .thread(5)
                .run();
        //TODO:如何保证job在爬西刺代理前执行，另外已验证有效的代理在爬取时也可能失效
        MyTimeJob job = new MyTimeJob();
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        //设置定时任务，从现在开始，每两小时执行一次
        timer.schedule(job,date,2*60*60*1000);

        //采用IP代理池的代理 抓取西刺
        Spider.create(new XiciDailiPageProcessor())
                .addUrl("http://www.xicidaili.com/nn")
                .setDownloader(httpClientDownloader)
                .addPipeline(new RedisPipeline())
                .thread(5)
                .run();
    }
}
