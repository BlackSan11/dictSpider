package OnlineDictionary;

import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class HTTPHeaders {
    @Cfg
    private static String HOST = "";
    @Cfg
    private static String URL = "";
    public static HashMap<String, String> DEFAULT_HEADERS = new HashMap<>();
    public static HashMap<String, String> LOGIN_HEADERS = new HashMap<>();
    public static HashMap<String, String> REPORT_HEADERS = new HashMap<>();

    public HTTPHeaders() {
        try {
            ConfigParser.parse(HTTPHeaders.class, "src/main/recources/config.ini");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DEFAULT_HEADERS.put("Host", HOST);
        DEFAULT_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
        DEFAULT_HEADERS.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        DEFAULT_HEADERS.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        DEFAULT_HEADERS.put("Accept-Encoding", "gzip, deflate");
        DEFAULT_HEADERS.put("Referer", URL + "/");
        DEFAULT_HEADERS.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        DEFAULT_HEADERS.put("X-Requested-With", "XMLHttpRequest");
        DEFAULT_HEADERS.put("Connection", "keep-alive");
        DEFAULT_HEADERS.put("Upgrade-Insecure-Requests", "1");


        LOGIN_HEADERS.put("Host", HOST);
        LOGIN_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
        LOGIN_HEADERS.put("Accept", "application/json, text/javascript, */*; q=0.01");
        LOGIN_HEADERS.put("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        LOGIN_HEADERS.put("Accept-Encoding", "gzip, deflate, br");
        LOGIN_HEADERS.put("Referer", URL+"/");
        LOGIN_HEADERS.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        LOGIN_HEADERS.put("X-Requested-With", "XMLHttpRequest");
        LOGIN_HEADERS.put("Connection", "keep-alive");
//

        REPORT_HEADERS.put("Host", HOST);
        REPORT_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
        REPORT_HEADERS.put("Accept", "text/html, */*; q=0.01");
        REPORT_HEADERS.put("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        REPORT_HEADERS.put("Accept-Encoding", "gzip, deflate");
        REPORT_HEADERS.put("Referer", URL + "/asp/Reports/ReportStudentGrades.asp");
        REPORT_HEADERS.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        REPORT_HEADERS.put("X-Requested-With", "XMLHttpRequest");
        REPORT_HEADERS.put("Connection", "keep-alive");
    }
}
