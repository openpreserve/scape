/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opflabs.scape.tb.gw.gen;

public enum IOType {

    INPUT {

        @Override
        public String toString() {
            return "input";
        }
    },
    OUTPUT {

        @Override
        public String toString() {
            return "output";
        }
    }
}
