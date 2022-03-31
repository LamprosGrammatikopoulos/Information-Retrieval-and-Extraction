import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.ArrayList;

class Main {
    public static String attributes_list = "";

    public static void main(String[] args) {
        ArrayList arrayList = new ArrayList<String>();
        try {
            //InputStream in = new FileIO().getClass().getResourceAsStream(myObj);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("dataset.txt"), "UTF-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    String strToken = st.nextToken();
                    if (!isNumeric(strToken) && !arrayList.contains(strToken)) {
                        arrayList.add(strToken);
                    }
                }
            }

            for (int i=0; i<arrayList.size(); i++) {
                if (i==0) {
                    attributes_list = translate(arrayList.get(i).toString());
                }
                else if(i>0) {
                    attributes_list = attributes_list + ',' + translate(arrayList.get(i).toString());
                }
                System.out.println(arrayList.get(i));
            }
        }
        catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        createARFF();
    }

    public static String translate(String greekToken) {
        String englishToken = "";
        //English translation
        if (greekToken.equals("γύρο_χοιρινό")) {
            englishToken = "guroXoirino";
        }
        else if (greekToken.equals("κοτομπέικον")) {
            englishToken = "kotompeikon";
        }
        else if (greekToken.equals("χωριάτικη")) {
            englishToken = "xwriatikh";
        }
        else if (greekToken.equals("σαγανάκι")) {
            englishToken = "saganaki";
        }
        else if (greekToken.equals("ψωμί_λευκό")) {
            englishToken = "pswmiLeuko";
        }
        else if (greekToken.equals("αναψυκτικό")) {
            englishToken = "anapsuktiko";
        }
        else if (greekToken.equals("φρουτοσαλάτα")) {
            englishToken = "froutosalata";
        }
        else if (greekToken.equals("πατάτες")) {
            englishToken = "patates";
        }
        else if (greekToken.equals("παγωτό")) {
            englishToken = "pagwto";
        }
        else if (greekToken.equals("ψωμί_μαύρο")) {
            englishToken = "pswmiMauro";
        }
        else if (greekToken.equals("χυμός")) {
            englishToken = "xumos";
        }
        else if (greekToken.equals("καίσαρα")) {
            englishToken = "kaisara";
        }
        else if (greekToken.equals("αγγουροντομάτα")) {
            englishToken = "aggourontomata";
        }
        else if (greekToken.equals("γύρο_κοτόπουλο")) {
            englishToken = "guroKotopoulo";
        }
        else if (greekToken.equals("μαρούλι")) {
            englishToken = "marouli";
        }
        else if (greekToken.equals("τυροκροκέτες")) {
            englishToken = "turokroketes";
        }
        else if (greekToken.equals("ψωμί_πολύσπορο")) {
            englishToken = "pswmiPolusporo";
        }
        else if (greekToken.equals("μπύρα")) {
            englishToken = "mpura";
        }
        else if (greekToken.equals("μπιφτέκι_λαχανικών")) {
            englishToken = "mpiftekiLaxanikwn";
        }
        else if (greekToken.equals("γιαούρτι-μέλι")) {
            englishToken = "giaourtiMeli";
        }
        else if (greekToken.equals("νερό")) {
            englishToken = "nero";
        }
        else if (greekToken.equals("καλαμάρι")) {
            englishToken = "kalamari";
        }
        else if (greekToken.equals("μιλκσέικ")) {
            englishToken = "milkshake";
        }
        else if (greekToken.equals("κοτομπουκιές")) {
            englishToken = "kotompoukies";
        }
        return englishToken;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static void createARFF() {
        int max = findMaxOrder();
        try {
            FileWriter fWriter = new FileWriter("dataset.arff");

            //ARFF headers
            fWriter.write("@RELATION Sasmos\n");
            System.out.println("MAXX==> "+max);
            for (int i = 0; i < max; i++) {
                fWriter.write("@ATTRIBUTE class" + i + " {" + attributes_list + "}\n");
            }
            fWriter.write("\n@DATA\n");

            try {

                //Scanner myReader = new Scanner(myObj);
                ArrayList lineArray = new ArrayList<String>();
                String line = "";

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream("dataset.txt"), "UTF-8"));
                String readerLine = "";

                while ((readerLine = bufferedReader.readLine()) != null ) {
                    lineArray.clear();
                    StringTokenizer st = new StringTokenizer(readerLine, ",");

                    while (st.hasMoreTokens()) {
                        String strToken = st.nextToken();

                        if (!isNumeric(strToken) && !lineArray.contains(translate(strToken) + ",")) {
                            lineArray.add(translate(strToken) + ",");
                        }
                        if (max <= lineArray.size()) {
                            max = lineArray.size();
                        }
                    }
                    if (lineArray.size() == max) {
                        String tmp = lineArray.get(max-1).toString();
                        lineArray.remove(max-1);
                        lineArray.add(tmp.replace(",",""));
                    }

                    int lineSize = lineArray.size();
                    if (lineArray.size() < max) {
                        for(int i=lineSize; i<max; i++) {
                            if (i == lineSize) {
                                lineArray.add("?");
                            }
                            else {
                                lineArray.add(",?");
                            }
                        }
                    }
                    line = "";
                    for(int i=0; i<lineArray.size(); i++) {
                       line = line + lineArray.get(i);
                    }
                    fWriter.write(line + "\n");
                }

            }
            catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
            }
            fWriter.close();

            System.out.println("File created successfully.");
        }
        catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }

    public static int findMaxOrder() {
        int max = 0;
        try {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("dataset.txt"), "UTF-8"));
            ArrayList lineArray = new ArrayList<String>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {

                lineArray.clear();
                StringTokenizer st = new StringTokenizer(line, ",");

                while (st.hasMoreTokens()) {
                    String strToken = st.nextToken();
                    if (!isNumeric(strToken) && !lineArray.contains(translate(strToken))) {
                        lineArray.add(translate(strToken));
                    }
                    if (max <= lineArray.size()) {
                        max = lineArray.size();
                    }
                }
            }

        }
        catch (IOException e) {
            System.out.println("An error occurred.");
        }
        return max;
    }
}
