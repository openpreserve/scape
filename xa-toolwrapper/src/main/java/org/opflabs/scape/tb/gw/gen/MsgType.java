/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opflabs.scape.tb.gw.gen;

public enum MsgType {

    REQUEST {

        @Override
        public String toString() {
            return "Request";
        }
    },
    RESPONSE {

        @Override
        public String toString() {
            return "Response";
        }
    }
}
