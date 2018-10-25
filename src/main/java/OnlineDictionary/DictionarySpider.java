package OnlineDictionary;

import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class DictionarySpider {
    @Cfg
    private static String URL = "";
    @Cfg
    private static String LOGIN = "";
    @Cfg
    private static String PASSWORD = "";

    @Cfg
    private static String SID_REPORTS = "";
    @Cfg
    private static String ThmID = "";
    @Cfg
    private static String PCLID_IUP = "";
    @Cfg
    private static String RPTID = "";

    private static String FIRST_AUTH_STEP_LINK = "";


    @Cfg
    private static String CID = "";
    @Cfg
    private static String SID_LOGIN = "";
    @Cfg
    private static String PID = "";
    @Cfg
    private static String CN = "";
    @Cfg
    private static String SFT = "";
    @Cfg
    private static String SCID = "";
    @Cfg
    private static String LASTNAME = "";
    @Cfg
    private static String CACHEVER = "";


    private static String PP = "";
    private static String BACK = "";

    private static String A = "";
    private static String NA = "";
    private static String TA = "";
    private static String RT = "";
    private static String RP = "";


    private static String AT = "";

    private static String ADT = "";
    private static String DDT = "";
    private HTTPHeaders headers = new HTTPHeaders();
    HashMap<String, String> cookiesActual = new HashMap();
    LinkedList<Subject> subjects = new LinkedList<>();
    private static DictionarySpider INSTANCE = new DictionarySpider();
    private static final Logger log = Logger.getLogger(DictionarySpider.class);

    private DictionarySpider() {
        PropertyConfigurator.configure("src/main/recources/log4j.properties");
        try {
            ConfigParser.parse(DictionarySpider.class, new InputStreamReader(
                    new FileInputStream("src/main/recources/config.ini"), "UTF-8"), "src/main/recources/config.ini");
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
    }

    private String getCustomVER() {
        Date date = new Date();
        return String.valueOf(date.getTime() / 1000L);
    }

    private String getPW2(String salt) {
        String pw2 = DigestUtils.md5Hex(salt + DigestUtils.md5Hex(this.PASSWORD));
        return pw2;
    }

    public void loginWarningStep1(String responseBody) throws ParseException {
        JSONObject responseBodyJsonOBJ, requestDataJsonOBJ = null;
        JSONParser jsonParser = new JSONParser();
        responseBodyJsonOBJ = (JSONObject) jsonParser.parse(responseBody);
        requestDataJsonOBJ = (JSONObject) jsonParser.parse(String.valueOf(responseBodyJsonOBJ.get("RequestData")));
        AT = String.valueOf(responseBodyJsonOBJ.get("AT"));
        Connection.Response noLimitResponse = null;
        try {
            noLimitResponse = Jsoup.connect(URL + "/asp/SecurityWarning.asp")
                    .method(Connection.Method.POST)
                    .headers(HTTPHeaders.DEFAULT_HEADERS)
                    .cookies(cookiesActual)
                    .ignoreContentType(true)
                    .data("at", AT)
                    .data("ATLIST", String.valueOf(requestDataJsonOBJ.get("ATLIST")))
                    .data("WarnType", "1")
                    .execute();
            noLimitResponse = Jsoup.connect(URL + "/angular/school/main/")
                    .method(Connection.Method.POST)
                    .headers(HTTPHeaders.DEFAULT_HEADERS)
                    .cookies(cookiesActual)
                    .ignoreContentType(true)
                    .data("AT", AT)
                    .data("VER", getVer())
                    .data("LoginType", "0")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getVer() {
        Date date = new Date();
        return String.valueOf(date.getTime() / 1000L);
    }

    public synchronized LinkedList<Subject> parsReports() {
        subjects = new LinkedList<Subject>();
        if(cookiesActual == null || cookiesActual.size() < 1) this.auth();
        while (true) {
            try {
                ZonedDateTime zonedNow = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/Moscow"));
                String dateT = zonedNow.getDayOfMonth() + "." + zonedNow.getMonthValue() + "." + String.valueOf(zonedNow.getYear()).substring(2, 4);
                Connection.Response getReportResponse = Jsoup.connect(URL + "/asp/Reports/ReportStudentGrades.asp")
                        .method(Connection.Method.POST)
                        .headers(HTTPHeaders.REPORT_HEADERS)
                        .cookies(cookiesActual)
                        .ignoreContentType(true)
                        .data("AT", AT)
                        .data("LoginType", "0")
                        .data("RPNAME", "Отчет+об+успеваемости")
                        .data("RPTID", "StudentGrades")
                        .data("ThmID", ThmID)
                        .data("VER", getVer())
                        .timeout(10000)
                        .followRedirects(true)
                        .execute();
                Elements subjectsList = getReportResponse.parse().select("select[class=form-control]");
                if (subjectsList == null || subjectsList.size() < 1) return null; //не можем получить список предметов
                Element subjectsList2 = subjectsList.first();
                subjectsList = subjectsList2.select("option");
                LinkedList<Rating> ratings = new LinkedList<>();
                for (Element subject : subjectsList) {
                    getReportResponse = Jsoup.connect(URL + "/asp/Reports/ReportStudentGrades.asp")
                            .method(Connection.Method.POST)
                            .headers(HTTPHeaders.REPORT_HEADERS)
                            .cookies(cookiesActual)
                            .ignoreContentType(true)
                            .data("LoginType", "0")
                            .data("AT", AT)
                            .data("VER", getVer())
                            .data("PP", "/asp/Reports/ReportStudentGrades.asp")
                            .data("BACK", "/asp/Reports/ReportStudentGrades.asp")
                            .data("ThmID", ThmID)
                            .data("RPTID", RPTID)
                            .data("A", A)
                            .data("NA", NA)
                            .data("TA", TA)
                            .data("RT", RT)
                            .data("RP", RP)
                            .data("SID", SID_REPORTS)
                            .data("PCLID_IUP", PCLID_IUP)
                            .data("SCLID", subject.attr("value"))
                            .data("ADT", "1.09.18") //dateT
                            .data("DDT", "31.08.19")
                            .timeout(10000)
                            .followRedirects(true)
                            .execute();
                    getReportResponse = Jsoup.connect(URL + "/asp/Reports/StudentGrades.asp")
                            .method(Connection.Method.POST)
                            .headers(HTTPHeaders.REPORT_HEADERS)
                            .cookies(cookiesActual)
                            .ignoreContentType(true)
                            .data("LoginType", "0")
                            .data("AT", AT)
                            .data("VER", getVer())
                            .data("PP", "/asp/Reports/ReportStudentGrades.asp")
                            .data("BACK", "/asp/Reports/ReportStudentGrades.asp")
                            .data("ThmID", "")
                            .data("RPTID", "StudentGrades")
                            .data("A", A)
                            .data("NA", NA)
                            .data("TA", TA)
                            .data("RT", RT)
                            .data("RP", RP)
                            .data("SID", SID_REPORTS)
                            .data("PCLID_IUP", PCLID_IUP)
                            .data("SCLID", subject.attr("value"))
                            .data("ADT", "1.09.18") //dateT
                            .data("DDT", "31.08.19")
                            .timeout(10000)
                            .followRedirects(true)
                            .execute();
                    //System.out.println(getReportResponse.body());
                    Elements trs = getReportResponse.parse().select("table[class=table-print]");
                    if (trs == null || trs.size() < 1) continue;
                    Element trs2 = trs.first();
                    trs = trs2.select("tr");
                    if (trs.size() > 2) {
                        trs.remove(trs.first());
                        trs.remove(trs.last());
                        for (Element tr : trs) {
                            Elements tds = tr.select("td");
                            ratings.add(new Rating(tds.get(0).text(), tds.get(1).text(), tds.get(2).text(), tds.get(3).text(), subject.attr("value"), subject.text()));
                        }
                        if (ratings.size() > 0) {
                            log.info("GETED REPORT: " + subject.text());
                            subjects.add(new Subject(subject.attr("value"), subject.text(), ratings));
                        }
                        ratings = new LinkedList<>();
                    }
                }
                return this.subjects;

            } catch (IOException e) {
                cookiesActual = new HashMap<>();
                this.auth();
                new Bot().ratingsParsErr();
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void auth() {
        while(true){
            System.out.println("Попытка авторизации");
            Connection.Response logInResponse = null;
            try {
                Connection.Response startCookiesResponse = Jsoup.connect(URL + "/asp/scripts/ajaxmethods_login.asp?" +
                        "CID=" + CID +
                        "&SID=" + SID_LOGIN +
                        "&PID=" + PID +
                        "&CN=" + CN +
                        "&SFT=" + SFT +
                        "&LASTNAME=" + LASTNAME +
                        "&method=kPrepareLoginForm&login=message" +
                        "&cacheVer=" + CACHEVER)
                        .method(Connection.Method.GET)
                        .execute();
                Connection.Response getDataResponse = Jsoup.connect(URL + "/webapi/auth/getdata")
                        .method(Connection.Method.POST)
                        .cookies(startCookiesResponse.cookies())
                        .ignoreContentType(true)
                        .execute();
                // System.out.println(getDataResponse.body());
                JSONParser getDataJP = new JSONParser();
                JSONObject getDataJObj = (JSONObject) getDataJP.parse(getDataResponse.body());
                logInResponse = Jsoup.connect(URL + "/asp/postlogin.asp")
                        .headers(headers.LOGIN_HEADERS)
                        .cookies(startCookiesResponse.cookies())
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .data("CID", this.CID)
                        .data("SID", this.SID_LOGIN)
                        .data("PID", this.PID)
                        .data("CN", this.CN)
                        .data("SFT", this.SFT)
                        .data("SCID", this.SCID)
                        .data("LoginType", "1")
                        .data("lt", String.valueOf(getDataJObj.get("lt")))
                        .data("UN", this.LOGIN)
                        .data("PW", getPW2(String.valueOf(getDataJObj.get("salt")).substring(0, 8)))
                        .data("pw2", getPW2(String.valueOf(getDataJObj.get("salt"))))
                        .data("ver", String.valueOf(getDataJObj.get("ver")))
                        .followRedirects(true)
                        .execute();
                log.info("AUTH: " + logInResponse.cookies().toString());
                //TODO: проверка, если залогинились или если нет в лог
                cookiesActual.putAll(logInResponse.cookies());
                if (logInResponse.body().contains("SecurityWarning")) {
                    loginWarningStep1(logInResponse.body());
                }

                System.out.println("Авторизовались");
                break;
            } catch (IOException e) {
                // System.out.println(logInResponse);
                //System.out.println(logInResponse.body());
                e.printStackTrace();
            } catch (ParseException e) {
                // System.out.println(logInResponse);
                e.printStackTrace();
            }
            System.out.println("Неудача");
        }
    }

    public static DictionarySpider getInstance() {
        return INSTANCE;
    }
}
