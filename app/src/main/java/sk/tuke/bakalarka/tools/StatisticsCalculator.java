package sk.tuke.bakalarka.tools;


import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import sk.tuke.bakalarka.entities.ClothingItem;

public class StatisticsCalculator {
    public static List<Integer> calculatePercentiles(List<ClothingItem> clothingItems) {
        int countLessOrEqual = 0;
        int countAll = 0;
        List<Integer> percentiles = new ArrayList<>();
        //calculate sum of wears for every clothing item in the same type
        //get count of clothing items that have less or equal wear count as clothingItem

        //iterate over all clothingItems in list
        for(ClothingItem calculatedItem : clothingItems) {
            countAll += 1;

            //timesWorn = 0 => percentile = 0 go to another clothingItem
            if(calculatedItem.getTimesWorn() == 0) {
                percentiles.add(0);
                continue;
            }
            //timesWorn > 0 => compare calculatedItem with other clothingItems
            for(ClothingItem item : clothingItems) {
                if(item.equals(calculatedItem)) {
                    continue;
                }
                if (item.getTimesWorn() <= calculatedItem.getTimesWorn()) {
                    countLessOrEqual += 1;
                }
            }
            percentiles.add(countLessOrEqual);
            countLessOrEqual = 0;
        }


        countAll -= 1;

        for(int i = 0; i < percentiles.size(); i++) {
            float percentileFloat = (float) percentiles.get(i);
            percentileFloat = percentileFloat/countAll*100;
            percentiles.set(i,(int) percentileFloat);
        }
        return percentiles;
    }
    public static String calculatePricePerWear(ClothingItem clothingItem) {
        if(clothingItem.getTimesWorn() == 0) {
            return "never worn :(";
        }
        double pricePerWear = clothingItem.getPurchasePrice()/clothingItem.getTimesWorn();
        return roundToTwoDecimal(pricePerWear);
    }

    public static String calculateWearPerClean(ClothingItem clothingItem) {
        if(clothingItem.getTimesWashed() == 0) {
            return "never washed";
        }
        double wearPerClean = (double)  clothingItem.getTimesWorn() / clothingItem.getTimesWashed();
        return roundToTwoDecimal(wearPerClean);
    }

    public static String getWearingFrequency(ClothingItem clothingItem) {
        int percentile = clothingItem.getWearPercentile();
        if(percentile <= 25) {
            return  "very low";
        }else if(percentile <= 50) {
            return "low";
        }else if(percentile <= 75) {
            return "medium";
        }else if(percentile <= 100) {
            return "high";
        }
        return null;
    }

    public static String getInClosetFor(ClothingItem clothingItem) {
        if(clothingItem.getDateOfPurchase() == null) {
            return null;
        }
        long monthsDifference = ChronoUnit.MONTHS.between(clothingItem.getDateOfPurchase(), LocalDate.now());
        long yearsDifference = monthsDifference / 12;
        monthsDifference = monthsDifference % 12;
        String inClosetFor = String.format("%s years %s months", yearsDifference, monthsDifference);
        return inClosetFor;
    }

    public static String getTimesWornInLastYear(ClothingItem clothingItem) {
        return String.valueOf(clothingItem.getTimesWornLastYear(String.valueOf(LocalDate.now().getYear())));
    }
    public static String roundToTwoDecimal(double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        String roundedNumberText = df.format(number);
        return roundedNumberText;
    }

    public static String parseStatisticsToString(String description, String value) {
        return String.format("%s: %s",description,value);
    }
}
