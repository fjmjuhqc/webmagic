package us.codecraft.webmagic.processor.example;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.RedisPipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.IPProxyProvider;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;

public class IP3366PageProcessor implements PageProcessor {

    //部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    @Override
    public void process(Page page) {
        List<Proxy> proxyList = new ArrayList<Proxy>();
        List<Selectable> nodes = page.getHtml().xpath("//tbody/tr").nodes();
        for (Selectable node : nodes) {
            String ip = node.xpath("/tr/td[1]/text()").get();
            String port = node.xpath("/tr/td[2]/text()").get();
            String type = node.xpath("/tr/td[4]/text()").get();
            //String speed = node.xpath("/tr/td[6]/text()").get();
            Proxy proxy = new Proxy(ip,Integer.parseInt(port),type);
            proxyList.add(proxy);
        }

        page.putField("IP3366IPPool",proxyList);
        page.addTargetRequests(page.getHtml().xpath("//div[@id='listnav']/ul/a/@href")
                .replace("\\?stype=1","http://www.ip3366.net/free/?stype=1").all());
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(new IPProxyProvider());
        Spider.create(new IP3366PageProcessor()).addUrl("http://www.ip3366.net/free/?stype=1&page=1")
                //.setDownloader(httpClientDownloader)
                .addPipeline(new RedisPipeline())
                .thread(5)
                .run();
    }
}
