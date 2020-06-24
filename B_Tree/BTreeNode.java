import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class BTreeNode {
    static final long NULLLONG = -1L;
    long position;
    long parent;
    Map<Integer, Integer> keys; // hashed word, frequency of word in text
    int leaf;
    long[] children;

    // setting values for tree nodes
    public static int t = 100;
    public static int childMax = 2 * t;
    public static int keyMax = (2 * t) - 1;

    BTreeNode(){
        this.position = NULLLONG;
        this.parent = NULLLONG;
        this.keys = new HashMap<>();
        this.children = new long[childMax];
        initalizeChildren();
        this.leaf = 0;
    }

    public void initalizeChildren(){
        for(int i=0; i<childMax; i++){
            children[i] = NULLLONG;
        }
    }

    public void setposition(long position){
        this.position = position;
    }


    public void setparent(long parent){
        this.parent = parent;
    }

    public void setkeys(Map<Integer, Integer> keys){
        this.keys = keys;
    }

    public void setleaf(int leaf){
        this.leaf = leaf;
    }

    public void setchild(long[] children){
        this.children = children;
    }

    public Map<Integer, Integer> sortedKeys() {
        System.out.println("SIZE OF KEYS BEFORE SORTING: " + this.keys.size());
        Map<Integer, Integer> sorted = new TreeMap<>();
        sorted.putAll(this.keys);
        return sorted;
    }

    public Object[] keysToArray(){
        Map<Integer, Integer> sortedkeys = sortedKeys();
        Object[] keyArray = sortedkeys.entrySet().toArray();

        Object[] finalArray = new Object[keyMax];
        for(int i=0; i<keyMax; i++){
            if(i<keyArray.length){
                finalArray[i] = keyArray[i];
            } else {
                finalArray[i] = -1;
            }
        }
        return finalArray;
    }

    public int keyCount(){
        int counter = 0;
        Object[] keyarray = keysToArray();
        for(int i=0; i<keyarray.length; i++){
           //System.out.println("THIS IS I: " + i);
            if(keyarray[i].toString().contains("=")){
               // System.out.println("KEYARRAY "+ keyarray[i]);
                counter++;
            }
        }
        return counter;
    }

    public boolean isFull(){
        int counter = keyCount();
       // System.out.println("COUNTER " + counter);
        if((2* t) - 1 == counter){
            System.out.println("TRUE");
            return true;
        } else {
            System.out.println("FALSE");
            return false;
        }
    }

    public void setNumChild(int num){
        long[] children = new long[num];
        for(int i=0; i<this.children.length; i++){
            children[i] = this.children[i];
        }
        setchild(children);
    }
}
