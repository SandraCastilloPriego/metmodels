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
package MM.modules.utils.get_pubchem_info;

import MM.data.Dataset;
import MM.taskcontrol.AbstractTask;
import MM.taskcontrol.TaskStatus;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.xml.writer.AttributeList;
import org.jfree.xml.writer.XMLWriter;
import org.sbml.jsbml.*;

/**
 *
 * @author scsandra
 */
public class GetPubchemInfoTask extends AbstractTask {

        private float progress = 0.0f;
        private String message = "Getting Info... ";

        public GetPubchemInfoTask() {           
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
                        
                       this.getAnswer(this.createXMLFile());
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        private String createXMLFile() throws FileNotFoundException, IOException {

                Writer w = new StringWriter();
                XMLWriter xmlW = new XMLWriter(w);
                xmlW.writeXmlDeclaration();
                xmlW.allowLineBreak();

                AttributeList attributes = new AttributeList();
                
                xmlW.startBlock();               
                xmlW.writeTag("PCT-Data", false);
                xmlW.startBlock();               
                xmlW.writeTag("PCT-Data_input", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-InputData", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-InputData_download", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-Download", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-Download_uids", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-QueryUids", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-QueryUids_ids", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-ID-List", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-QueryUids", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-ID-List_db", false);
                xmlW.writeText("pccompound");
                xmlW.writeText("</PCT-ID-List_db>");               
                xmlW.writeTag("PCT-ID-List_uids", false);
                xmlW.startBlock();    
                xmlW.writeTag("PCT-ID-List_uids_E", false);
                xmlW.writeText("2");
                xmlW.writeText("</PCT-ID-List_uids_E>");   
                xmlW.endBlock();
                xmlW.writeText("</PCT-ID-List_uids>");  
                xmlW.endBlock();
                xmlW.writeText("</PCT-ID-List>");   
                xmlW.endBlock();
                xmlW.writeText("</PCT-QueryUids_ids>");   
                xmlW.endBlock();
                xmlW.writeText("</PCT-QueryUids>");   
                xmlW.endBlock();
                xmlW.writeText("</PCT-Download_uids>");  
                xmlW.writeText("</PCT-Download_uids>");  
                attributes.setAttribute("value", "sdf");
                xmlW.writeTag("PCT-ID-List_uids_E",attributes,true);
                
                xmlW.close();

                return w.toString();
        }

        private void getAnswer(String xmlFile2Send) {
                try {
                        URL url = new URL("http://pubchem.ncbi.nlm.nih.gov/pc_fetch/pc_fetch.cgi");
                        URLConnection connection = url.openConnection();
                        HttpURLConnection httpConn = (HttpURLConnection) connection;

                        InputStream fin = new ByteArrayInputStream(xmlFile2Send.getBytes("UTF-8"));
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        // Copy the SOAP file to the open connection.
                        copy(fin, bout);
                        fin.close();
                        byte[] b = bout.toByteArray();
                        // Set the appropriate HTTP parameters.
                       // httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
                       // httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                      //  httpConn.setRequestProperty("SOAPAction", "http://pubchem.ncbi.nlm.nih.gov/search/search.cgi");
                        httpConn.setRequestMethod("POST");
                        httpConn.setDoOutput(true);
                        httpConn.setDoInput(true);
                        // Everything's set up; send the XML that was read in to b.
                        OutputStream out = httpConn.getOutputStream();
                        out.write(b);
                        out.close();
                        // Read the response and write it to standard out.
                        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
                        BufferedReader in = new BufferedReader(isr);
                        String inputLine;                       
                        while ((inputLine = in.readLine()) != null) {
                                System.out.println(inputLine);
                        }
                        in.close();
                        fin.close();
                        httpConn.disconnect();

                } catch (Exception ex) {
                        ex.printStackTrace();
                }

        }

        // copy method from From E.R. Harold's book "Java I/O"
        public static void copy(InputStream in, OutputStream out)
                throws IOException {

                // do not allow other threads to read from the
                // input or write to the output while copying is
                // taking place

                synchronized (in) {
                        synchronized (out) {

                                byte[] buffer = new byte[256];
                                while (true) {
                                        int bytesRead = in.read(buffer);
                                        if (bytesRead == -1) {
                                                break;
                                        }
                                        out.write(buffer, 0, bytesRead);
                                }
                        }
                }
        }

        
}
