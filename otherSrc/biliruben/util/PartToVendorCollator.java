package biliruben.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import biliruben.html.BrickLinkScanner;
import biliruben.html.FileBrickLinkScanner;
import biliruben.threads.ThreadRunner;
import biliruben.threads.ThreadRunner.TRRunnable;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.csv.CSVRecord;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;
import com.biliruben.util.csv.CSVUtil;

/**
 * Given a table indicating part numbers and vendors who sell that part, collate the
 * data into the fewest groups possible by vendor
 * @author trey.kirk
 *
 */
public class PartToVendorCollator {

    // set to Integer.Min to defer exclusively to avg threshold
    //private static final double PRICE_TOTAL_TOLERANCE = Integer.MIN_VALUE;
    private static final double PRICE_TOTAL_TOLERANCE = 2.0;
    private static final double PRICE_AVG_THRESHOLD = 1;
    private static final String OPT_IGNORE_ON_HAND = "ignoreOnHand";
    private static final String OPT_VENDOR_DATA = "vendorData";
    private static final String OPT_PARTS_LIST = "partsList";
    private static final String OPT_SELECT_FOR = "selectFor";
    private static final String OPT_BREAKDOWN_DIR = "breakDownDir";
    private static final String OPT_CONSIDER_VENDORS = "considerVendors";
    private static final String OPT_BRICK_LINK_JSON = "brickLinkJson";
    private static final String OPT_AVG_THRESHOLD = "avgThreshold";
    private static final String OPT_PRICE_TOLERANCE = "priceTolerance";
    private static final String OPT_AVOID_VENDOR_FILE = "avoidVendors";
    private static final String OPT_SELECT_FIRST = "selectFirst";
    private static GetOpts _opts;
    private static String _partsList;
    private static Set<String> _requiredPartNumbers;
    private static Map<String, Set<String>> _vendors;
    private static Map<String, Set<String>> _parts;
    private static Map<String, Vendor> _allVendors;
    private static List<Set<String>> _selectedVendors;
    private static Boolean _ignoreOnHand;
    private static Integer _selectFor;
    private static List<String> _consideredVendors;
    private static long _iteration;
    private static int _report;
    private static Double _priceAverageThreshold;
    private static Double _priceTotalTolerance;
    private static int _selectedVendorTargetSize;
    private static HashMap<String, Double> _priceAverages;
    private static String _brickLinkJson;
    private static String _vendorAvoidFile;
    private static Monitor _monitor;
    private static Boolean _selectFirst;
    
    private static class Part {
        private String _part;
        private double _price;
        private String _description;
        private int _qty;
        
        Part(String part, double price, String description, int qty) {
            this._part = part;
            this._price = price;
            this._description = description;
            this._qty = qty;
        }
        
        int getQty() {
            return _qty;
        }
        
        double getPrice() {
            return _price;
        }
        
        String getPartNumber() {
            return _part;
        }
        
        String getDescription() {
            return _description;
        }
        
        @Override
        public String toString() {
            return this._part;
        }
    }
    
    private static class Monitor implements Runnable {

        private boolean _isRunning;
        private long _reportIncrement;
        private Map<Integer, Set<String>> _depthMap;

        Monitor(long increment) {
            _reportIncrement = increment;
            _depthMap = new HashMap<Integer, Set<String>>();
        }

        @Override
        public void run() {
            _isRunning = true;
            while (_isRunning) {
                try {
                    Thread.sleep(_reportIncrement);
                    report();
                } catch (InterruptedException e) { 
                    // FOAD 
                }
            }
        }
        
        public void update(int depth, Set<String> vendors) {
            vendors = vendors == null ? new HashSet<String>() : vendors;
            _depthMap.put(depth, new HashSet<String>(vendors));
        }

        private void report() {
            StringBuffer buff = new StringBuffer();
            buff.append("Iterations: " + PartToVendorCollator._iteration).append("\n");
            buff.append("Depth Map: ");
            for (Integer depthKey : _depthMap.keySet()) {
                buff.append(depthKey).append("=").append(_depthMap.get(depthKey)).append("\n");
            }
            System.out.println(buff.toString());
        }
        
    }
    
    private static class Vendor {
        private Map<String, Part> _partsMap;
        private String _vendorName;
        
        Vendor(String vendor) {
            this._partsMap = new HashMap<String, Part>();
            this._vendorName = vendor;
        }
        
