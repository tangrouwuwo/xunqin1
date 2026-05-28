package com.xunqin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.xunqin.mapper")
public class XunqinWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(XunqinWebsiteApplication.class, args);
    }

    /**
     * 应用启动完成后自动打开浏览器
     */
    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        String url = "http://localhost:8080/api";
        try {
            // 检查是否支持桌面操作
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("已自动打开浏览器: " + url);
            } else {
                // 如果不支持桌面操作，尝试使用系统命令
                String os = System.getProperty("os.name").toLowerCase();
                Runtime runtime = Runtime.getRuntime();
                if (os.contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    runtime.exec("open " + url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    runtime.exec("xdg-open " + url);
                }
                System.out.println("已自动打开浏览器: " + url);
            }
        } catch (Exception e) {
            System.err.println("无法自动打开浏览器，请手动访问: " + url);
            e.printStackTrace();
        }
    }
}
