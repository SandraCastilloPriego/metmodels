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
package MM.modules.database.neo4j.modelRelationships;

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
import org.neo4j.tooling.GlobalGraphOperations;

/**
 *
 * @author scsandra
 */
public class CombineModelTask extends AbstractTask {

        private float progress = 0.0f;
        private String message = "Search for Pathways... ";
        private String DBdirectory, modelFile;
        private String modelName;

        public CombineModelTask(ParameterSet parameters) {
                this.DBdirectory = parameters.getParameter(CombineModelParameters.fileName).getValue().getAbsolutePath();
                this.modelFile = parameters.getParameter(CombineModelParameters.comparisonFile).getValue().getAbsolutePath();
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

                        Map<String, String> comparison = readComparisonFile(this.modelFile);                        
                        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(this.DBdirectory);
                  
                       
                        Transaction tx = graphDb.beginTx(); 
                        System.out.println("working");
                        try {
                                for (Node n : GlobalGraphOperations.at(graphDb).getAllNodes()) {                                
                                        System.out.println(n.getProperty("Id"));
                                        if (comparison != null && comparison.containsKey(n.getProperty("Id"))) {
                                                n.setProperty(this.modelName, true);
                                        } else if (comparison != null && comparison.containsValue(n.getProperty("Id"))) {
                                                n.setProperty(this.modelName, true);
                                        } else {
                                                n.setProperty(this.modelName, false);
                                        }
                                }
                                tx.success();
                        } finally {
                                tx.finish();

                        }
                        registerShutdownHook(graphDb);
                        graphDb.shutdown();


                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }


                setStatus(TaskStatus.FINISHED);
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
}
