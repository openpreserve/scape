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

import combinatorics.CombinatoricsVector;
import combinatorics.Generator;
import combinatorics.combination.simple.SimpleCombinationGenerator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matchbox evaluation against ground truth. The evaluation process first
 * creates the matchbox output and ground truth lists. It then counts each page
 * tuple from the matchbox output that is in the ground truth as correctly
 * identified tuple (true positive). Those that are not in the ground truth are
 * counted as incorrectly identified tuples (false positives), and finally,
 * those that are in the ground truth but not in the matchbox output are counted
 * as missed tuples (false negatives).
 * The precision is then calculated as the number of true positives (i.e. the 
 * number of items correctly labeled as duplicate page pairs) divided by the 
 * total number of elements assumed to be duplicate page pairs (i.e. the sum of 
 * true positives and false positives, which are items incorrectly labeled as 
 * being duplicate page pairs ). Recall is then defined as the number of 
 * true positives divided by the total number of elements of duplicate page 
 * pairs (i.e. the sum of true positives and false negatives, which are items 
 * have not been labeled as being duplicate page pairs but actually should have 
 * been).
 * The ground truth contains single page instances without duplicates and 
 * n-tuples (duplicates, triples, quadruples, etc.). n-tuples with n>2 are 
 * expanded, the result is a list of 2-tuples which is used to determine the
 * number of missed duplicates (false negatives).
 *
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class MatchboxEval {

    private int truePositives = 0; //  correctly identified duplicates (true positives)
    private int falsePositives = 0; // missed duplicates (false positives)
    private int falseNegatives = 0;// incorrectly identified duplicates (false negatives)
    private int precision = 0; // precision
    private int recall = 0; // recall
    private int fmeasure = 0; // f-measure
    private String matchboxOutput; // matchbox output
    private String groundTruth; // groundtruth
    private String log;

    /**
     * Ground truth content
     *
     * @return Ground truth content
     */
    public String getGroundTruth() {
        return groundTruth;
    }

    /**
     * Matchbox output content
     *
     * @return Matchbox output content
     */
    public String getMatchboxOutput() {
        return matchboxOutput;
    }

    /**
     * Getter for correctly identified duplicates (true positives).
     * Number of "true positives", which is the number of page pairs correctly 
     * identified as being duplicates.
     * @return correctly identified duplicates (true positives)
     */
    public int getTruePositives() {
        return truePositives;
    }

    /**
     * Getter incorrectly identified duplicates (false positives).
     * Number of "false positives", which are page pairs that are incorrectly 
     * labeled as being page pairs but in fact the are not.
     * @return incorrectly identified duplicates (false positives)
     */
    public int getFalsePositives() {
        return falsePositives;
    }

    /**
     * Getter for missed duplicates (false negatives).
     * Number of "false negatives", which are items that were not labeled as 
     * being page pairs but actually they should have been.
     * @return missed duplicates (false negatives)
     */
    public int getFalseNegatives() {
        return falseNegatives;
    }

    /**
     * Getter for Precision. 
     * Precision prec = true positives / ( true positives / false positives )
     *
     * @return Precision
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Getter for Recall. Recall rec = true positives / ( true positives / false
     * negatives )
     *
     * @return Recall
     */
    public int getRecall() {
        return recall;
    }

    /**
     * Getter for F-Measure. F-measure fm = 2 * (prec * rec) / (prec + rec)
     *
     * @return F-Measure
     */
    public int getFmeasure() {
        return fmeasure;
    }

    /**
     * Getter for evaluation log
     * 
     * @return evaluation log
     */
    public String getLog() {
        return log;
    }

    /**
     * Constructor for evaluation class.
     *
     * @param mb Matchbox output
     * @param gt Ground truth
     */
    public MatchboxEval(String mb, String gt) {
        this.matchboxOutput = mb;
        this.groundTruth = gt;
        log = "";
    }

    /**
     * Evaluation process. The evaluation process first creates the matchbox
     * output and ground truth lists. It then counts each page tuple from the
     * matchbox output that is in the ground truth as correctly identified tuple
     * (true positives). Those that are not in the ground truth are counted as
     * incorrectly identified tuples (false positives), and finally, those that
     * are in the ground truth but not in the matchbox output as missed tuples
     * (false negatives).
     *
     * @see getExpandedTupleList
     */
    public void evaluate() {
        ArrayList<ArrayList<Integer>> matchBoxTupleList = getMatchboxList(matchboxOutput, "=>", 0, 2);
        ArrayList<ArrayList<Integer>> gtTupleList = getGroundtruthList(groundTruth);
        for (ArrayList<Integer> matchBoxTuple : matchBoxTupleList) {
            Integer first = matchBoxTuple.get(0);
            Integer second = matchBoxTuple.get(1);
            boolean hasGtTuple = false;
            for (ArrayList<Integer> gtNTuple : gtTupleList) {
                if (gtNTuple.contains(first) && gtNTuple.contains(second)) {
                    log += ("Correct: " + first + " " + second)+"\n";
                    truePositives++;
                    hasGtTuple = true;
                }
            }
            if (!hasGtTuple) {
                log += ("Incorrect: " + first + " " + second)+"\n";
                falsePositives++;
            }
        }
        ArrayList<ArrayList<Integer>> expandedTupleList = getExpandedTupleList(gtTupleList);
        for (ArrayList<Integer> gtTuple : expandedTupleList) {
            Integer first = gtTuple.get(0);
            Integer second = gtTuple.get(1);
            boolean noGtTuple = true;
            for (ArrayList<Integer> matchBoxTuple : matchBoxTupleList) {
                if (matchBoxTuple.contains(first) && matchBoxTuple.contains(second)) {
                    noGtTuple = false;
                }
            }
            if (noGtTuple) {
                log += ("Missed: " + first + " " + second)+"\n";
                falseNegatives++;
            }
        }

        // Precision prec = true positives / ( true positives / false positives )
        double precD = ((double) truePositives / ((double) truePositives + (double) falsePositives));
        precision = (int) (precD * 100);
        // Recall rec = true positives / ( true positives / false negatives )
        double recD = ((double) truePositives / ((double) truePositives + (double) falseNegatives));
        recall = (int) (recD * 100);
        // F-measure fm = 2 * (prec * rec) / (prec + rec)
        double fmD = 2 * (precD * recD) / (precD + recD);
        fmeasure = (int) (fmD * 100);
    }

    /**
     * Create the expanded tuple list. The ground truth contains single page
     * instances without duplicates and n-tuples (duplicates, triples,
     * quadruples, etc.). n-tuples with n>2 are expanded, the result is a list
     * of 2-tuples. The expanded list is used to find the missed duplicates
     * (false negatives).
     *
     * @see getCombinationList
     * @param gtTupleList
     * @return Expanded tuple list
     */
    protected ArrayList<ArrayList<Integer>> getExpandedTupleList(ArrayList<ArrayList<Integer>> gtTupleList) {
        ArrayList<ArrayList<Integer>> expandedList = new ArrayList<ArrayList<Integer>>();
        for (List<Integer> gtNTuple : gtTupleList) {
            if (gtNTuple.size() > 1) {
                if (gtNTuple.size() == 2) {
                    ArrayList<Integer> expandedTuple = new ArrayList<Integer>();
                    expandedTuple.addAll(gtNTuple);
                    expandedList.add(expandedTuple);
                }
                if (gtNTuple.size() > 2) {
                    List<ArrayList<Integer>> expandedTuples = getCombinationList(gtNTuple);
                    expandedList.addAll(expandedTuples);
                }
            }
        }
        return expandedList;
    }

    /**
     * Create a combinations list for an n-tuple. An n-tuples is expanded as
     * k-combination. A set of similar pages represent a distinct set of
     * elements which are not arranged in a particular order. For example: The
     * 3-tuple {7,8,9} is expanded to {{7,8},{7,9},{8,9}} The 4-tuple {7,8,9,10}
     * is expanded to {{7,8},{7,9},{7,10},{8,9},{8,10},{9,10}}
     *
     * @param gtNTuple
     * @return
     */
    protected ArrayList<ArrayList<Integer>> getCombinationList(List<Integer> gtNTuple) {
        // create simple combination generator to generate 2-combinations
        ArrayList<ArrayList<Integer>> expandedList = new ArrayList<ArrayList<Integer>>();
        // create combinatorics vector
        // create simple combination generator to generate 2-combinations
        CombinatoricsVector<Integer> initialVector = new CombinatoricsVector<Integer>(gtNTuple);
        // create simple combination generator to generate 2-combinations
        Generator<Integer> gen = new SimpleCombinationGenerator<Integer>(initialVector, 2);
        Iterator<CombinatoricsVector<Integer>> itr = gen.createIterator();
        while (itr.hasNext()) {
            CombinatoricsVector<Integer> combination = itr.next();
            ArrayList<Integer> tuple = new ArrayList<Integer>();
            tuple.add(combination.getValue(0));
            tuple.add(combination.getValue(1));
            expandedList.add(tuple);
        }
        return expandedList;
    }

    /**
     * Create a list from the ground truth output.
     *
     * @param gtcont ground truth output
     * @return list from the ground truth output
     */
    protected ArrayList<ArrayList<Integer>> getGroundtruthList(String gtcont) {
        StringTokenizer lineSt = new StringTokenizer(gtcont, "\n");
        ArrayList<ArrayList<Integer>> gtList = new ArrayList<ArrayList<Integer>>();
        while (lineSt.hasMoreTokens()) {
            ArrayList<Integer> duplList = new ArrayList<Integer>();
            String line = lineSt.nextToken();

            StringTokenizer pageSt = new StringTokenizer(line, ",");
            while (pageSt.hasMoreTokens()) {
                String page = pageSt.nextToken();

                Integer pageNum = Integer.parseInt(page);
                duplList.add(pageNum);
            }
            gtList.add(duplList);
        }
        return gtList;
    }

    /**
     * Create a list from the matchbox output.
     *
     * @param result matchbox output
     * @param lineCond regex match condition for reading lines from the output
     * @param positions fixed columns of the regex matches
     * @return list from the matchbox output
     */
    protected ArrayList<ArrayList<Integer>> getMatchboxList(String result, String lineCond, int... positions) {
        ArrayList<ArrayList<Integer>> resultList = new ArrayList<ArrayList<Integer>>();
        StringTokenizer st = new StringTokenizer(result, "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (line.contains(lineCond)) {
                ArrayList<Integer> subList = new ArrayList<Integer>();
                Pattern p = Pattern.compile("-?\\d+");
                Matcher m = p.matcher(line);
                int i = 0;
                while (m.find()) {
                    for (int pos : positions) {
                        if (i == pos) {
                            try {
                                Integer pageNum = Integer.parseInt(m.group());
                                subList.add(pageNum);
                            } catch (NumberFormatException _) {
                            }
                        }
                    }
                    i++;
                }
                resultList.add(subList);
            }
        }
        return resultList;
    }
}
