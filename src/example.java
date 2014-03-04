package com.dbp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBConnectionPoolMgr {
    private Vector connections = new Vector(10);
    private boolean _traceOn = true;
    private boolean initialized = false;
    private int _openConnections = 10;
    private static DBConnectionPoolMgr dbmgr = null;
    private DataSource pool;

    public DBConnectionPoolMgr() {
    }

    /** Use this method to set the maximum number of open connections before
     unused connections are closed.
     */

    public static DBConnectionPoolMgr getInstance() {
        if (dbmgr == null) {
            synchronized (DBConnectionPoolMgr.class) {
                if (dbmgr == null) {
                	dbmgr = new DBConnectionPoolMgr();
                }
            }
        }

        return dbmgr;
    }

    public void setOpenConnectionCount(int count) {
        _openConnections = count;
    }


    public void setEnableTrace(boolean enable) {
        _traceOn = enable;
    }


    /** Returns a Vector of java.sql.Connection objects */
    public Vector getConnectionList() {
        return connections;
    }


    /** Opens specified "count" of connections and adds them to the existing pool */
    public synchronized void setInitOpenConnections(int count)
            throws SQLException {
        Connection c = null;
        ConnectionObject co = null;

        for (int i = 0; i < count; i++) {
            c = createConnection();
            co = new ConnectionObject(c, false);

            connections.addElement(co);
            trace("ConnectionPoolManager: Adding new DB connection to pool (" + connections.size() + ")");
        }
    }


    /** Returns a count of open connections */
    public int getConnectionCount() {
        return connections.size();
    }

    public void freeConnection2(Connection connection)
    {
        if(connection != null)
            try
            {
            	System.out.println("### TONGHAP Connection DISConnection by.��ȣ========================") ;
                connection.close();
            }
            catch(SQLException e)
            {
                e.printStackTrace();
                System.out.println("### DBConnectionPoolMgr::freeConnection()..." + e.getMessage());
            }
    }
    public Connection getConnection2()
    {
        Connection connection = null;
        try
        {
        	System.out.println("### TONGHAP Connection getConnection by.��ȣ=============================") ;
            connection = pool.getConnection();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return connection;
    }

    /** Returns an unused existing or new connection.  */
    public synchronized Connection getConnection()
            throws Exception {
        if (!initialized) {
        	
            try {
                Context initCtx = new InitialContext();
                pool = (DataSource)initCtx.lookup("java:comp/env/jdbc/MssqlDB");
     
                if(pool == null)
                    throw new Exception("### DBConnectionPoolMgr::getPool()...'jdbc/mydb' is not found.");
                
                initialized = true;
            } catch(NamingException e) {
                e.printStackTrace();
                throw new Exception("### DBConnectionPoolMgr::getPool()..." + e.getMessage());
            }
        }


        Connection c = null;
        ConnectionObject co = null;
        boolean badConnection = false;

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            // If connection is not in use, test to ensure it's still valid!
            if (!co.inUse) {
                try {
                    badConnection = co.connection.isClosed();
                    if (!badConnection)
                        badConnection = (co.connection.getWarnings() != null);
                } catch (Exception e) {
                    badConnection = true;
                    e.printStackTrace();
                }

                // Connection is bad, remove from pool
                if (badConnection) {
                    connections.removeElementAt(i);
                    trace("ConnectionPoolManager: Remove disconnected DB connection #" + i);
                    System.out.println("===[����]��񿬰� ����======test$$$$==================");
                    continue;
                }

                c = co.connection;
                co.inUse = true;

                trace("ConnectionPoolManager: Using existing DB connection #" + (i + 1));
                break;
            }
        }

        if (c == null) {
            c = createConnection();
            co = new ConnectionObject(c, true);
            connections.addElement(co);

            trace("ConnectionPoolManager: Creating new DB connection #" + connections.size());
//            System.out.println("===[����]��񿬰� ��========================");
        }
        return c;
    }


    /** Marks a flag in the ConnectionObject to indicate this connection is no longer in use */
    public synchronized void freeConnection(Connection c) {
        if (c == null)
            return;

        ConnectionObject co = null;

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (c == co.connection) {
                co.inUse = false;
                break;
            }
        }

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if ((i + 1) > _openConnections && !co.inUse)
                removeConnection(co.connection);
            trace("ConnectionPoolManager freeConnection's : Remove disconnected DB connection #" + i);
          //  System.out.println("===[����]��񿬰� ����========================");
        }
    }

    public void freeConnection(Connection c, PreparedStatement p, ResultSet r) {
        try {
            if (r != null) r.close();
            if (p != null) p.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection c, Statement s, ResultSet r) {
        try {
            if (r != null) r.close();
            if (s != null) s.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection c, PreparedStatement p) {
        try {
            if (p != null) p.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection c, Statement s) {
        try {
            if (s != null) s.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /** Marks a flag in the ConnectionObject to indicate this connection is no longer in use */
    public synchronized void removeConnection(Connection c) {
        if (c == null)
            return;

        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (c == co.connection) {
                try {
                    c.close();
                    connections.removeElementAt(i);
                    trace("Removed " + c.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }


    private Connection createConnection()
            throws SQLException {
        Connection con = null;

        try {
        	con = pool.getConnection();
        } catch (Throwable t) {
            throw new SQLException(t.getMessage());
        }

        return con;
    }


    /** Closes all connections and clears out the connection pool */
    public void releaseFreeConnections() {
        trace("ConnectionPoolManager.releaseFreeConnections()");

        ConnectionObject co = null;

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (!co.inUse)
                removeConnection(co.connection);
        }
    }


    /** Closes all connections and clears out the connection pool */
    public void finalize() {
        trace("ConnectionPoolManager.finalize()");

        ConnectionObject co = null;

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            try {
                co.connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            co = null;
        }

        connections.removeAllElements();
    }

	public void disConn(DBConnectionPoolMgr dbMgr, Connection conn) throws Exception {
		try {
			dbMgr = DBConnectionPoolMgr.getInstance();
			dbMgr.freeConnection(conn);			
		}catch(Exception ex){
			System.out.println("disConn's Error = "+ex);
		}finally{
			if(conn != null)	{ dbMgr.freeConnection(conn); }
		}
	}
    private void trace(String s) {
        if (_traceOn) {
           // System.err.println(s);
        }
    }

}


class ConnectionObject {
    public java.sql.Connection connection = null;
    public boolean inUse = false;

    public ConnectionObject(Connection c, boolean useFlag) {
        connection = c;
        inUse = useFlag;
    }
}
