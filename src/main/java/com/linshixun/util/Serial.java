package com.linshixun.util;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Serial {

    public static boolean exists(String fileName) {
        return (new File(fileName)).exists();
    }

    public static boolean store(Object p, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));
            out.writeObject(p);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Object load(File fileName) {
        try {
            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
            return in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean storeHessian(Object p, String fileName) {
        try {
            Hessian2Output out = new Hessian2Output(new GZIPOutputStream(new FileOutputStream(fileName),true));
            out.writeObject(p);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Object loadHessian(File fileName) {
        try {
            Hessian2Input hessian2Input = new Hessian2Input(new GZIPInputStream(new FileInputStream(fileName)));
            return hessian2Input.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object load(String fileName) {
        return load(new File(fileName));
    }

    public static void main(String[] args) {
        HashSet<Object> ass=new HashSet<>();
        HashMap<String,String> ssa=new HashMap<>();
        ssa.put("ss","ff");
        ass.add(ssa);

        storeHessian(ass,"testHessian");
        Object testHessian = loadHessian(new File("testHessian"));
        System.out.println(testHessian);
    }
}