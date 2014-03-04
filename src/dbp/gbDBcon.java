package dbp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class gbDBcon {
	
	public gbDBcon(){		
	}
	
   public static Connection createConnection(){
		//String driver = "oracle.jdbc.driver.OracleDriver";
		String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

		String url = "jdbc:sqlserver://211.110.11.69:1433";
		Connection con = null;

        try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, "sa", "epmssa01");
			System.out.println("db Connection OK!!");
        } catch (Throwable t) {
        		SQLException(t.getMessage());
        }

        return con;
    }
	
	public static gbDBcon getGbDBcon(){
	
	    Statement statement = null;
	    String query = null;
	    ResultSet resultSet = null;
	    
	    Connection con = createConnection();
	    try {
	       
	        statement = con.createStatement();
	         
	         
	        // Query문 실행
	        query = "insert into customer values('0000000012', '강백호', 20,'010-2222-3423', 'B', 'M', '111-333')";
	        int count = statement.executeUpdate(query);
	         
	        System.out.println(count + "번 실행 성공하였습니다.");
	         
	        // Query문 실행 (SELECT)
	        query = "select * from customer, address where customer.address = address.zipcode";
	        resultSet = statement.executeQuery(query);
	
	         
	        // ResultSet을 통해 값 조회
	        while (resultSet.next()) {
	            String str = resultSet.getString("id");
	            String str2 = resultSet.getString("name");
	            int age = resultSet.getInt("age");
	            String city = resultSet.getString("city");
	            String dong = resultSet.getString("dong");
	             
	            System.out.println(str + " " + str2 + " " + age + " " + city + " " + dong);
	        }
	
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        if (resultSet != null) {
	            try {
	                resultSet.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	
	        }
	        if (statement != null) {
	
	            try {
	                statement.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        if (con != null) {
	            try {
	                con.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	
	    }
		return null;
	
	}
	
	private static void SQLException(String msg){
		System.out.println("SQLException :\n"+msg);
	}

}
