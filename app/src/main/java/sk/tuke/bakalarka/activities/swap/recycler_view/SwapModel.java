package sk.tuke.bakalarka.activities.swap.recycler_view;

public class SwapModel {
    String imageUrl;
    String condition;
    String price;
    public SwapModel(String imageUrl, String condition, String price) {
        this.condition = condition;
        this.imageUrl = imageUrl;
        this.price = price;
    }
}
