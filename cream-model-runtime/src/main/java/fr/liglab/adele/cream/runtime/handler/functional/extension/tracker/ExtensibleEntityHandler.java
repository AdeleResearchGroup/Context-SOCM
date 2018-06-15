package fr.liglab.adele.cream.runtime.handler.functional.extension.tracker;

import java.util.List;

import org.apache.felix.ipojo.InstanceManager;

public interface ExtensibleEntityHandler {

    public void attachExtension(InstanceManager extension, List<String> specifications);

    public void detachExtension(InstanceManager extension, List<String> specifications);
    
    public void extensionStateChanged(InstanceManager extension, List<String> specifications, int extensionState);

}
