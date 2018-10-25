package OnlineDictionary;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import static javax.script.ScriptEngine.FILENAME;

public class RatingsAnalizer {
    static Integer attentionThreshold = 3;
    LinkedList<Subject> subjects = new LinkedList<>();
    LinkedList<Rating> ratingsHist = new LinkedList<>();
    private static RatingsAnalizer INSTANCE = new RatingsAnalizer();

    private RatingsAnalizer() {
    }

    public synchronized void analize() throws IOException, InterruptedException { //-274671491

        int flagHas = 0;

        subjects = DictionarySpider.getInstance().parsReports();
        ratingsHist = getHistory();
        System.out.println(ratingsHist);
       /* Gson gson = new Gson();
        String jsn = gson.toJson(subjects);
        System.out.println(jsn);
        LinkedList<Subject> obj = gson.fromJson(jsn, LinkedList.class);
        System.out.println(obj);*/
        ZonedDateTime zonedNow = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/Moscow"));
        String dateT = zonedNow.getDayOfMonth() + "." + zonedNow.getMonthValue() + "." + String.valueOf(zonedNow.getYear()).substring(2, 4);
        if (subjects != null) {
            if (subjects.size() > 0) {
                for (Subject subject : subjects) {
                    for (Rating rating : subject.getRatings()) {
                        if(rating.getRating().equals("-"))continue;
                        if (!ratingsHist.contains(rating) && Integer.parseInt(rating.getRating()) <= attentionThreshold) {
                            if (new Bot().sendBadRatingMsg(subject, rating)) saveToHistrory(rating);
                            flagHas++;
                        }
                    }
                    Thread.sleep(300);
                }
                if (flagHas == 0) {
                    new Bot().withoutBadRatingsToday();
                } else flagHas = 0;
                new Bot().sendAllBadRatingsCount(subjects);
            }
        } else {
            new Bot().ratingsParsErr();
        }
    }

    private LinkedList<Rating> getHistory() {
        BufferedReader br;
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream("src/main/recources/rhistory.json"), "UTF8"));
            String nextString;
            StringBuffer sb = new StringBuffer();
            while ((nextString = br.readLine()) != null) {
                sb.append(nextString);
            }
            Gson gson = new Gson();
            Type listType = new TypeToken<LinkedList<Rating>>(){}.getType();
            LinkedList<Rating> yourClassList = new Gson().fromJson("[" + sb.toString()+ "]", listType);
            br.close();
            return yourClassList;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveToHistrory(Rating rating) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter("src/main/recources/rhistory.json", true)
        );
        bufferedWriter.write(new Gson().toJson(rating) + ",");
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static RatingsAnalizer getInstance() {
        return INSTANCE;
    }
}
