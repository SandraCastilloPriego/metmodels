/*
 * Copyright 2007-2012 
 * 
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
package MM.parameters.parametersType;

import MM.main.MMCore;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 */
public class FileNameComponent extends JPanel implements ActionListener {

        public static final int TEXTFIELD_COLUMNS = 15;
        public static final Font smallFont = new Font("SansSerif", Font.PLAIN, 10);
        private JTextField txtFilename;
        private boolean multiSelection = false;
        private String path;

        public FileNameComponent(boolean MultiSelection) {

                this.multiSelection = MultiSelection;
                txtFilename = new JTextField();
                txtFilename.setColumns(TEXTFIELD_COLUMNS);
                txtFilename.setFont(smallFont);
                add(txtFilename);

                JButton btnFileBrowser = new JButton("...");
                btnFileBrowser.addActionListener(this);
                add(btnFileBrowser);

        }

        public File getValue() {
                String fileName = txtFilename.getText();
                File file = new File(fileName);
                return file;
        }

        public File[] getValues() {
                String fileName = txtFilename.getText();
                String[] fileNames = fileName.split(";");
                File[] files = new File[fileNames.length];
                for (int i = 0; i < files.length; i++) {
                        files[i] = new File(fileNames[i]);
                }

                return files;
        }

        public void setValue(File value) {
                txtFilename.setText(value.getPath());
        }

        public void setPath(String path) {
                this.path = path;
        }

        public void setValues(File[] value) {
                String files = "";
                for (File file : value) {
                        files += file + ";";
                }
                txtFilename.setText(files);
        }

        public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(this.multiSelection);


                String[] currentPath = txtFilename.getText().split(";");
                if (path != null && path.length() > 0) {
                        fileChooser.setCurrentDirectory(new File(path));
                        currentPath[0] = path;
                }

                if (currentPath[0].length() > 0) {
                        File currentFile = new File(currentPath[0]);
                        File currentDir = currentFile.getParentFile();
                        if (currentDir != null && currentDir.exists()) {
                                fileChooser.setCurrentDirectory(currentDir);
                        }
                }

                int returnVal = fileChooser.showDialog(MMCore.getDesktop().getMainFrame(), "Select file");

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                        if (this.multiSelection) {
                                File[] files = fileChooser.getSelectedFiles();
                                String text = "";
                                for (File file : files) {
                                        text += file.getAbsolutePath() + ";";
                                }
                                txtFilename.setText(text);
                        } else {
                                String selectedPath = fileChooser.getSelectedFile().getPath();
                                txtFilename.setText(selectedPath);
                        }
                }
        }
}
