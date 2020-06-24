import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class similarity {
    HashMap<Double, BTree> similarities = new HashMap<>(); // all
    // if you wanted to change number of clusters do it by hand sorry
    HashMap<Double, BTree> cluster1 = new HashMap<>();
    HashMap<Double, BTree> cluster2 = new HashMap<>();
    HashMap<Double, BTree> cluster3 = new HashMap<>();
    HashMap<Double, BTree> cluster4 = new HashMap<>();
    HashMap<Double, BTree> cluster5 = new HashMap<>();

    double clustermean1 =0;
    double clustermean2 =0;
    double clustermean3 =0;
    double clustermean4 =0;
    double clustermean5 =0;

    String bestURL;
    int clusterNum;
    HashMap<Double, BTree> bestCluster;

    public String getBestURL(){
        return bestURL;
    }

    public HashMap<Double, BTree> getBestCluster(){
        return bestCluster;
    }

    public int getCluster(){
        return clusterNum;
    }

    public void kmeans(BTree insertedValue, ArrayList<BTree> allBTree, int eventuallyStop){
        System.out.println("K means");
        initializeSimilarities(insertedValue, allBTree);
       // double[] minmax = minMax(similarities);       // dont use, should it?

        initalClusters();

        for(int i=0; i<eventuallyStop; i++){
            boolean change = false;
            System.out.println("Cycling: " + i);

            for(Map.Entry<Double, BTree> entry: similarities.entrySet()){
                int cluster = pickCluster(entry.getKey());
                if(cluster==1){
                    if(!cluster1.containsKey(entry.getKey())){
                        cluster1.put(entry.getKey(), entry.getValue());
                        updateMean(cluster);
                        change = true;
                    }
                } else if(cluster==2){
                    if(!cluster2.containsKey(entry.getKey())){
                        cluster2.put(entry.getKey(), entry.getValue());
                        updateMean(cluster);
                        change = true;
                    }
                } else if(cluster==3){
                    if(!cluster3.containsKey(entry.getKey())){
                        cluster3.put(entry.getKey(), entry.getValue());
                        updateMean(cluster);
                        change = true;
                    }

                } else if(cluster==4){
                    if(!cluster4.containsKey(entry.getKey())){
                        cluster4.put(entry.getKey(), entry.getValue());
                        updateMean(cluster);
                        change = true;
                    }
                } else if(cluster==5){
                    if(!cluster5.containsKey(entry.getKey())){
                        cluster5.put(entry.getKey(), entry.getValue());
                        updateMean(cluster);
                        change = true;
                    }
                }
            }

            if(!change){
                break;
            }
        }

        System.out.println("Picking best");

        int cluster = pickCluster(1);
        double bestmatch = 0;
        if(cluster ==1){
            bestmatch = clustermean1;
            bestURL = cluster1.get(bestmatch).URL;
            bestCluster = cluster1;
            clusterNum = 1;
        } else if(cluster==2){
            bestmatch = clustermean2;
            bestURL = cluster2.get(bestmatch).URL;
            bestCluster = cluster2;
            clusterNum = 2;
        } else if(cluster==3){
            bestmatch = clustermean3;
            bestURL = cluster3.get(bestmatch).URL;
            bestCluster = cluster3;
            clusterNum = 3;
        } else if(cluster==4){
            bestmatch = clustermean4;
            bestURL = cluster4.get(bestmatch).URL;
            bestCluster = cluster4;
            clusterNum = 4;
        } else if(cluster==5){
            bestmatch = clustermean5;
            bestURL = cluster5.get(bestmatch).URL;
            bestCluster = cluster5;
            clusterNum = 5;
        }
    }

    public int pickCluster(double similarity){
        System.out.println("Picking clusters");
        double distance = 0;
        double curval = 0;
        int bestCluster = 1;
        distance = similarity - clustermean1;
        curval = similarity - clustermean2;

        if(curval < distance){
            curval = distance;
            bestCluster = 2;
        }

        curval = similarity - clustermean3;

        if(curval < distance){
            curval = distance;
            bestCluster = 3;
        }

        curval = similarity - clustermean4;

        if(curval < distance){
            curval = distance;
            bestCluster = 4;
        }

        curval = similarity - clustermean5;

        if(curval < distance){
            curval = distance;
            bestCluster = 5;
        }

        return bestCluster;
    }

    public void updateMean(int clusternum){
        System.out.println(("Updating means"));
        double mean = 0;   // actual mean of the data
        double bestdistance = 0; // shortest distance from mean
        double bestcompare = 0; // actual value in map that's closest
        if(clusternum ==1){
            for(Map.Entry<Double, BTree> entry: cluster1.entrySet()){
                mean += entry.getKey();
            }
            mean = mean / cluster1.size();

            for(Map.Entry<Double, BTree> entry: cluster1.entrySet()){
                double currentNum = entry.getKey() - mean;
                if(currentNum < bestdistance){
                    bestcompare = entry.getKey();
                }
            }

            clustermean1 = bestcompare;
        } else if(clusternum ==2){
            for(Map.Entry<Double, BTree> entry: cluster2.entrySet()){
                mean += entry.getKey();
            }
            mean = mean / cluster1.size();

            for(Map.Entry<Double, BTree> entry: cluster2.entrySet()){
                double currentNum = entry.getKey() - mean;
                if(currentNum < bestdistance){
                    bestcompare = entry.getKey();
                }
            }

            clustermean2 = bestcompare;

        } else if(clusternum ==3){
            for(Map.Entry<Double, BTree> entry: cluster3.entrySet()){
                mean += entry.getKey();
            }
            mean = mean / cluster3.size();

            for(Map.Entry<Double, BTree> entry: cluster3.entrySet()){
                double currentNum = entry.getKey() - mean;
                if(currentNum < bestdistance){
                    bestcompare = entry.getKey();
                }
            }

            clustermean3 = bestcompare;

        } else if(clusternum ==4){
            for(Map.Entry<Double, BTree> entry: cluster4.entrySet()){
                mean += entry.getKey();
            }
            mean = mean / cluster4.size();

            for(Map.Entry<Double, BTree> entry: cluster4.entrySet()){
                double currentNum = entry.getKey() - mean;
                if(currentNum < bestdistance){
                    bestcompare = entry.getKey();
                }
            }

            clustermean4 = bestcompare;
        } else if(clusternum ==5){
            for(Map.Entry<Double, BTree> entry: cluster5.entrySet()){
                mean += entry.getKey();
            }
            mean = mean / cluster5.size();

            for(Map.Entry<Double, BTree> entry: cluster5.entrySet()){
                double currentNum = entry.getKey() - mean;
                if(currentNum < bestdistance){
                    bestcompare = entry.getKey();
                }
            }

            clustermean5 = bestcompare;
        }
    }


    public void helperRemove(Object[] keys, Object random){
        for(int j=0; j<keys.length; j++){
            if(keys[j] == random){
                keys[j] = -1;
            }
        }
    }
    public void RandomFillClusters(int clusternum){
        System.out.println("Randomly filling clusters");
        Random r = new Random();
        Object[] keys = similarities.keySet().toArray();

        for(int i =0; i<20; i++){
            Object random = keys[r.nextInt(keys.length)];
            double randomD = Double.parseDouble(random.toString());
            BTree value = similarities.get(random);

            if(randomD == -1) {
                i++;
            } else if(clusternum ==1){
                cluster1.put(randomD, value);
                helperRemove(keys, random);
            } else if(clusternum ==2){
                cluster2.put(randomD, value);
                helperRemove(keys, random);
            } else if(clusternum ==3){
                cluster3.put(randomD, value);
                helperRemove(keys, random);
            } else if(clusternum ==4){
                cluster4.put(randomD, value);
                helperRemove(keys, random);
            } else if(clusternum ==5){
                cluster5.put(randomD, value);
                helperRemove(keys, random);
            }

            if(i == 19){
                updateMean(clusternum);
            }

        }
    }

    public void initalClusters() {
        System.out.println("Initially filling clusters");
        int featureNum = similarities.size();

        RandomFillClusters(1);
        RandomFillClusters(2);
        RandomFillClusters(3);
        RandomFillClusters(4);
        RandomFillClusters(5);

    }

    public double[] minMax(HashMap<Double, BTree> find){
        double[] minMax = new double[2];
        double min = 0;
        double max = 0;
        for(Map.Entry<Double, BTree> entry: find.entrySet()){
            double key = entry.getKey();
            if(key < min){
                min = entry.getKey();
            }

            if(key > max){
                max = entry.getKey();
            }
        }
        minMax[0] = min;
        minMax[1] = max;

        return minMax;
    }

    public void initializeSimilarities(BTree entryBTree, ArrayList<BTree> listofBTree) {
        System.out.println("Gathering all tf-idf");
        String mostSimilarSite = null;
        double mostSimilar = 0;
        double similarity = 0;
        double dotproduct = 0;
        double bottom = 0;
        ArrayList<Integer> allWords = new ArrayList<>();


        for(BTree bt: listofBTree){
            System.out.println("Website: " + bt.URL);
            for(Map.Entry<Long, BTreeNode> entry :bt.cache.entrySet()){
                for(Map.Entry<Integer, Integer> subentry: entry.getValue().keys.entrySet()){
                    allWords.add(subentry.getKey());
                    System.out.println("adding words" + subentry.getKey());
                }
            }
        }

        int counter = 0;
        double[] document1 = new double[allWords.size()];
        for (int word : allWords) {
            document1[counter] = tf(entryBTree, word) * idf(listofBTree, word);
            counter += 1;
        }

        double[] document2 = new double[allWords.size()];
        for (BTree bt : listofBTree) {
            dotproduct = 0;
            bottom = 0;
            counter = 0;


            for (int word : allWords) {
                double tf = tf(bt, word);
                double idf = idf(listofBTree, word);

                //System.out.println("TF" + tf);
                // System.out.println("IDF" + idf);

                if (tf == 0 || idf == 0) {
                    document2[counter] = 0;
                } else {
                    document2[counter] = tf * idf;
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

            similarities.put(similarity, bt);

          /*  System.out.println("Checking " + bt.URL);
            System.out.println("Similarity score " + similarity);
            System.out.println("Most similar score is " + mostSimilar);

            if (similarity > mostSimilar) {
                mostSimilar = similarity;
                mostSimilarSite = bt.URL;
                System.out.println("most similar site " + mostSimilarSite);
            }

           */
        }
    }

    private static double idf(ArrayList<BTree> bList, int key) {
        double idf = 0;
        double numberOfDocuments = bList.size();
        double numberOfDocumentsWithTerm = getPagesWithWord(bList, key);

        idf = Math.log(numberOfDocuments / (numberOfDocumentsWithTerm + 1));
        idf = Double.parseDouble(String.format("%2.10f", idf));

        return idf;
    }

    private static double tf(BTree bt, int key) {
        double tf = 0;
        for (Map.Entry<Long, BTreeNode> entry: bt.cache.entrySet()) {
            if (entry.getValue().keys.containsKey(key)) {
                double raw = entry.getValue().keys.get(key);
                tf = Math.log10(1 + raw);
                tf = Double.parseDouble(String.format("%2.10f", tf));
            } else {
                tf = 0;
            }
        }
        return tf;
    }

    private static int getPagesWithWord(ArrayList<BTree> b, int word) {
        int pagesWithWord = 0;
        for (BTree bt : b) {
            for (Map.Entry<Long, BTreeNode> entry: bt.cache.entrySet()) {
                if (entry.getValue().keys.containsKey(word)) {
                    pagesWithWord += 1;
                }
            }
        }
        return pagesWithWord;
    }
}
