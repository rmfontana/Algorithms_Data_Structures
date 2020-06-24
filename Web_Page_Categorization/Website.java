import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.time.LocalDate;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Website {
    public String HTMLContent;
    public String content;
    public String headings;
    public String URL;
    public Map<String, Integer> words;
    public Map<String, Integer> importantWords;
    ArrayList<String> connectedSites;

    public Website(String URL) throws IOException {
        this.HTMLContent = "";
        this.content = "";
        this.headings = "";
        this.URL = URL;
        this.connectedSites = new ArrayList<>();
        this.words = new HashMap();
        this.importantWords = new HashMap();
    }

    public static String getCutURL(String w) {
        return w.replaceAll("[^A-Za-z]+", "");
    }

    private static double idf(ArrayList<Website> wArray, String key) {

        double idf = 0;
        double numberOfDocuments = wArray.size();
        double numberOfDocumentsWithTerm = getPagesWithWord(wArray, key);

        idf = Math.log(numberOfDocuments/(numberOfDocumentsWithTerm + 1));
        idf = Double.parseDouble(String.format("%2.10f", idf));

        return idf;
    }

    private static double tf(Website w, String key) {
        // term frequency = (# times word appears in document / # total words in document)

        double tf;
        if (w.words.containsKey(key)) {
            // augmented frequency
            double raw = w.words.get(key); // raw frequency
            tf = Math.log10(1 + raw);
            tf = Double.parseDouble(String.format("%2.10f", tf));

        } else if(w.importantWords.containsKey(key)) {
            tf = w.importantWords.get(key);
            tf = Double.parseDouble(String.format("%2.10f", tf));
        } else {
            tf = 0;
        }


        return tf;
    }

    private static int getPagesWithWord(ArrayList<Website> w, String word) {
        int pagesWithWord = 0;
        for (Website website : w) {
            if (website.words.containsKey(word)) {
                pagesWithWord += 1;
            }
        }
        return pagesWithWord;
    }

    public static Website similarity(Website w1, ArrayList<Website> w) {
        // go through all the words in our website
        Website mostSimilarSite = null;
        double mostSimilar = 0;
        double similarity;
        double dotproduct = 0;
        double bottom = 0;
        ArrayList<String> allWords = new ArrayList<>();
        ArrayList<String> allHeadings = new ArrayList<>();

        for (String word : w1.words.keySet()) {
            if (!allWords.contains(word)) {
                allWords.add(word);
            }
        }

        for (String word : w1.importantWords.keySet()){
            if (!allHeadings.contains(word)){
                allHeadings.add(word);
            }
        }

        for (Website website : w) {
            for (String word : website.words.keySet()) {
                if (!allWords.contains(word)) {
                    allWords.add(word);
                }
            }

            for (String word : website.importantWords.keySet()) {
                if (!allHeadings.contains(word)){
                    allHeadings.add(word);
                }
            }
        }

        int counter = 0;
        double[] document1 = new double[allWords.size() + allHeadings.size()];
        // apply separate weights for heading vs body
        for (String word : allWords) {
            document1[counter] = tf(w1, word) * idf(w, word) *.20;
            counter += 1;
        }

        for (String word : allHeadings) {
            document1[counter] = tf(w1, word) * idf(w, word) *.80;
            counter += 1;
        }

        double[] document2 = new double[allWords.size() + allHeadings.size()];
        for (Website website : w) {
            dotproduct = 0;
            bottom = 0;
            counter = 0;


            for (String word : allWords) {
                double tf = tf(website, word);
                double idf = idf(w, word);

                if (tf == 0 || idf == 0) {
                    document2[counter] = 0;
                } else {
                    document2[counter] = tf * idf *.20;
                }
                counter += 1;
            }

            for (String word : allHeadings) {
                double tf = tf(website, word);
                double idf = idf(w, word);

                if (tf == 0 || idf == 0) {
                    document2[counter] = 0;
                } else {
                    document2[counter] = tf * idf * .80;
                }
                counter += 1;
            }

            double sum1 = 0, sum2 = 0;
            double key1=0, key2=0;
            for(int i=0; i < document1.length; i++){
                key1 = document1[i];
                key2 = document2[i];

                if(key1 != 0 && key2 !=0){
                    dotproduct = (key1 * key2) + dotproduct;
                    sum1 = Math.pow(key1, 2) + sum1;
                    sum2 = Math.pow(key2, 2) + sum2;
                }
            }

            bottom = Math.sqrt(sum1) * Math.sqrt(sum2);

            if (dotproduct == 0 || bottom == 0) {
                similarity = 0;
            } else {
                similarity = dotproduct / bottom;
                similarity = Double.parseDouble(String.format("%2.10f", similarity));
            }


            System.out.println("Checking " + website.URL);
            System.out.println("Similarity score " + similarity);
            System.out.println("Most similar score is " + mostSimilar);

            if (similarity > mostSimilar) {
                mostSimilar = similarity;
                mostSimilarSite = website;
                System.out.println("most similar site " + mostSimilarSite.URL);
            }
        }

        return mostSimilarSite;
    }

    public static Website getPersistent(String URL) throws Exception {
        // Get contents from local file

        // "hash" the url to get the key
        String filePath;
        if (URL.startsWith("C:") && URL.endsWith(".txt")) {
            filePath = URL;
        } else if (URL.endsWith(".txt")) {
            filePath = System.getProperty("user.dir") + File.separator + getCutURL(URL);
        } else {
            filePath = System.getProperty("user.dir") + File.separator + getCutURL(URL);
        }

        File file = new File(filePath);

        // get the first few lines
        BufferedReader Buff = new BufferedReader(new FileReader(file));
        Buff.readLine(); // throw out the date
        Website w = new Website(Buff.readLine()); // get the URL

        // get the rest of the contents
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = Buff.readLine()) != null) {
            sb.append(line);
        }

        // fill in all the data
        w.HTMLContent = sb.toString();
        w.setWords();
        w.getConnectedSites();
        return w;

    }

    public static Boolean checkPersistent(String URL) {
        // see if we have a file on this URL

        String filePath = URL.replaceAll("[^A-Za-z]+", "") + ".txt";
        File checkDir = new File(System.getProperty("user.dir"));
        File[] allDir = checkDir.listFiles();

        if (allDir != null) {
            for (File dir : allDir) {
                if (dir.toString().equals(filePath)) {
                    return true;
                }
            }
        }

        return false;
    }

    public LocalDate getDate() throws IOException {
        // get when the website was last updated

        URL url = new URL(this.URL);
        URLConnection connection = url.openConnection();
        String dateInString = connection.getHeaderField("Last-Modified");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        return LocalDate.parse(dateInString, formatter);
    }

    public void getConnectedSites() {
        // go through all document links and add them website
        Document document = Jsoup.parse(HTMLContent, "https://en.wikipedia.org/wiki/");
        Elements URLs = document.select("a[href]");

        for (Element e : URLs) {
            String url = (e.attr("abs:href"));
            int numberOfColons = url.replaceAll("[^:]", "").length();

            if (numberOfColons == 1 && !url.contains("#") && url.contains("https://en.wikipedia.org/wiki/")) {
                connectedSites.add(url);
            }
        }
    }

    public void setWords() {
        // stop words obtained from nltk stop words library
        String[] stopWords = {"a", "about", "above", "after", "again", "against", "ain", "all", "am", "an", "and", "any", "are", "aren", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "couldn", "couldn't", "d", "did", "didn", "didn't", "do", "does", "doesn", "doesn't", "doing", "don", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "isn", "isn't", "it", "it's", "its", "itself", "just", "ll", "m", "ma", "me", "mightn", "mightn't", "more", "most", "mustn", "mustn't", "my", "myself", "needn", "needn't", "no", "nor", "not", "now", "o", "of", "off", "on", "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "re", "s", "same", "shan", "shan't", "she", "she's", "should", "should've", "shouldn", "shouldn't", "so", "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "ve", "very", "was", "wasn", "wasn't", "we", "were", "weren", "weren't", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "won", "won't", "wouldn", "wouldn't", "y", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "could", "he'd", "he'll", "he's", "here's", "how's", "i'd", "i'll", "i'm", "i've", "let's", "ought", "she'd", "she'll", "that's", "there's", "they'd", "they'll", "they're", "they've", "we'd", "we'll", "we're", "we've", "what's", "when's", "where's", "who's", "why's", "would", "able", "abst", "accordance", "according", "accordingly", "across", "act", "actually", "added", "adj", "affected", "affecting", "affects", "afterwards", "ah", "almost", "alone", "along", "already", "also", "although", "always", "among", "amongst", "announce", "another", "anybody", "anyhow", "anymore", "anyone", "anything", "anyway", "anyways", "anywhere", "apparently", "approximately", "arent", "arise", "around", "aside", "ask", "asking", "auth", "available", "away", "awfully", "b", "back", "became", "become", "becomes", "becoming", "beforehand", "begin", "beginning", "beginnings", "begins", "behind", "believe", "beside", "besides", "beyond", "biol", "brief", "briefly", "c", "ca", "came", "cannot", "can't", "cause", "causes", "certain", "certainly", "co", "com", "come", "comes", "contain", "containing", "contains", "couldnt", "date", "different", "done", "downwards", "due", "e", "ed", "edu", "effect", "eg", "eight", "eighty", "either", "else", "elsewhere", "end", "ending", "enough", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "except", "f", "far", "ff", "fifth", "first", "five", "fix", "followed", "following", "follows", "former", "formerly", "forth", "found", "four", "furthermore", "g", "gave", "get", "gets", "getting", "give", "given", "gives", "giving", "go", "goes", "gone", "got", "gotten", "h", "happens", "hardly", "hed", "hence", "hereafter", "hereby", "herein", "heres", "hereupon", "hes", "hi", "hid", "hither", "home", "howbeit", "however", "hundred", "id", "ie", "im", "immediate", "immediately", "importance", "important", "inc", "indeed", "index", "information", "instead", "invention", "inward", "itd", "it'll", "j", "k", "keep", "keeps", "kept", "kg", "km", "know", "known", "knows", "l", "largely", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "line", "little", "'ll", "look", "looking", "looks", "ltd", "made", "mainly", "make", "makes", "many", "may", "maybe", "mean", "means", "meantime", "meanwhile", "merely", "mg", "might", "million", "miss", "ml", "moreover", "mostly", "mr", "mrs", "much", "mug", "must", "n", "na", "name", "namely", "nay", "nd", "near", "nearly", "necessarily", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "ninety", "nobody", "non", "none", "nonetheless", "noone", "normally", "nos", "noted", "nothing", "nowhere", "obtain", "obtained", "obviously", "often", "oh", "ok", "okay", "old", "omitted", "one", "ones", "onto", "ord", "others", "otherwise", "outside", "overall", "owing", "p", "page", "pages", "part", "particular", "particularly", "past", "per", "perhaps", "placed", "please", "plus", "poorly", "possible", "possibly", "potentially", "pp", "predominantly", "present", "previously", "primarily", "probably", "promptly", "proud", "provides", "put", "q", "que", "quickly", "quite", "qv", "r", "ran", "rather", "rd", "readily", "really", "recent", "recently", "ref", "refs", "regarding", "regardless", "regards", "related", "relatively", "research", "respectively", "resulted", "resulting", "results", "right", "run", "said", "saw", "say", "saying", "says", "sec", "section", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sent", "seven", "several", "shall", "shed", "shes", "show", "showed", "shown", "showns", "shows", "significant", "significantly", "similar", "similarly", "since", "six", "slightly", "somebody", "somehow", "someone", "somethan", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specifically", "specified", "specify", "specifying", "still", "stop", "strongly", "sub", "substantially", "successfully", "sufficiently", "suggest", "sup", "sure", "take", "taken", "taking", "tell", "tends", "th", "thank", "thanks", "thanx", "thats", "that've", "thence", "thereafter", "thereby", "thered", "therefore", "therein", "there'll", "thereof", "therere", "theres", "thereto", "thereupon", "there've", "theyd", "theyre", "think", "thou", "though", "thoughh", "thousand", "throug", "throughout", "thru", "thus", "til", "tip", "together", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "ts", "twice", "two", "u", "un", "unfortunately", "unless", "unlike", "unlikely", "unto", "upon", "ups", "us", "use", "used", "useful", "usefully", "usefulness", "uses", "using", "usually", "v", "value", "various", "'ve", "via", "viz", "vol", "vols", "vs", "w", "want", "wants", "wasnt", "way", "wed", "welcome", "went", "werent", "whatever", "what'll", "whats", "whence", "whenever", "whereafter", "whereas", "whereby", "wherein", "wheres", "whereupon", "wherever", "whether", "whim", "whither", "whod", "whoever", "whole", "who'll", "whomever", "whos", "whose", "widely", "willing", "wish", "within", "without", "wont", "words", "world", "wouldnt", "www", "x", "yes", "yet", "youd", "youre", "z", "zero", "a's", "ain't", "allow", "allows", "apart", "appear", "appreciate", "appropriate", "associated", "best", "better", "c'mon", "c's", "cant", "changes", "clearly", "concerning", "consequently", "consider", "considering", "corresponding", "course", "currently", "definitely", "described", "despite", "entirely", "exactly", "example", "going", "greetings", "hello", "help", "hopefully", "ignored", "inasmuch", "indicate", "indicated", "indicates", "inner", "insofar", "it'd", "keep", "keeps", "novel", "presumably", "reasonably", "second", "secondly", "sensible", "serious", "seriously", "sure", "t's", "third", "thorough", "thoroughly", "three", "well", "wonder"};
        ArrayList<String> stopWordList = new ArrayList<>(Arrays.asList(stopWords));

        if (content.equals("")) {
            Document document = Jsoup.parse(HTMLContent);
            Elements paragraphs = document.getElementsByTag("p");
            Elements lists = document.getElementsByTag("li");

            content = paragraphs.text() + lists.text();

            Elements hTags = document.select("h1, h2, h3, h4, h5, h6");
            headings = hTags.text();
        }

        String[] wordarray = content.replaceAll("[^a-zA-Z]", " ").split("\\b+");
        ArrayList<String> wordsList = new ArrayList<>(Arrays.asList(wordarray));
        String[] importantwordarray = headings.replaceAll("[^a-zA-Z]", " ").split("\\b+");
        ArrayList<String> importantwordList = new ArrayList<>(Arrays.asList(importantwordarray));


        for (String importantWord : importantwordarray){
            importantWord = importantWord.toLowerCase();
            if (importantWords.containsKey(importantWord)) {
                int value = importantWords.get(importantWord);
                importantWords.put(importantWord, value + 1);
            } else if (importantWord.length() > 1) {
                importantWords.put(importantWord, 1);
            }
        }

        for (String word : wordarray) {
            word = word.toLowerCase();
            if (words.containsKey(word)) {
                int value = words.get(word);
                words.put(word, value + 1);
            } else if (word.length() > 1 && !stopWordList.contains(word) && !importantwordList.contains((word))) {
                words.put(word, 1);
            }

            if(importantWords.containsKey(word)){
                int value = importantWords.get(word);
                if(wordsList.contains(word)){
                   int value2 =  Collections.frequency(wordsList, word);
                   importantWords.put(word, value + value2);
                }
            }
        }

    }

    public void getContents() throws IOException {
        // gets contents of website (and returns them)
        Document document = Jsoup.connect(this.URL).get();

        // change this so that instead of website.content this is held by the local files
        HTMLContent = document.toString();
        Elements paragraphs = document.getElementsByTag("p");
        Elements lists = document.getElementsByTag("li");

        content = paragraphs.text() + lists.text();

        // get headings and put them in a separate parameter
        Elements hTags = document.select("h1, h2, h3, h4, h5, h6");
        headings = hTags.text();
    }

    public void makePersistent() throws IOException {
        //Check if local file. If not there, make one. If there, update if needed.

        String filePath = System.getProperty("user.dir") + File.separator + getCutURL(this.URL) + ".txt";
        File file = new File(filePath);

        LocalDate newModTime = getDate();

        if (file.createNewFile()) {
            //put in the stuff

            System.out.println("new file created");
            FileWriter writer = new FileWriter(file);
            writer.write(newModTime.toString());
            writer.write(System.getProperty("line.separator"));
            writer.write(URL);
            writer.write(System.getProperty("line.separator"));
            writer.write(HTMLContent);
            writer.close();
        } else {
            //check date and append the stuff

            System.out.println("old file looked at");
            BufferedReader Buff = new BufferedReader(new FileReader(filePath));
            String text = Buff.readLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate dateTime = LocalDate.parse(text, formatter);

            // if date on top of file is before the last modified time, modify the file
            if (dateTime.isBefore(newModTime)) {
                System.out.println("file updated");

                // delete contents
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.setLength(0);

                // get new date and write that
                FileWriter writer = new FileWriter(file);
                writer.write(newModTime.toString());
                writer.write(System.getProperty("line.separator"));
                writer.write(URL);
                writer.write(System.getProperty("line.separator"));

                // get new contents and write them
                this.getContents();
                writer.write(HTMLContent);
                writer.close();

            }
        }
    }
}
