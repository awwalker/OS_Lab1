import java.io.File;
import java.io.IOException;
import java.util.*;

public class Linker {
    // Define the machine word size as 600.
    private static final int globalMachineSize = 600;

    private static HashMap<String, Integer> symbolTable;
    private static ArrayList<memoryMapEntry> memoryMap;
    private static int relativeAddress;
    private static ArrayList<String> needToUseList;

    public static void main(String[] args) {
        // Get the file to read and create an output file.
        try {
            File read = new File(args[0]);
            // Read from the file.
            Scanner sc = new Scanner(read);

            // Create all the modules from this input.
            ArrayList<Module> modules = createModules(sc);
            // Define a new ArrayList storing the symbols that need to be used.
            needToUseList = new ArrayList<>();

            // Create the symbol table from the newly created modules.
            symbolTable = createSymbolTable(modules);

            // Save the symbols that need to be used.
            for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
                needToUseList.add(entry.getKey());
            }

            // Create the memory map using the symbol table and modules.
            memoryMap = createMemoryMap(modules);

            // Print out the symbol table.
            System.out.println("Symbol Table:");
            for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
                System.out.println(entry.getKey() + ", " + entry.getValue());
            }
            // Print out the memory map.
            System.out.println("Memory Map:");
            int index = 0;
            for (memoryMapEntry memEntry : memoryMap) {
                System.out.printf("%s: %s %s\n", index, memEntry.getKey(), memEntry.getValue());
                index++;
            }

            // Print any warnings regarding unused symbols.
            if (!needToUseList.isEmpty()) {
                for (String symbol : needToUseList) {
                    System.out.printf("Warning: %s defined but never used.\n", symbol);
                }
            }

