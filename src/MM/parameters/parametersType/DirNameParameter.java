/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MM.parameters.parametersType;

import MM.parameters.UserParameter;
import java.io.File;
import java.util.Collection;
import org.w3c.dom.Element;

/**
 *
 * @author scsandra
 */
public class DirNameParameter implements UserParameter<File, DirNameComponent> {

        private String name, description;
        private File value;
        private String extension;

        public DirNameParameter(String name, String description) {
                this(name, description, null);
        }

        public DirNameParameter(String name, String description, String extension) {
                this.name = name;
                this.description = description;
                this.extension = extension;
        }

        @Override
        public String getName() {
                return name;
        }

        @Override
        public String getDescription() {
                return description;
        }

        @Override
        public DirNameComponent createEditingComponent() {
                return new DirNameComponent(false);
        }

        @Override
        public File getValue() {
                return value;
        }

        @Override
        public void setValue(File value) {
                this.value = value;
        }

        @Override
        public DirNameParameter clone() {
                DirNameParameter copy = new DirNameParameter(name, description);
                copy.setValue(this.getValue());
                return copy;
        }

        @Override
        public void setValueFromComponent(DirNameComponent component) {
                File compValue = component.getValue();
                if (extension != null) {
                        if (!compValue.getName().endsWith(extension)) {
                                compValue = new File(compValue.getPath() + "." + extension);
                        }
                }
                this.value = compValue;
        }

        @Override
        public void setValueToComponent(DirNameComponent component, File newValue) {
                component.setValue(newValue);
        }

        @Override
        public void loadValueFromXML(Element xmlElement) {
                String fileString = xmlElement.getTextContent();
                if (fileString.length() == 0) {
                        return;
                }
                this.value = new File(fileString);
        }

        @Override
        public void saveValueToXML(Element xmlElement) {
                if (value == null) {
                        return;
                }
                xmlElement.setTextContent(value.getPath());
        }

        @Override
        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
                        errorMessages.add(name + " is not set");
                        return false;
                }
                return true;
        }       
}
