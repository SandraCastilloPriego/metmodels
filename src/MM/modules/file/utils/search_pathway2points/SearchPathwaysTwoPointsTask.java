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
package MM.modules.file.utils.search_pathway2points;

import MM.data.Dataset;
import MM.main.MMCore;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.*;

/**
 *
 * @author scsandra
 */
public class SearchPathwaysTwoPointsTask extends AbstractTask {

        private Dataset[] datasets;
        private float progress = 0.0f;
        private String message = "Search for Pathways between two points... ";
        private String dataset, initialId, finalId;
        private List<String> used;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JTextArea tf;
        private List<String> pathway;

        public SearchPathwaysTwoPointsTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
                this.dataset = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.data).getValue();
                this.initialId = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.idFrom).getValue();
                this.finalId = (String) parameters.getParameter(SearchPathwaysTwoPointsParameters.idTo).getValue();


                this.pathway = new ArrayList<>();

                this.frame = new JInternalFrame("Result", true, true, true, true);

                this.tf = new JTextArea();
                this.panel = new JScrollPane(tf);
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
                        this.compareReactions();
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        public void compareReactions() {
                setStatus(TaskStatus.PROCESSING);
                try {
                        if (getStatus() == TaskStatus.PROCESSING) {


                                frame.setSize(new Dimension(700, 500));
                                frame.add(this.panel);


                                MMCore.getDesktop().addInternalFrame(frame);


                                SBMLDocument doc = null;
                                for (Dataset d : datasets) {
                                        if (this.dataset.contains(d.getDatasetName())) {
                                                doc = d.getDocument();
                                        }
                                }

                                Model m = doc.getModel();

                                List<String> reactionsIds = new ArrayList<>();
                                for (Reaction r : m.getListOfReactions()) {
                                        boolean found = false;
                                        for (SpeciesReference reactant : r.getListOfReactants()) {
                                                Species s = reactant.getSpeciesInstance();
                                                if (s.getId() == null ? initialId == null : s.getId().equals(initialId)) {
                                                        reactionsIds.add(r.getId());
                                                        found = true;
                                                        break;
                                                }
                                        }
                                        if (!found) {
                                                for (SpeciesReference product : r.getListOfProducts()) {
                                                        Species s = product.getSpeciesInstance();
                                                        if (s.getId() == null ? initialId == null : s.getId().equals(initialId)) {
                                                                reactionsIds.add(r.getId());
                                                        }
                                                }
                                        }
                                }

                                for (String reactionId : reactionsIds) {
                                        this.used = new ArrayList<>();

                                        pathway.clear();

                                        String s = this.tf.getText();
                                        s = s.concat("Reaction: " + reactionId + "\n");
                                        this.tf.setText(s);

                                        boolean fin = printPathway(m.getReaction(reactionId), m);

                                        if (fin) {
                                                this.printPathway();
                                        }
                                }
                        }


                } catch (Exception ex) {
                        Logger.getLogger(SearchPathwaysTwoPointsTask.class.getName()).log(Level.ERROR, null, ex);
                }

                setStatus(TaskStatus.FINISHED);
        }

        private boolean printPathway(Reaction reaction, Model m) {
                boolean exit = false;
                // For each product of the reaction
                for (SpeciesReference pref : reaction.getListOfProducts()) {

                        Species p = pref.getSpeciesInstance();

                        //if the product id is the same as the final id, the algorithm is over.
                        if (p.getId() == null ? this.finalId == null : p.getId().equals(this.finalId)) {
                                pathway.add(reaction.getName() + " - " + reaction.getId() + "--->" + p.getName() + " - " + p.getId() + "\n");
                                return true;
                        }

                        for (Reaction r : m.getListOfReactions()) {
                                if (r != reaction) {
                                        ListOf<SpeciesReference> reactants = r.getListOfReactants();
                                        for (SpeciesReference reactantRef : reactants) {
                                                Species reactant = reactantRef.getSpeciesInstance();
                                                if (reactant.getId() == null ? p.getId() == null : reactant.getId().equals(p.getId())) {
                                                        pathway.add(reaction.getName() + " - " + reaction.getId() + "--->" + p.getName() + " - " + p.getId() + "---> " + r.getName() + " - " + r.getId() + "\n");
                                                        this.used.add(reaction.getId());
                                                        if (!this.used.contains(r.getId())) {
                                                                return printPathway(r, m);
                                                        }
                                                }

                                        }
                                }

                        }

                }
                return exit;
        }

        private void printPathway() {
                String text = this.tf.getText();
                for (String s : pathway) {
                        text = text.concat(s + "\n");
                }
                text = text.concat("--------------------------------" + "\n");
                this.tf.setText(text);
        }
}
