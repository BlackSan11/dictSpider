import OnlineDictionary.Bot;
import OnlineDictionary.DictionarySpider;
import OnlineDictionary.RatingsAnalizer;
import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    @Cfg
    private static Integer TIMER_H = null;

    public static void main(String[] args) throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ConfigParser.parse(Main.class, new InputStreamReader(
                new FileInputStream("src/main/recources/config.ini"), "UTF-8"), "src/main/recources/config.ini");
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());
            //ratingsAnalizer.analize();
            LocalDateTime localNow = LocalDateTime.now();
            ZoneId currentZone = ZoneId.of("Europe/Moscow");
            ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
            ZonedDateTime zonedNext5 ;
            zonedNext5 = zonedNow.withHour(TIMER_H).withMinute(0).withSecond(0);
            if(zonedNow.compareTo(zonedNext5) > 0)
                zonedNext5 = zonedNext5.plusDays(1);
            Duration duration = Duration.between(zonedNow, zonedNext5);
            long initalDelay = duration.getSeconds();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                try {
                    RatingsAnalizer.getInstance().analize();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            scheduler.scheduleAtFixedRate(task, initalDelay, 24*60*60, TimeUnit.SECONDS);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
