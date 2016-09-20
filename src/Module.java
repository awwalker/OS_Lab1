import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aaronwalker on 9/12/16.
 */
class Module {
    // The three parts of a module.
    private HashMap<String, Integer> definitionList; // The definition list.
    private List<String> useList; // The use list.
    private List<String> programText; // The program text.
    private int baseAddress;
    private int size;

    /* Create a new Module */
    Module(
            ArrayList<String> definitionListText,
            ArrayList<String> useListString,
            ArrayList<String> programText
    ) {
        this.definitionList = createDefinitionList(definitionListText);
        this.useList = useListString;
        this.programText = programText.subList(1, programText.size()); // Only store the actual texts not the module
        // size.
        // Set the size of the module equal to the length of the program text.
        this.size = Integer.parseInt(programText.get(0));
    }

    /* getProgramText - return the program text of the current module. */
    List<String> getProgramText() {
        return programText;
    }

    /*  getBaseAddress - return the baseAddress of the current module. */
    int getBaseAddress() {
        return baseAddress;
    }

    /* createDefinitionList - uses a subset of the passed file to create the current module's definition list. */
    private HashMap<String, Integer> createDefinitionList(ArrayList<String> definitionListText) {
        HashMap<String, Integer> definitionList = new HashMap<>();
        // Find the number of definitions.
        int numDefinitions = Integer.parseInt(definitionListText.get(0));
        for (int i = 1; i <= numDefinitions * 2; i += 2) { // Iterate through the definitionListText of K,V pairs.
            // Get the variable name.
            String newVar = definitionListText.get(i);
            // Get the value of the variable.
            int newValue = Integer.parseInt(definitionListText.get(i + 1));
            // Add the new symbol to the map.
            definitionList.put(newVar, newValue);
        }
        return definitionList;
    }

    /* getSize - returns the size of the current module. */
    int getSize() {
        return size;
    }

    /* toString - Overrides the generic toString method in order to print
    * the defintions, uses, program text, and base address of a module.
    */
    @Override
    public String toString() {
        StringBuilder modString = new StringBuilder();
        modString.append("Definitions: \n");
        for (Map.Entry<String, Integer> entry : definitionList.entrySet()) {
            modString.append(entry.getKey());
            modString.append(",");
            modString.append(entry.getValue());
            modString.append("\n");
        }
        modString.append("\n Use List: \n");
        for (String s : useList) {
            modString.append(s);
            modString.append("\n");
        }
        modString.append("\n Program Text: \n");
        for (String s : programText) {
            modString.append(s);
            modString.append("\n");
        }
        modString.append("\n The Base Address: ");
        modString.append(baseAddress);

        return modString.toString();
    }

    /* setBaseAddress - sets the modules base address */
    void setBaseAddress(int baseAddr) {
        baseAddress = baseAddr;
    }

    /* getDefinitionList - returns the definition list of the current module. */
    HashMap<String, Integer> getDefinitionList() {
        return definitionList;
    }


    /* getUseList - returns the use list of the current module. */
    List<String> getUseList() {
        return useList;
    }

    /* setAbsoluteAddress - uses the modules base address and the relative
    * value of a variable in the modules definition list to update its
    * address.
    */
    void setAbsoluteAddresses() {
        // Need the baseAddress to resolve the external references.
        int baseAddr = baseAddress;
        for (Map.Entry<String, Integer> entry : definitionList.entrySet()) {
            // Update the current value by adding the size of the past modules.
            entry.setValue(entry.getValue() + baseAddr);
        }
    }
}

