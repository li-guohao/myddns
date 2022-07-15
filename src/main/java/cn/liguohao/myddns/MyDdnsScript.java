package cn.liguohao.myddns;

import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author li-guohao
 * @date 2022/07/14
 */
public class MyDdnsScript {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyDdnsScript.class);

    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern HTTP_PATTERN =
        Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");
    private static final List<String> IPV4_NET_OPEN_API_LIST = new ArrayList<>();

    private static final String MY_DOMAIN = System.getenv(EnvNameConstants.MY_DOMAIN);
    private static final String DEFAULT_DOMAIN = "liguohao.cn";

    private static final String DOMAIN = Strings.isEmpty(MY_DOMAIN) ? DEFAULT_DOMAIN : MY_DOMAIN;
    private static final String HOME_DOMAIN = "home" + "." + DOMAIN;
    /**
     * key: domainPrefix    value: remarks
     */
    private static final Map<String, String > DOMAIN_PREFIX_MAP = new HashMap<>();


    static {
        IPV4_NET_OPEN_API_LIST.add("http://checkip.amazonaws.com/");
        IPV4_NET_OPEN_API_LIST.add("https://ipv4.icanhazip.com/");

        // 备注不支持中文
        DOMAIN_PREFIX_MAP.put("router", "OpenWrt");
        DOMAIN_PREFIX_MAP.put("media", "Jellyfin");
        DOMAIN_PREFIX_MAP.put("webserver", "NginxWebUI");
        DOMAIN_PREFIX_MAP.put("blog", "Halo");
        DOMAIN_PREFIX_MAP.put("file", "Cloudreve");
        DOMAIN_PREFIX_MAP.put("music", "Navidrome");
        DOMAIN_PREFIX_MAP.put("monitor", "Grafana");
        DOMAIN_PREFIX_MAP.put("prom", "Prometheus");
        DOMAIN_PREFIX_MAP.put("status", "Statping");
        DOMAIN_PREFIX_MAP.put("container", "Portainer");
        DOMAIN_PREFIX_MAP.put("git", "Gitea");

    }


    public static void main(String[] args) throws Exception {

        List<String> apiList = new ArrayList<>(IPV4_NET_OPEN_API_LIST.size());
        Collections.copy(IPV4_NET_OPEN_API_LIST, apiList);


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LOGGER.info("start exec schedule task");

                // 1. 由于路由器已经设置了二级域名，查询这个二级域名的解析是否存在，直接获取IP即可
                Optional<DescribeSubDomainRecordsResponse.Record> recordOptional =
                    IAcsClientKit.getSubDomainRecord(HOME_DOMAIN);

                if (recordOptional.isPresent()) {
                    // 更新(添加或者修改)其它子域名的记录 和 备注
                    for (Map.Entry<String, String> entry : DOMAIN_PREFIX_MAP.entrySet()) {
                        IAcsClientKit.updateSubDomainRecord(entry.getKey(), DOMAIN,
                            recordOptional.get().getValue(), entry.getValue());
                    }

                } else {
                    LOGGER.warn("unknown ipv4 record value with subdomain={}", HOME_DOMAIN);
                }

                // 如果 上面的不行，再通过互联网接口，获取外网IP进行设置
                // 如果上面的跑的没问题，下方的就不实现了
//                List<String> ipv4ResultList = new ArrayList<>();
//
//                for (String api : apiList) {
//                    try {
//                        String ipv4 = doGet(api);
//                        ipv4ResultList.add(ipv4);
//                        LOGGER.info("success get ipv4 [{}] by request url {}", ipv4, api);
//                    } catch (IOException e) {
//                        LOGGER.warn("fail request, url: " + api, e);
//                    }
//                }


                LOGGER.info("finish once exec schedule task");
            }
        };

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(runnable, 1, 15, TimeUnit.MINUTES);


        LOGGER.info("ddns script has started.");
        Thread.currentThread().join();


    }


    private static String doGet(String url) throws IOException {
        if (url == null || "".equals(url) || !HTTP_PATTERN.matcher(url).matches()) {
            throw new IllegalArgumentException("invalid  url: " + url);
        }

        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(new URL(url).openStream()))) {
            String ip = in.readLine();
            if (IPV4_PATTERN.matcher(ip).matches()) {
                return ip;
            } else {
                throw new IOException("invalid IPv4 address: " + ip);
            }
        }

    }
}
