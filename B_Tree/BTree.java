import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BTree {
    static final int bufferSize = 3212;

    public String URL; // one website for each BTree
    BTreeNode root;
    public LinkedHashMap<Long, BTreeNode> cache;
    static final int initialCapacity = 100;

    // to take up space in buffers (don't know how to do this right)
    static final long NULLONG = -1L;
    static final int NULLINT = -1;

    // size of each for bytebuffers. Why is this not Integer.size, etc?
    static final int INT_SIZE = 4;
    static final int LONG_SIZE = 8;
    static final int CHAR_SIZE = 2;

    // offsets
    static final int POSITIONOFFSET = 0; // long position
    static final int PARENTOFFSET = POSITIONOFFSET + LONG_SIZE; // long parent
    static final int LEAFSTATUSOFFSET = PARENTOFFSET + LONG_SIZE; // int leafstatus
    static final int FIRSTKEYOFFSET = LEAFSTATUSOFFSET + INT_SIZE; // int firstkey. keep getting keys until reach long then we are on children

    int nextPosition = 0;
    ArrayList<BTreeNode> collectAllChildren = new ArrayList<BTreeNode>();

    BTree(String URL){
        this.URL = URL;
        root = null;
        cache = new LinkedHashMap<Long, BTreeNode>(initialCapacity);
    }

    public static ByteBuffer allocateNode(BTreeNode n){
        ByteBuffer b = ByteBuffer.allocate(bufferSize);
        System.out.println("pos: "+ n.position);
        System.out.println("parent: " + n.parent);
        System.out.println("leaf: " + n.leaf);

        b.putLong(POSITIONOFFSET, n.position);
        b.putLong(PARENTOFFSET, n.parent);
        b.putInt(LEAFSTATUSOFFSET, n.leaf);

        Map<Integer, Integer> map = n.sortedKeys();
        int placement = FIRSTKEYOFFSET;

        int count = 0;
        System.out.println("keys:");
        for(Map.Entry<Integer, Integer> entry: map.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
            b.putInt(placement, entry.getKey());
            placement += INT_SIZE;
            b.putInt(placement, entry.getValue());
            placement += INT_SIZE;
            count ++;
        }

        int counter = map.size();
        if(map.size() < BTreeNode.keyMax){
            while(counter < BTreeNode.keyMax){
                b.putInt(placement, NULLINT);
                placement += INT_SIZE;
                b.putInt(placement, NULLINT);
                placement += INT_SIZE;
                counter++;
            }
        }

        System.out.println("children: ");
        // no null check because insert -1 values all nodes same size
        for(int i=0; i < n.children.length; i++){
            System.out.println(n.children[i]);
            b.putLong(placement, n.children[i]);
            placement += LONG_SIZE;
        }


        return b;
    }

    public long getPos(){
        long pos = nextPosition;
        nextPosition += bufferSize;
        return pos;
    }

    public void writeToCache(BTreeNode n){
        if(n.position == -1){
            n.setposition(getPos());
        }
        cache.put(n.position, n);
    }

    public void diskWrite(ArrayList<BTreeNode> allnodes) throws IOException {
        File file = new File("C:/Users/Rose/IdeaProjects/project2-csc365-takemaybe5/BTree/" + URL + ".txt");
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = null;

        for(BTreeNode n: allnodes){
            System.out.println("Writing to disk: ");
            ByteBuffer b = allocateNode(n);
            raf.seek(n.position);
            fileChannel = raf.getChannel();
            fileChannel.write(b);
        }
        fileChannel.close();
    }

    public BTreeNode diskRead(long position) throws IOException{
        if(cache.containsKey(position)){
            return cache.get(position);
        } else {
            File file = new File("C:/Users/Rose/IdeaProjects/project2-csc365-takemaybe5/BTree/" + URL + ".txt");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            BTreeNode n = new BTreeNode();

            raf.seek(position);
            FileChannel fileChannel = raf.getChannel();
            ByteBuffer b = ByteBuffer.allocate(bufferSize);
            fileChannel.read(b);

            long parent = b.getLong(PARENTOFFSET);
            int leaf = b.getInt(LEAFSTATUSOFFSET);

            Map<Integer, Integer> keys = new HashMap<Integer, Integer>();
            int placement = FIRSTKEYOFFSET;
            for (int i = 0; i < BTreeNode.keyMax; i++) {
                int key = b.getInt(placement);
                placement += INT_SIZE;
                int value = b.getInt(placement);
                keys.put(key, value);
                placement += INT_SIZE;
            }

            // dont worry about anything because all nodes same size;
            // shouldnt have to do this bc this is called in constructor
            for(int i=0; i<BTreeNode.childMax; i++){
                n.children[i] = b.getLong(placement);
                placement += LONG_SIZE;
            }

            // for debugging
            System.out.println("Read DISK");
            System.out.println("Pos: " + position);
            System.out.println("Par: " + parent);
            System.out.println("Leaf: " + leaf);
            System.out.println("Keys: ");
            for(Map.Entry<Integer, Integer> entry : keys.entrySet()){
                System.out.println(entry.getKey());
            }

            System.out.println("Children: ");
            for(int i=0; i<n.children.length; i++){
                System.out.println(n.children[i]);
            }


            n.setposition(position);
            n.setkeys(keys);
            n.setparent(parent);
            n.setleaf(leaf);
            return n;
        }
    }

    public void BTreeCreate(BTreeNode n) throws IOException {
        n.setleaf(1);
        writeToCache(n);
        this.root = n;
    }

    // this is causing the keys to reset at size 0 ...
    public Map<Integer, Integer> arrayToMap(Object[] keyarray){
        System.out.println("SIZE OF ARRAY BEFORE TO MAP: " + keyarray.length);
        Map<Integer, Integer> keysmap = new HashMap<>();
        for(int i=0; i< keyarray.length; i++){
            String keyvaluepair = keyarray[i].toString();
            System.out.println("KEY VALUE PAIR : " + keyvaluepair);
            if(keyvaluepair.contains("=")){
                String[] values = keyvaluepair.split("=");
                int key = Integer.parseInt(values[0]);
                int value = Integer.parseInt(values[1]);
                keysmap.put(key, value);
            } else {
                return keysmap;
            }
        }
        return keysmap;
    }

    public int[] arraytoValues(Object[] keyarray, int index){
        int[] keyvalue = new int[2];
        String keyvaluepair = keyarray[index].toString();

        if(keyvaluepair.contains("=")){
            String[] values = keyvaluepair.split("=");
            int key = Integer.parseInt(values[0]);
            int value = Integer.parseInt(values[1]);
            keyvalue[0] = key;
            keyvalue[1] = value;
        } else {
            keyvalue[0] = -1;
            keyvalue[1] = -1;
        }
        return keyvalue;
    }

    public void BTreeSplitChild(BTreeNode x, int i, BTreeNode y) throws IOException {
        BTreeNode z = new BTreeNode();
        z.setleaf(y.leaf);


        Object[] ykeys = y.keysToArray();
        Object[] zkeys = z.keysToArray();

        for(int j=0; j<BTreeNode.t-1; j++){
            zkeys[j] = ykeys[j + BTreeNode.t];
            ykeys[j + BTreeNode.t] = NULLINT;
        }

        if(y.leaf == 0){
            for(int j=0; j<BTreeNode.t; j++){
                z.children[j] = y.children[j + BTreeNode.t];
                y.children[j + BTreeNode.t] = NULLONG;
            }
        }

        Object[] xkeys = x.keysToArray();

        for(int j=BTreeNode.keyMax -1; j > i; --j){
            x.children[j+1] = x.children[j];


        }

        long positionz = getPos();
        z.setposition(positionz);
        x.children[i+1] = z.position;

        z.setparent(x.position);

        for(int j=BTreeNode.keyMax -2; j>=i; --j){
            xkeys[j+1] = xkeys[j];
        }

        xkeys[i] = ykeys[BTreeNode.t -1];
        ykeys[BTreeNode.t -1] = NULLINT;

        x.setkeys(arrayToMap(xkeys));
        y.setkeys(arrayToMap(ykeys));
        z.setkeys(arrayToMap(zkeys));

        writeToCache(x);
        writeToCache(y);
        writeToCache(z);
    }

    public void BTreeInsertNonFull(BTreeNode x, int k, int v) throws IOException {
        int i = x.keyCount() -1;
        System.out.println("KEYCOUNT " + x.keyCount());
        Object[] xkeys = x.keysToArray();

        if(x.leaf == 1){
            System.out.println("I: " + i);
            System.out.println("K: " + k);
            while(i>=0 && arraytoValues(xkeys, i)[0] != -1 && k < arraytoValues(xkeys, i)[0]){
                System.out.println("k: " + k);
                System.out.println("xvalues[0]: " + arraytoValues(xkeys, i)[0]);
                System.out.println("i: " + i);
                System.out.println(xkeys.length);

                // handling boundary values
                if(i==198){
                    i--;
                }

                xkeys[i+1] = xkeys[i];
                i--;
            }

            xkeys[i+1] = k + "=" + v;
            x.setkeys(arrayToMap(xkeys));
            writeToCache(x);
        } else {
            while(i>=0 && arraytoValues(xkeys, i)[0] != -1 && k < arraytoValues(xkeys, i)[0]){
                // consider adding parameter && arraytoValues(xkeys, i)[0] != -1
                i--;
            }
            i++;


            BTreeNode xchild = diskRead(x.children[i]);

            // Debugging information
            if (xchild.children[0] == 0) {
                System.out.println("pos: " + x.position);
                System.out.println("par: " + x.parent);
                System.out.println("keys: ");
                for(Map.Entry<Integer, Integer> entry : x.keys.entrySet()){
                    System.out.println(entry.getKey());
                }

                System.out.println("children: ");
                for(int q=0; q< x.children.length; q++){
                    System.out.println(x.children[i]);
                }

                System.exit(0);
            }

            if(xchild.isFull()){
                BTreeSplitChild(x, i, xchild);

                if(k > arraytoValues(xkeys, i)[0] && arraytoValues(xkeys, i)[0] != -1){
                    i++;
                }

                if(x.children[i] != xchild.position){
                    xchild = diskRead(x.children[i]);
                }

                BTreeInsertNonFull(xchild, k, v);
            }
        }
    }

    public void BTreeInsert(int key, int value) throws IOException{
        BTreeNode root = this.root;

        if(root == null){
            // for debugging
            System.out.println("Root is null");

            BTreeNode n = new BTreeNode();
            n.setleaf(1);
            Map<Integer, Integer> keys = new HashMap<>();
            keys.put(key, value);
            n.setkeys(keys);
            BTreeCreate(n);
        } else {
            if(root.isFull()){
                // for debuggin
                System.out.println("Root is full");
                BTreeNode n = new BTreeNode();
                n.setposition(getPos());
                n.setleaf(0);
                n.children[0] = root.position;
                root.setparent(n.position);

                BTreeSplitChild(n, 0, root);
                BTreeInsertNonFull(n, key, value);
                this.root = n;
            } else {
                System.out.println("root not full");
                BTreeInsertNonFull(root, key, value);
            }
        }
    }

    public void getAllChildren(BTreeNode root) throws IOException {
        for(long l: root.children){
            if(l != -1){
                BTreeNode btn = diskRead(l);
                collectAllChildren.add(btn);

                // for debugging
                System.out.println("pos: " + btn.position);
                System.out.println("par: " + btn.parent);
                System.out.println("keys: ");
                for(Map.Entry<Integer, Integer> entry : btn.keys.entrySet()){
                    System.out.println(entry.getKey());
                }

                System.out.println("children: ");
                for(int i=0; i< btn.children.length; i++){
                    System.out.println(btn.children[i]);
                }
                getAllChildren(btn);
            }
        }

    }

}
