

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class ProxyWebUiTest {
	
    @Test
    public void testRedirectWithWebDriver() throws Exception {
        HttpProxyServer proxyServer = null;
        WebDriver driver = null;
        try {

            HttpFiltersSourceAdapter filtersSource = new HttpFiltersSourceAdapter() {
                public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                    return new HttpFiltersAdapter(originalRequest) {
                        @Override
                        public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                            return null;
                        }

                        @Override
                        public HttpObject serverToProxyResponse(HttpObject httpObject) {
                            return httpObject;
                        }
                    };
                }
            };
            proxyServer = DefaultHttpProxyServer.bootstrap()//
                    .withFiltersSource(filtersSource)//
                    .withPort(9191)//
                    .start();
            String driverPath = "C:\\Users\\user\\Downloads\\chromedriver_win32\\chromedriver.exe";
    		
            System.setProperty("webdriver.chrome.driver", driverPath);
    		
            DesiredCapabilities capability = new DesiredCapabilities();
            ChromeOptions options = new ChromeOptions();
    		options.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
    	    
            capability.setCapability(ChromeOptions.CAPABILITY, options);
            capability.setCapability(CapabilityType.PROXY, getSel());
            driver = new ChromeDriver(capability);
            driver.manage().timeouts().pageLoadTimeout(50, TimeUnit.SECONDS);
            
    	    // configure it as a desired capability
    	   // capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
    		 //String urlStr = String.format("http://localhost:%d/somewhere", proxyServer.getListenAddress().getPort());
            driver.get("https://www2.assetservicing.nabgroup.com/was/ncsonlineWEB/home.do");
            String source = driver.getPageSource();
            
            new Scanner(System.in).next();
            //assertThat(source, containsString("CONTENT"));

        } catch(Exception e){
        	System.out.println("exception");
        }finally {
        
            if (driver != null) {
                driver.close();
            }
            if (proxyServer != null) {
                proxyServer.abort();
            }
        }
    }
    
    private Proxy getSel() throws Exception {
    	InetSocketAddress add = new InetSocketAddress(InetAddress.getLocalHost(), 9191);
    	Proxy proxy = new Proxy();
        proxy.setProxyType(Proxy.ProxyType.MANUAL);

        String proxyStr = String.format("%s:%d", "127.0.0.1", add.getPort());
        proxy.setHttpProxy(proxyStr);
        proxy.setSslProxy(proxyStr);
        return proxy;
    }
    

}