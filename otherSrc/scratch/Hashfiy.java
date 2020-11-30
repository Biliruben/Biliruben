package scratch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.biliruben.util.csv.CSVRecord;
import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;
import com.biliruben.util.csv.CSVUtil;

public class Hashfiy {

    public static void main(String[] args) throws IOException {
        String newField = "employeeId";
        String fieldToHash = "DN";
        File file = new File (args[0]);
        CSVSource csv = new CSVSourceImpl(file, CSVType.WithHeader);
        String[] fields = csv.getFields();
        String[] newFields = Arrays.copyOf(fields, fields.length + 1);
        newFields[fields.length] = newField;
        CSVRecord csvRecord = new CSVRecord(newFields);
        for (Map<String, String> line : csv) {
            String tohash = line.get(fieldToHash);
            long longhash = tohash.hashCode();
            long hashCode = Math.abs(longhash);
            // prepend the 'z' to avoid numeric coercion in Excel
            line.put(newField,  "z" + hashCode);
            csvRecord.addLine(line);
        }

        String filePath = file.getParent();
        String fileName = filePath + File.separator + "new" + file.getName();
        System.out.println("Printing to " + fileName);
        FileOutputStream out = new FileOutputStream(new File(fileName));
        CSVUtil.exportToCsv(csvRecord, out);
    }

}
