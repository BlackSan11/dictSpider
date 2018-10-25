package OnlineDictionary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Rating {
    private String hashId = "";
    private String questType = "";
    private String questTheme = "";
    private String exeDate = "";
    private String rating = "";
    private String subjID = "";
    private String subjName = "";

    public Rating(String questType, String questTheme, String exeDate, String rating, String subjID, String subjName) {
        this.questType = questType;
        this.questTheme = questTheme;
        this.exeDate = exeDate;
        this.rating = rating;
        this.subjID = subjID;
        this.subjName = subjName;
        this.hashId = setIdHash();
    }

    public String getQuestType() {
        return questType;
    }

    public String getQuestTheme() {
        return questTheme;
    }

    public String getExeDate() {
        return exeDate;
    }

    public String getRating() {
        return rating;
    }

    private String setIdHash(){
        String idHash = "";
        idHash = this.subjID + this.questType + this.questTheme + this.exeDate + this.rating;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hashInBytes = md.digest(idHash.getBytes(StandardCharsets.UTF_8));

        // bytes to hex
        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(b);
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }

    public String getHashId() {
        return hashId;
    }

    public String getSubjID() {
        return subjID;
    }

    public String getSubjName() {
        return subjName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Rating rating = (Rating) o;
        return Objects.equals(this.getHashId(), rating.getHashId());
    }

}
