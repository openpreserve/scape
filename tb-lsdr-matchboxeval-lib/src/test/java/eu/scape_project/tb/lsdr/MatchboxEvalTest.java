/*
 *  Copyright 2012 The SCAPE Project Consortium.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package eu.scape_project.tb.lsdr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test class for matchbox evaluation.
 *
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class MatchboxEvalTest {

    private static MatchboxEval mbeval;
    private static String mbcont;
    private static String gtcont;

    /**
     * Set up.
     * @throws Exception 
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

        InputStream mbSource = MatchboxEval.class.getResourceAsStream("matchbox.txt");
        mbcont = IOUtils.toString(mbSource, "UTF-8");

        InputStream gtSource = MatchboxEval.class.getResourceAsStream("groundtruth.txt");
        gtcont = IOUtils.toString(gtSource, "UTF-8");

        mbeval = new MatchboxEval(mbcont, gtcont);
        System.out.println("test setUp");
        mbeval.evaluate();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getGroundtruthList method, of class MatchboxEval.
     */
    @Test
    public void testGetGroundtruthList() throws IOException {
        System.out.println("test getGroundtruthList");
        ArrayList<ArrayList<Integer>> gtlist = mbeval.getGroundtruthList(gtcont);
        this.printList("Groundtruth", gtlist);
        ArrayList<Integer> list = gtlist.get(5);
        Integer result = list.get(3);
        Integer expResult = 10;
        assertEquals(expResult, result);
    }

    /**
     * Test of getMatchboxList method, of class MatchboxEval.
     */
    @Test
    public void testGetMatchboxList() throws IOException {
        System.out.println("test getMatchboxList");
        ArrayList<ArrayList<Integer>> mblist = mbeval.getMatchboxList(mbcont, "=>", 0, 2);
        this.printList("Groundtruth", mblist);
        ArrayList<Integer> list = mblist.get(1);
        Integer result = list.get(1);
        Integer expResult = 4;
        assertEquals(expResult, result);
    }

    /**
     * Test of getCombinationList method, of class MatchboxEval.
     */
    @Test
    public void testGetCombination() throws IOException {
        System.out.println("test getCombination");

        List<Integer> gtNTuple = new ArrayList<Integer>();
        gtNTuple.add(1);
        gtNTuple.add(2);
        gtNTuple.add(3);
        gtNTuple.add(4);

        ArrayList<ArrayList<Integer>> clist = mbeval.getCombinationList(gtNTuple);
        this.printList("Combination", clist);

        ArrayList<Integer> comb1 = clist.get(0);
        assertEquals(new Integer(1), comb1.get(0));
        assertEquals(new Integer(2), comb1.get(1));

        ArrayList<Integer> comb2 = clist.get(1);
        assertEquals(new Integer(1), comb2.get(0));
        assertEquals(new Integer(3), comb2.get(1));

        ArrayList<Integer> comb3 = clist.get(2);
        assertEquals(new Integer(1), comb3.get(0));
        assertEquals(new Integer(4), comb3.get(1));

        ArrayList<Integer> comb4 = clist.get(3);
        assertEquals(new Integer(2), comb4.get(0));
        assertEquals(new Integer(3), comb4.get(1));

        ArrayList<Integer> comb5 = clist.get(4);
        assertEquals(new Integer(2), comb5.get(0));
        assertEquals(new Integer(4), comb5.get(1));

        ArrayList<Integer> comb6 = clist.get(5);
        assertEquals(new Integer(3), comb6.get(0));
        assertEquals(new Integer(4), comb6.get(1));
    }

    /**
     * Test of getMatchboxList method, of class MatchboxEval.
     */
    @Test
    public void testGetExpandedList() throws IOException {
        System.out.println("test getExpandedList");
        ArrayList<Integer> ntuple1 = new ArrayList<Integer>();
        ntuple1.add(1);
        ArrayList<Integer> ntuple2 = new ArrayList<Integer>();
        ntuple2.add(2);
        ntuple2.add(3);
        ntuple2.add(4);
        ArrayList<Integer> ntuple3 = new ArrayList<Integer>();
        ntuple3.add(5);
        ntuple3.add(6);
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        list.add(ntuple1);
        list.add(ntuple2);
        list.add(ntuple3);
        this.printList("Groundtruth list", list);
        ArrayList<ArrayList<Integer>> expandedList = mbeval.getExpandedTupleList(list);
        this.printList("Expanded list", expandedList);
        assertEquals(expandedList.get(0).get(0), new Integer(2));
        assertEquals(expandedList.get(0).get(1), new Integer(3));
        assertEquals(expandedList.get(1).get(0), new Integer(2));
        assertEquals(expandedList.get(1).get(1), new Integer(4));
        assertEquals(expandedList.get(2).get(0), new Integer(3));
        assertEquals(expandedList.get(2).get(1), new Integer(4));
        assertEquals(expandedList.get(3).get(0), new Integer(5));
        assertEquals(expandedList.get(3).get(1), new Integer(6));
    }

    @Test
    public void testGetTruePositives() {
        System.out.println("test getTruePositives");
        assertEquals(7, mbeval.getTruePositives());
    }

    @Test
    public void testGetFalsePositives() {
        System.out.println("test testGetFalsePositives");
        assertEquals(1, mbeval.getFalsePositives());
    }

    @Test
    public void testGetFalseNegatives() {
        System.out.println("test getFalseNegatives");
        assertEquals(3, mbeval.getFalseNegatives());
    }

    @Test
    public void testGetPrecision() {
        System.out.println("test getPrecision");
        assertEquals(70, mbeval.getPrecision());
    }

    @Test
    public void testGetRecall() {
        System.out.println("test getRecall");
        assertEquals(87, mbeval.getRecall());
    }

    @Test
    public void testGetFmeasure() {
        System.out.println("test testGetFmeasure");
        assertEquals(77, mbeval.getFmeasure());
    }

    private void printList(String name, ArrayList<ArrayList<Integer>> inlist) {
        System.out.println(name);
        for (ArrayList<Integer> list : inlist) {
            System.out.println("line:");
            for (Integer item : list) {
                System.out.print(item + " ");
            }
            System.out.println();
        }
    }
}
