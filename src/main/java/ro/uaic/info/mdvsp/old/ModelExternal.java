//package ro.uaic.info.mdvsp.old;
//
//import ilog.concert.IloException;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import ro.uaic.info.mdvsp.Model;
//
///**
// *
// * @author Cristian Frăsinaru
// */
//public class ModelExternal extends Model {
//
//    public ModelExternal(String filename) {
//        super(filename);
//    }
//    
//    
//    @Override
//    public void solve() throws IloException, FileNotFoundException {  
//        String repo = "d:/java/MDVSP/input/" + name + "_sol.txt";
//        try {
//            Path path = Paths.get(repo);
//            for (String line : Files.readAllLines(path)) {
//                if (line.isBlank()) {
//                    continue;
//                }
//                System.out.println(line);
//                String x[] = line.split("\t");
//                int i = Integer.parseInt(x[0]);
//                int j = Integer.parseInt(x[1]);
//                int value = (int) Double.parseDouble(x[2]);
//                sol[i][j] = value;
//            }
//        } catch (IOException ex) {
//            System.err.println("No external solution available\n" + ex);
//        }
//        
//        getTours();
//        printTours();
//    }
//
//    @Override
//    protected void extractSolution() {
//    }
//
//}
