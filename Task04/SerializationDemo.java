import java.io.*;
import java.util.*;
class Resident implements Serializable {
    private static final long serialVersionUID=1L;
    String name, registerNo;
    transient String password;
    transient Object liveConnection;
    Resident(String n,String r,String p){name=n;registerNo=r;password=p;}
}
class Room implements Serializable {
    private static final long serialVersionUID=1L;
    int roomNo; List<Resident> residents=new ArrayList<>();
    Room(int n){roomNo=n;}
}
class Hostel implements Serializable {
    private static final long serialVersionUID=1L;
    List<Room> rooms=new ArrayList<>();
}
public class SerializationDemo {
    public static void main(String[] args) throws Exception {
        Hostel h=new Hostel(); Room r=new Room(12);
        r.residents.add(new Resident("Arun","192311001","secret")); h.rooms.add(r);
        try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream("hostel.dat"))){out.writeObject(h);}
        try(ObjectInputStream in=new ObjectInputStream(new FileInputStream("hostel.dat"))){
            Hostel copy=(Hostel)in.readObject();
            System.out.println(copy.rooms.get(0).residents.get(0).name);
            System.out.println("Password after load="+copy.rooms.get(0).residents.get(0).password);
        }
    }
}
