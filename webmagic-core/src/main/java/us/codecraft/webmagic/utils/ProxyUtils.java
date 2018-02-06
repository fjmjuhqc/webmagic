package us.codecraft.webmagic.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.model.IPMessage;
import us.codecraft.webmagic.proxy.Proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Pooled Proxy Object
 * 
 * @author yxssfxwzy@sina.com <br>
 * @since 0.5.1
 */

public class ProxyUtils {

	private static final Logger logger = LoggerFactory.getLogger(ProxyUtils.class);

	//验证代理是否有效
	public static boolean validateProxy(Proxy p) {
		Socket socket = null;
		try {
			socket = new Socket();
			InetSocketAddress endpointSocketAddr = new InetSocketAddress(p.getHost(), p.getPort());
			socket.connect(endpointSocketAddr, 3000);
			return true;
		} catch (IOException e) {
			logger.warn("FAILRE - CAN not connect!  remote: " + p);
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					logger.warn("Error occurred while closing socket of validating proxy", e);
				}
			}
		}
	}
	//验证代理是否有效
	public static boolean validateProxy(String proxyInfo) {
		String[] result = proxyInfo.split(":");
		String ip = result[0];
		int port = Integer.parseInt(result[1]);

		Socket socket = null;
		try {
			socket = new Socket();
			InetSocketAddress endpointSocketAddr = new InetSocketAddress(ip, port);
			socket.connect(endpointSocketAddr, 3000);
			return true;
		} catch (IOException e) {
			logger.warn("FAILRE - CAN not connect!  remote: {" + ip+":"+port+"}");
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					logger.warn("Error occurred while closing socket of validating proxy", e);
				}
			}
		}

	}
	/**
	 * 测试此IP是否有效
	 * @param ipMessage ip代理
	 * @return 有效：true；无效：false
	 */
	public static boolean ipIsValidate(IPMessage ipMessage,CloseableHttpClient httpClient){
		HttpHost proxy = new HttpHost(ipMessage.getIPAddress(), Integer.parseInt(ipMessage.getIPPort()));
		RequestConfig config = RequestConfig.custom().setProxy(proxy).setConnectTimeout(5000).
				setSocketTimeout(5000).build();
		HttpGet httpGet = new HttpGet("https://www.baidu.com");
		httpGet.setConfig(config);

		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;" +
				"q=0.9,image/webp,*/*;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit" +
				"/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			return true;
		} catch (IOException e) {
			logger.warn("FAILRE - CAN not connect!  remote: " + ipMessage);
			return false;
		}finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.error("Fail to close !",e);
			}
		}
	}

	@Deprecated
	public static boolean ipIsValidate(Proxy ip){
		try {
			//Proxy类代理方法
			URL url =  new URL("http://www.baidu.com");
			// 创建代理服务器
			InetSocketAddress addr=null;
			addr=new InetSocketAddress(ip.getHost(),ip.getPort());
			java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, addr); // http 代理
			URLConnection conn = url.openConnection(proxy);
			InputStream in = conn.getInputStream();
			String s = IOUtils.toString(in);
			if(s.indexOf("百度")>0){
				return true;
			}else {
				return false;
			}
		} catch (Exception e) {
			logger.info("fail to connect ..."+ip);
			return false;
		}

	}
	//对IP进行过滤
	public static List<IPMessage> Filter(List<IPMessage> ipMessageList) {
		List<IPMessage> newIPMessages = new ArrayList<IPMessage>();

		for (int i = 0; i < ipMessageList.size(); i++) {
			String ipType = ipMessageList.get(i).getIPType();
			String ipSpeed = ipMessageList.get(i).getIPSpeed();

			ipSpeed = ipSpeed.substring(0, ipSpeed.indexOf('秒'));
			double Speed = Double.parseDouble(ipSpeed);

			if (ipType.equals("HTTPS") && Speed <= 2.0) {
				newIPMessages.add(ipMessageList.get(i));
			}
		}
		return newIPMessages;
	}
	//过滤无效ip
	public  static void IPIsable(List<IPMessage> ipMessageList) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		for (int i = 0; i < ipMessageList.size(); i++) {
			Proxy proxy = new Proxy(ipMessageList.get(i).getIPAddress(),Integer.parseInt(ipMessageList.get(i).getIPPort()));
			if (!validateProxy(proxy)){
				//不可用代理直接删除
				ipMessageList.remove(ipMessageList.get(i));
				i--;
			}
		}
		try {
			if (httpClient != null) {
				httpClient.close();
			}
		} catch (IOException e) {
			logger.error("Fail to close !",e);
		}
	}

}
