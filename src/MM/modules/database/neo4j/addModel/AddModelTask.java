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
package MM.modules.database.neo4j.addModel;

import MM.data.Dataset;
import MM.modules.database.neo4j.modelRelationships.CombineModelTask;
import MM.parameters.ParameterSet;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class AddModelTask extends AbstractTask {

        private Dataset[] datasets;
        private float progress = 0.0f;
        private String message = "Creating neo4j database... ";
        private String directory;

        public AddModelTask(Dataset[] datasets, ParameterSet parameters) {
                this.datasets = datasets;
                directory = parameters.getParameter(AddModelParameters.fileName).getValue().getAbsolutePath();
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
                setStatus(TaskStatus.PROCESSING);
                try {
                        int total = 0;
                        int p = 0;
                        for (Dataset d : datasets) {
                                SBMLDocument doc = d.getDocument();

                                Model m = doc.getModel();
                                for (Species s : m.getListOfSpecies()) {
                                        total++;
                                }
                                for (Reaction r : m.getListOfReactions()) {
                                        total++;
                                        total++;
                                }


                        }
                        Map<String, String> comparison = readComparisonFile("/home/scsandra/Documents/Cellfactory/Cellfactory/model/comparison/yeast6.03-Tree.csv");

                        for (Dataset d : datasets) {
                                if (getStatus() == TaskStatus.PROCESSING) {
                                        SBMLDocument doc = d.getDocument();

                                        Model m = doc.getModel();
                                        System.out.println(this.directory);
                                        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(this.directory);

                                        registerShutdownHook(graphDb);

                                        Map<String, Node> nodes = new HashMap<>();
                                        Transaction tx = graphDb.beginTx();
                                        try {


                                                for (Species s : m.getListOfSpecies()) {
                                                        Node n = graphDb.createNode();
                                                        n.setProperty("Id", s.getId());
                                                        n.setProperty("Name", s.getName());
                                                        n.setProperty("isReaction", false);
                                                        n.setProperty("Compartment", s.getCompartment());
                                                        nodes.put(s.getId(), n);
                                                        p++;
                                                        progress = (float) p / total;




                                                }

                                                for (Reaction r : m.getListOfReactions()) {
                                                        Node n = graphDb.createNode();
                                                        n.setProperty("Id", r.getId());
                                                        n.setProperty("Name", r.getName());
                                                        n.setProperty("isReaction", true);
                                                        nodes.put(r.getId(), n);
                                                        p++;
                                                        progress = (float) p / total;
                                                }

                                                for (Reaction r : m.getListOfReactions()) {
                                                        if (nodes.containsKey(r.getId())) {
                                                                Node rNode = nodes.get(r.getId());
                                                                for (SpeciesReference s : r.getListOfReactants()) {
                                                                        if (nodes.containsKey(s.getSpecies())) {
                                                                                Node sNode = nodes.get(s.getSpecies());
                                                                                sNode.createRelationshipTo(rNode, RelTypes.REACTANS);
                                                                        }
                                                                }

                                                                for (SpeciesReference s : r.getListOfProducts()) {
                                                                        if (nodes.containsKey(s.getSpecies())) {
                                                                                Node sNode = nodes.get(s.getSpecies());
                                                                                rNode.createRelationshipTo(sNode, RelTypes.PRODUCTS);
                                                                        }
                                                                }
                                                                if (comparison != null && comparison.containsKey(r.getId())) {
                                                                        rNode.setProperty("Yeast", true);
                                                                } else if (comparison != null && comparison.containsValue(r.getId())) {
                                                                        rNode.setProperty("Yeast", true);
                                                                } else {
                                                                        rNode.setProperty("Yeast", false);
                                                                }
                                                                rNode.setProperty("Compartment",r.getCompartment());
                                                        }
                                                        p++;
                                                        progress = (float) p / total;
                                                }
                                                // Database operations go here
                                                tx.success();
                                        } finally {
                                                tx.finish();

                                        }

                                        graphDb.shutdown();
                                }


                        }
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }


                setStatus(TaskStatus.FINISHED);
        }

        private static enum RelTypes implements RelationshipType {

                REACTANS, PRODUCTS
        }

        private static void registerShutdownHook(final GraphDatabaseService graphDb) {
// Registers a shutdown hook for the Neo4j instance so that it
// shuts down nicely when the VM exits (even if you "Ctrl-C" the
// running application).
                Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                                graphDb.shutdown();
                        }
                });
        }

        private Map<String, String> readComparisonFile(String modelFile) {
                try {
                        Map<String, String> comparisonMap = new HashMap<>();
                        CsvReader comparison = new CsvReader(new FileReader(modelFile), '\t');
                        try {
                                while (comparison.readRecord()) {
                                        try {
                                                String[] comparisonRow = comparison.getValues();
                                                comparisonMap.put(comparisonRow[0], comparisonRow[1]);
                                        } catch (IOException | NumberFormatException e) {
                                                e.printStackTrace();
                                        }
                                }
                        } catch (IOException ex) {
                                Logger.getLogger(CombineModelTask.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return comparisonMap;
                } catch (FileNotFoundException ex) {
                        Logger.getLogger(CombineModelTask.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                }
        }
}
