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

/**
 * @author huangqc <br>
 * @date 2018/1/29
 */
public class XiciDailiPageProcessor implements PageProcessor{
    //部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    @Override
    //process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    public void process(Page page) {
        //部分二：定义如何抽取页面信息，并保存下来
        List<Proxy> proxyList = new ArrayList<Proxy>();

        List<Selectable> nodes = page.getHtml().xpath("//tbody/tr").nodes();
        for (Selectable node : nodes.subList(1,nodes.size())){
            String ip = node.xpath("/tr/td[2]/text()").get();
            String port = node.xpath("/tr/td[3]/text()").get();
            String type = node.xpath("/tr/td[6]/text()").get();
            //String ipSpeed = node.xpath("/tr/td[7]/div/@title").get();
            Proxy proxy = new Proxy(ip,Integer.parseInt(port),type);
            proxyList.add(proxy);
        }
        //设置skip之后，这个页面的结果不会被Pipeline处理
        //page.setSkip(true);
        page.putField("XCIPPool",proxyList);

        //部分三：从页面发现后续的url地址来抓取
        page.addTargetRequests(page.getHtml().xpath("//div[@class='pagination']/a[@class='next_page']/@href")
                .regex("/nn/\\d+").replace("/nn","http://www.xicidaili.com/nn").all());
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(new IPProxyProvider());
        Spider.create(new XiciDailiPageProcessor())
                .addUrl("http://www.xicidaili.com/nn")
                .setDownloader(httpClientDownloader)
                .addPipeline(new RedisPipeline())
                .thread(5)
                .run();
    }
}
