package com.mangocity.httpclient.tester;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class GenericTester {

	@Test
	public void testGeneric(){
		/**
		 * List<String>[] listItems = new List<String>[1];
		 * List<Integer> asList = Arrays.asList();
		 * Object[] objects = listItems;
		 * objects[0] = asList;
		 * listItems[0].get(0);
		 */
	}
	
	@Test
	public void testDoubleToString(){
		Double d = 2342342234.13D;
		System.out.println(d);
		NumberFormat n = NumberFormat.getInstance();
		n.setGroupingUsed(false);
		System.out.println(n.format(d));
	}
	
	@Test
	public  void testArrCast(){
		List<?>[] listItemp = new List<?>[1];
		 List<Integer> asList = Arrays.asList(3);
		 Object[] objects = listItemp;
		 objects[0] = asList;
		 Integer i =  (Integer) listItemp[0].get(0);
		 System.out.println(i);
	}
}
