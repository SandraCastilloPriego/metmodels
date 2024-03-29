/*
 * Copyright 2007-2012 
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
package MM.data.impl.peaklists;

import MM.data.IdentificationType;
import MM.data.Row;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SCSANDRA
 */
public class SimplePeakListRowLCMS implements Row {

        private String FAComposition = "", allNames, Name, lipidClass = "0";
        private double averageMZ, averageRT, numFound, pValue, qValue;
        private int standard, ID, aligment;
        private boolean control, selection;
        private String VTTid = "", VTTAllIDs = "";
        private Hashtable<String, Double> peaks;
        private Hashtable<String, String> peaksString;
        private String pubchemID = "", comment, formula, url;
        private String identificationType = IdentificationType.UNKNOWN.toString();
        private List<Color> colors;

        public SimplePeakListRowLCMS(int ID, double averageMZ, double averageRT, double numFound, double pValue, double qValue,
                int standard, String lipidClass, String Name, String identificationType, String allNames, String FAComposition, String comment, String formula, String url) {
                this.ID = ID;
                this.FAComposition = FAComposition;
                this.averageMZ = averageMZ;
                this.averageRT = averageRT;
                this.numFound = numFound;
                this.standard = standard;
                this.lipidClass = String.valueOf(lipidClass);
                this.Name = Name;
                this.allNames = allNames;
                this.identificationType = identificationType;
                this.peaks = new Hashtable<String, Double>();
                this.peaksString = new Hashtable<String, String>();
                this.aligment = -1;
                this.colors = new ArrayList<Color>();
                this.comment = comment;
                this.formula = formula;
                this.url = url;
        }

        public SimplePeakListRowLCMS() {
                this.peaks = new Hashtable<String, Double>();
                this.peaksString = new Hashtable<String, String>();
                this.colors = new ArrayList<Color>();
                this.ID = -1;
                this.aligment = -1;
        }

        @Override
        public Row clone() {
                Row peakListRow = new SimplePeakListRowLCMS(this.ID, this.averageMZ, this.averageRT,
                        this.numFound, this.pValue, this.qValue, this.standard, this.lipidClass, this.Name, this.identificationType, this.allNames,
                        this.FAComposition, this.comment, this.formula, this.url);
                peakListRow.setVar("setNumberAligment", aligment);
                peakListRow.setVar("setVTTID", VTTid);
                peakListRow.setVar("setAllVTTD", VTTAllIDs);
                String str;
                Set<String> set = peaks.keySet();

                Iterator<String> itr = set.iterator();
                while (itr.hasNext()) {
                        str = itr.next();
                        peakListRow.setPeak(str, peaks.get(str));
                }
                ((SimplePeakListRowLCMS) peakListRow).setSelectionMode(this.isSelected());
                return peakListRow;
        }

        public String getIdentificationType() {
                return this.identificationType;
        }

        public void setIdentificationType(String identificationType) {
                this.identificationType = identificationType;
        }

        public double getMZ() {
                return this.averageMZ;
        }

        public double getRT() {
                return this.averageRT;
        }

        public double getNumFound() {
                return this.numFound;
        }

        public int getStandard() {
                return this.standard;
        }

        public String getMolClass() {
                return this.lipidClass;
        }

        public String getName() {
                return this.Name;
        }

        public Object getPeak(String ExperimentName) {
                if (this.peaks.containsKey(ExperimentName)) {
                        return this.peaks.get(ExperimentName);
                } else if (this.peaksString.containsKey(ExperimentName)) {
                        return this.peaksString.get(ExperimentName);
                }
                return null;
        }

        public void setMZ(double averageMZ) {
                this.averageMZ = averageMZ;
        }

        public void setRT(double averageRT) {
                this.averageRT = averageRT;
        }

        public void setNumFound(double numFound) {
                this.numFound = numFound;
        }

        public void setStandard(int standard) {
                this.standard = standard;
        }

        public void setLipidClass(String lipidClass) {
                this.lipidClass = lipidClass;
        }

        public void setName(String Name) {
                this.Name = Name;
        }

        public void setAllNames(String allNames) {
                if (allNames == null || allNames.endsWith("null")) {
                        this.allNames = "";
                } else {
                        this.allNames = allNames;
                }
        }

        public void setPeak(String name, Double value) {
                if (value != null && name != null) {
                        this.peaks.put(name, value);
                }
        }

        public String getFAComposition() {
                return this.FAComposition;
        }

        public void setFAComposition(String FAComposition) {
                if (FAComposition == null || FAComposition.endsWith("null")) {
                        this.FAComposition = "";
                } else {
                        this.FAComposition = FAComposition;
                }
        }

        public int getID() {
                return ID;
        }

        public void setID(int ID) {
                this.ID = ID;
        }

        public void setControl(boolean control) {
                this.control = control;
        }

        public boolean getControl() {
                return this.control;
        }

        public int getNumberPeaks() {
                return this.peaks.size();
        }

        public int getNumberAlignment() {
                return this.aligment;
        }

