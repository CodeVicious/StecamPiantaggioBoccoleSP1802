package stecamSP1802.testers;

import Moka7.*;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.google.common.collect.Maps;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.services.PLC;
import stecamSP1802.services.PLCListener;
import stecamSP1802.services.StatusManager;
import stecamSP1802.services.barcode.BarCodeListener;
import stecamSP1802.services.barcode.Parte;
import stecamSP1802.services.barcode.SerialService;
import stecamSP1802.services.barcode.WorkOrder;

import java.net.ConnectException;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class PLCTest {
    private S7Client moka;
    String PLCIp = "192.168.10.1";
    private int rack = 0;
    private int slot = 1;

    public PLCTest() {

    }

    public static void main(String[] args) {
        loadWO();

        while (true) {


        }

    }

    public static void testPiantaggio() {
        ConfigurationManager.getInstance().getConfiguration();
        try {
            Connection conLDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneLOCALSERVER());
            String SQLINSERT = " INSERT INTO [dbo].[piantaggi]" +
                    "([TS],[IOP],[PRG],[WO],[ESITO])" +
                    "     VALUES (?,?,?,?,?)";

            PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
            preparedStmt.setTimestamp(1, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
            preparedStmt.setString(2, "0000");
            preparedStmt.setString(3, "12345678A");
            preparedStmt.setString(4, "3333333");
            preparedStmt.setString(5, "OK");
            preparedStmt.execute();


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void loadWO() {
        ConfigurationManager.getInstance().getConfiguration();

        try {
            Connection conLDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneLOCALSERVER());
            String SQLSELECT = "SELECT * FROM [dbo].[cache]";
            Statement stmt = conLDB.createStatement();
            ResultSet rs = stmt.executeQuery(SQLSELECT);


            WorkOrder wo = new WorkOrder();
            Map<String, Parte> listaParti = Maps.newHashMap();

            while (rs.next()) {
                if (rs.getString("TipoArt").matches("PF")) {
                    wo.setBarCodeWO(rs.getString("wo"));
                    wo.setCodiceRicetta(rs.getString("Articolo"));
                    wo.setDescrizione(rs.getString("Descrizione"));
                } else {
                    listaParti.put(rs.getString("Articolo"), new Parte(rs.getString("Articolo"),
                            rs.getString("Descrizione"), rs.getBoolean("Checked")));
                }

            }
            wo.setListaParti(listaParti);



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public static void storeWO() {
        ConfigurationManager.getInstance().getConfiguration();
        String SQLDROP = "TRUNCATE TABLE [dbo].[cache]";


        WorkOrder wo = new WorkOrder("2222222", "67372816", "descrizione");
        Map<String, Parte> listaParti = Maps.newHashMap();
        listaParti.put("12312345", new Parte("1231234", "bla bla", true));
        listaParti.put("12313333", new Parte("1231234", "bla bla", true));
        listaParti.put("15555555", new Parte("1231234", "bla bla", true));

        wo.setListaParti(listaParti);

        try {
            Connection conLDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneLOCALSERVER());
            Statement stmt = conLDB.createStatement();
            stmt.executeUpdate(SQLDROP);

            String SQLINSERT = " INSERT INTO [dbo].[cache]" +
                    "([wo],[TipoArt],[Articolo],[Descrizione],[Checked])" +
                    "     VALUES (?,?,?,?,?)";

            PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);
            preparedStmt.setString(1, wo.getBarCodeWO());
            preparedStmt.setString(2, "PF");
            preparedStmt.setString(3, wo.getCodiceRicetta());
            preparedStmt.setString(4, wo.getDescrizione());
            preparedStmt.setBoolean(5, false);

            preparedStmt.execute();

            listaParti = wo.getListaParti();
            for (String s : listaParti.keySet()) {
                preparedStmt.setString(1, s);
                preparedStmt.setString(2, "Componente");
                preparedStmt.setString(3, listaParti.get(s).getCodice());
                preparedStmt.setString(4, listaParti.get(s).getDescrizione());
                preparedStmt.setBoolean(5, listaParti.get(s).getVerificato());
                preparedStmt.execute();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    public static void testDROPDB() {
        ConfigurationManager.getInstance().getConfiguration();

        try {
            Connection conLDB = DriverManager.getConnection(ConfigurationManager.getInstance().getConnessioneLOCALSERVER());
            String SQLSELECT = "SELECT * FROM [dbo].[b_OperatoreIMP]";
            String SQLDROP = "TRUNCATE TABLE [dbo].[b_Operatore_SYNK]";
            String SQLINSERT = " INSERT INTO [dbo].[b_Operatore_SYNK]" +
                    "           ([Matricola]" +
                    "           ,[NomeOperatore]" +
                    "           ,[ConduttoreDiLinea]" +
                    "           ,[HashPassword])" +
                    "     VALUES" +
                    "           (?,?,?,?)";

            try {
                PreparedStatement preparedStmt = conLDB.prepareStatement(SQLINSERT);


                Statement stmt = conLDB.createStatement();
                stmt.executeUpdate(SQLDROP);
                ResultSet rs = stmt.executeQuery(SQLSELECT);
                // Iterate through the data in the result set and display it.
                while (rs.next()) {
                    preparedStmt.setString(1, rs.getString("Matricola"));
                    preparedStmt.setString(2, rs.getString("NomeOperatore"));
                    preparedStmt.setInt(3, rs.getInt("ConduttoreDiLinea"));
                    preparedStmt.setString(4, rs.getString("HashPassword"));
                    preparedStmt.execute();
                    // execute the preparedstatement
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (SQLException e1) {
            e1.printStackTrace();
        }


    }

    public void testSERIAL() {
        SerialPort comPort;
        System.out.println("PORT " + SerialPort.getCommPorts());
        comPort = SerialPort.getCommPort("COM3");
        comPort.openPort();
        String barCode = "123456";
        System.out.println(barCode.matches("\\d{7,8}"));

        //matches("^\\d{8}[A-Z]?$"));
        //matches("^\\d{4}(?i)(99|CS|EM|MM|MV|NQ|PI|PR|UC|UE|US)\\d{5,8}$"));


        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {

                if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                String str = new String(newData);
                System.out.println("CICICI " + str);
                System.out.println("Read " + numRead + " bytes." + newData);

                for (int i = 0; i < newData.length; ++i)
                    System.out.print((char) newData[i]);
                System.out.println("\n");

            }
        });
    }

    public void testPLC() {
        byte[] plcToPc = new byte[1];
        byte[] pcToPlc = new byte[12];

        int plcToPcDb = 112;
        int pcToPlcDb = 114;
        double[] booleans = {0.3, 0.4, 0.5, 0.6};


        PLC plcMASTER = new PLC("PLC GROSSO", "192.168.10.1", plcToPc, pcToPlc, plcToPcDb, pcToPlcDb, booleans);


        class imple implements PLCListener {

            public void onPLCBitChanged(int address, int pos, boolean val, String plcName) {
                switch (address) {
                    case 3:
                        switch (pos) {
                            case 0:
                                System.out.println("Bit at address 0.1 of PLC " + plcName + " changed to " + val);
                        }
                }
            }
        }

        plcMASTER.listeners.add(new imple());

        Thread t1 = new Thread(plcMASTER);
        t1.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            plcMASTER.putDInt(false, 0, 23);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}