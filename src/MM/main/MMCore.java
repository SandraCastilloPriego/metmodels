/*
 * Copyright 2007-2012 
 *
 * This file is part of MM.
 *
 * MM is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MM is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MM; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package MM.main;

import MM.desktop.Desktop;
import MM.desktop.impl.MainWindow;
import MM.desktop.impl.helpsystem.HelpImpl;
import MM.modules.MMModule;
import MM.modules.MMProcessingModule;
import MM.modules.configuration.general.GeneralconfigurationParameters;
import MM.parameters.ParameterSet;
import MM.taskcontrol.TaskController;
import MM.taskcontrol.impl.TaskControllerImpl;
import MM.util.dialogs.ExitCode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This interface represents MM core modules - I/O, task controller and GUI.
 */
/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
public class MMCore implements Runnable {

        public static final File CONFIG_FILE = new File("conf/config.xml");
        public static final String STANDARD_RANGE = "standard_ranges";
        public static final String STANDARD_NAME = "standard_name";
        private static Logger logger = Logger.getLogger(MMCore.class.getName());
        private static GeneralconfigurationParameters preferences;
        private static TaskControllerImpl taskController;
        private static MMModule[] initializedModules;
        private static HelpImpl help;
        private static MainWindow desktop;
      

        /**
         * Returns a reference to local task controller.
         *
         * @return TaskController reference
         */
        public static TaskController getTaskController() {
                return taskController;
        }

        /**
         * Returns a reference to Desktop.
         */
        public static Desktop getDesktop() {
                return desktop;
        }

        /**
         * Returns an array of all initialized MM modules
         *
         * @return Array of all initialized MM modules
         */
        public static MMModule[] getAllModules() {
                return initializedModules;
        }

        /**
         *
         *
         * @return
         */
        public static HelpImpl getHelpImpl() {
                return help;
        }

        /**
         * Saves configuration and exits the application.
         *
         */
        public static ExitCode exitMM() {

                // If we have GUI, ask if use really wants to quit
                int selectedValue = JOptionPane.showInternalConfirmDialog(desktop.getMainFrame().getContentPane(),
                        "Are you sure you want to exit?", "Exiting...",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (selectedValue != JOptionPane.YES_OPTION) {
                        return ExitCode.CANCEL;
                }

                desktop.getMainFrame().dispose();

                logger.info("Exiting MM");

                System.exit(0);

                return ExitCode.OK;

        }

        /**
         * Main method
         */
        public static void main(String args[]) {
                // create the GUI in the event-dispatching thread
                MMCore core = new MMCore();
                SwingUtilities.invokeLater(core);

        }

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
                logger.log(Level.INFO, "Starting MM {0}", getMMVersion());

                logger.fine("Loading core classes..");

                // create instance of preferences
                preferences = new GeneralconfigurationParameters();               

                // create instances of core modules

                // load configuration from XML
                taskController = new TaskControllerImpl();
                desktop = new MainWindow();
                help = new HelpImpl();

                logger.fine("Initializing core classes..");


                // Second, initialize desktop, because task controller needs to add
                // TaskProgressWindow to the desktop
                desktop.initModule();

                // Last, initialize task controller
                taskController.initModule();

                logger.fine("Loading modules");

                Vector<MMModule> moduleSet = new Vector<>();

                for (Class<?> moduleClass : MMModulesList.MODULES) {

                        try {

                                logger.log(Level.FINEST, "Loading module {0}", moduleClass.getName());

                                // create instance and init module
                                MMModule moduleInstance = (MMModule) moduleClass.newInstance();

                                // add desktop menu icon
                                if (moduleInstance instanceof MMProcessingModule) {
                                        desktop.getMainMenu().addMenuItemForModule(
                                                (MMProcessingModule) moduleInstance);
                                }

                                // add to the module set
                                moduleSet.add(moduleInstance);

                        } catch (Throwable e) {
                                logger.log(Level.SEVERE,
                                        "Could not load module " + moduleClass, e);
                                e.printStackTrace();
                                continue;
                        }

                }

                MMCore.initializedModules = moduleSet.toArray(new MMModule[0]);

                if (CONFIG_FILE.canRead()) {
                        try {
                                loadConfiguration(CONFIG_FILE);
                        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
                                
                        }
                }

                // register shutdown hook
                ShutDownHook shutDownHook = new ShutDownHook();
                Runtime.getRuntime().addShutdownHook(shutDownHook);

                // show the GUI
                logger.info("Showing main window");
                ((MainWindow) desktop).setVisible(true);

                // show the welcome message
                desktop.setStatusBarText("Welcome to MM!");
                preferences.setProxy();

        }

