package function;
import it.unisa.dia.gas.jpbc.Element;
import java.sql.*;
import java.util.HashMap;
/*建立数据库和表的：
CREATE DATABASE TEST;
CREATE  TABLE people(name char(50),PKi TEXT(500),Ki TEXT(500),PK TEXT(500),phi int,rho int);
设置的用户名为root，密码为314159265,可以在MysqlManager中13,14行修改，为了方便没有设置主键.
先getConnection(),进行完相关操作后close()
*/
public class MysqlManager {
    private static  Connection mConnect;
    private static final String USER="root";//这里是登录的用户名
    private static final String PASSWORD="314159265";//这里是密码，下面一行3306/test的test是database（数据库）的名称，
    private static final String ford="jdbc:mysql://localhost:3306/test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");//连接数据库
            mConnect= DriverManager.getConnection(ford, USER, PASSWORD);
            System.out.println("连接成功");
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mConnect;

    }
    public static void Insert(String name,Element one,Element two,Element three,int phi,int rho ){
        PreparedStatement stmt;
        byte[] PKi=one.toString().getBytes();
        byte[] Ki=two.toString().getBytes();
        byte[] PK=three.toString().getBytes();
        String sql = "insert into people values (?,?,?,?,?,?)";
        try {
            stmt = (PreparedStatement) mConnect.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setBytes(2, PKi);
            stmt.setBytes(3, Ki);
            stmt.setBytes(4, PK);
            stmt.setInt(5, phi);
            stmt.setInt(6, rho);
            stmt.executeUpdate();

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static HashMap<String,byte[]> Select(String Name){
        HashMap<String,byte[]> result=new HashMap<String,byte[]>();
        try{
            Statement stmt=mConnect.createStatement();
            String sql="SELECT * FROM people WHERE name ='"+Name+"'";
            ResultSet rs =stmt.executeQuery(sql);
            //System.out.println(rs);
            while(rs.next()){
                // 通过字段检索
                String name  = rs.getString("name");
                byte[] PKi = rs.getBytes("PKi");
                byte[] Ki = rs.getBytes("Ki");
                byte[] PK = rs.getBytes("Ki");
                int phi=rs.getInt("phi");
                int rho=rs.getInt("rho");
                result.put("PKi", PKi);
                result.put("Ki", Ki);
                result.put("PK", PK);
                result.put("phi",toLH(phi));
                result.put("rho",toLH(rho));
                // 输出数据
                System.out.println("name: " + name);
                System.out.println("PKi: " + PKi);
                System.out.println("Ki: " + Ki);
                System.out.println("PK: " + PK);
                System.out.println("phi: " + phi);
                System.out.println("rho: " + rho);
                }
            // 完成后关闭
            rs.close();
            stmt.close();

        }catch(SQLException se) {
            se.printStackTrace();
        }
        return result;
    }
    public static void  close() {
        try {
            mConnect.close();
            System.out.println("数据库关闭");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }

    }
    public static void update(String condition1,int condition2,String result1,int result2) {
        PreparedStatement stmt = null;
        try {

            String sql = "update people set "+result1+" = ? where "+condition1+" = ?";
            stmt = mConnect.prepareStatement(sql);
            stmt.setInt(1, result2);
            stmt.setInt(2, condition2);
            int result =stmt.executeUpdate();// 返回值代表收到影响的行数
            if(result>0) {
                System.out.println("修改成功");
            }else {
                System.out.println("修改失败");
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static byte[] toLH(int n) {//int 转byte[]
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }
    public static int toin(byte[] bytes ) {//byte[]转int
        int int1=bytes[0]&0xff;
        int int2=(bytes[1]&0xff)<<8;
        int int3=(bytes[2]&0xff)<<16;
        int int4=(bytes[3]&0xff)<<24;
        return int1|int2|int3|int4;
    }

}
