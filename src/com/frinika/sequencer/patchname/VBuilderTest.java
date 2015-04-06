package com.frinika.sequencer.patchname;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import com.frinika.sequencer.patchname.Node;

/**
 * 
 * @author Paul
 * @deprecated
 */
public class VBuilderTest {

	public static void printSTARS(int depth) {
		for (int i = 0; i < depth; i++)
			System.out.print("*");
	}

	public static void printDB(Vector<Node> list, int depth) {
		for (Node o : list) {
			if (o instanceof Node) {
				printSTARS(depth);
				System.out.println(o);
				if (o.getData() instanceof Vector)
					printDB((Vector<Node>) o.getData(), depth + 1);
			} else {
				System.out.println("OOOPS");
			}
		}
	}

	public static void main(String args[]) {
		
		String name="default";
		File file = new File("src/patchnames/"+name+".txt");
		File oFile = new File("src/patchnames/"+name+".pat");

		PatchNameMap b = null;
		try {
			b = new PatchNameMap(new FileInputStream(file));
			FileOutputStream fos = new FileOutputStream(oFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(b);
			oos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//printDB(b.topList, 0);
		PatchNameMap c=null;

		FileInputStream fis;
		try {
			fis = new FileInputStream(oFile);

			ObjectInputStream ois = new ObjectInputStream(fis);
			c = (PatchNameMap) ois.readObject();
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printDB(c.getList(), 0);
		
		// Vector<Vector<NameObject>> lists=b.getListOfLists("Tweet");
		/*
		 * for (Vector<NameObject> list:lists) {
		 * System.out.println("----------------------------------"); for
		 * (NameObject n:list) { System.out.println(n); } }
		 */

	}
}
