package sk.tuke.bakalarka.tools;



import java.util.HashMap;
import java.util.Map;

public class ColorCategorizer {

    private static final Map<String, String> colorCategories = new HashMap<>();

    static {
        colorCategories.put("mediumvioletred", "pink");
        colorCategories.put("deeppink", "pink");
        colorCategories.put("palevioletred", "pink");
        colorCategories.put("hotpink", "pink");
        colorCategories.put("lightpink", "pink");
        colorCategories.put("pink", "pink");
        colorCategories.put("darkred", "red");
        colorCategories.put("red", "red");
        colorCategories.put("firebrick", "red");
        colorCategories.put("crimson", "red");
        colorCategories.put("indianred", "red");
        colorCategories.put("lightcoral", "red");
        colorCategories.put("salmon", "red");
        colorCategories.put("darksalmon", "red");
        colorCategories.put("lightsalmon", "red");
        colorCategories.put("orangered", "orange");
        colorCategories.put("tomato", "orange");
        colorCategories.put("darkorange", "orange");
        colorCategories.put("coral", "orange");
        colorCategories.put("orange", "orange");
        colorCategories.put("darkkhaki", "yellow");
        colorCategories.put("khaki", "yellow");
        colorCategories.put("peachpuff", "yellow");
        colorCategories.put("yellow", "yellow");
        colorCategories.put("palegoldenrod", "yellow");
        colorCategories.put("moccasin", "yellow");
        colorCategories.put("papayawhip", "yellow");
        colorCategories.put("lightgoldenrodyellow", "yellow");
        colorCategories.put("lemonchiffon", "yellow");
        colorCategories.put("lightyellow", "yellow");
        colorCategories.put("maroon", "brown");
        colorCategories.put("brown", "brown");
        colorCategories.put("saddlebrown", "brown");
        colorCategories.put("sienna", "brown");
        colorCategories.put("chocolate", "brown");
        colorCategories.put("darkgoldenrod", "brown");
        colorCategories.put("peru", "brown");
        colorCategories.put("rosybrown", "brown");
        colorCategories.put("goldenrod", "brown");
        colorCategories.put("sandybrown", "lightbrown");
        colorCategories.put("tan", "lightbrown");
        colorCategories.put("burlywood", "lightbrown");
        colorCategories.put("wheat", "lightbrown");
        colorCategories.put("navajowhite", "lightbrown");
        colorCategories.put("bisque", "lightbrown");
        colorCategories.put("blanchedalmond", "lightbrown");
        colorCategories.put("cornsilk", "lightbrown");
        colorCategories.put("indigo", "purple");
        colorCategories.put("purple", "purple");
        colorCategories.put("darkmagenta", "purple");
        colorCategories.put("darkviolet", "purple");
        colorCategories.put("darkslateblue", "purple");
        colorCategories.put("blueviolet", "purple");
        colorCategories.put("darkorchid", "purple");
        colorCategories.put("fuchsia", "purple");
        colorCategories.put("magenta", "purple");
        colorCategories.put("slateblue", "purple");
        colorCategories.put("mediumslateblue", "purple");
        colorCategories.put("mediumorchid", "purple");
        colorCategories.put("mediumpurple", "purple");
        colorCategories.put("orchid", "purple");
        colorCategories.put("violet", "purple");
        colorCategories.put("plum", "purple");
        colorCategories.put("thistle", "purple");
        colorCategories.put("lavender", "purple");
        colorCategories.put("midnightblue", "blue");
        colorCategories.put("navy", "blue");
        colorCategories.put("darkblue", "blue");
        colorCategories.put("mediumblue", "blue");
        colorCategories.put("blue", "blue");
        colorCategories.put("royalblue", "blue");
        colorCategories.put("steelblue", "blue");
        colorCategories.put("dodgerblue", "blue");
        colorCategories.put("deepskyblue", "blue");
        colorCategories.put("cornflowerblue", "blue");
        colorCategories.put("skyblue", "lightblue");
        colorCategories.put("lightskyblue", "lightblue");
        colorCategories.put("lightsteelblue", "lightblue");
        colorCategories.put("lightblue", "lightblue");
        colorCategories.put("powderblue", "lightblue");
        colorCategories.put("teal", "cyan");
        colorCategories.put("darkcyan", "cyan");
        colorCategories.put("lightseagreen", "cyan");
        colorCategories.put("cadetblue", "cyan");
        colorCategories.put("darkturquoise", "cyan");
        colorCategories.put("mediumturquoise", "cyan");
        colorCategories.put("turquoise", "cyan");
        colorCategories.put("aqua", "cyan");
        colorCategories.put("cyan", "cyan");
        colorCategories.put("aquamarine", "cyan");
        colorCategories.put("paleturquoise", "cyan");
        colorCategories.put("lightcyan", "cyan");
        colorCategories.put("darkgreen", "green");
        colorCategories.put("green", "green");
        colorCategories.put("darkolivegreen", "green");
        colorCategories.put("forestgreen", "green");
        colorCategories.put("seagreen", "green");
        colorCategories.put("olive", "green");
        colorCategories.put("olivedrab", "green");
        colorCategories.put("mediumseagreen", "green");
        colorCategories.put("limegreen", "green");
        colorCategories.put("lime", "green");
        colorCategories.put("springgreen", "green");
        colorCategories.put("mediumspringgreen", "green");
        colorCategories.put("darkseagreen", "green");
        colorCategories.put("mediumaquamarine", "green");
        colorCategories.put("yellowgreen", "green");
        colorCategories.put("lawngreen", "green");
        colorCategories.put("chartreuse", "green");
        colorCategories.put("lightgreen", "green");
        colorCategories.put("greenyellow", "green");
        colorCategories.put("palegreen", "green");
        colorCategories.put("mistyrose", "beige");
        colorCategories.put("antiquewhite", "beige");
        colorCategories.put("linen", "beige");
        colorCategories.put("beige", "beige");
        colorCategories.put("whitesmoke", "beige");
        colorCategories.put("lavenderblush", "beige");
        colorCategories.put("oldlace", "beige");
        colorCategories.put("aliceblue", "beige");
        colorCategories.put("seashell", "beige");
        colorCategories.put("ghostwhite", "beige");
        colorCategories.put("honeydew", "beige");
        colorCategories.put("floralwhite", "beige");
        colorCategories.put("azure", "beige");
        colorCategories.put("mintcream", "beige");
        colorCategories.put("snow", "beige");
        colorCategories.put("ivory", "beige");
        colorCategories.put("darkslategray", "gray");
        colorCategories.put("dimgray", "gray");
        colorCategories.put("slategray", "gray");
        colorCategories.put("gray", "gray");
        colorCategories.put("lightslategray", "gray");
        colorCategories.put("darkgray", "gray");
        colorCategories.put("lightgray", "gray");
        colorCategories.put("gainsboro", "gray");
        colorCategories.put("black", "black");
        colorCategories.put("white", "white");
        colorCategories.put("gold", "gold");
        colorCategories.put("silver", "silver");

    }

    public static String categorizeColor(String colorName) {
        return colorCategories.getOrDefault(colorName.toLowerCase(), "Other");
    }

}

