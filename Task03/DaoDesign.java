import java.util.*;
interface Dao<T,K> {
    void insert(T item);
    T findById(K id);
    List<T> findAll();
    void update(T item);
    void delete(K id);
}
class Student {
    int id; String name; String registerNo;
    Student(int id,String name,String registerNo){this.id=id;this.name=name;this.registerNo=registerNo;}
}
class StudentDao implements Dao<Student,Integer> {
    private final Map<Integer,Student> rows=new HashMap<>();
    public void insert(Student s){rows.put(s.id,s);}
    public Student findById(Integer id){return rows.get(id);}
    public List<Student> findAll(){return new ArrayList<>(rows.values());}
    public void update(Student s){rows.put(s.id,s);}
    public void delete(Integer id){rows.remove(id);}
}
public class DaoDesign {
    public static void main(String[] args){
        StudentDao dao=new StudentDao(); dao.insert(new Student(1,"Arun","192311001"));
        System.out.println(dao.findById(1).name);
        System.out.println("Rows="+dao.findAll().size());
    }
}
