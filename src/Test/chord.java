package Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.impl.*;
import game.util;

public class chord {

	Node buildNode(String x) {
		ID i = new ID(util.hexStringToByteArray(x));
		Node n = new NodeImpl(i);
		return n;
	}
	
	public boolean same(List<Node> first, List<Node> second) {
		if(!(first.size() == second.size())) {
			return false;
		} 
		for(int i = 0; i < first.size(); i++) {
			if(!first.get(i).getNodeID().equals(second.get(i).getNodeID())) {
				return false;
			}
		}
		return true;
	}
	

	@Test
	public void test() {
		Node a = buildNode("00000000000000000001");
		Node b = buildNode("BBBBBBBBBBBBBBBBBBBB");
		Node c = buildNode("CCCCCCCCCCCCCCCCCCCC");
		Node d = buildNode("DDDDDDDDDDDDDDDDDDDD");
		Node e = buildNode("EEEEEEEEEEEEEEEEEEEE");

		List<Node> sorted = new ArrayList<>(Arrays.asList(a,b,c,d,e));
		List<Node> sorted_a = new ArrayList<>(Arrays.asList(b,c,d,e));
		List<Node> sorted_b = new ArrayList<>(Arrays.asList(c,d,e,a));
		List<Node> sorted_c = new ArrayList<>(Arrays.asList(d,e,a,b));
		List<Node> sorted_d = new ArrayList<>(Arrays.asList(e,a,b,c));
		List<Node> sorted_e = new ArrayList<>(Arrays.asList(a,b,c,d));
		
		List<Node> unsorted_1 = new ArrayList<>(Arrays.asList(a,c,d,e,b));
		List<Node> unsorted_2 = new ArrayList<>(Arrays.asList(c,a,e,b,d));
		List<Node> unsorted_3 = new ArrayList<>(Arrays.asList(e,c,a,b,d));
		List<Node> unsorted_4 = new ArrayList<>(Arrays.asList(c,a,b,d,e));
		List<Node> unsorted_5 = new ArrayList<>(Arrays.asList(d,b,a,c,e));
		List<Node> unsorted_6 = new ArrayList<>(Arrays.asList(c,e,d,a,b));
		
		List<List<Node>> llsnodes = new ArrayList<>(Arrays.asList(sorted_a,
				sorted_b,
				sorted_c,
				sorted_d,
				sorted_e));
		
		
		List<List<Node>> llusnodes = new ArrayList<>(Arrays.asList(unsorted_1,
				unsorted_2,
				unsorted_3,
				unsorted_4,
				unsorted_5,
				unsorted_6));
		
		for(int i = 0; i < 6; i++) {
			for(int k = 0; k < 5; k++) {
				List<Node> unsorted_nodes = new ArrayList<>(llusnodes.get(i));
				unsorted_nodes.remove(sorted.get(k));
				assertTrue(same(llsnodes.get(k), ((NodeImpl)sorted.get(k)).getSortedFingertable(unsorted_nodes)));
			}
		}
		
	    
	}

}