        void addPart(Part part) {
            this._partsMap.put(part.getPartNumber(), part);
        }
        
        void addParts(Collection<Part> parts) {
            for (Part part : parts) {
                addPart(part);
            }
        }
        
        Part getPart(String partNumber) {
            return this._partsMap.get(partNumber);
        }
        
        Map<String, Part> getParts() {
            return this._partsMap;
        }
        
        Set<String> getPartsList() {
            return this._partsMap.keySet();
        }
        
        String getVendorName() {
            return this._vendorName;
        }
        
        @Override
        public String toString() {
            return this._vendorName;
        }

        public void removePart(Part toRemove) {
            _partsMap.remove(toRemove.getPartNumber());
        }
    }


    /**
     * @param args
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        _monitor = new Monitor(30000);
        Thread t = new Thread(_monitor);
        _iteration = 0;
        _report = 50000;
        init(args);
        // 1. Collect the data. Create a list of parts, eliminating unneeded ones 
        rollupData();
        t.start();
        // 2. Select the vendors by part rarity - roll up the data by part and select
        //     the part with the fewest vendors. For each vendor, pop their offered parts
        //     from the list of required parts and recurs until there are no parts left.
        selectByPart();
        _monitor._isRunning = false;
        t.interrupt();
    }
    
    private static void selectByPart() throws IOException, InterruptedException {
        Set<String> vendorSet = new HashSet<String>();
        Set<String> requiredPartsCopy = new HashSet<String>(_requiredPartNumbers);
        // first, find the parts nobody sells
        List<String> notSold = findRarestParts(requiredPartsCopy, 0);
        if (notSold != null) {
            System.out.println("No vendors found for parts: " + notSold);
            requiredPartsCopy.removeAll(notSold);
        }
        selectByPartInner(0, vendorSet, requiredPartsCopy);
        
        System.out.println("Vendors selected:");
        for (Set vendors : _selectedVendors) {
            System.out.println("(" + vendors.size() + ") " + vendors);
        }
        System.out.println("Parts not sold:");
        System.out.println(notSold);
        breakdownVendors();
    }
    
    private static String generateFileName(int cost, Set<String> forVendorNames) {
        StringBuilder buff = new StringBuilder();

        buff.append(forVendorNames.size()).append( " - $").append(cost).append(" - ");
        for (String vendorName : forVendorNames) {
            buff.append(vendorName.replaceAll("[\\s&,\\[\\]\\!\\*\\\\\\/]", "")).append("_");
        }
        buff.deleteCharAt(buff.length() - 1);
        if (buff.length() > 32) {
            buff.delete(32, buff.length());
            buff.append("_").append(System.currentTimeMillis() % 100000);
        }
        buff.append(".csv");
        return buff.toString();
    }
    
    private static void breakdownVendors() throws IOException {
        String breakDownDirectory = _opts.getStr(OPT_BREAKDOWN_DIR);
        File directory = new File(breakDownDirectory);
        if (!directory.isDirectory() || !directory.canWrite()) {
            throw new FileNotFoundException("Cannot write to directory: " + directory);
        }
        for (Set<String> selectedVendors : _selectedVendors) {
            System.out.println("Breaking down: " + selectedVendors);
            Set<String> remainingParts = new HashSet<String>(_requiredPartNumbers);
            Map<Vendor, List<Part>> vendorsParts = new HashMap<Vendor, List<Part>>();
            for (String selectedVendor : selectedVendors) {
                Vendor vendor = _allVendors.get(selectedVendor);
                // first report on the parts only this vendor sells
                // If we force-select a vendor, there's no garauntee it was in the search result
                if (vendor != null) {
                    Set<String> vendorPartsList = new HashSet<String>(vendor.getPartsList());
                    // Go through the other vendors' list and remove their part numbers from this set
                    for (String otherVendor : selectedVendors) {
                        if (!otherVendor.equals(vendor.getVendorName())) {
                            Vendor otherVendorObj = _allVendors.get(otherVendor);
                            if (otherVendorObj != null && vendorPartsList != null) {
                                vendorPartsList.removeAll(otherVendorObj.getPartsList());
                            }
                        }
                    }
                    //System.out.println("Unique parts for " + vendor + ": " + vendorPartsList);

                    // the remaining set are the only parts this vendors sells
                    List<Part> vendorRemainingParts = new ArrayList<Part>();
                    for (String remainingPart : vendorPartsList) {
                        vendorRemainingParts.add(vendor.getPart(remainingPart));
                    }
                    vendorsParts.put(vendor, vendorRemainingParts);
                    remainingParts.removeAll(vendorPartsList);
                }
            }
            for (String remainingPart : remainingParts) {
                Vendor cheapestVendor = findCheapestVendor(remainingPart, vendorsParts.keySet());
                if (cheapestVendor == null) {
                    // part that's not sold, skip it
                    continue;
                }
                Part cheapestPart = cheapestVendor.getPart(remainingPart);
                //System.out.println("Cheapest vendor part: " + cheapestPart + " - " + cheapestVendor);
                // add the cheapestPart to the vendor to part map
                List<Part> partsList = vendorsParts.get(cheapestVendor);
                if (partsList == null) {
                    partsList = new ArrayList<Part>();
                    vendorsParts.put(cheapestVendor, partsList);
                }
                partsList.add(cheapestPart);
            }
            CSVRecord record = new CSVRecord();
            double total = 0.0;
            for (Vendor vendor : vendorsParts.keySet()) {
                for (Part part : vendorsParts.get(vendor)) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("Vendor", vendor.getVendorName());
                    data.put("Part", part.getPartNumber());
                    data.put("Description", part.getDescription());
                    data.put("Price", "" + part.getPrice());
                    data.put("Qty", "" + part.getQty());
                    data.put("Average", "" + _priceAverages.get(part.getPartNumber()));
                    total += part.getPrice() * part.getQty();
                    record.addLine(data);
                }
            }
            record.setFields(new String[]{"Part", "Qty", "Vendor", "Description", "Price", "Average"});
            String fileName = generateFileName((int)total, selectedVendors);
            File targetCsv = new File(directory.getPath() + File.separator + fileName);
            CSVUtil.exportToCsv(record, new FileOutputStream(targetCsv));
            System.out.println(targetCsv + " : $" + total);
        }
    }
    
    private static Vendor findCheapestVendor(String forPart, Set<Vendor> fromVendors) {
        double lowestPrice = -1;
        Vendor lowestVendor = null;
        for (Vendor vendor : fromVendors) {
            Part part = vendor.getPart(forPart);
            if (part != null) {
                if (lowestPrice == -1 || part.getPrice() < lowestPrice){
                    lowestVendor = vendor;
                    lowestPrice = part.getPrice();
                }
            }
        }
        return lowestVendor;
    }

    private static void selectByPartInner(int depth, Set<String> vendorList, Set<String> requiredParts) throws IOException {
        depth++;
        _iteration++;
        if (vendorList.size() >= _selectedVendorTargetSize || !Util.isEmpty(_selectedVendors) && _selectFirst) {
            // already too many, go on
            return;
        }
        _monitor.update(depth, vendorList);
        
        List<String> rarestParts = findRarestParts(requiredParts, Integer.MAX_VALUE);
        // no need to iterate over the list. The other(s) will bubble up in recursive calls
        String rarePart = rarestParts.get(0);
        Set<String> rarePartVendors = _parts.get(rarePart);
        for (String rarePartVendor : rarePartVendors) {
            if (Util.isEmpty(rarePartVendor)) {
                System.err.println("WTF rare part vendors! part: " + rarePart + ": " + rarePartVendors);
            }
            // We're a recursion method, so make defensive copies
            Set<String> requiredPartsCopy = new HashSet<String>(requiredParts);
            Set<String> allVendorsParts = _vendors.get(rarePartVendor);
            vendorList.add(rarePartVendor);
            requiredPartsCopy.removeAll(allVendorsParts);
            if (requiredPartsCopy.isEmpty()) {
                select(vendorList);
            } else {
                selectByPartInner(depth, vendorList, requiredPartsCopy);
            }
            // We've returned and we're going to try with a different vendor, so
            // pop this jackass out of our set
            vendorList.remove(rarePartVendor);
        }
    }
    
    private static void select(Set<String> selectedVendors) throws IOException {
        Set<String> selectedVendorCopy = new HashSet<String>(selectedVendors);
                // If the selectedVendors set is too large, ignore them
                // we'll need to copy the Set because the caller will modify it afterwards
                // Now that we have a vendor list, append the requested vendors to be included in the breakdown
                if (!Util.isEmpty(_consideredVendors)) {
                    selectedVendorCopy.addAll(_consideredVendors);
                }
                if (_selectedVendors == null) {
                    _selectedVendors = new ArrayList<Set<String>>();
                    _selectedVendors.add(selectedVendorCopy);
                } else {
                    // look at the first in the list. If we're smaller, NEW LIST!
                    Set<String> preSelected = _selectedVendors.get(0);
                    int targetSize = preSelected.size() < _selectFor ? _selectFor : preSelected.size();
                    if (selectedVendorCopy.size() < targetSize && targetSize > _selectFor) {
                         // the preSelected set is neither the smallest nor has it met our selectFor threshold
                        // refresh the list
                        System.out.println("Selecting: " + selectedVendorCopy.size());
                        _selectedVendorTargetSize = selectedVendorCopy.size();
                         _selectedVendors = new ArrayList<Set<String>>();
                    } 
                    if (selectedVendorCopy.size() <= targetSize) {
                        // if the selectedVendorCopy is a subset of an existing group, pop out the existing group
                        List<Set<String>> toRemove = new ArrayList<Set<String>>();
                        for (Set<String> testVendors : _selectedVendors) {
                            Set<String> testVendorsCopy = new HashSet<String>(testVendors);
                            testVendorsCopy.retainAll(selectedVendorCopy);
                            if (selectedVendorCopy.equals(testVendorsCopy)) {
                                toRemove.add(testVendors);
                            }
                        }
                        for (Set<String> popIt : toRemove) {
                            _selectedVendors.remove(popIt);
                        }
                        _selectedVendors.add(selectedVendorCopy);
                    }
                }
    }
    
    private static List<String> findRarestParts(Set<String> requiredParts, int minimumThreshold) {
        // Before we find the rarest part vendors, go through the list of _consideredParts (those manually
        // chosen to always be considered). For any that are below the minimumThreshold and still in the 
        // required list, add their vendors as well

        int totalVendors = -1;
        List<String> rarestParts = new ArrayList<String>();
        for (String part : requiredParts) {
            Set<String> vendors = _parts.get(part);
            if (vendors == null) {
                // need at least an empty
                vendors = new HashSet<String>();
            }
            if (vendors.size() <= minimumThreshold) {
                // Parts who nobody sells are not what I'm interested in
                if (totalVendors == -1 || (vendors.size() < totalVendors)) {
                    // New (or first) lowest total, start over
                    totalVendors = vendors.size();
                    rarestParts = new ArrayList<String>();
                }

                if (totalVendors == vendors.size()) {
                    // a part that has the current fewest vendors
                    rarestParts.add(part);
                }
            }
        }
        return rarestParts;
    }
    
    private static void rollupData() throws IOException {
        BrickLinkScanner scanner = null;
        if (Util.isEmpty(_brickLinkJson)) {
            scanner = new BrickLinkScanner(_partsList, "Part", "Quantity");
        } else {
            scanner = new FileBrickLinkScanner(_brickLinkJson, _partsList, "Part", "Quantity");
        }
        String rpp = _opts.getStr(BrickLinkScanner.PARAM_RPP);
        if (!Util.isEmpty(rpp)) {
            scanner.getParamMap().put(BrickLinkScanner.PARAM_RPP, rpp);
        }
        
        File partsList = new File(_partsList);
        if (!partsList.exists()) {
            throw new IOException("File not found: " + _partsList);
        }
        
        CSVSource partsListCsv = new CSVSourceImpl(partsList, CSVType.WithHeader);
        _requiredPartNumbers = new HashSet<String>();
        Map<String, String> qtyList = new HashMap<String, String>();
        for (Map<String, String> line : partsListCsv) {
            String orderNumber = line.get("Order Number");
            String qtyRequired = line.get("Quantity");
            String partNumber = line.get("Part");
            if (_ignoreOnHand || Util.isEmpty(orderNumber)) {
                qtyList.put(partNumber, qtyRequired);
                _requiredPartNumbers.add(partNumber);
            }
        }
        
        List<String> avoidedVendors = readAvoidVendorsFile();
        
        Map<String, List<Map>> allPartsData = scanner.getAllPartsData();

        _vendors = new HashMap<String, Set<String>>();
        _allVendors = new HashMap<String, Vendor>();
        for (String part : allPartsData.keySet()) {
            for (Map<String, String> line : allPartsData.get(part)) {
                line.put("part", part);
                line.put("qty", qtyList.get(line.get("part")));
                Part partObj = inflatePart(line, scanner);
                Vendor vendor = inflateVendor(line, avoidedVendors);
                if (vendor != null && !Util.isEmpty(vendor.getVendorName())) {
                    _allVendors.put(vendor.getVendorName(), vendor);
                    vendor.addPart(partObj);
                }
            }
        }
        
        _parts = new HashMap<String, Set<String>>();
        // Finally, roll up part to vendor
        for (String vendorName : _allVendors.keySet()) {
            Vendor vendor = _allVendors.get(vendorName);
            for (String requiredPart : _requiredPartNumbers) {
                Part part = vendor.getPart(requiredPart);
                if (part != null) {
                    Set<String> vendors = _parts.get(part.getPartNumber());
                    if (vendors == null) {
                        vendors = new TreeSet<String>();
                        _parts.put(part.getPartNumber(), vendors);
                    }
                    vendors.add(vendorName);
                    Set<String> parts = _vendors.get(vendorName);
                    if (parts == null) {
                        parts = new TreeSet<String>();
                        _vendors.put(vendorName, parts);
                    }
                    parts.add(part.getPartNumber());
                }
            }
        }

        
        
        
        // Now, go through the parts to vendor's map and eliminate vendors who charge too much. 
        _priceAverages = new HashMap<String, Double>();
        for (String partNumber : _parts.keySet()) {
            Set<String> vendorNames = _parts.get(partNumber);
            int totalParts = 0;
            double totalPrice = 0.0;
            for (String vendorName : vendorNames) {
                Vendor vendor = _allVendors.get(vendorName);
                Part vendorsPart = vendor.getPart(partNumber);
                if (vendorsPart != null) {
                    totalParts++;
                    totalPrice += vendorsPart.getPrice();
                }
            }
            _priceAverages.put(partNumber, totalPrice / totalParts);
        }
        System.out.println("Average prices:");
        for (String part : new TreeSet<String>(_priceAverages.keySet())) {
            System.out.println(part + " $" + _priceAverages.get(part));
        }
        
        for (String vendorName : _allVendors.keySet()) {
            Vendor vendor = _allVendors.get(vendorName);
            Set<Part> dontSell = new HashSet<Part>();
            for (String partNumber : vendor.getPartsList()) {
                if (_priceAverages.get(partNumber) != null) {
                    Part part = vendor.getPart(partNumber);
                    if (part != null && part.getPrice() > 0) {
                        if (part.getPrice() >= (_priceAverages.get(partNumber) * _priceAverageThreshold)) {
                            // We're thinking about skipping this part from this vendor because they charge
                            // more than we want to pay of the average price. But if the total price for their warez is below our
                            // tolerance, we'll go ahead and continue to consider it
                            double totalPrice = part.getPrice() * part.getQty();
                            double averageTotalPrice = _priceAverages.get(partNumber) * part.getQty();
                            if (totalPrice - averageTotalPrice > _priceTotalTolerance) {
                                dontSell.add(part);
                            }
                        }
                    }
                }
            }
            for (Part toRemove : dontSell) {
                System.out.println("Removing part " + toRemove + " ($" + toRemove.getPrice() + "/avg $" + _priceAverages.get(toRemove.getPartNumber()) + ") for vendor " + vendor);
                vendor.removePart(toRemove);
                Set<String> vendorIndex = _parts.get(toRemove.getPartNumber());
                if (vendorIndex != null) {
                    vendorIndex.remove(vendorName);
                }
                Set<String> partIndex = _vendors.get(vendorName);
                if (partIndex != null) {
                    partIndex.remove(toRemove.getPartNumber());
                }
            }
        }
        
        _selectedVendorTargetSize = _vendors.keySet().size();
        
    }
    
    private static List<String> readAvoidVendorsFile() throws IOException {
        List<String> avoidedVendors = new ArrayList<String>();
        if (!Util.isEmpty(_vendorAvoidFile)) {
            File f = new File(_vendorAvoidFile);
            FileReader r = new FileReader(f);
            BufferedReader br = new BufferedReader(r);
            String vendor = br.readLine();
            while (vendor != null) {
                avoidedVendors.add(vendor);
                vendor = br.readLine();
            }
        }
        return avoidedVendors;
        
    }

    private static Vendor inflateVendor(Map<String, String> data, List<String> avoidedVendors) {
        String vendorName = data.get("strStorename");
        if (vendorName == null || avoidedVendors.contains(vendorName)) {
            return null;
        }
        Vendor vendor = _allVendors.get(vendorName);
        if (vendor == null) {
            vendor = new Vendor(vendorName);
            _allVendors.put(vendorName, vendor);
        }
        return vendor;
    }
    
    private static Part inflatePart(Map<String, String> data, BrickLinkScanner scanner) throws IOException {
        String partNumber = data.get("part");
        String priceStr = data.get("mDisplaySalePrice");
        Double price = 0.0;
        try {
            if (!Util.isEmpty(priceStr)) {
                price = Double.valueOf(priceStr);
            } else {
                System.err.println("Price could not be determined for part " + partNumber + ": " + priceStr);
            }
        } catch (NumberFormatException e) {
            System.err.println("Price could not be determined for part " + partNumber + ": " + priceStr);
        }
        String description = scanner.fetchDescription(partNumber);
        int qty = data.get("qty") != null ? Integer.valueOf(data.get("qty")) : 0;
        Part part = new Part(partNumber, price, description, qty);
        return part;
    }
    
    private static void init(String[] args) throws IOException {
        _opts = new GetOpts(PartToVendorCollator.class);
        _opts.addLegend(BrickLinkScanner.getOpts().getLegend("rpp"));
        
        OptionLegend legend = new OptionLegend(OPT_PARTS_LIST);
        legend.setRequired(true);
        legend.setDescription("CSV indicating parts currently on-hand");
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_VENDOR_DATA);
        legend.setRequired(true);
        legend.setDescription("CSV with vendor selling parts data");
        //_opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_IGNORE_ON_HAND);
        legend.setFlag(true);
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_SELECT_FOR);
        legend.setDescription("Select all vendor groups of this size or less. Otherwise only the smallest is selected");
        legend.setRequired(false);
        //_opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_BREAKDOWN_DIR);
        legend.setDescription("Breakdown directory");
        legend.setRequired(true);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_CONSIDER_VENDORS);
        legend.setDescription("Vendors to consider all parts for, in comma delimited format. All vendors specified will be included in the final reports");
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_BRICK_LINK_JSON);
        legend.setDescription("BrickLink JSON data to parse instead of fetching live results");
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_AVG_THRESHOLD);
        legend.setDescription("Threshold of the average to accept parts. This is a multiplier, so a value of '1' allows parts at or below average. '.8' only allows parts 80% of average");
        legend.setDefaultValue("" + PRICE_AVG_THRESHOLD);
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_PRICE_TOLERANCE);
        legend.setDescription("Tolerance of total price difference for parts not meeting the average threshold. A value of '1' allows parts that do not meet the average threshold, but have a total price of up to $1 over the average threshold total value to still be allowed");
        legend.setDefaultValue("" + PRICE_TOTAL_TOLERANCE);
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_AVOID_VENDOR_FILE);
        legend.setDescription("File of vendor names to avoid");
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_SELECT_FIRST);
        legend.setDescription("Stops after the first combination of vendors. Good for a quick breakdown of consideredVendors");
        legend.setFlag(true);
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        _opts.parseOpts(args);
        
        _selectFirst = Boolean.valueOf(_opts.getStr(OPT_SELECT_FIRST));
        _vendorAvoidFile = _opts.getStr(OPT_AVOID_VENDOR_FILE);
        _partsList = _opts.getStr(OPT_PARTS_LIST);
        _ignoreOnHand = Boolean.valueOf(_opts.getStr(OPT_IGNORE_ON_HAND));
        String selectForStr = _opts.getStr(OPT_SELECT_FOR);
        if (selectForStr == null || "".equals(selectForStr.trim())) {
            _selectFor = 0;
        } else {
            _selectFor = Integer.valueOf(selectForStr);
        }
        String consideredVendorsCsv = _opts.getStr(OPT_CONSIDER_VENDORS);
        if (!Util.isEmpty(consideredVendorsCsv)) {
            CSVSourceImpl csv = new CSVSourceImpl(consideredVendorsCsv, CSVType.WithOutHeader);
            String[] data = csv.getNextLine();
            _consideredVendors = Arrays.asList(data);
        }
        
        _brickLinkJson = _opts.getStr(OPT_BRICK_LINK_JSON);
        _priceAverageThreshold = Double.valueOf(_opts.getStr(OPT_AVG_THRESHOLD));
        _priceTotalTolerance = Double.valueOf(_opts.getStr(OPT_PRICE_TOLERANCE));
    }

}
