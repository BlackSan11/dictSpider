package OnlineDictionary;

import java.util.*;

public class Subject {
    private String SCLID = "";
    private String name = "";
    private LinkedList<Rating> ratings = new LinkedList<>();
    private HashMap<Integer, Integer> badRatingsCol = new HashMap<>();
    private HashMap<Integer, Integer> totalRatingsCol = new HashMap<>();

    public Subject(String SCLID, String name, LinkedList<Rating> ratings) {
        this.SCLID = SCLID;
        this.name = name;
        this.ratings = ratings;
        setBadRatingsCount();
        setTotalRatingsCount();
    }

    public Subject(String SCLID, String name) {
        this.SCLID = SCLID;
        this.name = name;
    }

    public void setBadRatingsCount() {
        for (Rating rating : ratings) {
            try {
                if (Integer.parseInt(rating.getRating()) <= RatingsAnalizer.attentionThreshold) {
                    if (badRatingsCol.containsKey(Integer.parseInt(rating.getRating()))) {
                        badRatingsCol.put(Integer.parseInt(rating.getRating()), badRatingsCol.get(Integer.parseInt(rating.getRating())) + 1);
                    } else {
                        badRatingsCol.put(Integer.parseInt(rating.getRating()), 1);
                    }
                }
            } catch (NumberFormatException e) {
               // e.printStackTrace();
            }
        }
    }

    public void setTotalRatingsCount() {
        for (Rating rating : ratings) {
            try {
                if (totalRatingsCol.containsKey(Integer.parseInt(rating.getRating()))) {
                    totalRatingsCol.put(Integer.parseInt(rating.getRating()), totalRatingsCol.get(Integer.parseInt(rating.getRating())) + 1);
                } else {
                    totalRatingsCol.put(Integer.parseInt(rating.getRating()), 1);
                }
            } catch (NumberFormatException e) {
               // e.printStackTrace();
            }
        }
    }

    public void addRating(Rating rating) {
        this.ratings.add(rating);
    }

    public String getSCLID() {
        return SCLID;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Rating> getRatings() {
        return ratings;
    }

    public HashMap<Integer, Integer> getBadRatingsCol() {
        return this.badRatingsCol;
    }

    public HashMap<Integer, Integer> getTotalRatingsCol() {
        return totalRatingsCol;
    }

    protected int getBadRatingsCount() {
        int count = 0;
        for (Map.Entry<Integer, Integer> integerIntegerEntry : this.badRatingsCol.entrySet()) {
            count += integerIntegerEntry.getValue();
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Subject subject = (Subject) o;
        return Objects.equals(this.getName(), subject.getName());
    }

}

class SubjectBadRatingColComparator implements Comparator<Subject> {
    @Override
    public int compare(Subject o1, Subject o2) {
        return o2.getRatings().size() - o1.getRatings().size();
    }
}
