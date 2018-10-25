package OnlineDictionary;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jfork.nproperty.Cfg;
import jfork.nproperty.ConfigParser;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Bot extends TelegramLongPollingBot {
    @Cfg
    private static String BOT_TOKEN = "";
    @Cfg
    private static String BOT_USERNAME = "";
    @Cfg
    private static String LOW_RATING_WARNING_MSG = "";
    @Cfg
    private static String TOTAL_LOW_RATINGS_SUBJECT_YEAR_MSG = "";
    @Cfg
    private static String PREFIX_TOTAL_LOW_RATINGS_YEAR_MSG = "";
    @Cfg
    private static String TOTAL_LOW_RATINGS_YEAR_MSG = "";
    @Cfg
    private static String GROUP_CHAT_ID = "";
    @Cfg
    private static String NO_TOTAL_LOW_RATINGS_SUBJECT_YEAR_MSG = "";
    @Cfg
    private static String ERROR_CHAT_ID = "";
    @Cfg
    private static String ERROR_MSG = "";
    @Cfg
    private static String TODAY_BAD_RATINGS_NO = "";
    private int showTO = 2;

    public Bot() {
        try {
            ConfigParser.parse(Bot.class, new InputStreamReader(
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

    @Override
    public void onUpdateReceived(Update update) {
        switch (update.getMessage().getText()) {
            case "/now":
                try {
                    send(String.valueOf(update.getMessage().getChatId()), "Одну минуту, я соберу информацию...");
                    RatingsAnalizer.getInstance().analize();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case "/total":
                send(String.valueOf(update.getMessage().getChatId()), "Одну минуту, я соберу информацию...");
                StringBuffer msg = new StringBuffer();
                List<Subject> subjects = DictionarySpider.getInstance().parsReports();//new LinkedList();
                if (subjects == null || subjects.size() < 0) break;
                Collections.sort(subjects, new SubjectBadRatingColComparator());
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("<b>НА " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "</b> \n\n");
                LinkedList badRatingsCount;
                double srAr = 0;
                for (Subject subject : subjects) {
                    if (subject.getRatings().size() < 1) continue;
                    srAr = 0;
                    stringBuffer.append("\uD83D\uDCD9<b>" + subject.getName() + "</b>");
                    for (Map.Entry<Integer, Integer> integerIntegerEntry : subject.getTotalRatingsCol().entrySet()) {
                        badRatingsCount = new LinkedList<>();
                        badRatingsCount.add(String.valueOf(integerIntegerEntry.getKey())); //оценка
                        badRatingsCount.add(String.valueOf(integerIntegerEntry.getValue())); //кол-во таких оценок
                        stringBuffer.append("\n---------" + generateFromTpl(TOTAL_LOW_RATINGS_SUBJECT_YEAR_MSG, badRatingsCount));
                        srAr += (integerIntegerEntry.getKey() * integerIntegerEntry.getValue());
                    }
                    srAr = (srAr / subject.getRatings().size());
                    stringBuffer.append("\n <b>Средний бал: </b>" + String.format("%.2f", srAr));
                    stringBuffer.append("\n\n");
                }
                send(String.valueOf(update.getMessage().getChatId()), stringBuffer.toString());
                break;
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
            Type listType = new TypeToken<LinkedList<Rating>>() {
            }.getType();
            LinkedList<Rating> yourClassList = new Gson().fromJson("[" + sb.toString() + "]", listType);
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

    public Boolean sendBadRatingMsg(Subject subject, Rating rating) {
        LinkedList<String> dinElms = new LinkedList();
        dinElms.add(subject.getName()); //название предмета
        dinElms.add(rating.getQuestType()); //тип задания
        dinElms.add(rating.getQuestTheme()); //тема задания
        dinElms.add(rating.getExeDate()); //дата выставления оценки
        dinElms.add(rating.getRating()); //оценка
        //формируем общее кол-во плохих оценок по предмету
        if (subject.getBadRatingsCol() != null && subject.getBadRatingsCol().size() > 0) {
            LinkedList<String> badRatingsCount = new LinkedList<>();
            String subjectYearBadRatingsTotalMsg = "";
            for (Integer badRating : subject.getBadRatingsCol().keySet()) {
                badRatingsCount = new LinkedList<>();
                badRatingsCount.add(String.valueOf(badRating)); //оценка
                badRatingsCount.add(String.valueOf(subject.getBadRatingsCol().get(badRating))); //кол-во таких оценок
                subjectYearBadRatingsTotalMsg = subjectYearBadRatingsTotalMsg + generateFromTpl(TOTAL_LOW_RATINGS_SUBJECT_YEAR_MSG, badRatingsCount);
            }
            dinElms.add(subjectYearBadRatingsTotalMsg);
        } else {
            dinElms.add(NO_TOTAL_LOW_RATINGS_SUBJECT_YEAR_MSG);// оценка
        }
        return send(GROUP_CHAT_ID, generateFromTpl(LOW_RATING_WARNING_MSG, dinElms));
    }

    public void sendAllBadRatingsCount(LinkedList<Subject> subjects) {
        if (subjects != null && subjects.size() > 0) {
            HashMap<Integer, Integer> badRatingsCol = new HashMap<>();
            for (Subject subject : subjects) {
                if (subject.getBadRatingsCol() != null && subject.getBadRatingsCol().size() > 0) {
                    for (Integer badRating : subject.getBadRatingsCol().keySet()) {
                        if (badRatingsCol.containsKey(badRating)) {
                            badRatingsCol.put(badRating, badRatingsCol.get(badRating) + subject.getBadRatingsCol().get(badRating));
                        } else {
                            badRatingsCol.put(badRating, subject.getBadRatingsCol().get(badRating));
                        }
                    }
                }

            }
            if (badRatingsCol != null && badRatingsCol.size() > 0) {
                String message = "";
                for (Integer integer : badRatingsCol.keySet()) {
                    LinkedList<String> dinElms = new LinkedList<>();
                    dinElms.add(String.valueOf(integer));
                    dinElms.add(String.valueOf(badRatingsCol.get(integer)));
                    message = message + generateFromTpl(TOTAL_LOW_RATINGS_YEAR_MSG, dinElms);
                }
                send(GROUP_CHAT_ID, PREFIX_TOTAL_LOW_RATINGS_YEAR_MSG + message);
            }
        }
    }

    private String generateFromTpl(String tpl, LinkedList<String> dinamicElements) {
        String msgResult = tpl;
        if (dinamicElements.size() > 0) {
            for (int i = 0; i < dinamicElements.size(); i++) {
                String repStr = "{" + (i + 1) + "}";
                msgResult = msgResult.replace(repStr, String.valueOf(dinamicElements.get(i)));
            }
        }
        return msgResult;
    }

    public void ratingsParsErr() {
        send(ERROR_CHAT_ID, ERROR_MSG);
    }

    public void withoutBadRatingsToday() {
        send(GROUP_CHAT_ID, TODAY_BAD_RATINGS_NO);
    }

    private Boolean send(String chatId, String text) { //
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chatId)
                .disableWebPagePreview()
                .setText(text)
                .setParseMode(ParseMode.HTML);
        try {
            execute(message); // Call method to send the message
            return true;
        } catch (TelegramApiException e) {
            message.setParseMode(null);
            try {
                execute(message); // Call method to send the message
                return true;
            } catch (TelegramApiException ee) {
                System.out.println("Problem with send message. Markdown?");
                ee.printStackTrace();

            }
        }
        return false;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
