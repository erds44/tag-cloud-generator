import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

//import components.utilities.Reporter;
import jdk.javadoc.doclet.Reporter;

/**
 * Convert a text file into a well-formed HTML web page.
 *
 * @author Zhijian Yao & Dong Zhao Group 5
 */
public final class TagCloudGenerator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGenerator() {
    }

    /**
     * Separators for input file.
     */
    private static final String SEPARATORS = " \t\n\r,-.!?[]';:/()";

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code SEPARATORS}) or "separator string" (maximal length string of
     * characters in {@code SEPARATORS}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection entries(SEPARATORS) = {}
     * then
     *   entries(nextWordOrSeparator) intersection entries(SEPARATORS) = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection entries(SEPARATORS) /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of entries(SEPARATORS)  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of entries(SEPARATORS))
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";
        int i = position;
        boolean isSeparator = SEPARATORS.indexOf(text.charAt(i)) != -1;
        while (i < text.length()
                && isSeparator == (SEPARATORS.indexOf(text.charAt(i)) != -1)) {
            i++;
        }
        return text.substring(position, i);
    }

    /**
     * override compare method
     *
     * @return integer
     * @ensures make MapInt a decreasing order
     */
    private static class MapInt
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            int r = 0;
            if (o1.getValue() == o2.getValue()) {
                r = o1.getKey().compareTo(o2.getKey());
            } else {
                r = -o1.getValue().compareTo(o2.getValue());
            }
            return r;
        }
    }

    /**
     * override compare method
     *
     * @return integer
     * @ensures make MapString a Alphabetical order
     */
    private static class MapString
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            int r = 0;
            if (o1.getKey().equals(o2.getKey())) {
                r = -o1.getValue().compareTo(o2.getValue());
            } else {
                r = o1.getKey().compareTo(o2.getKey());
            }
            return r;
        }
    }

    /**
     * transfer input file into map.
     *
     * @param input
     *            the name of input
     * @param element
     *            the map containing words and counts
     */
    private static void transferInputToMap(BufferedReader input,
            Map<String, Integer> element) {
        String s = "";
        try {
            s = input.readLine();
        } catch (IOException e) {
            System.err.println("Error read input file");
        }
        while (s != null) {
            int index = 0;
            while (index < s.length()) {
                String word = nextWordOrSeparator(s, index);
                if (SEPARATORS.indexOf(word.charAt(0)) == -1) {
                    word = word.toLowerCase();
                    if (!element.containsKey(word)) {
                        element.put(word, 1);
                    } else {
                        int count = element.get(word);
                        count++;
                        element.replace(word, count);
                    }
                }
                index += word.length();

            }
            try {
                s = input.readLine();
            } catch (IOException e) {
                System.err.println("Errpr read input file");
            }
        }
    }

    /**
     * Convert input into a queue.
     *
     * @param input
     *            the name of the input
     * @param output
     *            the name of output
     * @param topWords
     *            the queue to store words and counts
     * @param numberOfWords
     *            number of top words
     * @return int the difference between max and min count
     */
    private static int convertToQueue(BufferedReader input, PrintWriter output,
            List<Map.Entry<String, Integer>> topWords, int numberOfWords) {
        // Initialization
        Comparator<Map.Entry<String, Integer>> intSort = new MapInt();
        Map<String, Integer> element = new TreeMap<String, Integer>();
        List<Map.Entry<String, Integer>> holder = new ArrayList<>();
        // Add to Map
        transferInputToMap(input, element);
        // integer sort
        Set<Map.Entry<String, Integer>> s = element.entrySet();
        Iterator<Map.Entry<String, Integer>> it = s.iterator();
        while (it.hasNext()) {
            holder.add(it.next()); // Aliasing will not affect my operation
        }
//        Reporter.assertElseFatalError(numberOfWords <= holder.size(),
//                "Error number of words exceeds the number of words in file");
        holder.sort(intSort);
        int range = holder.get(0).getValue()
                - holder.get(numberOfWords - 1).getValue();

        for (int i = 0; i < numberOfWords; i++) {
            topWords.add(holder.get(i));
        }
        return range;
    }

    /**
     * generate a html page.
     *
     * @param title
     *            the name of the page title
     * @param out
     *            the name of output
     * @param topWords
     *            the queue to store words and counts
     * @param range
     *            the different between max and min count
     * @param n
     *            number of top words
     * @param minCount
     *            the minimum count in the list
     *
     */
    static void generatePage(String title, PrintWriter out,
            List<Map.Entry<String, Integer>> topWords, int range, int n,
            int minCount) {
        out.println("<html>");
        out.println("   <head>");
        out.println(
                "       <title>TOP " + n + " word(s) in " + title + "</title>");
        out.println(
                "         <link href=\"tagCloud.css\" rel =\"stylesheet\" type=\"text/css\">");
        out.println("   </head>");
        out.println("   <body>");
        out.println("       <h2>TOP " + n + " word(s) in " + title + "</h2>");
        out.println("       <hr>");
        out.println("       <div class=\"cdiv\">");
        out.println("       <p class=\"cbox\">");
        int i = 0;
        while (i < topWords.size()) {
            Map.Entry<String, Integer> p = topWords.get(i);
            String word = p.getKey();
            int count = p.getValue();
            int font = (int) (((38.0 * (count - minCount)) / range)) + 11;
            out.println("<span style=\"cursor:default\" class=\"f" + font + "\""
                    + " title=\"count: " + count + "\">" + word + "</span>");
            i++;
        }
        out.println("       </p>");
        out.println("       </div>");
        out.println("   </body>");
        out.println("</html>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        List<Map.Entry<String, Integer>> topWords = new ArrayList<Map.Entry<String, Integer>>();
        BufferedReader input = null;
        PrintWriter output = null;
        System.out.println("Enter the input file name: ");
        String inputfile = in.nextLine();
        try {
            input = new BufferedReader(new FileReader(inputfile));
        } catch (IOException e) {
            System.err.println("Error open input file");
        }
        System.out.println("Enter the output file name: ");
        String outputfile = in.nextLine();
        try {
            output = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputfile)));
        } catch (IOException e) {
            System.err.println("Error writer into file: " + outputfile);
        }
        System.out.println("Enter the number of words: ");
        int numberOfWords = Integer.parseInt(in.nextLine());
        int range = convertToQueue(input, output, topWords, numberOfWords);
        int minCount = topWords.get(numberOfWords - 1).getValue();
        Comparator<Map.Entry<String, Integer>> stringSort = new MapString();
        topWords.sort(stringSort);
        generatePage(inputfile, output, topWords, range, numberOfWords,
                minCount);
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Error close input file");
        }
        output.close();
        in.close();
    }

}
