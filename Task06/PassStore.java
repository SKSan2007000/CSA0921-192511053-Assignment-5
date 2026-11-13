import java.io.*;
import java.nio.file.*;
import java.util.*;
public class PassStore {
    private final Path file;
    PassStore(Path file){this.file=file;}
    public void add(String id,String reg,String name,int route,double amount)throws IOException{
        try(BufferedWriter w=Files.newBufferedWriter(file,StandardOpenOption.CREATE,StandardOpenOption.APPEND)){
            w.write(String.join(",",id,reg,name,String.valueOf(route),String.valueOf(amount))); w.newLine();
        }
    }
    public String[] find(String id)throws IOException{
        try(BufferedReader r=Files.newBufferedReader(file)){
            String line;
            while((line=r.readLine())!=null){
                String[] fields=line.split(",",-1);
                if(fields.length!=5) continue;
                try{Integer.parseInt(fields[3]);Double.parseDouble(fields[4]);}
                catch(NumberFormatException e){continue;}
                if(fields[0].equals(id)) return fields;
            }
        } return null;
    }
    public void listRoute(int route)throws IOException{
        try(BufferedReader r=Files.newBufferedReader(file)){
            String line; while((line=r.readLine())!=null){
                String[] f=line.split(",",-1);
                if(f.length==5) try{if(Integer.parseInt(f[3])==route)System.out.println(line);}
                catch(NumberFormatException ignored){}
            }
        }
    }
    public static void main(String[] args)throws Exception{
        Path p=Path.of("passes.csv"); Files.deleteIfExists(p);
        PassStore s=new PassStore(p); s.add("P1001","192311001","Arun",12,4500);
        Files.writeString(p,"P1002,DAMAGED LINE\n",StandardOpenOption.APPEND);
        s.add("P1003","192311003","Divya",7,3800);
        System.out.println(Arrays.toString(s.find("P1003")));
        System.out.println(Arrays.toString(s.find("P1002")));
        System.out.println("Route 12:"); s.listRoute(12);
    }
}
