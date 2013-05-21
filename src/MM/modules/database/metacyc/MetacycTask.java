/*
 * Copyright 2007-2013 VTT Biotechnology
 * This file is part of MetModels.
 *
 * MetModels is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MetModels is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package MM.modules.database.metacyc;

import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import com.csvreader.CsvWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author scsandra
 */
public class MetacycTask extends AbstractTask {

        private float progress = 0.0f;
        private String message = "MetaCyc db... ";
        private List<MetacycData> data;
        private File file;

        public MetacycTask(ParameterSet parameters) {
                this.data = new ArrayList<>();
                this.file = (File) parameters.getParameter(MetacycParameters.fileName).getValue();
        }

        @Override
        public String getTaskDescription() {
                return message;
        }

        @Override
        public double getFinishedPercentage() {
                return progress;
        }

        @Override
        public void cancel() {
                setStatus(TaskStatus.CANCELED);
        }

        @Override
        public void run() {
                try {
                        setStatus(TaskStatus.PROCESSING);
                        this.getReactions();
                        // this.printData(this.file.getAbsolutePath());
                        setStatus(TaskStatus.FINISHED);
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        public void getReactions() {
                BufferedReader in = null;
                try {
                        String query = "http://metacyc.org/META/class-instances?object=Reactions";
                        URL metacyc = new URL(query);
                        URLConnection yc = metacyc.openConnection();
                        in = new BufferedReader(new InputStreamReader(
                                yc.getInputStream()));
                        String inputLine = null;
                        int count = 0;
                        CsvWriter w = new CsvWriter(file.getAbsolutePath());
                        while ((inputLine = in.readLine()) != null) {

                                try {
                                        if (!inputLine.isEmpty()) {
                                                if (inputLine.contains("REACTION&object")) {
                                                        count++;
                                                        String[] s = new String[1];
                                                        String reactionsString = processReactionLink(inputLine);
                                                        String reactionCompounds = processReactionCompoundsLink(inputLine);
                                                        String[] IDs = ParseCompounds(reactionCompounds);
                                                        w.writeRecord(IDs);
                                                        //  ParseReaction(reactionsString, reactionCompounds);
                                                }
                                        } else {
                                        }
                                } catch (Exception e) {
                                }
                                if (count == 50) {
                                        break;
                                }
                        }

                        w.close();

                        in.close();
                } catch (IOException ex) {
                }
        }

        private String processReactionLink(String inputLine) {

                Pattern pattern = Pattern.compile("REACTION&object=\\S*\"");
                Matcher m = pattern.matcher(inputLine);
                String r = "";
                while (m.find()) {
                        r = m.group();
                }

                if (!r.isEmpty()) {
                        r = r.substring(0, r.length() - 1);
                }
                return r;
        }

        private void ParseReaction(String s, String components) {
                BufferedReader in = null;
                MetacycData reactionData = new MetacycData();
                reactionData.setReaction(components);
                try {
                        String query = "http://metacyc.org/META/NEW-IMAGE?type=" + s;
                        URL metacyc = new URL(query);
                        URLConnection yc = metacyc.openConnection();
                        in = new BufferedReader(new InputStreamReader(
                                yc.getInputStream()));
                        String inputLine = null;
                        boolean genes = false;
                        boolean enzymes = false;
                        boolean pathways = false;
                        boolean enzPrimary = false;
                        boolean enzSynonyms = false;
                        boolean unificationLinks = false;
                        boolean relationalLinks = false;
                        GeneData gd = null;
                        while ((inputLine = in.readLine()) != null) {
                                try {

                                        if (!inputLine.isEmpty()) {
                                                if (inputLine.contains("EC-NUMBER&object")) {
                                                        String ecNumber = processECNumber(inputLine);
                                                        reactionData.setECNumber(ecNumber);
                                                }
                                                if (inputLine.contains("and Genes: ") && !genes) {
                                                        genes = true;
                                                        gd = new GeneData();
                                                }
                                                if (genes) {
                                                        if (inputLine.contains("REACTION&object") || inputLine.contains("DISPLAY&object")) {
                                                                gd.enzymeLink = this.processLink(inputLine);
                                                                // System.out.println("Link: " + gd.enzymeLink);
                                                        }
                                                        if (inputLine.contains("<b>Enzyme</b>:")) {
                                                                String enzymeName = inputLine.substring(inputLine.indexOf("<b>Enzyme</b>:") + 14, inputLine.indexOf("<br>"));
                                                                gd.enzymeName = this.formatString(enzymeName);
                                                                //  System.out.println("Name: " + gd.enzymeName);
                                                                enzymes = true;
                                                        }
                                                        if (inputLine.contains("<b>Synonyms:</b>") && enzymes) {
                                                                String enzymeSynonyms = inputLine.substring(inputLine.indexOf("<b>Synonyms:</b>") + 16, inputLine.indexOf("', WIDTH"));
                                                                gd.enzymeSynonyms = this.formatString(enzymeSynonyms);
                                                                //  System.out.println("Synonyms: " + gd.enzymeSynonyms);
                                                                enzymes = false;
                                                        }
                                                        if (inputLine.contains("GENE&object")) {
                                                                gd.geneLink = this.processLink(inputLine);
                                                                //System.out.println("gene Links: " + gd.geneLink);
                                                                enzymes = false;
                                                        }
                                                        if (inputLine.contains("<b>Gene:</b>")) {
                                                                gd.geneName = inputLine.substring(inputLine.indexOf("<b>Gene:</b>") + 12, inputLine.indexOf("<br>"));
                                                                //  System.out.println("gene Name: " + gd.geneName);
                                                                enzymes = false;
                                                        }
                                                        if (inputLine.contains("<b>Synonyms:</b>") && !enzymes) {
                                                                gd.genesSynonyms = inputLine.substring(inputLine.indexOf("<b>Synonyms:</b>") + 16, inputLine.indexOf("<br>"));
                                                                //System.out.println("gene synonyms: " + gd.genesSynonyms);
                                                                enzymes = false;
                                                        }
                                                        if (inputLine.contains("<b>Product:</b>") && !enzymes) {
                                                                gd.product = inputLine.substring(inputLine.indexOf("<b>Product:</b>") + 15, inputLine.indexOf("<br>"));
                                                                // System.out.println("gene product: " + gd.product);
                                                        }

                                                        if (inputLine.contains("<b>Species:</b>")) {
                                                                gd.organismName = inputLine.substring(inputLine.indexOf("<b>Species:</b>") + 19, inputLine.indexOf("</i>"));
                                                                //  System.out.println("organism Name: " + gd.organismName);
                                                        }

                                                        if (inputLine.contains("ORGANISM&object")) {
                                                                gd.organismLink = this.processLink(inputLine);
                                                                //System.out.println("organism Link: " + gd.organismLink);   
                                                                reactionData.addEnzymesGenes(gd);
                                                                gd = new GeneData();
                                                        }

                                                        if (inputLine.contains("Show Atom Mapping") || inputLine.contains("In Pathway:")) {
                                                                reactionData.addEnzymesGenes(gd);
                                                                genes = false;
                                                        }
                                                }
                                                if (inputLine.contains("In Pathway: ")) {
                                                        pathways = true;
                                                }

                                                if (pathways) {
                                                        if (inputLine.contains("PATHWAY&object")) {
                                                                reactionData.addPathwayLink(this.processLink(inputLine));
                                                        }
                                                        if (inputLine.contains("<b>Pathway</b>:")) {
                                                                reactionData.addPathwayName(inputLine.substring(inputLine.indexOf("<b>Pathway</b>:") + 15, inputLine.indexOf("<br>")));
                                                        }

                                                        if (inputLine.contains("<b>Synonyms:</b>")) {
                                                                reactionData.addPathwaySynonyms(inputLine.substring(inputLine.indexOf("<b>Synonyms:</b>") + 16, inputLine.indexOf("', WIDTH")));
                                                                pathways = false;
                                                        }
                                                }

                                                if (inputLine.contains("Mass balance status: ")) {
                                                        pathways = false;
                                                        reactionData.addMassBalance(inputLine.substring(inputLine.indexOf("Mass balance status: ") + 21, inputLine.indexOf(".")));
                                                }

                                                if (enzPrimary) {
                                                        String name = inputLine.substring(0, inputLine.indexOf("<p"));
                                                        reactionData.addPrimaryName(this.formatString(name));
                                                        enzPrimary = false;
                                                }
                                                if (inputLine.contains("Enzyme Commission Primary Name: ")) {
                                                        enzPrimary = true;
                                                }

                                                if (enzSynonyms) {
                                                        reactionData.addPrimaryName(this.formatString(inputLine));
                                                }
                                                if (inputLine.contains("Enzyme Commission Synonyms: ")) {
                                                        enzSynonyms = true;
                                                }

                                                if (inputLine.contains("Enzyme Commission Summary: ") || inputLine.contains("Gene-Reaction Schematic:")) {
                                                        enzSynonyms = false;
                                                }

                                                if (relationalLinks) {
                                                        reactionData.addRelationalLinks(inputLine);
                                                }

                                                if (inputLine.contains("Relationship Links: ")) {
                                                        unificationLinks = false;
                                                        relationalLinks = true;
                                                }

                                                if (unificationLinks) {
                                                        reactionData.addUnificationLinks(inputLine);
                                                }

                                                if (inputLine.contains("Unification Links:")) {
                                                        unificationLinks = true;
                                                }

                                                if (inputLine.contains("Credits:") || inputLine.contains("References")) {
                                                        unificationLinks = false;
                                                        relationalLinks = false;
                                                }


                                        } else {
                                        }
                                } catch (Exception e) {
                                }
                        }

                        in.close();

                        this.data.add(reactionData);
                } catch (IOException ex) {
                        ex.printStackTrace();
                }

        }

        private String processECNumber(String inputLine) {
                Pattern pattern = Pattern.compile("EC-NUMBER&object=\\S*\"");
                Matcher m = pattern.matcher(inputLine);
                String r = "";
                while (m.find()) {
                        r = m.group();
                }

                if (!r.isEmpty()) {
                        r = r.substring(r.indexOf("=EC-") + 4, r.length() - 1);
                }
                return r;
        }

        private String processReactionCompoundsLink(String inputLine) {
                String r = inputLine;
                if (!r.isEmpty()) {
                        r = r.substring(r.indexOf("\">") + 2, r.indexOf("</A>"));
                }
                r = formatString(r);
                return r;
        }

        private String formatString(String r) {
                r = r.replaceAll("<SUB>", "");
                r = r.replaceAll("</SUB>", "");
                r = r.replaceAll("<SUP>", "");
                r = r.replaceAll("</SUP>", "");
                r = r.replaceAll("<sup>", "");
                r = r.replaceAll("</sup>", "");
                r = r.replaceAll("h&nu", "light");
                r = r.replaceAll("<sub>", "");
                r = r.replaceAll("</sub>", "");
                r = r.replaceAll("<i>", "");
                r = r.replaceAll("</i>", "");
                r = r.replaceAll("&", "");
                r = r.replaceAll(";", "");
                r = r.replaceAll("\\'", "'");
                r = r.replaceAll("<em>", "");
                r = r.replaceAll("</em>", "");
                r = r.replaceAll("&prime;", "′");
                r = r.replaceAll("<small>", "");
                r = r.replaceAll("</small>", "");
                return r;
        }

        private String processLink(String inputLine) {
                Pattern pattern = Pattern.compile("&object=\\S*\"");
                Matcher m = pattern.matcher(inputLine);
                String r = "";
                while (m.find()) {
                        r = m.group();
                }

                if (!r.isEmpty()) {
                        r = r.substring(0, r.length() - 1);
                }
                return r;
        }

        private void printData(String fileName) {
                try {
                        CsvWriter w = new CsvWriter(fileName);
                        String[] titles = {"EC Number", "Enzymes and Genes", "Pathway", "Reactants", "Products", "Direction", "Mass balance status", "Enzyme primary name",
                                "Enzyme synonyms", "Unification links", "Relationship links"};

                        w.writeRecord(titles);
                        System.out.println(this.data.size());
                        for (MetacycData reaction : this.data) {
                                String[] r = reaction.getString();
                                w.writeRecord(r);
                        }
                        w.close();
                } catch (IOException ex) {
                        Logger.getLogger(MetacycTask.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        private String[] ParseCompounds(String reactionCompounds) {
                reactionCompounds = reactionCompounds.replace(" = ", "+");
                reactionCompounds = reactionCompounds.replace(" rarr ", "+");
                reactionCompounds = reactionCompounds.replace(" larr ", "+");
                reactionCompounds = reactionCompounds.replace(" harr ", "+");
                System.out.println(reactionCompounds);
                String[] compounds = reactionCompounds.split(" + ");

                BufferedReader in = null;
                for (String compound : compounds) {
                        try {
                                System.out.println(compound);
                                String query = "http://metacyc.org/META/search-query?type=COMPOUND&name=" + compound;
                                URL metacyc = new URL(query);
                                URLConnection yc = metacyc.openConnection();
                                in = new BufferedReader(new InputStreamReader(
                                        yc.getInputStream()));
                                String inputLine = null;

                               /* while ((inputLine = in.readLine()) != null) {
                                        try {
                                        } catch (Exception e) {
                                        }

                                }*/
                        } catch (Exception r) {
                                System.out.println(compound);
                        }

                }
                return new String[3];
        }
}
