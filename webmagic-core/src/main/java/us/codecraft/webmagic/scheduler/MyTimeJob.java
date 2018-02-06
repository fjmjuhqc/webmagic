package us.codecraft.webmagic.scheduler;

import us.codecraft.webmagic.ipPoolService.IPPoolManageService;

import java.util.TimerTask;

public class MyTimeJob extends TimerTask {

    @Override
    public void run() {
        //定期清洗IP动态库
        System.out.println("维护ip动态库......");
        IPPoolManageService.checkProxy();
    }
}