        public static void saveConfiguration(File file)
                throws ParserConfigurationException, TransformerException,
                FileNotFoundException {

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                Document configuration = dBuilder.newDocument();
                Element configRoot = configuration.createElement("configuration");
                configuration.appendChild(configRoot);                

                Element modulesElement = configuration.createElement("modules");
                configRoot.appendChild(modulesElement);

                // traverse modules
                for (MMModule module : getAllModules()) {

                        String className = module.getClass().getName();

                        Element moduleElement = configuration.createElement("module");
                        moduleElement.setAttribute("class", className);
                        modulesElement.appendChild(moduleElement);

                        Element paramElement = configuration.createElement("parameters");
                        moduleElement.appendChild(paramElement);

                        ParameterSet moduleParameters = module.getParameterSet();
                        if (moduleParameters != null) {
                                moduleParameters.saveValuesToXML(paramElement);
                        }

                }


                // Save Parameters path
                String className = "ParameterPath";
                Element moduleElement = configuration.createElement("module");
                moduleElement.setAttribute("class", className);
                modulesElement.appendChild(moduleElement);

                Element paramElement = configuration.createElement("parameters");
                moduleElement.appendChild(paramElement);
                MMCore.getDesktop().saveParameterPathToXML(paramElement);



                TransformerFactory transfac = TransformerFactory.newInstance();
                Transformer transformer = transfac.newTransformer();
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(
                        "{http://xml.apache.org/xslt}indent-amount", "4");

                StreamResult result = new StreamResult(new FileOutputStream(file));
                DOMSource source = new DOMSource(configuration);
                transformer.transform(source, result);

                logger.log(Level.INFO, "Saved configuration to file {0}", file);

        }

        public static void loadConfiguration(File file)
                throws ParserConfigurationException, SAXException, IOException,
                XPathExpressionException {

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document configuration = dBuilder.parse(file);

                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();

                logger.finest("Loading desktop configuration");

                XPathExpression expr = xpath.compile("//configuration/Standards");
                NodeList nodes = (NodeList) expr.evaluate(configuration,
                        XPathConstants.NODESET);               

                logger.finest("Loading modules configuration");

                for (MMModule module : getAllModules()) {

                        String className = module.getClass().getName();
                        expr = xpath.compile("//configuration/modules/module[@class='" + className + "']/parameters");
                        nodes = (NodeList) expr.evaluate(configuration,
                                XPathConstants.NODESET);
                        if (nodes.getLength() != 1) {
                                continue;
                        }

                        Element moduleElement = (Element) nodes.item(0);

                        ParameterSet moduleParameters = module.getParameterSet();
                        if (moduleParameters != null) {
                                moduleParameters.loadValuesFromXML(moduleElement);
                        }
                }

                String className = "ParameterPath";
                expr = xpath.compile("//configuration/modules/module[@class='" + className + "']/parameters");
                if (nodes.getLength() == 1) {
                        nodes = (NodeList) expr.evaluate(configuration,
                                XPathConstants.NODESET);

                        Element moduleElement = (Element) nodes.item(0);
                        MMCore.getDesktop().loadParameterPathFromXML(moduleElement);
                }

                logger.log(Level.INFO, "Loaded configuration from file {0}", file);
        }
        
        public static String getMMVersion() {
                return MMVersion.MM;
        }

        public static GeneralconfigurationParameters getPreferences() {
                return preferences;
        }

        public static void setPreferences(GeneralconfigurationParameters preferences2) {
                preferences = preferences2;
        }        
}
