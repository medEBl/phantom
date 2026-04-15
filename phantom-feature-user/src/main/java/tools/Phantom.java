package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Phantom {
    String url ="jdbc:mysql://localhost:3306/phantom";
    String user="root";
    String mdp="";
    private Connection cnx;
    static Phantom phantom;
    private Phantom(){
        try {
            cnx = DriverManager.getConnection(url, user, mdp);
            System.out.println("cnx etablie");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public static Phantom getInstance(){
        if(phantom==null){
            phantom=new Phantom();
        }
        return phantom;
    }
    public Connection getCnx(){
        return cnx;
    }
}
