package cn.liguohao.myddns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author li-guohao
 * @date 2022/07/14
 */
public class MyDdnScript {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyDdnScript.class);

    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern HTTP_PATTERN =
        Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");
    private static final List<String> IPV4_NET_OPEN_API_LIST = new ArrayList<>();

    static {
        IPV4_NET_OPEN_API_LIST.add("http://checkip.amazonaws.com/");
        IPV4_NET_OPEN_API_LIST.add("https://ipv4.icanhazip.com/");
    }


    public static void main(String[] args) throws Exception {

        List<String> apiList = new ArrayList<>(IPV4_NET_OPEN_API_LIST.size());
        Collections.copy(IPV4_NET_OPEN_API_LIST, apiList);

        initAndGetAliyunDnsClient();


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<String> ipv4ResultList = new ArrayList<>();

                for (String api : apiList) {
                    try {
                        String ipv4 = doGet(api);
                        ipv4ResultList.add(ipv4);
                        LOGGER.info("success get ipv4 [{}] by request url {}", ipv4, api);
                    } catch (IOException e) {
                        LOGGER.warn("fail request, url: " + api, e);
                    }
                }



            }
        };


    }

    private static void initAndGetAliyunDnsClient() throws Exception {

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
