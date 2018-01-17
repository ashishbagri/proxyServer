import java.net.InetSocketAddress;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class ProxyTest {
	static org.slf4j.Logger LOG = LoggerFactory.getLogger(ProxyTest.class);
	 private static final String REDIRECT_PATH = "/";
	private static HttpFiltersAdapter answerRedirectFilter(HttpRequest originalRequest) {
        return new HttpFiltersAdapter(originalRequest) {
            @Override
            public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                HttpResponse answer = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
                HttpHeaders.setHeader(answer, HttpHeaders.Names.LOCATION, REDIRECT_PATH);
               /// HttpHeaders.setHeader(answer, Names.CONNECTION, Values.CLOSE);
                return answer;
            }
        };
    }

    private static HttpFiltersAdapter answerContentFilter(HttpRequest originalRequest) {
        return new HttpFiltersAdapter(originalRequest) {
            @Override
            public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                ByteBuf buffer = Unpooled.wrappedBuffer("CONTENT".getBytes());
                HttpResponse answer = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buffer);
                HttpHeaders.setContentLength(answer, buffer.readableBytes());
                return answer;
            }
        };
    }
	public static void main(String[] args) throws Exception {
		LOG.warn("waring");
		String driverPath = "C:\\Users\\user\\Downloads\\chromedriver_win32\\chromedriver.exe";
		
		WebDriver driver = null;
		HttpProxyServer proxy = DefaultHttpProxyServer.bootstrap().withFiltersSource(new HttpFiltersSource() {
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext channelHandlerContext) {
                        return null;
                    }

                    public int getMaximumRequestBufferSizeInBytes() {
                        return Integer.MAX_VALUE;
                    }

                    public int getMaximumResponseBufferSizeInBytes() {
                        return Integer.MAX_VALUE;
                    }
                }).withPort(0).start();
	    //proxy.start(0);

	    //get the Selenium proxy object - org.openqa.selenium.Proxy;
	    org.openqa.selenium.Proxy seleniumProxy = getSel(proxy);
	    ChromeOptions options = new ChromeOptions();
		options.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
	    
	    // configure it as a desired capability
	    DesiredCapabilities capabilities = new DesiredCapabilities();
	    capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
	    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		//set chromedriver system property
		System.setProperty("webdriver.chrome.driver", driverPath);
		driver = new ChromeDriver(capabilities);

	    // open seleniumeasy.com
	    driver.get("https://www2.assetservicing.nabgroup.com/was/ncsonlineWEB/home.do");
	}
	 private static Proxy getSel(HttpProxyServer proxy2) throws Exception {
	    	InetSocketAddress add = proxy2.getListenAddress();//new InetSocketAddress(InetAddress.getLocalHost(), proxy.);
	    	Proxy proxy = new Proxy();
	        proxy.setProxyType(Proxy.ProxyType.MANUAL);

	        String proxyStr = String.format("%s:%d", "127.0.0.1", add.getPort());
	        proxy.setHttpProxy(proxyStr);
	        proxy.setSslProxy(proxyStr);
	        return proxy;
	    }
}