        public void setNumberAlignment(int aligment) {
                this.aligment = aligment;
        }

        public void setNumberAligment(int aligment) {
                this.aligment = aligment;
        }

        public String getAllNames() {
                return this.allNames;
        }

        public String getPubChemID() {
                return this.pubchemID;
        }

        public void removePeaks() {
                this.peaks = new Hashtable<String, Double>();
        }

        public Double[] getPeaks(String[] columnNames) {
                Double[] aPeaks = new Double[this.peaks.size()];
                if (columnNames == null) {
                        String str;
                        Set<String> set = peaks.keySet();
                        int cont = 0;
                        Iterator<String> itr = set.iterator();
                        while (itr.hasNext()) {
                                str = itr.next();
                                aPeaks[cont++] = peaks.get(str);
                        }
                } else {
                        for (int i = 0; i < columnNames.length; i++) {
                                aPeaks[i] = peaks.get(columnNames[i]);
                        }
                }
                return aPeaks;
        }

        public boolean isSelected() {
                return this.selection;
        }

        public void setSelectionMode(boolean selectionMode) {
                this.selection = selectionMode;
        }

        public void setPeak(String str, String get) {
                this.peaksString.put(str, get);
        }

        public void removeNoSamplePeaks(String[] group) {
                Hashtable<String, Double> newPeaks = new Hashtable<String, Double>();
                for (String name : group) {
                        if (this.peaks.containsKey(name)) {
                                newPeaks.put(name, this.peaks.get(name));
                        }
                }
                this.peaks = newPeaks;
        }

        public String getVTTID() {
                return this.VTTid;
        }

        public void setVTTID(String VTTID) {
                if (VTTID == null || VTTID.endsWith("null")) {
                        this.VTTid = "";
                } else {
                        this.VTTid = VTTID;
                }
        }

        public String getAllVTTID() {
                return this.VTTAllIDs;
        }

        public void setAllVTTD(String AllVTTIDs) {
                if (AllVTTIDs == null || AllVTTIDs.endsWith("null")) {
                        this.VTTAllIDs = "";
                } else {
                        this.VTTAllIDs = AllVTTIDs;
                }
        }

        public void setPubChemID(String pubchemID) {
                if (pubchemID == null || pubchemID.endsWith("null")) {
                        this.pubchemID = "";
                } else {
                        this.pubchemID = pubchemID;
                }
        }

        public double getPValue() {
                return this.pValue;
        }

        public void setPValue(double pValue) {
                this.pValue = pValue;
        }

        public double getQValue() {
                return this.qValue;
        }

        public void setQValue(double qValue) {
                this.qValue = qValue;
        }

        public String getComment() {
                return this.comment;
        }

        public void setComment(String comment) {
                this.comment = comment;
        }

        public String getMolecularFormula() {
                return this.formula;
        }

        public void setMolecularFormula(String formula) {
                this.formula = formula;
        }

        public String getURL() {
                return this.url;
        }

        public void setURL(String url) {
                this.url = url;
        }


        public Hashtable<String, Double> getPeaksTable() {
                return this.peaks;
        }

        public void scaleArea(double[] scalings, String[] columnNames) {
                for (int i = 0; i < columnNames.length; i++) {
                        if (this.getPeak(columnNames[i]).getClass().toString().contains("Double")) {
                                this.setPeak(columnNames[i], (Double) this.getPeak(columnNames[i]) * scalings[i]);
                        }
                }
        }

        public Object getVar(String varName) {
                try {
                        Method m = this.getClass().getMethod(varName, new Class[]{});
                        return m.invoke(this);

                } catch (IllegalAccessException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
        }

        public void setVar(String varName, Object value) {
                try {
                        Class partypes[] = new Class[1];
                        if (value.getClass().toString().contains("Double")) {
                                partypes[0] = Double.TYPE;
                        } else if (value.getClass().toString().contains("Integer")) {
                                partypes[0] = Integer.TYPE;
                        } else if (value.getClass().toString().contains("String")) {
                                partypes[0] = String.class;
                        } else if (value.getClass().toString().contains("Boolean")) {
                                partypes[0] = Boolean.TYPE;
                        } else {
                                partypes[0] = Object.class;
                        }
                        Method m = this.getClass().getMethod(varName, partypes);
                        Object[] parameters = new Object[1];
                        parameters[0] = value;
                        m.invoke(this, parameters);

                } catch (IllegalAccessException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                        Logger.getLogger(SimplePeakListRowLCMS.class.getName()).log(Level.SEVERE, null, ex);
                }

        }

        @Override
        public String toString() {
                return this.ID + " - " + this.Name;
        }

        @Override
        public Color getColor(int column) {
                try {
                        return this.colors.get(column);
                } catch (Exception e) {
                        return null;
                }
        }

        @Override
        public void setColor(Color color, int column) {
                try {
                        this.colors.set(column, color);
                } catch (Exception e) {
                        this.colors.add(color);
                }
        }

}