            // Proactively close scanner object.
            sc.close();
        } catch (ArrayIndexOutOfBoundsException e) { // Catch no input file given.
            System.out.println("Error: No input file detected");
        } catch (IOException e) { // Catch input file unable to be read.
            System.out.println("Error: No Input File Detected: java.io.FileNotFoundException");
        }
    }

    /* createMemoryMap - uses the given modules to create the linker's memory map. */
    private static ArrayList<memoryMapEntry> createMemoryMap(ArrayList<Module> modules) {
        // This will be the second and final pass through the data.
        ArrayList<memoryMapEntry> memoryMap = new ArrayList<>();
        for (Module module : modules) {
            // Retrieve the current module's use list.
            List<String> useList = module.getUseList();
            // Retrieve the current module's program text.
            List<String> programText = module.getProgramText();
            // Iterate through each text in the modules program text.
            for (String text : programText) {
                // Want to work with ints not strings.
                int wholeAddr = Integer.parseInt(text);
                // Save the lastDigit.
                int lastDigit = wholeAddr % 10;
                // Save the baseAddr.
                int baseAddr = (wholeAddr / 10000) * 1000;
                // Save the text minus the last digit.
                int newAddr = wholeAddr / 10;
                // Save the middle part of the text.
                int absoluteAddr = newAddr % 1000;


                String err = "";

                switch (lastDigit) {
                    // If the lastDigit was 1 or 2 nothing needs to be done.
                    case 1:
                        break;
                    case 2:
                        // Error check for oversized absoluteAddr.
                        if (absoluteAddr > globalMachineSize) {
                            // Send back the base address.
                            newAddr = baseAddr;
                            // Error for when absolute address exceeds machine size.
                            err = "Error: Absolute address exceeds machine size; 0 used.";
                        }
                        break;
                    case 3:
                        // Update the absoluteAddr
                        newAddr = baseAddr + absoluteAddr + module.getBaseAddress();
                        if (absoluteAddr > module.getBaseAddress() + module.getSize()) {
                           newAddr = baseAddr;
                            err = "Error: Relative address exceeds the size of the module; 0 used.";
                        }
                        break;
                    case 4:
                        // Retrieve the symbol being discussed.
                        // Error check for address out of scope of use list.
                        if (absoluteAddr > useList.size()) {
                            err = "Error: External address exceeds length of use list; treated as " +
                                    "immediate";
                            break;
                        }
                        newAddr = baseAddr;
                        String symbol = useList.get(absoluteAddr);
                        if (!symbolTable.containsKey(symbol)) {
                            err = "Error: " + symbol + " is not defined: 0 used.";
                            break;
                        }
                        // Have used the symbol remove it from the needToUseList.
                        needToUseList.remove(symbol);
                        newAddr += symbolTable.get(symbol);
                        break;
                    default:
                        break;
                }
                // Store the new entry in the map.
                memoryMap.add(new memoryMapEntry(newAddr, err));
            }
        }
        return memoryMap;
    }

    /* createSymbolTable - uses the given modules to create the linker's symbol table */
    private static HashMap<String, Integer> createSymbolTable(ArrayList<Module> modules) {
        HashMap<String, Integer> symbolTable = new HashMap<>();
        for (Module module : modules) {
            HashMap<String, Integer> definitionList = module.getDefinitionList();
            for (Map.Entry<String, Integer> entry : definitionList.entrySet()) {
                if (!symbolTable.containsKey(entry.getKey())) {
                    symbolTable.put(entry.getKey(), entry.getValue());
                    continue;
                }
                // Error if symbol is defined multiple times across modules.
                System.out.printf("Error: %s defined multiple times, using %s\n", entry.getKey(), symbolTable.get
                        (entry.getKey()));
            }
        }
        return symbolTable;
    }

    /* createModules - uses the given file and the known number of modules to create all modules */
    private static ArrayList<Module> createModules(Scanner sc) {
        // Get just the number of modules.
        int numModules = sc.nextInt();
        // Create all the modules.
        ArrayList<Module> modules = new ArrayList<>();

        // Use method createModules.
        for (int i = 0; i < numModules; i++) {
            ArrayList<String> modDefinitionList = new ArrayList<>();
            ArrayList<String> modUseList = new ArrayList<>();
            ArrayList<String> modProgramText = new ArrayList<>();
            // Iterate through and add the correct parts to each ArrayList.
            // Gather the number of definitions.
            int numDefinitions = Integer.parseInt(sc.next());
            modDefinitionList.add(Integer.toString(numDefinitions));
            // Find all the definitions in this current module.
            for (int j = 0; j < numDefinitions; j++) {
                // Append each 'Key' and 'Value' to the definitions ArrayList.
                modDefinitionList.add(sc.next()); // Add the Key.
                modDefinitionList.add(sc.next()); // Add the Value.
            }
            // Gather the number of uses.
            int numUses = Integer.parseInt(sc.next());
            // Find all the uses in this current module.
            for (int j = 0; j < numUses; j++) {
                modUseList.add(sc.next()); // Add the use.
            }
            String lenProgramTextStr = sc.next();
            int lenProgramText = Integer.parseInt(lenProgramTextStr);
            modProgramText.add(lenProgramTextStr);
            // Find all the Program text in this module.
            for (int j = 0; j < lenProgramText; j++) {
                modProgramText.add(sc.next());
            }
            // Create the new module.
            Module newModule = new Module(modDefinitionList, modUseList, modProgramText);
            newModule.setBaseAddress(relativeAddress);
            newModule.setAbsoluteAddresses();
            // Save the new module.
            modules.add(newModule);
            // Update the relative address by the module size.
            relativeAddress += newModule.getSize();
        }
        return modules;
    }

    /* memoryMapEntry - defines an entry in the memory map an integer key and any error value */
    private static class memoryMapEntry {
        private Integer key; // The value of the memory map entry.
        private String value; // The error, if any of the map entry.

        /* Create a new memoryMapEntry */
        memoryMapEntry(
                Integer key,
                String value
        ) {
            this.key = key;
            this.value = value;
        }

        /* getKey - returns the integer key of this memoryMapEntry */
        Integer getKey() {
            return key;
        }

        /* getValue - returns the string error of this memoryMapEntry if one exists */
        String getValue() {
            return value;
        }
    }
}

/*
                Read #mods
                  read #defs
                     for d from 1 to #defs
                         read def[d]
                         handle def[d]

                  read #uses
                     for u from 1 to #uses
                         read use[u]
                         handle use[u]

                  read #text
                     for t from 1 to #text
                         read text[t]
                         handle text[t]
*/


