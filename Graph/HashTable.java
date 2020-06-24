package com.company;

import java.util.ArrayList;

public class HashTable {
    static final int startSize = 40; 
    private static Node[] table;

    HashTable(){
        table = new Node[startSize];
    }

    private int hashcode(String key){
        char charArray[] = key.toCharArray();
        int hash = 0;

        for(int i = 0; i < key.length(); i++){
            int x = charArray[i];
            x = x * 31; // multiple by prime
            x ^= x << 13; // use three shifts w/ proven numbers
            x ^= x >>> 7;
            x ^= x << 17;
            hash = x;
        }

        return hash;
    }

    public boolean contains(Website key){
        int hash = hashcode(key.URL);
        int index = hash & (table.length -1);

        for(Node e = table[index]; e != null; e = e.next){
            if (key.URL.equals(e.key.URL)) {
                return true;
            }
        }
        return false;
    }

    public void add(Website key){
        int hash = hashcode(key.URL);
        int index = hash & (table.length - 1);

        if(table[index] == null){
            table[index] = new Node(key, null);
            return;
        }

        for(Node e = table[index]; e != null; e = e.next){
            if(key.URL.equals(e.key.URL)){
                return;
            } else if(e.next == null) {
                table[index] = new Node(key, table[index]);
            }
        }
    }

    public void printall(){
        for(int i = 0; i <table.length; ++i){
            for(Node e = table[i]; e != null; e = e.next){
                System.out.println(e.key.URL);
            }
        }
    }

    public ArrayList<Website> collectAll(){
        ArrayList<Website> allSites = new ArrayList<Website>();
        for(int i = 0; i <table.length; ++i){
            for(Node e = table[i]; e != null; e = e.next){
                allSites.add(e.key);
            }
        }
        return allSites;
    }

    public int getSize(){
        int size = 0;
        for(int i = 0; i < table.length; ++i){
            for(Node e = table[i]; e != null; e = e.next){
                size +=1;
            }
        }
        return size;
    }

}
