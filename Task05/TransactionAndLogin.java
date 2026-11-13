import java.sql.*;
public class TransactionAndLogin {
    static void transfer(Connection c,int from,int to,double amount)throws SQLException{
        boolean old=c.getAutoCommit(); c.setAutoCommit(false);
        try(PreparedStatement debit=c.prepareStatement("UPDATE wallet SET balance=balance-? WHERE id=?");
            PreparedStatement credit=c.prepareStatement("UPDATE wallet SET balance=balance+? WHERE id=?")){
            debit.setDouble(1,amount); debit.setInt(2,from); debit.executeUpdate();
            credit.setDouble(1,amount); credit.setInt(2,to); credit.executeUpdate();
            c.commit();
        }catch(SQLException e){c.rollback();throw e;}
        finally{c.setAutoCommit(old);}
    }
    static boolean safeLogin(Connection c,String id,String pwd)throws SQLException{
        String sql="SELECT 1 FROM users WHERE id=? AND pwd=?";
        try(PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,id); ps.setString(2,pwd);
            try(ResultSet rs=ps.executeQuery()){return rs.next();}
        }
    }
    public static void main(String[] args){
        System.out.println("Transaction and PreparedStatement methods are ready for a configured JDBC connection.");
    }
}
